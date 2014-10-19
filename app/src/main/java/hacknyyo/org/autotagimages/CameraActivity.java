package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.Camera;
import android.media.Image;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.hardware.Camera.Parameters;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.IOException;
import java.util.List;

import static android.view.View.resolveSize;

public class CameraActivity extends Activity implements SurfaceHolder.Callback{

    ImageButton cameraButton;
    ImageButton smartButton;
    SurfaceView surfaceView;
    SurfaceHolder surfaceHolder;
    Camera camera;
    List<Camera.Size> mSupportedPreviewSizes;
    Camera.Size mPreviewSize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        FrameLayout layout = new FrameLayout(this);
        Camera camera = Camera.open();
        final CameraView view = new CameraView(this,camera);
        layout.addView(view);
        FrameLayout.LayoutParams flp1 = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        flp1.gravity = Gravity.BOTTOM;
        RelativeLayout buttonLayout = (RelativeLayout)getLayoutInflater().inflate(R.layout.camera_viewer, null);
        layout.addView(buttonLayout,flp1);
        setContentView(layout);
        cameraButton = (ImageButton)findViewById(R.id.cameraButton);
        cameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.takeSmartPicture(true);
                Toast.makeText(CameraActivity.this,"Took regular picture",Toast.LENGTH_LONG).show();
            }
        });
        smartButton = (ImageButton)findViewById(R.id.smartButton);
        smartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                view.takeSmartPicture(false);
                Toast.makeText(CameraActivity.this,"Took smart picture",Toast.LENGTH_LONG).show();
            }
        });
        //FrameLayout layout = new FrameLayout(this);

        //FrameLayout frameLayout = new FrameLayout(this);
        //frameLayout.addView(view);

        //RelativeLayout relativeLayout = (RelativeLayout)findViewById(R.id.buttonlayout);
        //frameLayout.addView(cameraButton);
        /*
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        smartButton = (ImageButton) findViewById(R.id.smartButton);

        surfaceView = (SurfaceView) findViewById(R.id.cameraView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        //Show_Preview();

        /*
        buttonStartCameraPreview.setOnClickListener( // write below code inside this block );
                buttonStopCameraPreview.setOnClickListener( // write below code inside this block); }
        */
    }

    public void Show_Preview() {
        camera = Camera.open();
        List<Camera.Size> mSupportedPreviewSizes = camera.getParameters().getSupportedPreviewSizes();
        try{
            //camera.setPreviewCallback(this);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch(IOException e){

        }
        /*
        if (!previewing) {
            camera = Camera.open();
            if (camera != null) {
                try {
                    camera.setPreviewDisplay(surfaceHolder);
                    camera.startPreview();
                    previewing = true;
                } catch (IOException e) {
// TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
        */
    }
    /*
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(surfaceView.getMinimumHeight(), widthMeasureSpec);
        final int height = resolveSize(surfaceView.getMinimumHeight(), heightMeasureSpec);
        surfaceView.setMeasuredDimension(width, height);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }
    }
    */

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
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

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open();
        try{
            //camera.setPreviewCallback(this);
            camera.setPreviewDisplay(surfaceHolder);
            camera.startPreview();
        }catch(IOException e){

        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }
}
