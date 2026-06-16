package database.models;

public class categories {

	private  int ID;
	private String Name;
	
	public categories(int id,String name) {
		this.ID=id;
		this.Name=name;
	}
	
	public int ID() {
		return this.ID;
	}
	
	public String Name() {
		return this.Name;
	}
}
