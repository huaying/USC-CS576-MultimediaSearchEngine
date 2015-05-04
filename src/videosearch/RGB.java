package videosearch;

import java.awt.image.BufferedImage;
import javafx.scene.image.Image;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by huayingt on 4/17/15.
 */
public class RGB {

    static public void main(String args[]) throws IOException {

    }
    static public void writeVideo() throws  IOException{
        String path_src = "../database/";
        String video_name;
        int c;
        long len;
        byte[] bytes;

        File dir = new File(path_src);

        for (File subdir : dir.listFiles()){
            video_name = subdir.getName();
            Debug.print(video_name);
            if(!subdir.getName().startsWith(".")) {
                FileOutputStream out = new FileOutputStream(path_src+"/"+video_name+"/"+video_name+".video");
                for (File file : subdir.listFiles()) {
                    if (file.getName().endsWith(".rgb")) {

                        FileInputStream is = new FileInputStream(file);
                        len = file.length();
                        bytes = new byte[(int) len];

                        int offset = 0;
                        int numRead;
                        while (offset < bytes.length && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                            offset += numRead;
                        }
                        out.write(bytes);

                    }
                }
            }
        }

    }
    static public void wirteJpeg() throws IOException {

        String path_src = "../database/";
        String path_des = "../databasejpg/";

        File dir = new File(path_src);
        List<Image> frames = new ArrayList<Image>();

        //File f = new File("../x/text.jpg");
        //f.getParentFile().mkdir();
        //ImageIO.write(RGB.getRGBImage("../database/flowers/flowers526.rgb"),"JPEG",f);

        File dbjpg = new File(path_des);
        if(!dbjpg.exists()){
            dbjpg.mkdir();
        }
        for (File subdir : dir.listFiles()){

            if(!subdir.getName().startsWith(".")) {
                Debug.print(subdir.getName());
                for (File file : subdir.listFiles()) {
                    if (file.getName().endsWith(".rgb")) {
                        File f = new File(path_des+subdir.getName()+"/"+ FilenameUtils.removeExtension(file.getName())+".jpg");
                        if(!f.getParentFile().exists()){
                            f.getParentFile().mkdir();
                        }
                        ImageIO.write(RGB.getRGBImage(file.getPath()),"JPEG",f);
                        Debug.print("from: "+file.getPath()+" to: "+f.getPath());
                    }

                }
            }
        }

    }
    static public ArrayList getRGBVideo(String path) {

        int width = 352;
        int height = 288;
        int len = 304128;

        ArrayList<BufferedImage> bufferedImages = new ArrayList<BufferedImage>();

        try {

            File file = new File(path);
            InputStream is = new FileInputStream(file);

            long totallen = file.length();
            byte[] bytes = new byte[len];


            int offset = 0;
            int numRead;
            int ind;
            BufferedImage img;

            while ((numRead = is.read(bytes, offset, bytes.length - offset))>=0){
                if(offset < bytes.length){
                    offset += numRead;
                }else{
                    img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
                    ind = 0;
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
                    bufferedImages.add(img);
                    offset = 0;
                    bytes = new byte[len];
                }
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bufferedImages;
    }
    static public BufferedImage getRGBImage(String path) {

        int width = 352;
        int height = 288;
        int len = 304128;

        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        try {

            File file = new File(path);
            InputStream is = new FileInputStream(file);

            //long len = file.length();
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
