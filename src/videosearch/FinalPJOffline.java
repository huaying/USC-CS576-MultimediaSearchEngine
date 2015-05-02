package videosearch;

import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class FinalPJOffline {



    public static void main(String args[]) throws IOException {

        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();

        for(int k=0; k<Constant.CATEGORY.length; k++) {
            File dir = new File(Constant.DB_DIR_PATH + Constant.CATEGORY[k] + "/");
            List<String> imageFiles = new ArrayList<String>();
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(Constant.IMAGE_EXTENTSION)) {
                    imageFiles.add(file.getName());
                }
            }

            dbProcessor.offlineTableInitialize(Constant.CATEGORY[k]);
            for (String imagename : imageFiles) {
                String imageNamewithoutEx = FilenameUtils.removeExtension(imagename);
                String imagePath = Constant.DB_DIR_PATH + Constant.CATEGORY[k] + "/" + imagename;
                BufferedImage img = converter(imagePath);

                /*image feature extract*/

                //tamura feature
                TextFeatureExtractor textFeatureExtractor = new TextFeatureExtractor();
                double[] histogram = textFeatureExtractor.extract(img);
                SurfExtractor surfExtractor = new SurfExtractor();
                int surf = surfExtractor.execute(ImageUtils.scaleImage(img, Constant.SCALE_INDEX));
                dbProcessor.storeTextFeature(imagename, Constant.CATEGORY[k], histogram, surf);
            }
        }
        dbProcessor.closeConnection();

    }

    public static BufferedImage converter(String imagePath){
        BufferedImage img = new BufferedImage(Constant.WIDTH, Constant.HEIGHT, BufferedImage.TYPE_INT_RGB);
        File file = new File(imagePath);
        try {
            InputStream is = new FileInputStream(file);
            long len = file.length();
            byte[] bytes = new byte[(int)len];

            int offset = 0;
            int numRead = 0;
            while (offset < bytes.length && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
                offset += numRead;
            }


            int ind = 0;
            for(int y = 0; y < Constant.HEIGHT; y++){
                for(int x = 0; x < Constant.WIDTH; x++){
                    byte a = 0;
                    byte r = bytes[ind];
                    byte g = bytes[ind+Constant.HEIGHT*Constant.WIDTH];
                    byte b = bytes[ind+Constant.HEIGHT*Constant.WIDTH*2];

                    int pix = 0xff000000 | ((r & 0xff) << 16) | ((g & 0xff) << 8) | (b & 0xff);
//						int pix = ((a << 24) + (r << 16) + (g << 8) + b);
                    img.setRGB(x,y,pix);
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
