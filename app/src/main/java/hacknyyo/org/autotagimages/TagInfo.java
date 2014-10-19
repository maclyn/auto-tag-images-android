package hacknyyo.org.autotagimages;

/**
 * Created by Rushil on 10/18/2014.
 */
public class TagInfo {

    public String classes;
    public double probs;

    public void setClasses(String classes){
        this.classes = classes;
    }

    public void setProbs(double probs){
        this.probs = probs;
    }

    public String getClasses(){
        return this.classes;
    }

    public double getProbs(){
        return this.probs;
    }

}
