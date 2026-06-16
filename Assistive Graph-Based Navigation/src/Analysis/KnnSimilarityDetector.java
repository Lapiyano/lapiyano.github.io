package Analysis;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Headless logic for KNN-based structural similarity detection between tactile paving graphs.
 * Extracted from the original KnnViewer to be integrated into the main JavaFX UI.
 * 
 * NOTE: this is purely structural.. no pixels involved. We just check if the 
 * graphs "look" same based on edge angles and node counts.
 */
public class KnnSimilarityDetector {

    // split names for the datasets.. make sure these match the folders in Dataset/
    private static final String[] SPLITS = {"test", "train", "valid"};
    public static final String[] CAT_NAMES = {"T", "H", "V", "W"};

    private List<ImageEntry> allImages = new ArrayList<>();
    private double[] featureMax = new double[5]; //
    private boolean loaded = false;

    public KnnSimilarityDetector() {
        // Data loading should be triggered explicitly
    }

    public void reset() {
        allImages.clear();
        loaded = false;
    }

    public void loadData() {
        int totalLoaded = 0;
        try {
            for (String split : SPLITS) {
                // fixed the paths to use project root... src/ 
                String gcnnPath = "src/jsons/gcnn_" + split + ".json";
                String cocoPath = "src/Dataset/" + split + "/_annotations.coco.json";

                File gcnnFile = new File(gcnnPath);
                File cocoFile = new File(cocoPath);

                if (!gcnnFile.exists() || !cocoFile.exists()) continue;

                //parser
                Map<Integer, CocoImage> cocoImages = loadCocoImages(cocoPath);
                Map<Integer, List<CocoAnnotation>> cocoAnns = loadCocoAnnotations(cocoPath);
                List<GcnnImage> gcnnData = loadGcnnData(gcnnPath);

                int splitCount = 0;
                for (GcnnImage gi : gcnnData) {
                    CocoImage ci = cocoImages.get(gi.imageId);
                    if (ci == null) continue;

                    List<CocoAnnotation> anns = cocoAnns.get(gi.imageId);
                    ImageEntry entry = new ImageEntry();
                    entry.id = gi.imageId;
                    entry.fileName = ci.fileName;
                    entry.split = split.toUpperCase();
                    entry.filePath = "src/Dataset/" + split + "/" + ci.fileName;

                    for (int i = 0; i < gi.boxes.size(); i++) {
                        GcnnBox gb = gi.boxes.get(i);
                        // drop empty graphs... they break the similarity math
                        if (gb.vertices != null && gb.vertices.length > 0 && gb.edges != null && gb.edges.length > 0) {
                            BoxEntry be = new BoxEntry();
                            be.categoryId = gb.categoryId;
                            be.features = extractFeatures(gb);
                            if (anns != null && i < anns.size()) {
                                be.bbox = anns.get(i).bbox;
                            } else {
                                //set default bbox if missing
                                be.bbox = new double[]{0, 0, 50, 50};
                            }
                            entry.boxes.add(be);
                        }
                    }

                    if (!entry.boxes.isEmpty()) {
                        allImages.add(entry);
                        splitCount++;
                    }
                }
                totalLoaded += splitCount;
            }
            computeFeatureRanges();
            loaded = true;
        } catch (Exception e) {
 
            e.printStackTrace();
        }
    }

    public boolean isLoaded() {
        return loaded;
    }

    public List<ImageEntry> getAllImages() {
        return allImages;
    }

    public ImageEntry findImageByName(String name) {
        for (ImageEntry ie : allImages) {
            if (ie.fileName.equalsIgnoreCase(name)) return ie;
        }
        return null;
    }

    private void computeFeatureRanges() {
        // find max values for normaliztion... simple min-max scaling
        Arrays.fill(featureMax, 1e-9);
        for (ImageEntry ie : allImages) {
            for (BoxEntry be : ie.boxes) {
                for (int i = 0; i < 5; i++) {
                    if (be.features[i] > featureMax[i]) {
                        featureMax[i] = be.features[i];
                    }
                }
            }
        }
    }

    private double[] extractFeatures(GcnnBox box) {
        double[] f = new double[5];
        // 1.Log scale for vertices: stops big graphs from hogging the similarity
        f[0] = Math.log(1 + box.vertices.length);
        
        //Edge Density: how "connected" the tactile strip is
        f[1] = (double) box.edges.length / Math.max(1, box.vertices.length);

        double sumDeg = 0;
        double sumDistC = 0;
        for (double[] v : box.vertices) {
            if (v.length >= 4) {
                sumDeg += v[2]; // avg degree
                sumDistC += v[3]; // avg dist from center
            }
        }
        f[2] = sumDeg / box.vertices.length;
        f[3] = sumDistC / box.vertices.length;

        //check for horizontal-ish edges.
        int near0 = 0;
        for (double[] e : box.edges) {
            if (e.length >= 3 && Math.abs(e[2]) < 0.524) { // < 30 degrees approx
                near0++;
            }
        }
        f[4] = (double) near0 / Math.max(1, box.edges.length);

        return f;
    }

