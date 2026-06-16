package gui.Components;

import javafx.scene.control.Button;

import utils.CustomColours;



public class CustomButton extends Button {

	
	public CustomButton(String name,CustomColours colour) {
		super(name);
		this.setStyle(colour.getValue());
	}
}