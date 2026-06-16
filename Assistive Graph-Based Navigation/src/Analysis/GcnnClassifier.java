package Analysis;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import dataStructures.graph.models.AdjacencyListGraph;
import database.models.annotations;
import utilities.models.Coordinates;

/**
 * Handles calling the python GCNN model for inference.
 * Its a bit complex because we try local env first then docker if that fails.
 */
public class GcnnClassifier {

    // default logger just prints to console but the UI overrides this to show in the log area
    private Consumer<String> logger = System.out::println;

    public static class Prediction {
        public int categoryId;
        public String categoryName;
        public String color;

        public Prediction(int categoryId) {
            this.categoryId = categoryId;
            //mapping these to match the COCO categories used in training
            switch (categoryId) {
                case 1:
                    categoryName = "Horizontal Directional";
                    color = "#2dce89"; //green-ish
                    break;
                case 2:
                    categoryName = "Vertical Directional";
                    color = "#5e72e4"; //blue-ish
                    break;
                case 3:
                    categoryName = "Warning Tactile";
                    color = "#f5a623"; //orange-ish
                    break;
                default:
                    categoryName = "Unknown (" + categoryId + ")";
                    color = "#ffffff";
                    break;
            }
        }
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    public List<Prediction> classify(List<AdjacencyListGraph<Coordinates, Double>> graphs, List<annotations> anns) throws Exception {
        // first we output the graph data to a temp json so python can read it easily
        String jsonPath = GcnnDataProcessor.prepareInferenceGraph(graphs, anns);
        
        //try local pythorn first since its way faster than docker
        List<Prediction> results = runLocalInference(jsonPath);
        if (results == null) {
            logger.accept("[CLF] Local Python failed or not found. Falling back to Docker...");
            results = runDockerInference(jsonPath);
        }
        return results != null ? results : new ArrayList<>();
    }

    private List<Prediction> runLocalInference(String jsonPath) {
        List<Prediction> results = new ArrayList<>();
        try {
            logger.accept("[CLF] Attempting local Python inference...");
            
            // 1. Resolve Python Command (Prioritize .venv)
            String pythonCmd = isWindows() ? "python" : "pythons";
            String venvPath = isWindows() ? ".venv/Scripts/python.exe" : ".venv/bin/python";
            
            if (new java.io.File(venvPath).exists()) {
                pythonCmd = venvPath;
            } else if (new java.io.File("src/" + venvPath).exists()) {
                pythonCmd = "src/" + venvPath;
            }

            // 2. Resolve Script Path
            String scriptPath = "GeometricTrackGCNN.py";
            if (!new java.io.File(scriptPath).exists()) {
                scriptPath = "src/GeometricTrackGCNN.py";
            }
            
            List<String> command = new ArrayList<>();
            command.add(pythonCmd);
            command.add(scriptPath);
            command.add("--predict");
            command.add(jsonPath);
            
            ProcessBuilder pb = new ProcessBuilder(command);
            Process p = pb.start();
            boolean success = captureOutput(p, results);
            
            if (success && p.waitFor() == 0) {
                logger.accept("[CLF] Local inference successful.");
                return results;
            }
        } catch (Exception e) {
            logger.accept("[CLF] Local Python error: " + e.getMessage());
        }
        return null; /// try docker
    }

    private List<Prediction> runDockerInference(String jsonPath) throws Exception {
        List<Prediction> results = new ArrayList<>();
        String currentDir = System.getProperty("user.dir");
        logger.accept("[CLF] Attempting Docker inference (image: gcnn-navigation)...");
        
        // 1. Determine Source Directory (where Dockerfile/scripts live)
        String mountDir = currentDir;
        String internalScriptPath = "GeometricTrackGCNN.py";
        String internalJsonPath = jsonPath;

        if (new java.io.File(currentDir, "src").exists() && new java.io.File(currentDir, "src/GeometricTrackGCNN.py").exists()) {
            // Running from Root: Mount 'src' to '/app'
            mountDir = new java.io.File(currentDir, "src").getAbsolutePath();
            internalScriptPath = "GeometricTrackGCNN.py";
            // jsonPath is "src/temp_inference.json", needs to be "temp_inference.json" inside container
            internalJsonPath = new java.io.File(jsonPath).getName();
        }

        List<String> command = new ArrayList<>();
        command.add("docker");
        command.add("run");
        command.add("--rm");
        command.add("-v");
        command.add(mountDir + ":/app");
        command.add("gcnn-navigation");
        command.add("python");
        command.add(internalScriptPath);
        command.add("--predict");
        command.add(internalJsonPath);
        
        ProcessBuilder pb = new ProcessBuilder(command);
        Process p = pb.start();
        boolean success = captureOutput(p, results);
        
        if (success && p.waitFor() == 0) {
            logger.accept("[CLF] Docker inference successful.");
            return results;
        } else {
            logger.accept("[CLF] Docker inference failed. Ensure Docker is running and image 'gcnn-navigation' is built.");
            return null;
        }
    }

    /**
     * helper to grab the RES: lines from the python scripts stdout
     */
    private boolean captureOutput(Process p, List<Prediction> results) throws Exception {
        boolean resFound = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.accept("  [PY] " + line);
                if (line.startsWith("RES:")) {
                    //format is RES:img_id,pred,0,0,0,0
                    String[] parts = line.split(":");
                    String[] data = parts[1].split(",");
                    int classId = Integer.parseInt(data[1]);
                    results.add(new Prediction(classId));
                    resFound = true;
                }
            }
        }
        //debug
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                logger.accept("  [PY-ERR] " + line);
            }
        }
        return resFound;
    }

    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}
