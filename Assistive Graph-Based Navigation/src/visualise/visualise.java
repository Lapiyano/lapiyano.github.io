package visualise;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.List;

import Connections.dbLoader;
import database.models.annotations;

public class visualise extends Canvas{

	private double scale;
	dbLoader loader = new dbLoader("src/tactile_train.db"); 
	
	public visualise(int x, double displayHeight, double scale) {
		super(x,displayHeight);
		this.scale=scale;
		
	}
    public void drawRectangles(List<annotations> annotations) {
        drawLabeledRectangles(annotations, null);
    }

    public void drawLabeledRectangles(List<annotations> annotations, List<String> labels) {
    	GraphicsContext gc = this.getGraphicsContext2D();       
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());
        
        for (int i = 0; i < annotations.size(); i++) {
            annotations a = annotations.get(i);
            double[] bbox = a.getBbox();
            
            double x = bbox[0] * scale;
            double y = bbox[1] * scale;
            double w = bbox[2] * scale;
            double h = bbox[3] * scale;

            gc.setStroke(Color.YELLOW);
            gc.setLineWidth(1.5);
            gc.strokeRect(x, y, w, h);

            if (labels != null && i < labels.size()) {
                String label = labels.get(i);
                gc.setFill(Color.YELLOW);
                gc.setGlobalAlpha(0.7);
                gc.fillRect(x, y, 20, 20);
                
                gc.setGlobalAlpha(1.0);
                gc.setFill(Color.BLACK);
                gc.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 14));
                gc.fillText(label, x + 4, y + 15);
            }
        }
    }

    public void drawResults(List<annotations> annotations, List<Analysis.GcnnClassifier.Prediction> preds, List<dataStructures.graph.models.AdjacencyListGraph<utilities.models.Coordinates, Double>> graphs) {
        GraphicsContext gc = this.getGraphicsContext2D();
        gc.clearRect(0, 0, this.getWidth(), this.getHeight());

        for (int i = 0; i < annotations.size(); i++) {
            annotations a = annotations.get(i);
            double[] bbox = a.getBbox();
            
            double x = bbox[0] * scale;
            double y = bbox[1] * scale;
            double w = bbox[2] * scale;
            double h = bbox[3] * scale;

            String colorHex;
            String label;

            if (i < preds.size()) {
                colorHex = preds.get(i).color;
                label = preds.get(i).categoryName;
            } else {
                colorHex = "#FFFFFF"; // White for unknown
                label = "Unknown";
            }
            
            Color c = Color.web(colorHex);

            gc.setStroke(c);
            gc.setLineWidth(2.5);
            gc.strokeRect(x, y, w, h);

            // Draw label background
            gc.setFill(c);
            gc.setGlobalAlpha(0.8);
            gc.fillRect(x, y, w, 20);

            gc.setGlobalAlpha(1.0);
            gc.setFill(Color.WHITE);
            gc.setFont(javafx.scene.text.Font.font("Courier New", javafx.scene.text.FontWeight.BOLD, 12));
            gc.fillText(label, x + 5, y + 15);
        }

        // Redraw graph on top
        if (graphs != null) {
            drawGraph(graphs, annotations);
        }
    }

    public void drawGraph(List<dataStructures.graph.models.AdjacencyListGraph<utilities.models.Coordinates, Double>> graphs, List<annotations> anns) {
        GraphicsContext gc = this.getGraphicsContext2D();
        
        for (int i = 0; i < graphs.size(); i++) {
            annotations ann = anns.get(i);
            double[] bbox = ann.getBbox();
            dataStructures.graph.models.AdjacencyListGraph<utilities.models.Coordinates, Double> graph = graphs.get(i);

            double offsetX = bbox[0] * scale;
            double offsetY = bbox[1] * scale;

            // Draw edges
            gc.setStroke(Color.CYAN);
            gc.setLineWidth(0.8);
            java.util.Iterator<dataStructures.graph.interfaces.IEdge<Double>> eIt = graph.edges();
            while (eIt.hasNext()) {
                var e = eIt.next();
                var ends = graph.endVertices(e);
                utilities.models.Coordinates c1 = ends[0].getElement();
                utilities.models.Coordinates c2 = ends[1].getElement();
                gc.strokeLine(offsetX + c1.getX() * scale, offsetY + c1.getY() * scale, 
                              offsetX + c2.getX() * scale, offsetY + c2.getY() * scale);
            }

            // Draw vertices
            gc.setFill(Color.GREEN);
            java.util.Iterator<dataStructures.graph.interfaces.IVertex<utilities.models.Coordinates>> vIt = graph.Vertices();
            while (vIt.hasNext()) {
                utilities.models.Coordinates c = vIt.next().getElement();
                gc.fillOval(offsetX + c.getX() * scale - 1, offsetY + c.getY() * scale - 1, 2, 2);
            }
        }
    }
}