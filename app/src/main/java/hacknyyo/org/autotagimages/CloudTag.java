package hacknyyo.org.autotagimages;

import android.app.Dialog;

import java.util.List;

/**
 * Created by Rushil on 10/18/2014.
 */
public class CloudTag {
    /*
    public List<String> classes;
    public List<Double> probs;

    public List<String> getClasses(){
        return classes;
    }

    public List<Double> getProbs(){
        return probs;
    }
    */
    public String status_code;
    public String status_msg;
    public List<Result> results;

    public String getStatus_code(){
        return status_code;
    }
    public String getStatus_msg(){
        return status_msg;
    }
    public List<Result> getResults(){
        return results;
    }
}
