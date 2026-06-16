package Analysis;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import dataStructures.graph.interfaces.IEdge;
import dataStructures.graph.interfaces.IVertex;
import dataStructures.graph.models.AdjacencyListGraph;
import database.models.annotations;
import utilities.models.Coordinates;

public class GcnnDataProcessor {

    public static String prepareInferenceGraph(List<AdjacencyListGraph<Coordinates, Double>> graphs, List<annotations> anns) throws IOException {
        StringBuilder sb = new StringBuilder("[\n");
        // The Python script expects a list of images, each with a 'boxes' array
        // Here we treat the selected image as one entry in the list
        sb.append("  {\n    \"image_id\": 0,\n    \"boxes\": [\n");
        for (int i = 0; i < graphs.size(); i++) {
            sb.append(serializeGraph(graphs.get(i), anns.get(i)));
            if (i < graphs.size() - 1) sb.append(",\n");
        }
        sb.append("\n    ]\n  }\n]");
        
        // Resolve path: if we are in 'src', use current dir. If we are in root, use 'src/'
        String path = "temp_inference.json";
        if (new java.io.File("src").exists() && new java.io.File("src").isDirectory()) {
            path = "src/" + path;
        }
        
        try (FileWriter fw = new FileWriter(path)) {
            fw.write(sb.toString());
        }
        return path;
    }

    private static String serializeGraph(AdjacencyListGraph<Coordinates, Double> graph, annotations ann) {
        double bW = ann.getBbox()[2];
        double bH = ann.getBbox()[3];
        Coordinates center = new Coordinates(bW / 2.0, bH / 2.0);
        
        StringBuilder sb = new StringBuilder("      {\n");
        sb.append("        \"category_ID\": ").append(ann.getCategoryID()).append(",\n");
        sb.append("        \"vertices\": [\n");
        
        List<IVertex<Coordinates>> vList = new ArrayList<>();
        Iterator<IVertex<Coordinates>> vIt = graph.Vertices();
        while (vIt.hasNext()) {
            IVertex<Coordinates> v = vIt.next();
            vList.add(v);
            Coordinates c = v.getElement();
            // Feature format: [relX, relY, degree, distCenter] (Centered as in modelExports)
            double relX = (c.getX() - center.getX()) / bW;
            double relY = (c.getY() - center.getY()) / bH;
            double degree = (double) graph.Degree(v);
            double distCenter = Math.sqrt(Math.pow(c.getX() - center.getX(), 2) + Math.pow(c.getY() - center.getY(), 2));
            
            sb.append(String.format("          [%.6f, %.6f, %.0f, %.6f]", relX, relY, degree, distCenter));
            if (vIt.hasNext()) sb.append(",\n");
        }
        
        sb.append("\n        ],\n        \"edges\": [\n");
        Iterator<IEdge<Double>> eIt = graph.edges();
        while (eIt.hasNext()) {
            IEdge<Double> e = eIt.next();
            IVertex<Coordinates>[] ends = graph.endVertices(e);
            
            int srcIdx = vList.indexOf(ends[0]);
            int tgtIdx = vList.indexOf(ends[1]);
            if (srcIdx < 0 || tgtIdx < 0) continue; 

            // Edge format: [srcIdx, tgtIdx, angle, distance] (Matching modelExports)
            double angle = Math.atan2(ends[1].getElement().getY() - ends[0].getElement().getY(), 
                                    ends[1].getElement().getX() - ends[0].getElement().getX());
            double distance = e.getElement(); // Already Euclidean distance from Extraction.java

            sb.append(String.format("          [%d, %d, %.6f, %.4f]", srcIdx, tgtIdx, angle, distance));
            if (eIt.hasNext()) sb.append(",\n");
        }
        sb.append("\n        ]\n      }");
        return sb.toString();
    }
}
