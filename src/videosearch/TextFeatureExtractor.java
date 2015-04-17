package videosearch;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;

/**
 * Created by zjjcxt on 4/13/15.
 */
public class TextFeatureExtractor {
    private static final int MAX_IMG_HEIGHT = 64;
    private double[] histogram; // stores all three tamura features in one histogram.
    private int[][] grayScales; //store greyScale of piexls
    private int imgWidth, imgHeight;

    public double[] extract(BufferedImage image) {
        histogram = new double[2];
        double[] directionality;
        ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(),
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        BufferedImage bimg = op.filter(image, null);
        bimg = ImageUtils.scaleImage(bimg, MAX_IMG_HEIGHT);
        Raster raster = bimg.getRaster();
        int[] tmp = new int[3];
        this.grayScales = new int[raster.getWidth()][raster.getHeight()];
        for (int i = 0; i < raster.getWidth(); i++) {
            for (int j = 0; j < raster.getHeight(); j++) {
                raster.getPixel(i, j, tmp);
                this.grayScales[i][j] = tmp[0];
            }
        }
        imgWidth = bimg.getWidth();
        imgHeight = bimg.getHeight();
        histogram[0] = this.coarseness(imgWidth, imgHeight);
        histogram[1] = this.contrast();
        return histogram;
    }

    public double coarseness(int n0, int n1) {
        double result = 0;
        for (int i = 1; i < n0 - 1; i++) {
            for (int j = 1; j < n1 - 1; j++) {
                result = result + Math.pow(2, this.sizeLeadDiffValue(i, j));
            }
        }

        result = (1.0 / (n0 * n1)) * result;
        return result;
    }

    public int sizeLeadDiffValue(int x, int y) {
        double result = 0, tmp;
        int maxK = 1;

        for (int k = 0; k < 3; k++) {
            tmp = Math.max(this.differencesBetweenNeighborhoodsHorizontal(x, y, k),
                    this.differencesBetweenNeighborhoodsVertical(x, y, k));
            if (result < tmp) {
                maxK = k;
                result = tmp;
            }
        }
        return maxK;
    }

    public double differencesBetweenNeighborhoodsHorizontal(int x, int y, int k) {
        double result = 0;
        result = Math.abs(this.averageOverNeighborhoods(x + (int) Math.pow(2, k - 1), y, k) -
                this.averageOverNeighborhoods(x - (int) Math.pow(2, k - 1), y, k));
        return result;
    }

    public double differencesBetweenNeighborhoodsVertical(int x, int y, int k) {
        double result = 0;
        result = Math.abs(this.averageOverNeighborhoods(x, y + (int) Math.pow(2, k - 1), k) -
                this.averageOverNeighborhoods(x, y - (int) Math.pow(2, k - 1), k));
        return result;
    }

    public double averageOverNeighborhoods(int x, int y, int k) {
        double result = 0, border;
        border = Math.pow(2, 2 * k);
        int x0 = 0, y0 = 0;

        for (int i = 0; i < border; i++) {
            for (int j = 0; j < border; j++) {
                x0 = x - (int) Math.pow(2, k - 1) + i;
                y0 = y - (int) Math.pow(2, k - 1) + j;
                if (x0 < 0) x0 = 0;
                if (y0 < 0) y0 = 0;
                if (x0 >= imgWidth) x0 = imgWidth - 1;
                if (y0 >= imgHeight) y0 = imgHeight - 1;
                result = result + grayScales[x0][y0];
            }
        }
        result = (1 / Math.pow(2, 2 * k)) * result;
        return result;
    }

    public double contrast() {
        double result = 0, my, sigma, my4 = 0, alpha4 = 0;
        my = this.calculateMy();
        sigma = this.calculateSigma(my);

        if (sigma <= 0)
            return 0; // fix based on the comments orf Arthur Lin. Black images would lead to a NaN in later division.

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                my4 = my4 + Math.pow(this.grayScales[x][y] - my, 4);
            }
        }
        alpha4 = my4 / (Math.pow(sigma, 4));
        // fixed based on the patches of shen72@users.sourceforge.net
        result = sigma / (Math.pow(alpha4, 0.25));
        return result;
    }

    public double calculateMy() {
        double mean = 0;

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                mean = mean + this.grayScales[x][y];
            }
        }
        mean = mean / (this.imgWidth * this.imgHeight);
        return mean;
    }

    public double calculateSigma(double mean) {
        double result = 0;

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                result = result + Math.pow(this.grayScales[x][y] - mean, 2);
            }
        }
        result = result / (this.imgWidth * this.imgHeight);
        return Math.sqrt(result);
    }



}
