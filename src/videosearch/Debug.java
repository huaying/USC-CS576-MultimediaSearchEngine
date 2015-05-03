package videosearch;

import javafx.scene.chart.AreaChart;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by huayingt on 4/16/15.
 */
public class Debug {
    static public boolean debug = true;
    static public void print(Object s){
        if(debug) {
            System.out.println(s);
        }
    }
    static public void print(Object s,Object s2){
        if(debug) {
            System.out.println(s+", "+s2);
        }
    }
    static public void print(Object s,Object s2,Object s3){
        if(debug) {
            System.out.println(s+", "+s2+", "+s3);
        }
    }
    static public void print(Object [] s){
        if(debug) {
            for (int i = 0; i < s.length; i++) {
                if (i == s.length - 1) {
                    System.out.println(s[i]);
                } else {
                    System.out.print(s[i] + ", ");
                }
            }
        }
    }
    static public void print(CategoryResult c){
        if(debug){
            System.out.print(c.getCategory()+", "+c.getSimilarity()+"\n");
        }
    }
    static public void main(String argv[]){
        //ArrayList<BufferedImage> b = RGB.getRGBVideo("../database/flowers/flowers.video");
        int t;
        if ((t = 3 + 1) > 3){
            Debug.print(t);
        }
    }
}
