package Connections;


import java.util.ArrayList;
import java.util.List;
import dataStructures.Dictionary.models.HashDictionary;
import database.models.annotations;

public class AnalysisContext {


	    private HashDictionary<Integer, List<annotations>> currentDataset;
	    private dbLoader loader;
	    private String phase;

	    public AnalysisContext(String phase, String dbPath) {
	        this.phase = phase.toLowerCase();
	        this.loader = new dbLoader(dbPath);
	        
	        
	        this.currentDataset = new HashDictionary<>(307);
	    }

	    
	    public void loadData(List<Integer> ids) {
	        System.out.println("Initializing " + phase + " phase...");
	        for (int id : ids) {
	            List<annotations> data = loader.getAnnotationsForImage(id);
	            if (data != null && !data.isEmpty()) {
	                currentDataset.insert(id, data);
	            }
	        }
	    }

	    public List<Integer> getAllLoadedIds() {
	        List<Integer> ids = new ArrayList<>();
	        var it = currentDataset.entries();
	        while (it.hasNext()) {
	            ids.add(it.next().getKey());
	        }
	        return ids;
	    }

	    public List<annotations> getAnnotations(int imageId) {
	        var entry = currentDataset.find(imageId);
	        return (entry != null) ? entry.getElement() : null;
	    }
	}

