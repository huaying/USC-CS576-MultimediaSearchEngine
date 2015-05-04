package videosearch;

import java.util.List;

/**
 * Created by zjjcxt on 4/15/15.
 */
public class TextFeature {


    private String imageName;
    private double contrast;
    private int surf;
    private List<Integer> colorHistogram;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getSurf() {
        return surf;
    }

    public void setSurf(int surf) {
        this.surf = surf;
    }

    public double getContrast() {
        return contrast;
    }

    public void setContrast(double contrast) {
        this.contrast = contrast;
    }

    public List<Integer> getColorHistogram() {
        return colorHistogram;
    }

    public void setColorHistogram(List<Integer> colorHistogram) {
        this.colorHistogram = colorHistogram;
    }

    public TextFeature() {
    }

    public TextFeature(String imageName, double contrast, int surf, List<Integer> colorHistogram) {
        this.imageName = imageName;
        this.contrast = contrast;
        this.surf = surf;
        this.colorHistogram = colorHistogram;
    }
}
