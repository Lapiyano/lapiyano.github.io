package Extraction;

import java.util.ArrayList;
import java.util.List;
import dataStructures.graph.interfaces.IVertex;
import dataStructures.graph.models.AdjacencyListGraph;
import dataStructures.tree.models.BinaryTree;
import database.models.annotations;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import utilities.models.Coordinates;

/**
 * @author Thobela FF 223079625
 * @version CSC-Mini project
 * function that handles the entire extraction
 */
public class Extraction {

    public Image image;
    public ArrayList<AdjacencyListGraph<Coordinates, Double>> graphs;
    public BinaryTree<Coordinates, AdjacencyListGraph<Coordinates, Double>> customTree;
    private List<annotations> annotations;

    // constructor
    public Extraction(Image image, List<annotations> annotations) {
        this.image = image;
        this.graphs = new ArrayList<>();
        this.customTree = new BinaryTree<>();
        this.annotations = annotations;
    }

    
    /**
     * @author Thobela FF 223079625
     * getter for the list of graphs 
     * @return list of graphs
     */
    public ArrayList<AdjacencyListGraph<Coordinates, Double>> getGraph(){
    	return graphs;
    }
    
    /**
     * @author Thobela FF 223079625
     * Functtion performs the extraction by calling the other functions
     * @return image pieces
     */
    public List<Image> extract() {
        List<Image> pieces = extractRectanglePieces(image, annotations);
        
   
        this.graphs = new ArrayList<>(); 
        
        for (int i = 0; i < pieces.size(); i++) {
            Image piece = pieces.get(i);
            annotations ann = annotations.get(i);
            this.graphs.add(generateGraph(piece, ann));
        }
        
        return pieces;
    }

    /**
     * @author Thobela FF 223079625
     * @param piece
     * @param annotations
     * @return graph
     */
    private AdjacencyListGraph<Coordinates, Double> generateGraph(Image piece,annotations annotations) {
    	
    	
    	int w = (int) piece.getWidth();
        int h = (int) piece.getHeight();

        boolean[][] Block = createBlock(w, h, annotations);
        
    	piece=applyGrayscale(piece);
    	 piece=applyBlur(piece);
    	 piece = ApplySobelOperator(piece,Block);
    	
        List<Coordinates> nodes = findNodes(piece);
        AdjacencyListGraph<Coordinates, Double> graph = new AdjacencyListGraph<>();
        List<IVertex<Coordinates>> vertixlist = new ArrayList<>();

        for (Coordinates c : nodes) {
            vertixlist.add(graph.insertVertex(c));
        }

        populateEdges(graph, vertixlist);
        return graph;
    }

