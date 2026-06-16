package gui.Components;



import javafx.scene.control.TextArea;
import utils.CustomColours;

public class CustomTextArea extends TextArea{
	
	
	private String description;
	private String name;
	public CustomTextArea(String name,CustomColours colour) {
		this.setText(name);
		this.setEditable(false);
		this.setStyle(colour.getValue());
		this.description=colour.getValue();
		this.name=name;
	}
	
	public void appendText(String log) {
		this.appendText(log);
	}
	
	public void clearArea() {
		this.clear();
		this.setText(name);
	}
	
	
	
}