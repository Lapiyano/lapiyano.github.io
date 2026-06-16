package export;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import Connections.dbLoader;
import Extraction.Extraction;
import dataStructures.Dictionary.models.HashDictionary;
import dataStructures.graph.interfaces.IEdge;
import dataStructures.graph.interfaces.IVertex;
import dataStructures.graph.models.AdjacencyListGraph;
import database.models.Images;
import database.models.annotations;
import database.models.categories;
import javafx.scene.image.Image;
import utilities.models.Coordinates;
/**
 * @author Thobela FF 223079625
 * @version CSC-Mini project 
 * exports graph data to a json file for analysis
 */
public class modelExports {

    private HashDictionary<Integer, List<annotations>> annotation_dictionary;
    private HashDictionary<Integer, Images> images_dictionary;
    private HashDictionary<Integer, categories> categories_dictionary;

    //constructor
    public modelExports() {}

    
    /**
     * @author Thobela FF 223079625
     * exports graph data into json
     * @param type
     * @return
     */
    public String extractToJson(String type) {
        String filespath = "";
        String db = "";

        if (type.equals("test")) {
            filespath = "src/Dataset/test/";
            db = "src/tactile_test.db";
        } else if (type.equals("train")) {
            filespath = "src/Dataset/train/";
            db = "src/tactile_train.db";
        } else {
            filespath = "src/Dataset/valid/";
            db = "src/tactile_valid.db";
        }

        dbLoader loader = new dbLoader(db);

        annotation_dictionary = new HashDictionary<>(loader.getAllImages().size());
        images_dictionary = new HashDictionary<>(loader.getAllImages().size());
        categories_dictionary = new HashDictionary<>(loader.getAllCategories().size());

        for (Images img : loader.getAllImages()) {
            images_dictionary.insert(img.ID(), img);
            annotation_dictionary.insert(img.ID(), loader.getAnnotationsForImage(img.ID()));
        }

        StringBuilder json = new StringBuilder();
        json.append("[");

        boolean firstImage = true;
        Iterator<Integer> imageKeys = images_dictionary.Keys();
        
        while (imageKeys.hasNext()) {
            Integer key = imageKeys.next();
            Images img = images_dictionary.find(key).getElement();
            File f = new File(filespath + img.Filename());

            if (!f.exists() || f.length() == 0) {
                continue;
            }

            try {
                Image image = new Image(f.toURI().toString(), 0, 0, false, true);

                if (image.isError() || image.getWidth() <= 0 || image.getHeight() <= 0) {
                    continue;
                }

                List<annotations> annList = annotation_dictionary.find(key).getElement();
                Extraction ext = new Extraction(image, annList);
                ext.extract();
                
                List<AdjacencyListGraph<Coordinates, Double>> graphs = ext.graphs;
                List<Image> pieces = ext.extractRectanglePieces(image, annList);

                if (!firstImage) {
                    json.append(",");
                }
                firstImage = false;

                json.append("{");
                json.append("\"image_id\": ").append(img.ID()).append(",");
                json.append("\"boxes\": [");

                for (int x = 0; x < graphs.size(); x++) {
                    if (x > 0) json.append(",");
                    
                    AdjacencyListGraph<Coordinates, Double> graph = graphs.get(x);
                    Image piece = pieces.get(x);
                    
                    double centreX = piece.getWidth() / 2.0;
                    double centreY = piece.getHeight() / 2.0;
                    Coordinates centre = new Coordinates(centreX, centreY);

                    json.append("{");
                    json.append("\"category_ID\": ").append(annList.get(x).getCategoryID()).append(",");

                    HashDictionary<IVertex<Coordinates>, Integer> verticalMap = new HashDictionary<>(graph.numVertices());
                    json.append("\"vertices\": [");
                    Iterator<IVertex<Coordinates>> vertices = graph.Vertices();
                    int vIdx = 0;
                    while (vertices.hasNext()) {
                        IVertex<Coordinates> v = vertices.next();
                        verticalMap.insert(v, vIdx);
                        Coordinates c = v.getElement();
                        
                        double relativeX = (c.getX() - centreX) / piece.getWidth();
                        double relativeY = (c.getY() - centreY) / piece.getHeight();
                        int degree = graph.Degree(v);
                        double distCenter = calculateDistance(centre, c);

                        json.append(String.format(Locale.US, "[%f, %f, %d, %f]", 
                            relativeX, relativeY, degree, distCenter));
                        
                        if (vertices.hasNext()) json.append(",");
                        vIdx++;
                    }
                    json.append("],");

                    json.append("\"edges\": [");
                    Iterator<IEdge<Double>> edges = graph.edges();
                    while (edges.hasNext()) {
                        IEdge<Double> edge = edges.next();
                        IVertex<Coordinates>[] endPoints = graph.endVertices(edge);
                        
                        int index1 = verticalMap.find(endPoints[0]).getElement();
                        int index2 = verticalMap.find(endPoints[1]).getElement();
                        
                        double angle = calculateAngle(endPoints[0].getElement(), endPoints[1].getElement());
                        double distance = calculateDistance(endPoints[0].getElement(), endPoints[1].getElement());

                        json.append(String.format(Locale.US, "[%d, %d, %f, %f]", 
                        		index1, index2, angle, distance));
                        
                        if (edges.hasNext()) json.append(",");
                    }
                    json.append("]");
                    json.append("}");
                }
                json.append("]}");
            } catch (Exception e) {
                System.err.println("Error processing " + img.Filename() + ": " + e.getMessage());
            }
        }

        json.append("]");
        return json.toString();
    }
    
    
    /**
     * @author Thobela FF 223079625
     * calculates the uclidean distance between two points
     * @param c1
     * @param c2
     * @return distance
     */
    private double calculateDistance(Coordinates c1, Coordinates c2) {
        return Math.sqrt(Math.pow(c2.getX() - c1.getX(), 2) + Math.pow(c2.getY() - c1.getY(), 2));
    }
    
    
    /**
     * @author Thobela FF 223079625
     * calculate angle between two pints
     * @param c1
     * @param c2
     * @return
     */
    private double calculateAngle(Coordinates c1, Coordinates c2) {
        return Math.atan2(c2.getY() - c1.getY(), c2.getX() - c1.getX());
    }
}