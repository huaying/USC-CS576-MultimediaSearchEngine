package videosearch;

/**
 * Created by zjjcxt on 4/15/15.
 */
public class CategoryResult {

    private String category;
    private double similarity;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public CategoryResult() {
    }

    public CategoryResult(String category, double similarity) {
        this.category = category;
        this.similarity = similarity;
    }
}
