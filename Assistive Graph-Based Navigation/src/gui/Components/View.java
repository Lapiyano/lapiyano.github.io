package gui.Components;

import java.util.List;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class View  extends Canvas {

	
	 private GraphicsContext gc;

    public View(int x, int y) { 
    	super(x,y) ;
    	gc = this.getGraphicsContext2D();
   }
    
  
    
    
    
    /*public void renderGraph(List<Graph> Graph) {
    	

        for (graphinstance s : Graph) {
        	
            if (!s.isValid) continue;

            //Transparency level for the bounds
            gc.setGlobalAlpha(0.15);
            gc.setFill(s.isHazard ? Color.RED : Color.LIME);
            drawBounds(gc, s); 
            
            //transparency level for labels and the nodes/edges
            gc.setGlobalAlpha(1.0);
            Utilities.drawEdges(s, gc);
            Utilities.drawNodes(s,gc);
        }
    }*/
    
    

    
}