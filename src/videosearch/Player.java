package videosearch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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


    public Player(ImageView imageView, Notifier notifier){
        this.imageView = imageView;
        this.notifier = notifier;


        that = this;
        playing = false;
        num_frame = 0;

        movies = new HashMap<String, List>();
    }

    protected  void preload(String path) throws IOException{

        File dir = new File(path);
        List<Image>frames = new ArrayList<Image>();
        ArrayList<BufferedImage> imgs;

        for (File file : dir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".video")) {
                imgs = RGB.getRGBVideo(file.getPath());
                for(BufferedImage img : imgs) {
                    frames.add(SwingFXUtils.toFXImage(img, null));
                }
                Debug.print("load: " + filename);
                break;
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
        movies.put(path,frames);

    }

    protected void load(String path) throws IOException {

        this.path = path;
        if(!movies.containsKey(path)) preload(path);
        frames = movies.get(path);
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
        }
    }

    protected void play(){
        playing = true;
        timer.play();
    }

    protected void pause(){
        playing = false;
        timer.pause();
    }

    protected void stop(){
        playing = false;
        timer.stop();
        this.preview();
        resetTimer();
    }

    protected boolean isPlaying(){
        return playing;
    }

    private void resetTimer(){
        cur_frame_id = 0;
        timer = new Timeline(new KeyFrame(Duration.millis(33), new EventHandler<ActionEvent>() {

            @Override
            public void handle(ActionEvent event) {

                Debug.print("frame: " + cur_frame_id);

                if (cur_frame_id < num_frame) {
                    imageView.setImage(frames.get(cur_frame_id));
                    cur_frame_id++;
                    if(imageView.getId().equals("video_src")){
                        notifier.notifying(String.valueOf(cur_frame_id));
                    }
                } else {
                    notifier.notifying(imageView.getId());
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

    }

}
