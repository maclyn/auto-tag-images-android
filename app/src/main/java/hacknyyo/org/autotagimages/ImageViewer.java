package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;


public class ImageViewer extends Activity {
    ImageView img;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        getActionBar().hide();

        //Intent has file_state type
        FileState fs = getIntent().getParcelableExtra("filestate");
    }
}