    /**
     * @author Thobela FF 223079625
     * Populates edges
     * @param graph
     * @param nodes
     */
    private void populateEdges(AdjacencyListGraph<Coordinates, Double> graph, List<IVertex<Coordinates>> nodes) {
        double maxDistance = 15.0;
        double YTolerance = 3.0;
        double XTolerance = 5.0;

        for (int i = 0; i < nodes.size(); i++) {
            for (int j = i + 1; j < nodes.size(); j++) {
                IVertex<Coordinates> v1 = nodes.get(i);
                IVertex<Coordinates> v2 = nodes.get(j);

                double changeX = Math.abs(v1.getElement().getX() - v2.getElement().getX());
                double changeY = Math.abs(v1.getElement().getY() - v2.getElement().getY());
                double distance = Math.sqrt(changeX * changeX + changeY * changeY);

                if (distance < maxDistance) {
                    if (changeX < YTolerance || changeY < XTolerance) {
                        graph.insertEdge(v1, v2, distance);
                    }
                }
            }
        }
    }

    
    /**
     * @author Thobela FF 223079625
     * https://docs.opencv.org/3.4/d2/d2c/tutorial_sobel_derivatives.html
     * https://www.youtube.com/watch?v=uihBwtPIBxM
     * edge detection 
     * @param piece
     * @param BLOCK
     * @return Binary Map( black and white) 
     */
    private WritableImage ApplySobelOperator(Image piece,boolean[][] BLOCK) {
        int[][] Gx = {{-3, 0, 3}, {-10, 0, 10}, {-3, 0, 3}};
        int[][] Gy = {{-3, -10, -3}, {0, 0, 0}, {3, 10, 3}};

        int w = (int) piece.getWidth();
        int h = (int) piece.getHeight();

        PixelReader reader = piece.getPixelReader();
        WritableImage edgePiece = new WritableImage(w, h);
        PixelWriter writer = edgePiece.getPixelWriter();

        for (int y = 1; y < h - 1; y++) {
            for (int x = 1; x < w - 1; x++) {
            	
            	if (!BLOCK[x][y]) {
                    writer.setColor(x, y, Color.BLACK);
                    continue;
                }
            	
                double px = 0, py = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                    	

                        double val = reader.getColor(x + j, y + i).getBrightness();
                        px += val * Gx[i + 1][j + 1];
                        py += val * Gy[i + 1][j + 1];
                    }
                }
                double Gradient = Math.sqrt(px * px + py * py);
                writer.setColor(x, y, Gradient > 0.3 ? Color.WHITE : Color.BLACK);
            }
        }
        return edgePiece;
    }
    
    
    /**
     * @author Thobela FF 223079625
     * partition an image into multiple pieces using the annotation data 
     * @param fullImage
     * @param annotationsList
     * @return list of image pieces
     */
    public List<Image> extractRectanglePieces(Image fullImage, List<annotations> annotationsList) {
        List<Image> pieces = new ArrayList<>();
        if (annotationsList == null || annotationsList.isEmpty()) {
            return pieces;
        }
        
        PixelReader reader = fullImage.getPixelReader();

        for (annotations ann : annotationsList) {
            double[] bbox = ann.getBbox();
            int x = (int) bbox[0];
            int y = (int) bbox[1];
            int width = (int) bbox[2];
            int height = (int) bbox[3];

            if (width <= 0 || height <= 0) {
                System.err.println("Skipping invalid bbox: " + width + "x" + height);
                continue;
            }

            WritableImage piece = new WritableImage(reader, x, y, width, height);
            pieces.add(piece);
        }
        return pieces;
    }

    
    /**
     * @author Thobela FF 223079625
     * function is responsible for finding the local maximum intensity
     * @param reader
     * @param x
     * @param y
     * @param counter- if it raches 15 that point is considered the local maximum
     * @param Width-of image
     * @param Height- of image
     * @return
     */
    private Coordinates FindPeak(PixelReader reader, int x, int y, int counter, int Width, int Height) {
        if (counter > 15) {
            return new Coordinates(x, y);
        } else if (x >= Width - 1) {
            return new Coordinates(x, y);
        } else if (y >= Height - 1) {
            return new Coordinates(x, y);
        } else if (x <= 1 || y <= 1) {
            return new Coordinates(x, y);
        } else {
            int newX = x;
            int newY = y;
            double currentMaxBrightness = reader.getColor(x, y).getBrightness();

            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    double bightness = reader.getColor(x + i, y + j).getBrightness();
                    if (bightness > currentMaxBrightness) {
                        currentMaxBrightness = bightness;
                        newX = x + i;
                        newY = y + j;
                    }
                }
            }

            if (newX != x || newY != y) {
                return FindPeak(reader, newX, newY, counter + 1, Width, Height);
            } else {
                return new Coordinates(x, y);
            }
        }
    }

    
    /**
     * @author Thobela FF 223079625
     * function responsible for ensuring that we do not duplicate vertices and that they are not too close to one another  
     * @param list
     * @param coordinates
     * @param minDistance
     * @return boolean
     */
    private boolean isVertexUnique(List<Coordinates> list, Coordinates coordinates, double minDistance) {
        for (Coordinates c : list) {
            double changeX = c.getX() - coordinates.getX();
            double changeY = c.getY() - coordinates.getY();
            if (Math.sqrt(changeX * changeX + changeY * changeY) < minDistance) return false;
        }
        return true;
    }

    
    /**
     * @author Thobela FF 223079625
     * responsible for finding vertices
     * @param image
     * @return
     */
    private List<Coordinates> findNodes(Image image) {
        List<Coordinates> coordinates = new ArrayList<>();
        PixelReader reader = image.getPixelReader();
        int Width = (int) image.getWidth();
        int Height = (int) image.getHeight();

        for (int y = 5; y < Height - 5; y += 1) {
            for (int x = 5; x < Width - 5; x += 1) {
                if (reader.getColor(x, y).getBrightness() > 0.8) {
                    Coordinates node = FindPeak(reader, x, y, 0, Width, Height);
                    if (isVertexUnique(coordinates, node, 5.0)) {
                        coordinates.add(node);
                    }
                }
            }
        }
        return coordinates;
    }
    
    
    /**
     * @author Thobela FF 223079625
     * apply the Gaussian blur filter 
     * @param piece
     * @return blured image piece
     */
    private WritableImage applyBlur(Image piece) {
        int w = (int) piece.getWidth();
        int h = (int) piece.getHeight();
        
        double[][] k = {{1/16.0,2/16.0,1/16.0},{2/16.0,4/16.0,2/16.0},{1/16.0,2/16.0,1/16.0}};
        PixelReader reader = piece.getPixelReader();
        WritableImage out = new WritableImage(w, h);
        PixelWriter writer = out.getPixelWriter();
        for (int y = 1; y < h-1; y++)
            for (int x = 1; x < w-1; x++) {
                double val = 0;
                for (int i = -1; i <= 1; i++)
                    for (int j = -1; j <= 1; j++)
                        val += reader.getColor(x+j, y+i).getBrightness() * k[i+1][j+1];
                writer.setColor(x, y, Color.gray(Math.min(1.0, val)));
            }
        return out;
    }
    
    
    /**
     * @author Thobela FF 223079625
     * Apply grayscale to image piece
     * @param piece
     * @return the grayscale piece
     */
    private WritableImage applyGrayscale(Image piece) {
        int w = (int) piece.getWidth();
        int h = (int) piece.getHeight();
        PixelReader reader = piece.getPixelReader();
        WritableImage output = new WritableImage(w, h);
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                Color c = reader.getColor(x, y);
                double gray = 0.299 * c.getRed() + 0.587 * c.getGreen() + 0.114 * c.getBlue();
                writer.setColor(x, y, Color.gray(gray));
            }
        }
        return output;
    }
    
    
    
    
    /**
     * this function makes use of ray casting to 
     * @author Thobela FF 223079625
     * @param w-width of the piece
     * @param h-heigh of the piece
     * @param ann- annotations associated with the piece
     * @return a boolean mask of the mapping of the image
     */
    private boolean[][] createBlock(int w, int h, annotations ann) {
        boolean[][] block = new boolean[w][h];
        double startX = ann.getBbox()[0];
        double startY = ann.getBbox()[1];

        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
              
            	/**
            	 * uses ray casting
            	 * learnt from https://www.youtube.com/watch?v=RSXM9bgqxJM&pp=ygUVUmF5IENhc3RpbmcgYWxnb3JpdGht
            	 */
                block[x][y] = ann.insidePolygon(x + startX, y + startY);
            }
        }
        return block;
    }
}