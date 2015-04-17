package videosearch;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollBar;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class Controller extends VBox implements Notifier{

    @FXML private ImageView video_src;
    @FXML private ImageView video_clip;
    @FXML private Button play_src;
    @FXML private Button play_clip;
    @FXML private ScrollBar scrollbar;

    private Player player_src;
    private Player player_clip;


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

        player_src = new Player(video_src,this);
        player_clip = new Player(video_clip,this);
        player_src.load("../database/flowers/");
        player_clip.load("../query/first/");

        setScroll();

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
                Debug.print(new Number[]{oldValue,newValue});
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
