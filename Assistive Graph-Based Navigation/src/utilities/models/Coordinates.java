package utilities.models;


public class Coordinates {
	private double Y;
	private double X;
	
	public Coordinates(double x,double y) {
		this.X=x;
		this.Y=y;
	}
	
	
	public double[] getCoordinates() {
		
		double [] arr= {X,Y};
		return arr;
	}
	
	public static double distance(Coordinates c1, Coordinates c2) {
        return Math.sqrt(Math.pow(c2.getX() - c1.getX(), 2) + Math.pow(c2.getY() - c1.getY(), 2));
    }
	
	public double getY() {
		return this.Y;

	}
	
	public double getX() {
		return this.X;
	}
}