package videosearch;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;

import java.awt.image.BufferedImage;
import java.util.List;

/**
 * Created by zjjcxt on 5/1/15.
 */
public class SurfExtractor {
    public int execute(BufferedImage img){
        Surf surf = new Surf(img);
        List<SURFInterestPoint> interestPoints = surf.getFreeOrientedInterestPoints();
        return interestPoints.size();
    }
}
