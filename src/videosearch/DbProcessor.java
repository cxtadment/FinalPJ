package videosearch;

import java.sql.*;
import java.util.*;


/**
 * Created by zjjcxt on 4/13/15.
 */
public class DbProcessor {
    private Connection conn;
    private Statement stmt;

    public void buildConnection(){
        try {
            Class.forName(Constant.JDBC_DRIVER);
            conn = DriverManager.getConnection(Constant.DB_URL, Constant.USER, Constant.PASS);
            stmt = conn.createStatement();

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void offlineTableInitialize(String category){
        try {
            String deleteTableSql = "DROP TABLE IF EXISTS TEXTFEATURE_"+category+"";
            stmt.executeUpdate(deleteTableSql);
            String buildTextFeatureTable = "CREATE TABLE IF NOT EXISTS TEXTFEATURE_"+category+"" +
                    "(ID INT PRIMARY KEY auto_increment, INDEX VARCHAR(255), H1 DOUBLE, H2 DOUBLE, SURF INT)";
            stmt.executeUpdate(buildTextFeatureTable);
            String clearDataSql = "DELETE FROM TEXTFEATURE_"+category+"";
            stmt.executeUpdate(clearDataSql);
            String resetIdSql = "ALTER TABLE TEXTFEATURE_"+category+" ALTER COLUMN ID RESTART WITH 1";
            stmt.executeUpdate(resetIdSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onlineTableInitialize(){
        try {
            //create windowresult table
            String buildWindowResult = "CREATE TABLE IF NOT EXISTS WINDOWRESULT(WINDOWINDEX VARCHAR(255), CATEGORY VARCHAR(255), SIMILARITY DOUBLE)";
            stmt.executeUpdate(buildWindowResult);
            String clearWindowResult = "DELETE FROM WINDOWRESULT";
            stmt.executeUpdate(clearWindowResult);

            //create fileresult table
            String buildFileResult = "CREATE TABLE IF NOT EXISTS CATEGORYRESULT(CATEGORY VARCHAR(255), SIMILARITY DOUBLE)";
            stmt.executeUpdate(buildFileResult);
            String clearFileResult = "DELETE FROM CATEGORYRESULT";
            stmt.executeUpdate(clearFileResult);

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void storeTextFeature(String imagename, String category, double[] histogram, int surf)  {

        try {
            String sql = "INSERT INTO TEXTFEATURE_"+category+" VALUES(NULL ,  '"+imagename+"', "+histogram[0]+", "+histogram[1]+", "+surf+")";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void storeWindowResult(int windowIndex, String category, double windowSimilarity){
        try {
            String storeWindowSql = "INSERT INTO WINDOWRESULT VALUES("+windowIndex+", '"+category+"', "+windowSimilarity+")";
            stmt.executeUpdate(storeWindowSql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void storeCategoryResult(List<CategoryResult> categoryResultList){
        try {
            for(CategoryResult cr: categoryResultList){
                String categoryResultSql = "INSERT INTO CATEGORYRESULT VALUES('"+cr.getCategory()+"', "+cr.getSimilarity()+")";
                stmt.executeUpdate(categoryResultSql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<CategoryResult> getCategoryResult(){
        List<CategoryResult> categoryResultList = new ArrayList<CategoryResult>();
        try {
            String getCategoryResultSql = "SELECT * FROM CATEGORYRESULT";
            ResultSet rs = stmt.executeQuery(getCategoryResultSql);
            while(rs.next()){
                CategoryResult cr = new CategoryResult();
                cr.setCategory(rs.getString("CATEGORY"));
                cr.setSimilarity(rs.getDouble("Similarity"));
                categoryResultList.add(cr);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return categoryResultList;
    }

    public Map<Integer, Double> getWindowResult(String category){
        Map<Integer, Double> windowHashMap = new HashMap<Integer,Double>();
        try {
            String getWindowResultSql = "SELECT WINDOWINDEX, SIMILARITY FROM WINDOWRESULT WHERE CATEGORY='"+category+"'";
            ResultSet rs = stmt.executeQuery(getWindowResultSql);
            while(rs.next()){
                int index = rs.getInt("WINDOWINDEX");
                double similarity = rs.getDouble("SIMILARITY");
                windowHashMap.put(index, similarity);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return windowHashMap;
    }

    public List<TextFeature> getTextFeature(String category){
        List<TextFeature> textFeatureList = new ArrayList<TextFeature>();
        try {
            String sql = "SELECT * FROM TEXTFEATURE_"+category+"";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                TextFeature tf = new TextFeature();
                tf.setImageName(rs.getString("INDEX"));
                tf.setH1(rs.getDouble("H1"));
                tf.setH2(rs.getDouble("H2"));
                tf.setSurf(rs.getInt("SURF"));
                textFeatureList.add(tf);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return textFeatureList;
    }

    public void closeConnection(){
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
