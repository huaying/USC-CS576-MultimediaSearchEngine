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
        for(File file: dir.listFiles()){
            if(file.getName().endsWith(Constant.IMAGE_EXTENTSION)){
                imageFiles.add(file.getName());
            }
        }

        List<TextFeature> queryTextFeatureList = new ArrayList<TextFeature>();

        for(String imagename: imageFiles) {
            String imageNamewithoutEx = FilenameUtils.removeExtension(imagename);
            String imagePath = Constant.Query_DIR_PATH + imagename;
            FinalPJOffline offline = new FinalPJOffline();
            BufferedImage img = offline.converter(imagePath);
            TextFeatureExtractor textFeatureExtractor = new TextFeatureExtractor();
            double[] histogram = textFeatureExtractor.extract(img);
            SurfExtractor surfExtractor = new SurfExtractor();
            int surf = surfExtractor.execute(ImageUtils.scaleImage(img, Constant.SCALE_INDEX));
            TextFeature textFeature = new TextFeature();
            textFeature.setImageName(imagename);
            textFeature.setH1(histogram[0]);
            textFeature.setH2(histogram[1]);
            textFeature.setSurf(surf);
            queryTextFeatureList.add(textFeature);
        }


        compare(queryTextFeatureList);

    }

    public static void compare(List<TextFeature> queryTextFeatureList){
        DbProcessor dbProcessor = new DbProcessor();
        dbProcessor.buildConnection();
        dbProcessor.onlineTableInitialize();
        List<CategoryResult> categoryResultList = new ArrayList<CategoryResult>();
        //loof for category
        for(int k=0; k<Constant.CATEGORY.length; k++) {
            List<TextFeature> dbTextFeatureList = dbProcessor.getTextFeature(Constant.CATEGORY[k]);
            double categorySimilarity = 0;
            int loopSizePerFile = dbTextFeatureList.size() - queryTextFeatureList.size();
            //loop for windows
            for (int i = 0; i < loopSizePerFile; i++) {
                double windowSimilarity = 0;
                //loop for corresponding frame
                for (int j = 0; j < queryTextFeatureList.size(); j++) {
                    double dbH1 = dbTextFeatureList.get(j + i).getH1();
                    double dbH2 = dbTextFeatureList.get(j + i).getH2();
                    int dbSurf = dbTextFeatureList.get(j + i).getSurf();
                    double queryH1 = queryTextFeatureList.get(j).getH1();
                    double queryH2 = queryTextFeatureList.get(j).getH2();
                    int querySurf = queryTextFeatureList.get(j).getSurf();

                    double diffH1 = Math.abs(queryH1 - dbH1);
                    double diffH2 = Math.abs(queryH2 - dbH2);
                    int diffSurf = Math.abs(querySurf - dbSurf);

                    if (dbH1 == 0.0)
                        dbH1 = 1.0;
                    if (dbH2 == 0.0)
                        dbH2 = 1.0;
                    if (dbSurf == 0)
                        dbSurf = 1;

                    double errorH1 = diffH1 / dbH1;
                    if (errorH1 > 1.0)
                        errorH1 = 1.0;
                    double errorH2 = diffH2 / dbH2;
                    if (errorH2 > 1.0)
                        errorH2 = 1.0;
                    double errorSurf = (float)diffSurf/(float)dbSurf;
                    if (errorSurf > 1.0)
                        errorSurf = 1.0;

                    double frameSimilarity = ((1 - errorH1) + (1 - errorH2) + (1 - errorSurf)) / 3;
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


}
