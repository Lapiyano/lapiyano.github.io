package app;

import export.modelExports;

import javafx.application.Application;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;

public class ExportingMain extends Application {

	


	public static void main(String[] args) {
		
		launch();
	    
	}

	@Override
	public void start(Stage arg0) throws Exception {
		modelExports export = new modelExports();
	    
	    String[] types = {"test", "train", "valid"}; 

	    for (String type : types) {
	        try {
	            System.out.println("Starting extraction for: " + type + "...");
	            
	            String json = export.extractToJson(type);
	            
	            try (FileWriter file = new FileWriter("src/jsons/gcnn_" + type + ".json")) {
	                // System.out.println(json); // Optional: keep or remove depending on size
	                file.write(json);
	                file.flush();
	                System.out.println("Success! Exported to: gcnn_" + type + ".json");
	            } catch (IOException e) {
	                System.err.println("File Error: " + e.getMessage());
	            }

	        } catch (Exception ex) {
	            System.err.println("Extraction Error for " + type + ": " + ex.getMessage());
	            ex.printStackTrace();
	        }
	    }
	}

}
