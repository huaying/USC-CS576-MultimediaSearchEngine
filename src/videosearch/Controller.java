package videosearch;

import com.sun.org.apache.xerces.internal.impl.xpath.XPath;
import com.sun.org.apache.xerces.internal.parsers.AbstractXMLDocumentParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.chart.*;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Controller extends VBox implements Notifier{

    @FXML private ImageView video_src;
    @FXML private ImageView video_clip;
    @FXML private Button play_src;
    @FXML private Button play_clip;
    @FXML private ScrollBar scrollbar;
    @FXML private ComboBox matchbox;
    @FXML private AreaChart areachart;
    @FXML private Pane chartpane;
    @FXML private ComboBox analyzer;

    private Player player_src;
    private Player player_clip;

    List<CategoryResult> categoryResults;
    Map<String,HashMap> [] windowResults = new Map[4];
    List<String> paths;
    Line chartpointer;
    String cur_category;

    final int POINTER_START = 105;
    final int POINTER_END = 760;
    final int POINTER_LEN = POINTER_END - POINTER_START;
    final int POINTER_STARTY = 49;
    final int POINTER_ENDY = 163;


    public Controller() throws IOException {

        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource(
            "VideoSearch.fxml"
        ));

        fxmlLoader.setRoot(this);
        fxmlLoader.setController(this);


        try{
            fxmlLoader.load();
        }catch (IOException exception){
            throw new RuntimeException(exception);
        }



        setResult();

        setQueryVideo();
        setMatchList();
        setMatchVideo();

        initChartPointer();
        setAnalyserChoser();
    }

    private void initChartPointer(){

        chartpointer = new Line();
        chartpointer.setStartY(POINTER_STARTY);
        chartpointer.setEndY(POINTER_ENDY);
        chartpointer.setStartX(POINTER_START);
        chartpointer.setEndX(POINTER_START);
        chartpointer.setStrokeWidth(2);
        chartpointer.setStroke(Color.ORANGE);
        chartpane.getChildren().add(chartpointer);

    }

    private void moveChartPointer(int pos,int total){
        if(total !=0 ) {
            if (total > pos) {
                double m = POINTER_LEN * ((double) pos / (double) total) + POINTER_START;
                chartpointer.setStartX(m);
                chartpointer.setEndX(m);
            } else {
                chartpointer.setStartX(POINTER_END);
                chartpointer.setEndX(POINTER_END);
            }
        }
    }


    private void setAnalyser(String category){
        setAnalyser(category, 0);
        analyzer.setValue(category);
    }
    private void setAnalyser(String category,int analyze_mode){

        areachart.getData().clear();
        areachart.setCreateSymbols(false);
        XYChart.Series series = new XYChart.Series();

        Map<Integer,Double> map = windowResults[analyze_mode].get(category);
        for (Map.Entry<Integer,Double>entry : map.entrySet() ){
            Integer x = entry.getKey();
            Integer y = Double.valueOf(entry.getValue() * 100).intValue();
            series.getData().add(new XYChart.Data(x,y));
        }

        areachart.getData().add(series);
    }
    private void setAnalyserChoser(){
        analyzer.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                int id = analyzer.getSelectionModel().getSelectedIndex();
                setAnalyser(cur_category,id);
            }
        });

    }

    private void setResult(){

        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();

        for (int i=0;i < 4 ; i++) {
            windowResults[i] = new HashMap<String, HashMap>();
        }
        categoryResults = dbProcessor.getCategoryResult();
        cur_category = categoryResults.get(0).getCategory();
        for(CategoryResult cate : categoryResults){
            String cate_name = cate.getCategory();
            windowResults[0].put(cate_name, (HashMap<Integer, Double>) dbProcessor.getWindowResult(cate_name));
            windowResults[1].put(cate_name, (HashMap<Integer, Double>) dbProcessor.getWindowResult(cate_name));
            windowResults[2].put(cate_name, (HashMap<Integer, Double>) dbProcessor.getWindowResult(cate_name));
            windowResults[3].put(cate_name, (HashMap<Integer, Double>) dbProcessor.getWindowResult(cate_name));
        }

        dbProcessor.closeConnection();

    }
    private void setQueryVideo() throws IOException {
        player_clip = new Player(video_clip,this);
        player_clip.load("../query/second/");

    }
    private void setMatchVideo() throws IOException {

        player_src = new Player(video_src,this);
        Map<String,PlayerPreLoad> preloads= new HashMap<String,PlayerPreLoad>();

        paths = new ArrayList<String>();
//        for(CategoryResult cate : categoryResults){
//            //String path = "../databasejpg/" + cate.getCategory();
//            String path = "../database/" + cate.getCategory();
//            //paths.add(path);
//            //player_src.preload(path);
//            preloads.put(path, player_src.preload(path));
//        }
        categoryResults.parallelStream().forEach((cate) -> {
            try {
                String path = "../database/" + cate.getCategory();
                preloads.put(path, player_src.preload(path));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        for(CategoryResult cate : categoryResults){
            String path = "../database/" + cate.getCategory();
            paths.add(path);
            player_src.putPreload(path, preloads.get(path));

        }



        if(paths.size()!=0) {
            player_src.load(paths.get(0));
            setAnalyser(categoryResults.get(0).getCategory());
        }

        setScroll();
    }

    private void setMatchList(){

        NumberFormat percentageFormat = NumberFormat.getPercentInstance();
        percentageFormat.setMinimumFractionDigits(2);

        if(categoryResults.size()!=0) {
            CategoryResult cate = categoryResults.get(0);
            matchbox.setValue(String.format("%1$-16s",cate.getCategory())+percentageFormat.format(cate.getSimilarity()));
        }
        for(CategoryResult cate : categoryResults){
            matchbox.getItems().add(String.format("%1$-16s", cate.getCategory()) + percentageFormat.format(cate.getSimilarity()));
        }
        matchbox.valueProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                int id = matchbox.getSelectionModel().getSelectedIndex();
                try {
                    stopSrc();
                    player_src.load(paths.get(id));
                    cur_category = categoryResults.get(id).getCategory();
                    setAnalyser(cur_category);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });


    }

    @FXML
    private void playSrc(){
        if(player_src.isPlaying()){
            play_src.setText("Play");
            player_src.pause();
        }else {
            player_src.play();
            play_src.setText("Pause");
        }
    }

    @FXML
    private void playClip(){
        if(player_clip.isPlaying()){
            play_clip.setText("Play");
            player_clip.pause();
        }else {
            player_clip.play();
            play_clip.setText("Pause");
        }
    }

    @FXML
    private void stopSrc(){
        player_src.stop();
        play_src.setText("Play");
        scrollbar.setValue(0);
    }

    @FXML
    private void stopClip(){
        player_clip.stop();
        play_clip.setText("Play");
    }

    private void setScroll(){
        scrollbar.setMax(600);
        scrollbar.valueProperty().addListener(new ChangeListener<Number>() {
            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                //Debug.print(new Number[]{oldValue, newValue});
                player_src.gotoframe(newValue);
                moveChartPointer(newValue.intValue(), 450);
            }
        });
    }

    @Override
    public void notifying(String s) {
        //Debug.print(s);
        if(s.equals("video_src")){
            stopSrc();
        }
        if(s.equals("video_clip")){
            stopClip();

        }
        if(!s.startsWith("video")){
            scrollbar.setValue(Double.valueOf(s));
        }
    }
}
