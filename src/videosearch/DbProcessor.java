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

    public void offLineAudioTableInitialize(){
        try {
            String deleteATableSql = "DROP TABLE IF EXISTS AUDIOFEATURE";
            stmt.executeUpdate(deleteATableSql);
            String buildAudioFeatureTable = "CREATE TABLE IF NOT EXISTS AUDIOFEATURE (CATEGORY VARCHAR(255), FEATURE CLOB)";
            stmt.executeUpdate(buildAudioFeatureTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void offLineMotionTableInitialize(){
        try {
            String deleteATableSql = "DROP TABLE IF EXISTS MOTIONFEATURE";
            stmt.executeUpdate(deleteATableSql);
            String buildMotionFeatureTable = "CREATE TABLE IF NOT EXISTS MOTIONFEATURE (CATEGORY VARCHAR(255) , FRAMEID INT, BLOCKID INT, X INT, Y INT)";
            stmt.executeUpdate(buildMotionFeatureTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void offLineMotionTableInitialize2(){
        try {
            String deleteATableSql = "DROP TABLE IF EXISTS MOTIONFEATURE2";
            stmt.executeUpdate(deleteATableSql);
            String buildMotionFeatureTable = "CREATE TABLE IF NOT EXISTS MOTIONFEATURE2 (CATEGORY VARCHAR(255) , FRAMEID INT, MOTION DOUBLE)";
            stmt.executeUpdate(buildMotionFeatureTable);
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    public void offlineImageTableInitialize(String category){
        try {
            String deleteImageTableSql = "DROP TABLE IF EXISTS TEXTFEATURE_"+category+"";
            stmt.executeUpdate(deleteImageTableSql);
            String buildTextFeatureTable = "CREATE TABLE IF NOT EXISTS TEXTFEATURE_"+category+"" +
                    "(ID INT PRIMARY KEY auto_increment, INDEX VARCHAR(255), CONTRAST DOUBLE, SURF INT, COLORHISTOGRAM VARCHAR(255))";
            stmt.executeUpdate(buildTextFeatureTable);
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

    public void storeAudioFeature(String category, String audioFeature){
        try {
            String sql = "INSERT INTO AUDIOFEATURE VALUES('"+category+"', '"+audioFeature+"')";
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void storeMotionFeature(String category, int frameid, ArrayList<int []> vector){
        try {
            int i = 0;
            String insert_value = "";
            for(int [] v : vector){
                if(i > 0 ){
                    insert_value += ",";
                }
                insert_value+= "('"+category+"', "+frameid+", "+ (i++) +", "+v[0]+", "+v[1]+")";
                i++;
            }
            String sql = "INSERT INTO MOTIONFEATURE VALUES" + insert_value;
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void storeMotionFeature2(String category, ArrayList<Double> motions){
        try {
            int i = 0;
            String insert_value = "";
            for(double m : motions){
                if(i > 0 ){
                    insert_value += ",";
                }
                insert_value+= "('"+category+"',"+i+","+m+")";
                i++;
            }
            String sql = "INSERT INTO MOTIONFEATURE2 VALUES" + insert_value;
            Debug.print(sql);
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void storeTextFeature(String imagename, String category, double contrast, int surf, String colorHistogramResult)  {

        try {
            String sql = "INSERT INTO TEXTFEATURE_"+category+" VALUES(NULL ,  '"+imagename+"', "+contrast+", "+surf+", '"+colorHistogramResult+"')";
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
                tf.setContrast(rs.getDouble("CONTRAST"));
                tf.setSurf(rs.getInt("SURF"));
                String colorHistogram = rs.getString("COLORHISTOGRAM");
                List<String> temp = Arrays.asList(colorHistogram.split(","));
                List<Integer> colorHistogramResult = new ArrayList<Integer>();
                for(String item: temp){
                    colorHistogramResult.add(Integer.parseInt(item));
                }
                tf.setColorHistogram(colorHistogramResult);
                textFeatureList.add(tf);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return textFeatureList;
    }

    public List<MotionFeature> getMotionFeature(String category){
        List<MotionFeature> motionlist = new ArrayList<MotionFeature>();
        try {
            String sql = "SELECT * FROM MOTIONFEATURE WHERE CATEGORY='"+category+"'";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                MotionFeature mf = new MotionFeature(
                        rs.getInt("FRAMEID"),
                        rs.getInt("BLOCKID"),
                        rs.getInt("X"),
                        rs.getInt("Y")
                );
                motionlist.add(mf);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return motionlist;

    }
    public ArrayList<Double> getMotionFeature2(String category){
        ArrayList<Double> motionlist = new ArrayList<Double>();
        try {
            String sql = "SELECT * FROM MOTIONFEATURE2 WHERE CATEGORY='"+category+"'";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                motionlist.add(rs.getDouble("MOTION"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return motionlist;

    }

    public List<String> getAudioFeature(String category){
        List<String> audioResult = new ArrayList<String>();
        String result = "";
        try {
            String sql = "SELECT * FROM AUDIOFEATURE WHERE CATEGORY='"+category+"'";
            ResultSet rs = stmt.executeQuery(sql);
            while(rs.next()){
                result = rs.getString("FEATURE");
            }
            audioResult = Arrays.asList(result.split(","));
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return audioResult;

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
