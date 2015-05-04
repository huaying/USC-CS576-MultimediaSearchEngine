package videosearch;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.*;

/**
 * Created by huayingt on 5/3/15.
 */
public class SoundClip implements LineListener{

    boolean playing;

    public static void main(String[] args) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        new SoundClip().play();
    }

    @Override
    public void update(LineEvent event) {
        LineEvent.Type type = event.getType();

        if (type == LineEvent.Type.START) {
            System.out.println("Playback started.");

        } else if (type == LineEvent.Type.STOP) {
            playing = true;
            System.out.println("Playback completed.");
        }

    }

    public void play() throws  UnsupportedAudioFileException, IOException, LineUnavailableException {

        String filename = "../database/musicvideo/musicvideo.wav";

        InputStream bufferedIn = new BufferedInputStream(new FileInputStream(filename));
        AudioInputStream audio = AudioSystem.getAudioInputStream(bufferedIn);

        Clip clip = AudioSystem.getClip();
        clip.addLineListener(this);
        clip.open(audio);
        clip.setMicrosecondPosition(8000000);
        clip.start();

        while(!playing){
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        clip.stop();

    }

}
