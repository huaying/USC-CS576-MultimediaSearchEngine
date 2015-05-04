package videosearch;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;

/**
 * Created by zjjcxt on 4/13/15.
 */
public class ContrastFeatureExtractor {
    private static final int MAX_IMG_HEIGHT = 64;
    private double[] histogram; // stores all three tamura features in one histogram.
    private int[][] grayScales; //store greyScale of piexls
    private int imgWidth, imgHeight;

    public double extract(BufferedImage image) {
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
        double contrastIndex = this.contrast();
        return contrastIndex;
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
