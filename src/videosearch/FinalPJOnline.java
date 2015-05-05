package videosearch;

import org.apache.commons.io.FilenameUtils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Created by zjjcxt on 4/14/15.
 */
public class FinalPJOnline {

    private static int[] mtable = {0,2000,5000,50000,100000,700000};
    private static int[] mpercent_table = {0,1,20,40,80,100};
    private static int[] contrasttable = {0,1,30,100,200,600};
    private static int[] contrastpercent_table = {0,1,30,50,80,100};
    private static int[] surftable = {0, 150, 1000, 5000, 10000, 20000, 30000};
    private static int[] surfpercent_table = {0, 1, 35, 55, 80, 90, 100};
    private static int[] audiotable = {0, 550, 1200, 2300, 2800, 4000};
    private static int[] audiopercent_table = {0, 1, 50, 80, 90, 100};
    private static int[] colorhisttable = {0, 100, 1000, 5000, 20000, 40000, 80000, 120000};
    private static int[] colorhistpercent_table = {0, 1, 10, 30, 60, 80, 90, 100};


    public static void runOffline(){

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

        //extract motion info
        Motion motion = new Motion();
        ArrayList<Double> queryMotionFeatureList = motion.offline2(Constant.Query_DIR_PATH);

        compare(queryTextFeatureList, queryAudioFeatureList, queryMotionFeatureList);

    }

    public static void compare(List<TextFeature> queryTextFeatureList, List<Integer> queryAudioFeatureList, List<Double> queryMotionFeatureList){
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        dbProcessor.onlineTableInitialize();
        List<CategoryResult> categoryResultList = new ArrayList<CategoryResult>();
        //loof for category
        for(int k=0; k<Constant.CATEGORY.length; k++) {
            List<TextFeature> dbTextFeatureList = dbProcessor.getTextFeature(Constant.CATEGORY[k]);
            List<String> dbAudioFeatureList = dbProcessor.getAudioFeature(Constant.CATEGORY[k]);
            List<Double> dbMotionFeatureList = dbProcessor.getMotionFeature2(Constant.CATEGORY[k]);
            double categorySimilarity = 0;
            HashMap<Integer, Double> errorHistList = new HashMap<Integer, Double>();
            int loopSizePerFile = dbTextFeatureList.size() - queryTextFeatureList.size();
            //loop for windows
            for (int i = 0; i < loopSizePerFile; i++) {
                double windowSimilarity = 0;
                double diffAmountMotion = 0;
                double diffContrast = 0;
                double diffSurf = 0;
                double diffAudio = 0;
                double diffColorHist = 0;
                //loop for corresponding frame
                for (int j = 0; j < queryTextFeatureList.size(); j++) {

                    //deal with contrast comparison
                    double dbContrast = dbTextFeatureList.get(j + i).getContrast();
                    double queryContrast = queryTextFeatureList.get(j).getContrast();
                    diffContrast += Math.abs(queryContrast - dbContrast);

                    //deal with surf points comparison
                    int dbSurf = dbTextFeatureList.get(j + i).getSurf();
                    int querySurf = queryTextFeatureList.get(j).getSurf();
                    diffSurf += Math.abs(querySurf - dbSurf);

                    //deal with color histogram comparison
                    List<Integer> dbColorHist = dbTextFeatureList.get(j + i).getColorHistogram();
                    List<Integer> queryColorHist = queryTextFeatureList.get(j).getColorHistogram();
                    diffColorHist += getColorHistError(dbColorHist, queryColorHist);

                    //deal with audio comparison
                    double dbAudio = Double.parseDouble(dbAudioFeatureList.get(j + i));
                    double queryAudio = queryAudioFeatureList.get(j);
                    diffAudio += Math.abs(queryAudio - dbAudio);

                    //deal with motion comparison
                    double dbMotion;
                    double queryMotion;
                    if(j!=queryTextFeatureList.size()-1){
                        dbMotion = dbMotionFeatureList.get(i+j);
                        queryMotion = queryMotionFeatureList.get(j);
                        diffAmountMotion += Math.abs(dbMotion - queryMotion);
                    }

//
//                    if (dbContrast == 0.0)
//                        dbContrast = 1.0;
//                    if (dbSurf == 0)
//                        dbSurf = 1;
//                    if (dbAudio == 0.0)
//                        dbAudio = 1;
//
//                    double errorContrast = diffContrast / dbContrast;
//                    if (errorContrast > 1.0)
//                        errorContrast = 1.0;
//                    double errorSurf = (float)diffSurf/(float)dbSurf;
//                    if (errorSurf > 1.0)
//                        errorSurf = 1.0;
//                    double errorAudio = diffAudio/dbAudio;
//                    if (errorAudio > 1.0)
//                        errorAudio = 1.0;
                }
                double windowMotionSimilarity = calculateProbability(mtable, mpercent_table, diffAmountMotion);
                double windowContrastSimilarity = calculateProbability(contrasttable, contrastpercent_table, diffContrast);
                double windowSurfSimilarity = calculateProbability(surftable, surfpercent_table, diffSurf);
                double windowAudioSimilarity = calculateProbability(audiotable, audiopercent_table, diffAudio);
                double windowColorHistSimilarity = calculateProbability(colorhisttable, colorhistpercent_table, diffColorHist);
                windowSimilarity = (2*windowColorHistSimilarity + windowContrastSimilarity + windowMotionSimilarity + windowAudioSimilarity +2*windowSurfSimilarity)/7;
                dbProcessor.storeWindowResult(i + 1, Constant.CATEGORY[k], windowSimilarity);
                dbProcessor.storeWindowResultAudio(i + 1, Constant.CATEGORY[k], windowAudioSimilarity);
                dbProcessor.storeWindowResultImage(i + 1, Constant.CATEGORY[k], (windowContrastSimilarity + windowSurfSimilarity + windowColorHistSimilarity) / 3);
                dbProcessor.storeWindowResultMotion(i + 1, Constant.CATEGORY[k], windowMotionSimilarity);
                categorySimilarity += windowSimilarity*windowSimilarity;
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
        int[] dbhist = new int[64];
        int[] queryhist = new int[64];
        for(int i=0; i<dbColorHist.size(); i++){
            dbhist[i] = dbColorHist.get(i);
            queryhist[i] = queryColorHist.get(i);
        }
        return MetricsUtils.jsd(dbhist, queryhist);
    }

    public static double calculateProbability(int[] mtable, int[] mpercent_table, double v){
        double p = 99;

        for(int x=0; x < mtable.length;x++ ){
            if(x!=0) {
                if (v > mtable[x-1] && v < mtable[x]) {
                    p = ((v - mtable[x - 1]) / (mtable[x] - mtable[x - 1])) * (mpercent_table[x] - mpercent_table[x - 1]) + mpercent_table[x - 1];
                }
            }
        }
        p = (100-p)/100;
        return p;
    }

}
