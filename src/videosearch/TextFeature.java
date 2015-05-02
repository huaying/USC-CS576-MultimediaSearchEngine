package videosearch;

/**
 * Created by zjjcxt on 4/15/15.
 */
public class TextFeature {


    private String imageName;
    private double h1;
    private double h2;
    private int surf;

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public double getH1() {
        return h1;
    }

    public void setH1(double h1) {
        this.h1 = h1;
    }

    public double getH2() {
        return h2;
    }

    public void setH2(double h2) {
        this.h2 = h2;
    }

    public int getSurf() {
        return surf;
    }

    public void setSurf(int surf) {
        this.surf = surf;
    }

    public TextFeature() {
    }

    public TextFeature(String imageName, double h1, double h2, int surf) {
        this.imageName = imageName;
        this.h1 = h1;
        this.h2 = h2;
        this.surf = surf;
    }
}
