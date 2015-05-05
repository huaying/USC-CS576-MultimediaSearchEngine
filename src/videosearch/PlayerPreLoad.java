package videosearch;

import javafx.scene.media.MediaPlayer;

import java.util.List;

/**
 * Created by huayingt on 5/4/15.
 */
public class PlayerPreLoad {
    public List frames;
    public MediaPlayer mediaPlayer;

    PlayerPreLoad(List l,MediaPlayer m){
        frames = l;
        mediaPlayer = m;
    }
}
