package videosearch;

import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Created by zjjcxt on 4/14/15.
 */
public class FinalPJOnline {

    public static void main(String args[]){

        List<String> imageFiles = new ArrayList<String>();
        File dir = new File(Constant.Query_DIR_PATH);
        String audioName = "";
        for(File file: dir.listFiles()){
            if(file.getName().endsWith(Constant.IMAGE_EXTENTSION)){
                imageFiles.add(file.getName());
            }
            if(file.getName().endsWith(Constant.AUDIO_EXTENSION)){
                audioName = file.getName();
            }
        }

        List<TextFeature> queryTextFeatureList = new ArrayList<TextFeature>();

        for(String imagename: imageFiles) {
            String imageNamewithoutEx = FilenameUtils.removeExtension(imagename);
            String imagePath = Constant.Query_DIR_PATH + imagename;
            FinalPJOffline offline = new FinalPJOffline();
            BufferedImage img = offline.converter(imagePath);
            ContrastFeatureExtractor contrastFeatureExtractor = new ContrastFeatureExtractor();
            //contrast extract
            double contrastIndex = contrastFeatureExtractor.extract(img);
            SurfExtractor surfExtractor = new SurfExtractor();
            //surf extract
            int surf = surfExtractor.execute(ImageUtils.scaleImage(img, Constant.SCALE_INDEX));
            //colorhistogram extract
            ColorHistogramExtractor che = new ColorHistogramExtractor();
            List<Integer> colorHistResult = new ArrayList<Integer>();
            int[] colorHistogram = che.extract(img);
            for(int i=0; i<colorHistogram.length; i++){
                colorHistResult.add(colorHistogram[i]);
            }
            TextFeature textFeature = new TextFeature();
            textFeature.setImageName(imagename);
            textFeature.setContrast(contrastIndex);
            textFeature.setSurf(surf);
            textFeature.setColorHistogram(colorHistResult);
            queryTextFeatureList.add(textFeature);
        }

        //extract audio info
        WaveDecoder waveDecoder = new WaveDecoder();
        String audioPath = Constant.Query_DIR_PATH + audioName;
        List<Integer> queryAudioFeatureList = waveDecoder.extractAudioFeature(audioPath, 150);

        compare(queryTextFeatureList, queryAudioFeatureList);

    }

    public static void compare(List<TextFeature> queryTextFeatureList, List<Integer> queryAudioFeatureList){
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        dbProcessor.onlineTableInitialize();
        List<CategoryResult> categoryResultList = new ArrayList<CategoryResult>();
        //loof for category
        for(int k=0; k<Constant.CATEGORY.length; k++) {
            List<TextFeature> dbTextFeatureList = dbProcessor.getTextFeature(Constant.CATEGORY[k]);
            List<String> dbAudioFeatureList = dbProcessor.getAudioFeature(Constant.CATEGORY[k]);
            double categorySimilarity = 0;
            HashMap<Integer, Double> errorHistList = new HashMap<Integer, Double>();
            int loopSizePerFile = dbTextFeatureList.size() - queryTextFeatureList.size();
            //loop for windows
            for (int i = 0; i < loopSizePerFile; i++) {
                double windowSimilarity = 0;
                double windowErrorHist = 0;
                //loop for corresponding frame
                for (int j = 0; j < queryTextFeatureList.size(); j++) {
                    double dbContrast = dbTextFeatureList.get(j + i).getContrast();
                    int dbSurf = dbTextFeatureList.get(j + i).getSurf();
                    List<Integer> dbColorHist = dbTextFeatureList.get(j + i).getColorHistogram();
                    double dbAudio = Double.parseDouble(dbAudioFeatureList.get(j + i));

                    double queryContrast = queryTextFeatureList.get(j).getContrast();
                    int querySurf = queryTextFeatureList.get(j).getSurf();
                    List<Integer> queryColorHist = queryTextFeatureList.get(j).getColorHistogram();
                    double queryAudio = queryAudioFeatureList.get(j);

                    double diffContrast = Math.abs(queryContrast - dbContrast);
                    int diffSurf = Math.abs(querySurf - dbSurf);
                    double diffAudio = Math.abs(queryAudio - dbAudio);


                    if (dbContrast == 0.0)
                        dbContrast = 1.0;
                    if (dbSurf == 0)
                        dbSurf = 1;
                    if (dbAudio == 0.0)
                        dbAudio = 1;

                    double errorContrast = diffContrast / dbContrast;
                    if (errorContrast > 1.0)
                        errorContrast = 1.0;
                    double errorSurf = (float)diffSurf/(float)dbSurf;
                    if (errorSurf > 1.0)
                        errorSurf = 1.0;
                    double errorAudio = diffAudio/dbAudio;
                    if (errorAudio > 1.0)
                        errorAudio = 1.0;

                    double errorColorHist = getColorHistError(dbColorHist, queryColorHist);

                    double frameSimilarity = (1 - errorContrast);
                    windowSimilarity += frameSimilarity;
                }
                windowSimilarity = windowSimilarity / queryTextFeatureList.size();
                dbProcessor.storeWindowResult(i + 1, Constant.CATEGORY[k], windowSimilarity);
                categorySimilarity += windowSimilarity;
            }
            categorySimilarity = categorySimilarity/loopSizePerFile;
            CategoryResult categoryResult = new CategoryResult();
            categoryResult.setCategory(Constant.CATEGORY[k]);
            categoryResult.setSimilarity(categorySimilarity);
            categoryResultList.add(categoryResult);
        }
        //sort the list by similarity
        Collections.sort(categoryResultList, new Comparator<CategoryResult>() {
            public int compare(CategoryResult c1, CategoryResult c2) {
                return Double.compare(c2.getSimilarity(), c1.getSimilarity());
            }
        });
        //store the category result in db
        dbProcessor.storeCategoryResult(categoryResultList);

        //close db connection
        dbProcessor.closeConnection();


    }

    public static double getColorHistError(List<Integer> dbColorHist, List<Integer> queryColorHist){
        double result = 0.0;
        for(int i=0; i<queryColorHist.size(); i++){
            double diff = (double)Math.abs(dbColorHist.get(i) - queryColorHist.get(i));
            double fenmu = dbColorHist.get(i);
            if(fenmu == 0){
                fenmu = 1;
            }
            double error = diff/(double)fenmu;
            if(error > 1.0){
                error = 1;
            }
            result += error;
        }
        result = result/64;
        return result;
    }


}
