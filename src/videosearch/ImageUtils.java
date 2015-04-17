package videosearch;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;


public class ImageUtils {

    public static BufferedImage scaleImage(BufferedImage image, int maxSideLength) {
        assert (maxSideLength > 0);
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();
        double scaleFactor = 0.0;
        if (originalWidth > originalHeight) {
            scaleFactor = ((double) maxSideLength / originalWidth);
        } else {
            scaleFactor = ((double) maxSideLength / originalHeight);
        }
        // create new image
        BufferedImage img = new BufferedImage((int) (originalWidth * scaleFactor), (int) (originalHeight * scaleFactor), BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
//        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }


    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        assert (width > 0 && height > 0);
        // create smaller image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }

    public static BufferedImage cropImage(BufferedImage image, int fromX, int fromY, int width, int height) {
        assert (width > 0 && height > 0);
        // create smaller image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        g.drawImage(image, fromX, fromY, img.getWidth(), img.getHeight(), null);
        return img;
    }

    /**
     * Converts an image to grey. Use instead of color conversion op, which yields strange results.
     *
     * @param image
     */
    public static BufferedImage convertImageToGrey(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        return result;
    }

    /**
     * Inverts a grey scale image.
     *
     * @param image
     */
    public static void invertImage(BufferedImage image) {
        WritableRaster inRaster = image.getRaster();
        int[] p = new int[3];
        float v = 0;
        for (int x = 0; x < inRaster.getWidth(); x++) {
            for (int y = 0; y < inRaster.getHeight(); y++) {
                inRaster.getPixel(x, y, p);
                p[0] = 255 - p[0];
                inRaster.setPixel(x, y, p);
            }
        }
    }

    /**
     * Converts an image to a standard internal representation.
     * Taken from OpenIMAJ. Thanks to these guys!
     * http://sourceforge.net/p/openimaj
     *
     * @param bimg
     * @return
     */
    public static BufferedImage createWorkingCopy(BufferedImage bimg) {
        BufferedImage image;
        if (bimg.getType() == BufferedImage.TYPE_INT_RGB) {
            image = bimg;
        } else {
            image = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(bimg, null, 0, 0);
        }
        return image;
    }

    /**
     * Trims the white (and respective the black) border around an image.
     *
     * @param img
     * @return a new image, hopefully trimmed.
     */
    public static BufferedImage trimWhiteSpace(BufferedImage img) {
        // idea is to scan lines of an image starting from each side.
        // As soon as a scan line encounters non-white (or non-black) pixels we know there is actual image content.
        WritableRaster raster = img.getRaster();
        boolean hasWhite = true;
        int ymin = 0, ymax = raster.getHeight() - 1, xmin = 0, xmax = raster.getWidth() - 1;
        int[] pixels = new int[3 * raster.getWidth()];
        int thresholdWhite = 250;
        int thresholdBlack = 5;
        while (hasWhite) {
            raster.getPixels(0, ymin, raster.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < thresholdWhite && pixels[i] > thresholdBlack) hasWhite = false;
            }
            if (hasWhite) ymin++;
        }
        hasWhite = true;
        while (hasWhite && ymax > ymin) {
            raster.getPixels(0, ymax, raster.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < thresholdWhite && pixels[i] > thresholdBlack) hasWhite = false;
            }
            if (hasWhite) ymax--;
        }
        pixels = new int[3 * raster.getHeight()];
        hasWhite = true;
        while (hasWhite) {
            raster.getPixels(xmin, 0, 1, raster.getHeight(), pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < thresholdWhite && pixels[i] > thresholdBlack) hasWhite = false;
            }
            if (hasWhite) xmin++;
        }
        hasWhite = true;
        while (hasWhite && xmax > xmin) {
            raster.getPixels(xmax, 0, 1, raster.getHeight(), pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < thresholdWhite && pixels[i] > thresholdBlack) hasWhite = false;
            }
            if (hasWhite) xmax--;
        }
        BufferedImage result = new BufferedImage(xmax - xmin, ymax - ymin, BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(img, 0, 0, result.getWidth(), result.getHeight(),
                xmin, ymin, xmax, ymax, null);
        return result;
    }



}
