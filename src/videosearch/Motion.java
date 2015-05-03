package videosearch;

import javafx.embed.swing.SwingFXUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by huayingt on 5/2/15.
 */
public class Motion {

    BufferedImage img1;
    BufferedImage img2;
    int b_size = 4;
    int width = 352;
    int height = 288;
    int b_width = 352/b_size;
    int b_height = 288/b_size;
    ArrayList<int []> vectors;

    static public void main(String argv[]){
        new Motion();
    }
    Motion(){
        String path1 = "../database/sports/sports375.rgb";
        String path2 = "../database/sports/sports376.rgb";

        //File dir = new File("../database/");
        //for (File file : dir.listFiles()) {
        //    String filename = file.getName();
        //    if (filename.endsWith(".rgb")) {
        //        this.img1 = RGB.getRGBImage(file.getPath());
        //        this.img2 = RGB.getRGBImage(file.getPath());
        //
        //        Debug.print("load: " + filename);
        //        break;
        //    }
        //}


        this.img1 = RGB.getRGBImage(path1);
        this.img2 = RGB.getRGBImage(path2);
        for (int y = 0; y < b_height; y++) {
            for (int x = 0; x < b_width; x++) {
                //vectors.add(this.computeSAD(x * b_size, y * b_size));
                int [] a = this.computeSAD(x * b_size, y * b_size);
                Debug.print(x+","+y+": ",a[0],a[1]);
            }
        }

    }
    private int[] computeSAD(int x, int y){

        int k = 16;
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

}
