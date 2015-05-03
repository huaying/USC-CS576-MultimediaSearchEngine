package videosearch;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * Created by zjjcxt on 5/2/15.
 */
public class ColorHistogramExtractor {

    private int[] pixel = new int[3];
    private int[] histogram;
    private HistogramType histogramType;

    public enum HistogramType {
        RGB, HSV, Luminance, HMMD
    }

    public static int DEFAULT_NUMBER_OF_BINS = 64;
    public static HistogramType DEFAULT_HISTOGRAM_TYPE = HistogramType.RGB;

    public ColorHistogramExtractor() {
        histogramType = DEFAULT_HISTOGRAM_TYPE;
        histogram = new int[DEFAULT_NUMBER_OF_BINS];
    }


    public int[] extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);
        Arrays.fill(histogram, 0);
        WritableRaster raster = image.getRaster();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                if (histogramType == HistogramType.HSV) {
                    rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                    histogram[quant(pixel)]++;
                } else if (histogramType == HistogramType.Luminance) {
                    rgb2yuv(pixel[0], pixel[1], pixel[2], pixel);
                } else if (histogramType == HistogramType.HMMD) {
                    histogram[quantHmmd(rgb2hmmd(pixel[0], pixel[1], pixel[2]), DEFAULT_NUMBER_OF_BINS)]++;
                } else // RGB
                    histogram[quant(pixel)]++;
            }
        }
        normalize(histogram, image.getWidth() * image.getHeight());

        return histogram;
    }

    private int[] rgb2hmmd(int ir, int ig, int ib) {
        int hmmd[] = new int[5];

        float max = (float) Math.max(Math.max(ir, ig), Math.max(ig, ib));
        float min = (float) Math.min(Math.min(ir, ig), Math.min(ig, ib));

        float diff = (max - min);
        float sum = (float) ((max + min) / 2.);

        float hue = 0;
        if (diff == 0) hue = 0;
        else if (ir == max && (ig - ib) > 0) hue = 60 * (ig - ib) / (max - min);
        else if (ir == max && (ig - ib) <= 0) hue = 60 * (ig - ib) / (max - min) + 360;
        else if (ig == max) hue = (float) (60 * (2. + (ib - ir) / (max - min)));
        else if (ib == max) hue = (float) (60 * (4. + (ir - ig) / (max - min)));

        diff /= 2;

        hmmd[0] = (int) (hue);
        hmmd[1] = (int) (max);
        hmmd[2] = (int) (min);
        hmmd[3] = (int) (diff);
        hmmd[4] = (int) (sum);

        return (hmmd);
    }

    public void rgb2hsv(int r, int g, int b, int hsv[]) {

        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

    public void rgb2yuv(int r, int g, int b, int[] yuv) {
        int y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int u = (int) ((b - y) * 0.492f);
        int v = (int) ((r - y) * 0.877f);

        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
    }

    private int quant(int[] pixel) {
        if (histogramType == HistogramType.HSV) {
            int qH = (int) Math.floor(pixel[0] / 11.25);    // more granularity in color
            if (qH == 32) qH--;
            int qV = pixel[1] / 90;
            if (qV == 4) qV--;
            int qS = pixel[2] / 25;
            if (qS == 4) qS--;
            return qH * 16 + qV * 4 + qS;
        } else if (histogramType == HistogramType.HMMD) {
            return quantHmmd(rgb2hmmd(pixel[0], pixel[1], pixel[2]), 255);
        } else if (histogramType == HistogramType.Luminance) {
            return (pixel[0] * histogram.length) / (256);
        } else {
            // just for 512 bins ...
            int bin = 0;
            if (histogram.length == 512) {
                for (int i = 0; i < quant512.length - 1; i++) {
                    if (quant512[i] <= pixel[0] && pixel[0] < quant512[i + 1]) bin += (i + 1);
                    if (quant512[i] <= pixel[1] && pixel[1] < quant512[i + 1]) bin += (i + 1) * 8;
                    if (quant512[i] <= pixel[2] && pixel[2] < quant512[i + 1]) bin += (i + 1) * 8 * 8;
                }
                return bin;
            }
            // and for 64 bins ...
            else {
                int pos = (int) Math.round((double) pixel[2] / 85d) +
                        (int) Math.round((double) pixel[1] / 85d) * 4 +
                        (int) Math.round((double) pixel[0] / 85d) * 4 * 4;
                return pos;
            }
        }
    }

    private int quantHmmd(int[] hmmd, int quantizationLevels) {
        int h = 0;
        int offset = 0;    // offset position in the quantization table
        int subspace = 0;
        int q = 0;

        // define the subspace along the Diff axis

        if (hmmd[3] < 7) subspace = 0;
        else if ((hmmd[3] > 6) && (hmmd[3] < 21)) subspace = 1;
        else if ((hmmd[3] > 19) && (hmmd[3] < 61)) subspace = 2;
        else if ((hmmd[3] > 59) && (hmmd[3] < 111)) subspace = 3;
        else if ((hmmd[3] > 109) && (hmmd[3] < 256)) subspace = 4;

        // HMMD Color Space quantization
        // see MPEG7-CSD.pdf

        if (quantizationLevels == 256) {
            offset = 0;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        } else if (quantizationLevels == 128) {
            offset = 10;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        } else if (quantizationLevels == 64) {
            offset = 20;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);

        } else if (quantizationLevels == 32) {
            offset = 30;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        }
        return h;
    }

    public static final int[] quant512 = new int[]{18, 55, 91, 128, 165, 201, 238, 256};

    private static final int[] quantTable = {
            1, 32, 4, 8, 16, 4, 16, 4, 16, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 256 levels
            1, 16, 4, 4, 8, 4, 8, 4, 8, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 128 levels
            1, 8, 4, 4, 4, 4, 8, 2, 8, 1,            // Hue, Sum - subspace 0,1,2,3,4 for  64 levels
            1, 8, 4, 4, 4, 4, 4, 1, 4, 1};           // Hue, Sum - subspace 0,1,2,3,4 for  32 levels

    private void normalize(int[] histogram, int numPixels) {
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] * 255) / max;
        }
    }

}
