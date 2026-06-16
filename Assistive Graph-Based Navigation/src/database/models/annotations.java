package database.models;

import java.util.ArrayList;
import java.util.List;

import utilities.models.Coordinates;

public class annotations {

	private int ID;
	private int ImageID;
	private int categoryID;
	private double[] bbox = new double[4];
	private List<Coordinates> segmentations= new ArrayList<>();
	
	
	public annotations(int iD, int imageID, int categoryID, double[] bbox, List<Coordinates> segmentations) {
		ID = iD;
		ImageID = imageID;
		this.categoryID = categoryID;
		this.bbox = bbox;
		this.segmentations = segmentations;
	}
	
	
	
	public int getID() {
		return ID;
	}


	
	public void setID(int iD) {
		ID = iD;
	}


	public int getImageID() {
		return ImageID;
	}


	
	public void setImageID(int imageID) {
		ImageID = imageID;
	}


	
	public int getCategoryID() {
		return categoryID;
	}

	public void setCategoryID(int categoryID) {
		this.categoryID = categoryID;
	}


	
	public double[] getBbox() {
		return bbox;
	}


	public void setBbox(double[] bbox) {
		this.bbox = bbox;
	}


	public List<Coordinates> getSegmentations() {
		return segmentations;
	}


	public void setSegmentations(List<Coordinates> segmentations) {
		this.segmentations = segmentations;
	}
	
	
	/**
	 * 
	 * @param pointX
	 * @param pointY
	 * @return
	 */
	public boolean insidePolygon(double pointX, double pointY) {
        int n = segmentations.size();
        if (n == 0) return true; // Default to true if no polygon defined (process entire bbox)

        boolean Is_Inside = false;
        for (int i = 0, j = n - 1; i < n; j = i++) {
        	
        	Coordinates C1=segmentations.get(i);
        	Coordinates C2=segmentations.get(j);

            if (((C1.getY() > pointY) != (C2.getY() > pointY)) && (pointX < (C2.getX() - C1.getX()) * (pointY - C1.getY()) / (C2.getY() - C1.getY()) + C1.getX()))
            {
            	Is_Inside = !Is_Inside;
            }
        }
        return Is_Inside;
    }
	
}
