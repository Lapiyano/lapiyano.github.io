package Analysis;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import Connections.dbLoader;
import dataStructures.Dictionary.models.Entry;
import dataStructures.Dictionary.models.HashDictionary;
import dataStructures.graph.models.AdjacencyListGraph;
import database.models.Images;
import database.models.annotations;
import database.models.categories;
import javafx.scene.image.Image;
import utilities.models.Coordinates;

public class Classification {
	private ArrayList<AdjacencyListGraph<Coordinates, Double>> graphs;

	private HashDictionary<Integer, annotations> annotation_dictionary = new HashDictionary<>(307);
	private HashDictionary<Integer, Images> images_dictionary = new HashDictionary<>(307);
	private HashDictionary<Integer, categories> categories_dictionary = new HashDictionary<>(307);

	public Classification(String filename) {

		dbLoader loader = new dbLoader(filename);

		List<annotations> annList = loader.getAnnotationsForImage(4);
		List<Images> imagesList = loader.getAllImages();
		List<categories> categorisList = loader.getAllCategories();

		for (annotations a : annList) {
			annotation_dictionary.insert(a.getImageID(), a);

		}
		for (Images i : imagesList) {
			images_dictionary.insert(i.ID(), i);

		}

		for (categories c : categorisList) {
			categories_dictionary.insert(c.ID(), c);
		}

	}

	
	
	//the functions fetches the id using the file name and if it does not exist on open file( training,testing or valid) it returns -1
	public int getImageId(String imageName) {
	   
	    Iterator<Entry<Integer, Images>> it = images_dictionary.entries();
	    
	    while(it.hasNext()) {
	        Entry<Integer, Images> current = it.next();
	        
	        if(current.getElement().Filename().equals(imageName)) {
	            return current.getKey();
	        }
	    }
	    return -1;
	}
	
	
	
	public ArrayList<AdjacencyListGraph<Coordinates, Double>> classify(String name){
		
		
		int id= getImageId(name);
		
		
		return null;
	}
}
