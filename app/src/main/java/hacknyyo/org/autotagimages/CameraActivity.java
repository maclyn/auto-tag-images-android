package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera.Parameters;

import java.util.List;

public class CameraActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
    }

    /**
     * Scans through a list of tags and searches for tags that correspond to certain scene modes.
     * Sets the camera to any found corresponding tag. Otherwise, sets it to auto.
     * Could probably be implemented better and without breaks and a label but whatever.
     * @author Tim Hung
     * @param tags List of Strings of the tags of a photo.
     */
    public void detectCameraMode(List<String> tags) {
        //Scene mode names : beach, fireworks, night, party, portrait, snow, sunset
        String scene = Parameters.SCENE_MODE_AUTO;

        tagLoop: for(String s : tags) {
            if(s.toLowerCase().equals("beach")) { scene = Parameters.SCENE_MODE_BEACH; break tagLoop;}
            else if(s.toLowerCase().equals("fireworks")) { scene = Parameters.SCENE_MODE_FIREWORKS; break tagLoop;}
            else if(s.toLowerCase().equals("night")) { scene = Parameters.SCENE_MODE_NIGHT; break tagLoop;}
            else if(s.toLowerCase().equals("party")) { scene = Parameters.SCENE_MODE_PARTY; break tagLoop;}
            else if(s.toLowerCase().equals("portrait")) { scene = Parameters.SCENE_MODE_PORTRAIT; break tagLoop;}
            else if(s.toLowerCase().equals("snow")) { scene = Parameters.SCENE_MODE_SNOW; break tagLoop;}
            else if (s.toLowerCase().equals("sunset")) { scene = Parameters.SCENE_MODE_SUNSET; break tagLoop;}
        }
        //TODO Implement setSceneMode after the rest of CameraActivity is created.
        //setSceneMode(scene);
    }
}
