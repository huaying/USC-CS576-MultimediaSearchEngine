package videosearch;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;
import java.util.stream.IntStream;


/**
 * Created by huayingt on 5/2/15.
 */
public class MotionTest {

    BufferedImage img1;
    BufferedImage img2;
    ArrayList<BufferedImage> imgs = new ArrayList<BufferedImage>();
    int sample_size = 1;
    int b_size = 16;
    int width = 352;
    int height = 288;
    int b_width = 352/b_size;
    int b_height = 288/b_size;
    ArrayList<int []> vectors = new ArrayList<int[]>();
    ArrayList<Double> motions = new ArrayList<Double>();

    static public void main(String argv[]){
        //new MotionTest().offline2();
        new MotionTest().online2();
    }
    public void online2(){
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        boolean first_img = true;
        ArrayList<ArrayList> vectors_container = new ArrayList<ArrayList>();
        ArrayList<int []>ref_vector = new ArrayList<int[]>();

        File dir = new File("../query/second/");
        for (File file : dir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".rgb")) {
                imgs.add(RGB.getRGBImage(file.getPath()));
            }
        }
        double motion;
        for(int i=0; i<imgs.size(); i+=sample_size){
            if(i>0) {
                img1 = imgs.get(i - 1);
                img2 = imgs.get(i);
                motion = 0;
                double [][] ms = new double[b_height][b_width];
                IntStream.range(0, b_height).parallel().
                    forEach(y -> IntStream.range(0, b_width).
                            forEach(x -> {
                                int [] p = this.computeSAD(x*b_size,y*b_size);
                                ms[y][x] = distance(p[0],p[1]);
                            }));
                for (int y = 0; y < b_height; y++) {
                    for (int x = 0; x < b_width; x++) {
                        motion += ms[y][x];
                    }
                }
                motions.add(motion);
            }
        }
        ArrayList<Double> mfs = dbProcessor.getMotionFeature2("sports");

        int [] vector;
        int block_num = b_height*b_width;
        int frame_num = mfs.size();
        int query_frames = motions.size();
        int cmploop = frame_num - query_frames;
        double v;
        ArrayList<Double> vs = new ArrayList<>();

