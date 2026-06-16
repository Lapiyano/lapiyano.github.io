package Connections;

import java.sql.*;

import java.util.ArrayList;
import java.util.List;
import database.models.annotations;
import utilities.models.Coordinates;

/**
 * @author Thobela FF 223079625
 * @version CSC-Mini project
 * responsible for extracting data from the sqlite database
 */
public class dbLoader {
    private String dbPath;

    // constructor
    public dbLoader(String dbPath) {
        this.dbPath = "jdbc:sqlite:" + dbPath;//path database
    }

    
    /**
     * @author Thobela FF 223079625
     * retrives all the annotations linked to an image in the database
     * @param imageId
     * @return List of annotations
     */
    public List<annotations> getAnnotationsForImage(int imageId) {
        List<annotations> results = new ArrayList<>();
        String sql = "SELECT a.id, a.image_id, a.category_id, a.bbox_x, a.bbox_y, a.bbox_w, a.bbox_h, s.coords " +
                     "FROM annotations a " +
                     "LEFT JOIN segmentations s ON a.id = s.annotation_id " +
                     "WHERE a.image_id = ? " +
                     "GROUP BY a.id"; 

        try (Connection conn = DriverManager.getConnection(dbPath);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, imageId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                double[] bbox = new double[] {
                    rs.getDouble("bbox_x"), rs.getDouble("bbox_y"),
                    rs.getDouble("bbox_w"), rs.getDouble("bbox_h")
                };

                String coordsStr = rs.getString("coords");
                ArrayList<Coordinates> segs = (coordsStr != null && !coordsStr.isEmpty()) 
                                              ? parseCoords(coordsStr) 
                                              : new ArrayList<>();

                results.add(new annotations(
                    rs.getInt("id"),
                    rs.getInt("image_id"),
                    rs.getInt("category_id"),
                    bbox,
                    segs
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return results;
    }
    
    
    /**
     * @author Thobela FF 223079625
     * retrives all image information
     * @return list of image data
     */
    public List<database.models.Images> getAllImages() {
        List<database.models.Images> results = new ArrayList<>();
        String sql = "SELECT id, file_name, width, height FROM images";
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new database.models.Images(
                    rs.getInt("id"),
                    rs.getString("file_name"),
                    rs.getInt("width"),
                    rs.getInt("height")
                ));
            }
        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
        return results;
    }

    
    /**
     * @author Thobela FF 223079625
     * retrives all the catagories on the db 
     * @return list of catagories
     */
    public List<database.models.categories> getAllCategories() {
        List<database.models.categories> results = new ArrayList<>();
        String sql = "SELECT id, name FROM categories";
        try (Connection conn = DriverManager.getConnection(dbPath);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                results.add(new database.models.categories(
                    rs.getInt("id"),
                    rs.getString("name")
                ));
            }
        } catch (SQLException e) {
            System.err.println("DB Error: " + e.getMessage());
        }
        return results;
    }

    /**
     * @author Thobela FF 223079625
     * converts string to coordinate
     * @param data
     * @return
     */
    private ArrayList<Coordinates> parseCoords(String data) {
        ArrayList<Coordinates> list = new ArrayList<>();
        String[] parts = data.split(",");
        for (int i = 0; i < parts.length; i += 2) {
            double x = Double.parseDouble(parts[i]);
            double y = Double.parseDouble(parts[i+1]);
            list.add(new Coordinates(x, y)); 
        }
        return list;
    }
}