    public List<MatchResult> compareImages(ImageEntry selectedA, ImageEntry selectedB) {
        List<MatchResult> results = new ArrayList<>();
        if (selectedA == null || selectedB == null) return results;

        // brute force O(N*M) check... fine for small number of boxes per image
        for (int i = 0; i < selectedB.boxes.size(); i++) {
            BoxEntry boxB = selectedB.boxes.get(i);
            for (int j = 0; j < selectedA.boxes.size(); j++) {
                BoxEntry boxA = selectedA.boxes.get(j);
                double distSq = 0;
                for (int k = 0; k < 5; k++) {
                    // euclidean distance between feature vectors
                    double diff = (boxB.features[k] - boxA.features[k]) / Math.max(featureMax[k], 1e-9);
                    distSq += diff * diff;
                }
                double dist = Math.sqrt(distSq);
                // turn distance into a similarity %... sqrt(5) is max theoreticl dist
                double sim = Math.max(0, 100 * (1 - dist / Math.sqrt(5)));
                
                MatchResult res = new MatchResult();
                res.indexB = i;
                res.indexA = j;
                res.catB = boxB.categoryId;
                res.catA = boxA.categoryId;
                res.similarity = sim;
                results.add(res);
            }
        }

        // sort so the best matches are top of the list
        results.sort((r1, r2) -> Double.compare(r2.similarity, r1.similarity));
        return results;
    }

    public static class ImageEntry {
        public int id;
        public String fileName;
        public String split;
        public String filePath;
        public List<BoxEntry> boxes = new ArrayList<>();
        @Override public String toString() { return String.format("%s [%s] (%d boxes)", fileName, split, boxes.size()); }
    }

    public static class BoxEntry {
        public int categoryId;
        public double[] features;
        public double[] bbox; // [x, y, w, h]
    }

    public static class MatchResult {
        public int indexB, indexA, catB, catA;
        public double similarity;
    }

    private static class CocoImage { int id; String fileName; }
    private static class CocoAnnotation { int imageId; double[] bbox; }
    private static class GcnnImage { int imageId; List<GcnnBox> boxes = new ArrayList<>(); }
    private static class GcnnBox { 
        int categoryId; 
        double[][] vertices; 
        double[][] edges;    
    }

    // --- MINIMAL JSON PARSER ---

    private Map<Integer, CocoImage> loadCocoImages(String path) throws Exception {
        Map<Integer, CocoImage> map = new HashMap<>();
        String content = readFile(path);
        int imagesIdx = findKey(content, "images");
        if (imagesIdx == -1) return map;
        int arrStart = content.indexOf('[', imagesIdx);
        String imagesArr = extractArray(content, arrStart);
        for (String obj : splitObjects(imagesArr)) {
            CocoImage ci = new CocoImage();
            ci.id = (int) parseNumber(obj, "\"id\"");
            ci.fileName = parseString(obj, "\"file_name\"");
            map.put(ci.id, ci);
        }
        return map;
    }

    private Map<Integer, List<CocoAnnotation>> loadCocoAnnotations(String path) throws Exception {
        Map<Integer, List<CocoAnnotation>> map = new HashMap<>();
        String content = readFile(path);
        int annIdx = findKey(content, "annotations");
        if (annIdx == -1) return map;
        int arrStart = content.indexOf('[', annIdx);
        String annArr = extractArray(content, arrStart);
        for (String obj : splitObjects(annArr)) {
            CocoAnnotation ca = new CocoAnnotation();
            ca.imageId = (int) parseNumber(obj, "\"image_id\"");
            ca.bbox = parseDoubleArray(obj, "\"bbox\"");
            map.computeIfAbsent(ca.imageId, k -> new ArrayList<>()).add(ca);
        }
        return map;
    }

    private List<GcnnImage> loadGcnnData(String path) throws Exception {
        List<GcnnImage> list = new ArrayList<>();
        String content = readFile(path);
        for (String obj : splitObjects(content)) {
            GcnnImage gi = new GcnnImage();
            gi.imageId = (int) parseNumber(obj, "\"image_id\"");
            int boxesIdx = findKey(obj, "boxes");
            if (boxesIdx != -1) {
                int arrStart = obj.indexOf('[', boxesIdx);
                String boxesArr = extractArray(obj, arrStart);
                for (String boxObj : splitObjects(boxesArr)) {
                    GcnnBox gb = new GcnnBox();
                    gb.categoryId = (int) parseNumber(boxObj, "\"category_ID\"");
                    gb.vertices = parseMatrix(boxObj, "\"vertices\"");
                    gb.edges = parseMatrix(boxObj, "\"edges\"");
                    gi.boxes.add(gb);
                }
            }
            list.add(gi);
        }
        return list;
    }