//        Debug.print(mfs.get(120),motions.get(0));
        for (int i =0 ; i< cmploop ; i+=1){


            //compute the similarity of every clip
            v = 0;
            for(int j = 0 ; j<query_frames ; j++){
                //compute the similarity of every frame
                v += Math.abs(mfs.get(i + j) - motions.get(j));

            }
            vs.add(v);

            int [] mtable = {0,2000,5000,50000,100000,200000};
            int [] mpercent_table = {0,1,20,40,80,100};
            double p = 99;

            for(int x=0; x < mtable.length;x++ ){
                if(x!=0) {
                    if (v > mtable[x-1] && v < mtable[x]) {
                        p = ((v - mtable[x - 1]) / (mtable[x] - mtable[x - 1])) * (mpercent_table[x] - mpercent_table[x - 1]) + mpercent_table[x - 1];
                    }
                }
            }

            p = 100-p;
            Debug.print(i, v, p );

        }

        dbProcessor.closeConnection();


    }

    public ArrayList<Double> offline2(String filepath){
// ============= OffLine ===========================
        boolean first_img = true;


        HashMap<String,BufferedImage> imgmap = new HashMap<>();
        File dir = new File(filepath);
        Arrays.asList(dir.listFiles()).parallelStream().forEach(file -> {
            String filename = file.getName();
            if (filename.endsWith(".rgb")) {
                imgmap.put(filename,RGB.getRGBImage(file.getPath()));
            }
        });
        //sort hash_map by key
        imgs = new ArrayList<>(new TreeMap<String,BufferedImage>(imgmap).values());

//        for (File file : dir.listFiles()) {
//            String filename = file.getName();
//            if (filename.endsWith(".rgb")) {
//                imgs.add(RGB.getRGBImage(file.getPath()));
//            }
//        }
//        for (int i=0 ; i< imgs.size();i++){
//            Debug.print(i+1,imgs);
//            Debug.print(i+1,imgs1);
//        }

        double motion;
        for(int i=0; i<imgs.size(); i+=sample_size){
            if(i>0) {
                img1 = imgs.get(i - 1);
                img2 = imgs.get(i);
                motion = 0;
                double [][] ms = new double[b_height][b_width];
                IntStream.range(0, b_height).parallel().
                        forEach(y -> IntStream.range(0, b_width).
                                forEach(x -> {
                                    int [] p = this.computeSAD(x*b_size,y*b_size);
                                    ms[y][x] = distance(p[0],p[1]);
                                }));


                for (int y = 0; y < b_height; y++) {
                    for (int x = 0; x < b_width; x++) {
                        motion += ms[y][x];
                    }
                }
//                Debug.print("finish: " + i);
                motions.add(motion);
            }
        }
        return motions;

    }
    public void online(){
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        boolean first_img = true;
        ArrayList<ArrayList> vectors_container = new ArrayList<ArrayList>();
        ArrayList<int []>ref_vector = new ArrayList<int[]>();

        File dir = new File("../query/first/");
        for (File file : dir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".rgb")) {
                imgs.add(RGB.getRGBImage(file.getPath()));
            }
        }
        for(int i=0; i<imgs.size(); i+=sample_size){
            if(i>0) {
                img1 = imgs.get(i - 1);
                img2 = imgs.get(i);
                for (int y = 0; y < b_height; y++) {
                    for (int x = 0; x < b_width; x++) {
                        int [] p = this.computeSAD(x*b_size,y*b_size);
                        vectors.add(p);
                    }
                }
                Debug.print("finish: " + i);
                vectors_container.add(vectors);
                vectors = new ArrayList<int[]>();
            }
        }
        ArrayList<MotionFeature> mfs = (ArrayList<MotionFeature>) dbProcessor.getMotionFeature("musicvideo");
        MotionFeature mf;

        int [] vector;
        int block_num = b_height*b_width;
        int frame_num = mfs.size()/block_num;
        int query_frames = vectors_container.size();
        int cmploop = frame_num - query_frames;
        int v;

        for (int i =0 ; i< cmploop ; i+=1){


            //compute the similarity of every clip
            v = 0;
            for(int j = 0 ; j<query_frames ; j++){
                //compute the similarity of every frame
                 vectors = vectors_container.get(j);
                for (int bi = 0 ; bi<b_height;bi++){
                    for (int bj = 0 ; bj<b_width ; bj++){
                        mf = mfs.get((i+j) * block_num + (bi * b_width) + bj);
                        vector = vectors.get(bi * b_width + bj);
                        v += Math.abs(mf.x - vector[0]) + Math.abs(mf.y - vector[1]);
                    }
                }

            }
            Debug.print(i, v);
        }

        dbProcessor.closeConnection();


    }
    public void offline(){
// ============= OffLine ===========================
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        dbProcessor.offLineMotionTableInitialize();
        boolean first_img = true;
        File dir = new File("../database/musicvideo/");
        for (File file : dir.listFiles()) {
            String filename = file.getName();
            if (filename.endsWith(".rgb")) {
                imgs.add(RGB.getRGBImage(file.getPath()));
            }
        }

        for(int i=0; i<imgs.size(); i+=sample_size){
            if(i>0) {
                img1 = imgs.get(i - 1);
                img2 = imgs.get(i);
                for (int y = 0; y < b_height; y++) {
                    for (int x = 0; x < b_width; x++) {
                        int [] p = this.computeSAD(x*b_size,y*b_size);
                        vectors.add(p);
                    }
                }
                Debug.print("finish: " + i);
                dbProcessor.storeMotionFeature("musicvideo", i, vectors);
                vectors = new ArrayList<int[]>();
            }
        }
        dbProcessor.storeMotionFeature2("musicvideo", motions);
        dbProcessor.closeConnection();

    }
    private double distance(int x, int y){
        return Math.sqrt(x*x + y*y);
    }

    private int[] computeSAD(int x, int y){

        int k = 32;
        int left;
        int right;
        int up;
        int down;
        int min ;
        int tmp;
        int orix = x;
        int oriy = y;

        while(k != 1){
            int k2 = k>>2;
            left = (x < k2)?0: x- k2;
            right = (x + k2 + b_size > width)? width-b_size: x + k2;
            up = (y < k2)?0: y- k2;
            down = (y + k2 + b_size > height)? height-b_size: y + k2;

            min = 999999;
            if((tmp=this.compBlock(left, up, orix, oriy)) < min ){
                min = tmp;
                x = left;
                y = up;
            }
            if((tmp=this.compBlock(left, oriy, orix, oriy)) < min ) {
                min = tmp;
                x = left;
                y = oriy;
            }
            if((tmp=this.compBlock(left, down, orix, oriy)) < min ) {
                min = tmp;
                x = left;
                y = down;
            }
            if((tmp=this.compBlock(orix, up, orix, oriy)) < min ) {
                min = tmp;
                x = orix;
                y = up;
            }
            if((tmp=this.compBlock(orix, oriy, orix, oriy)) < min ) {
                min = tmp;
                x = orix;
                y = oriy;
            }
            if((tmp=this.compBlock(orix, down, orix, oriy)) < min ) {
                min = tmp;
                x = orix;
                y = down;
            }
            if((tmp=this.compBlock(right, up, orix, oriy)) < min ) {
                min = tmp;
                x = right;
                y = up;
            }
            if((tmp=this.compBlock(right, oriy, orix, oriy)) < min ) {
                min = tmp;
                x = right;
                y = oriy;
            }
            if((tmp=this.compBlock(right, down, orix, oriy)) < min ) {
                min = tmp;
                x = right;
                y = down;
            }
            k /= 2;
        }

        int [] vector = {x-orix,y-oriy};
        return vector;

    }

    private int compBlock(int refx,int refy, int x, int y){
        int r = 0;

        for (int i = 0 ; i<b_size;i++){
            for (int j = 0 ; j<b_size ; j++){

                r += this.compPixel(img1.getRGB(x + j, y + i), img2.getRGB(refx + j, refy + i));
            }
        }
        return r;

    }

    private int compPixel(int p1, int p2){
        int r1 =(p1 >> 16) & 0xff;
        int g1 = (p1 >> 8) & 0xff;
        int b1 = p1 & 0xff;
        int r2 = (p2 >> 16) & 0xff;
        int g2 =(p2 >> 8 ) & 0xff;
        int b2 = p2 & 0xff;
        return Math.abs(r1-r2) + Math.abs(g1-g2) + Math.abs(b1-b2);
    }
    public void test(){
        //String path1 = "../database/musicvideo/musicvideo121.rgb";
        //String path2 = "../database/musicvideo/musicvideo122.rgb";
        String path1 = "../query/first/first001.rgb";
        String path2 = "../query/first/first002.rgb";
        img1 = RGB.getRGBImage(path1);
        img2 = RGB.getRGBImage(path2);
        for (int y = 0; y < b_height; y++) {
            for (int x = 0; x < b_width; x++) {
                //vectors.add(this.computeSAD(x * b_size, y * b_size));
                int [] a = this.computeSAD(x * b_size, y * b_size);
                Debug.print(x+","+y+": ",a[0],a[1]);
            }
        }
    }

}
