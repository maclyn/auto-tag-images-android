package hacknyyo.org.autotagimages;

import android.app.Dialog;

import java.util.List;

/**
 * Created by Rushil on 10/18/2014.
 */
public class CloudTag {
    public List<String> classes;
    public List<Double> probs;

    public List<String> getClasses(){
        return classes;
    }

    public List<Double> getProbs(){
        return probs;
    }
}