    private int findKey(String s, String key) {
        int idx = s.indexOf("\"" + key + "\"");
        if (idx == -1) idx = s.indexOf("'" + key + "'");
        return idx;
    }

    private String readFile(String path) throws Exception {
        try (FileInputStream fis = new FileInputStream(path)) {
            byte[] data = fis.readAllBytes();
            return new String(data, StandardCharsets.UTF_8);
        }
    }

    private String extractArray(String s, int start) {
        if (start < 0 || start >= s.length()) return "";
        int depth = 0;
        for (int i = start; i < s.length(); i++) {
            if (s.charAt(i) == '[') depth++;
            else if (s.charAt(i) == ']') depth--;
            if (depth == 0 && i > start) return s.substring(start, i + 1);
        }
        return "";
    }

    private List<String> splitObjects(String arr) {
        List<String> list = new ArrayList<>();
        int depth = 0;
        int start = -1;
        for (int i = 0; i < arr.length(); i++) {
            char c = arr.charAt(i);
            if (c == '{') {
                if (depth == 0) start = i;
                depth++;
            } else if (c == '}') {
                depth--;
                if (depth == 0 && start != -1) {
                    list.add(arr.substring(start, i + 1));
                    start = -1;
                }
            }
        }
        return list;
    }

    private String parseString(String obj, String key) {
        int idx = findKey(obj, key.replace("\"", ""));
        if (idx == -1) return "";
        int colonIdx = obj.indexOf(':', idx + key.length());
        int start = obj.indexOf('"', colonIdx + 1);
        if (start == -1) return "";
        int end = obj.indexOf('"', start + 1);
        if (end == -1) return "";
        return obj.substring(start + 1, end);
    }

    private double parseNumber(String obj, String key) {
        int idx = findKey(obj, key.replace("\"", ""));
        if (idx == -1) return 0;
        int colonIdx = obj.indexOf(':', idx + key.length());
        int start = colonIdx + 1;
        while (start < obj.length() && (Character.isWhitespace(obj.charAt(start)))) start++;
        int end = start;
        while (end < obj.length() && (Character.isDigit(obj.charAt(end)) || obj.charAt(end) == '.' || obj.charAt(end) == '-' || obj.charAt(end) == 'e' || obj.charAt(end) == 'E')) end++;
        try { return Double.parseDouble(obj.substring(start, end)); } catch (Exception e) { return 0; }
    }

    private double[] parseDoubleArray(String obj, String key) {
        int idx = findKey(obj, key.replace("\"", ""));
        if (idx == -1) return new double[0];
        int start = obj.indexOf('[', idx);
        if (start == -1) return new double[0];
        int end = obj.indexOf(']', start);
        if (end == -1) return new double[0];
        String sub = obj.substring(start + 1, end).trim();
        if (sub.isEmpty()) return new double[0];
        String[] parts = sub.split(",");
        double[] res = new double[parts.length];
        for (int i = 0; i < parts.length; i++) {
            try { res[i] = Double.parseDouble(parts[i].trim()); } catch (Exception e) { res[i] = 0; }
        }
        return res;
    }

    private double[][] parseMatrix(String obj, String key) {
        int idx = findKey(obj, key.replace("\"", ""));
        if (idx == -1) return new double[0][0];
        int startArr = obj.indexOf('[', idx);
        String arrStr = extractArray(obj, startArr);
        List<double[]> rows = new ArrayList<>();
        int start = -1;
        for (int i = 0; i < arrStr.length(); i++) {
            if (arrStr.charAt(i) == '[') start = i;
            else if (arrStr.charAt(i) == ']') {
                if (start != -1 && i > start + 1) { 
                    String sub = arrStr.substring(start + 1, i).trim();
                    if (!sub.isEmpty() && !sub.contains("[")) { 
                        String[] parts = sub.split(",");
                        double[] r = new double[parts.length];
                        for (int j = 0; j < parts.length; j++) {
                             try { r[j] = Double.parseDouble(parts[j].trim()); } catch(Exception e) { r[j] = 0; }
                        }
                        rows.add(r);
                    }
                }
                start = -1;
            }
        }
        return rows.toArray(new double[0][0]);
    }
}
