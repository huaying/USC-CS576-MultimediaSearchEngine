package videosearch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.ObjectProperty;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 * Created by huayingt on 4/16/15.
 */
public class Player{

    private String path;
    private int cur_frame_id;
    private int num_frame;
    protected List<Image> frames;
    protected Map<String,List> movies;
    protected ImageView imageView;
    protected Timeline timer;
    protected boolean playing;
    protected Player that;
    protected Notifier notifier;
    protected Map<String,MediaPlayer> movies_music;
    MediaPlayer mediaPlayer;


    public Player(ImageView imageView, Notifier notifier){
        this.imageView = imageView;
        this.notifier = notifier;


        that = this;
        playing = false;
        num_frame = 0;

        movies = new HashMap<String, List>();
        movies_music = new HashMap<String, MediaPlayer>();
    }

    protected  PlayerPreLoad preload(String path) throws IOException{

        File dir = new File(path);
        List<Image>frames = new ArrayList<Image>();
        ArrayList<BufferedImage> imgs;
        MediaPlayer mp = null;

        for (File file : dir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".wav")){
                mp = new MediaPlayer(new Media(new File(file.getPath()).toURI().toString()));
            }
            if (filename.endsWith(".rgb")) {
                frames.add(SwingFXUtils.toFXImage(RGB.getRGBImage(
                        file.getPath()
                ),null));
                //Debug.print("load: " + filename);

            }else if(filename.endsWith(".jpg")){
                frames.add(SwingFXUtils.toFXImage(ImageIO.read(file),null));
                //Debug.print("load: " + filename);
            }

        }
        return new PlayerPreLoad(frames, mp);
    }
    protected void putPreload(String path,PlayerPreLoad pp){

        movies.put(path,pp.frames);
        movies_music.put(path, pp.mediaPlayer);
    }

    protected void load(String path) throws IOException {

        this.path = path;
        if(!movies.containsKey(path)){
            putPreload(path,preload(path));
        }
        frames = movies.get(path);
        mediaPlayer = movies_music.get(path);
        num_frame = frames.size();
        resetTimer();
        preview();

    }

    protected void preview(){
        if(frames.size() != 0 ) {
            imageView.setImage(frames.get(0));
        }
    }
    protected void gotoframe(Number n){

        int i = n.intValue();
        if(i != cur_frame_id && i != num_frame) {
            cur_frame_id = i;
            imageView.setImage(frames.get(i));
            mediaPlayer.stop();
            mediaPlayer.setStartTime(Duration.millis(33.33333*i));
            if (playing){
                mediaPlayer.play();
            }
        }
    }

    protected void play(){
        playing = true;
        timer.play();
        mediaPlayer.play();
    }

    protected void pause(){
        playing = false;
        timer.pause();
        mediaPlayer.pause();
    }

    protected void stop(){
        playing = false;
        timer.stop();
        mediaPlayer.stop();
        mediaPlayer.setStartTime(Duration.ZERO);
        this.preview();
        resetTimer();
    }

    protected boolean isPlaying(){
        return playing;
    }

    private void resetTimer(){
        cur_frame_id = 0;
        timer = new Timeline(new KeyFrame(Duration.millis(33), event -> {

            //Debug.print("frame: " + cur_frame_id,num_frame);

            if (cur_frame_id < num_frame) {
                imageView.setImage(frames.get(cur_frame_id));
                cur_frame_id++;
                if(imageView.getId().equals("video_src")){
                    notifier.notifying(String.valueOf(cur_frame_id));
                }
            } else {
                notifier.notifying(imageView.getId());
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

    }

}
