package videosearch;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class Controller extends VBox implements Notifier{

    @FXML private ImageView video_src;
    @FXML private ImageView video_clip;
    @FXML private Button play_src;
    @FXML private Button play_clip;
    @FXML private ScrollBar scrollbar;
    @FXML private ComboBox matchbox;

    private Player player_src;
    private Player player_clip;

    List<CategoryResult> categoryResults;
    List<String> paths;

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
    }
    private void setResult(){

        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();

        categoryResults = dbProcessor.getCategoryResult();
        dbProcessor.closeConnection();

    }
    private void setQueryVideo() throws IOException {
        player_clip = new Player(video_clip,this);
        player_clip.load("../query/first/");

    }
    private void setMatchVideo() throws IOException {

        player_src = new Player(video_src,this);
        //player_src.load("../databasejpg/flowers/");
        //player_src.load("../database/flowers/");

        paths = new ArrayList<String>();
        for(CategoryResult cate : categoryResults){
            String path = "../databasejpg/" + cate.getCategory();
            paths.add(path);
            player_src.preload(path);
        }

        if(paths.size()!=0) {
            player_src.load(paths.get(0));
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
                Debug.print(new Number[]{oldValue, newValue});
                player_src.gotoframe(newValue);
            }
        });
    }




    @Override
    public void notifying(String s) {
        Debug.print(s);
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
