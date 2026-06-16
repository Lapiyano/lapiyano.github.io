package database.models;

public class Images {

	private int ID;
	private String Filename;
	private int width;
	private int height;
	
	public Images(int id,String filename, int width, int height) {
		this.ID=id;
		this.Filename=filename;
		this.width=width;
		this.height=height;
	}
	
	
	public int ID() {
		return this.ID;
	}
	
	public String Filename() {
		return this.Filename;
	}

	 
	
	public int Width() {
		return this.width;
	}
	
	
	public int Height() {
		return this.height;
	}
}
