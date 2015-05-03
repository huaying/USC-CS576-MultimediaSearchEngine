package videosearch;

/**
 * Created by huayingt on 5/3/15.
 */
public class MotionFeature {
    public int frameid;
    public int blockid;
    public int x;
    public int y;

    MotionFeature(int frameid,int blockid, int x, int y){
        this.frameid = frameid;
        this.blockid = blockid;
        this.x = x;
        this.y = y;
    }
}
