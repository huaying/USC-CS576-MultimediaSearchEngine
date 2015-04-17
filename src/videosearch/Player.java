package videosearch;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huayingt on 4/16/15.
 */
public class Player{

    private String path;
    private int cur_frame_id;
    private int num_frame;
    protected List<Image> frames;
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
    }


    protected void load(String path){

        this.path = path;
        File dir = new File(path);
        frames = new ArrayList<Image>();

        for (File file : dir.listFiles()) {
            if (file.getName().endsWith(".rgb")) {
                frames.add(SwingFXUtils.toFXImage(this.getRGBImage(
                        file.getPath()
                ),null));
            }
        }
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
                    notifier.notifying(String.valueOf(cur_frame_id));
                } else {
                    notifier.notifying(imageView.getId());
                }
            }
        }));
        timer.setCycleCount(Timeline.INDEFINITE);

    }

    private BufferedImage getRGBImage(String path) {

        int width = 352;
        int height = 288;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {

            File file = new File(path);
            InputStream is = new FileInputStream(file);

            long len = file.length();
            byte[] bytes = new byte[(int) len];

            int offset = 0;
            int numRead;
            while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
            int ind = 0;
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    byte r = bytes[ind];
                    byte g = bytes[ind+height*width];
                    byte b = bytes[ind+height*width*2];
                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
                    img.setRGB(x, y, pix);
                    ind++;
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return img;
    }
}
