package hacknyyo.org.autotagimages;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.List;

/**
 * Created by Rushil on 10/19/2014.
 */
public class CameraView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    private Context mContext;
    private SurfaceHolder mHolder;
    private Camera mCamera;
    private List<Camera.Size> mSupportedPreviewSizes;
    private Camera.Size mPreviewSize;
    private List<String> classes;
    private String[] beach;
    private String[] fireworks;
    private String[] party;
    private String[] night;

    public CameraView(Context context, Camera camera) {
        super(context);
        mContext = context;
        mCamera = camera;

        // supported preview sizes
        mSupportedPreviewSizes = mCamera.getParameters().getSupportedPreviewSizes();
        for(Camera.Size str: mSupportedPreviewSizes)
            Log.d(TAG, str.width + "/" + str.height);

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // empty. surfaceChanged will take care of stuff
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        Log.e(TAG, "surfaceChanged => w=" + w + ", h=" + h);
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        if (mHolder.getSurface() == null){
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e){
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or reformatting changes here
        // start preview with new settings
        try {
            Camera.Parameters parameters = mCamera.getParameters();
            parameters.setPreviewSize(mPreviewSize.width, mPreviewSize.height);
            mCamera.setParameters(parameters);
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();

        } catch (Exception e){
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int width = resolveSize(getSuggestedMinimumWidth(), widthMeasureSpec);
        final int height = resolveSize(getSuggestedMinimumHeight(), heightMeasureSpec);

        if (mSupportedPreviewSizes != null) {
            mPreviewSize = getOptimalPreviewSize(mSupportedPreviewSizes, width, height);
        }

        float ratio;
        if(mPreviewSize.height >= mPreviewSize.width)
            ratio = (float) mPreviewSize.height / (float) mPreviewSize.width;
        else
            ratio = (float) mPreviewSize.width / (float) mPreviewSize.height;

        // One of these methods should be used, second method squishes preview slightly
        setMeasuredDimension(width, (int) (width * ratio));
//        setMeasuredDimension((int) (width * ratio), height);
    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null)
            return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.height / size.width;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;

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

    public void takeSmartPicture(boolean isNormal){
        if(mCamera != null){
            PhotoHandler ph = new PhotoHandler(getContext(),mCamera,isNormal, this);
            mCamera.takePicture(null,null, ph);
            Log.d("debug","Picture taken");
        }
    }

    public void setClasses(List<String> classes) {
        this.classes = classes;
        detectCameraMode(this.classes);
    }

    public void detectCameraMode(List<String> tags) {
        //Scene mode names : beach, fireworks, night, party, portrait, snow, sunset
        String scene = Camera.Parameters.SCENE_MODE_AUTO;

        tagLoop: for(String s : tags) {
            Log.d("debug",s);
            if(s.toLowerCase().equals("beach")) { scene = Camera.Parameters.SCENE_MODE_BEACH; break tagLoop;}
            else if(s.toLowerCase().equals("fireworks")) { scene = Camera.Parameters.SCENE_MODE_FIREWORKS; break tagLoop;}
            else if(s.toLowerCase().equals("night")) { scene = Camera.Parameters.SCENE_MODE_NIGHT; break tagLoop;}
            else if(s.toLowerCase().equals("furniture") || s.toLowerCase().equals("room")) { scene = Camera.Parameters.SCENE_MODE_PARTY; break tagLoop;}
            else if(s.toLowerCase().equals("portrait")) { scene = Camera.Parameters.SCENE_MODE_PORTRAIT; break tagLoop;}
            else if(s.toLowerCase().equals("snow")) { scene = Camera.Parameters.SCENE_MODE_SNOW; break tagLoop;}
            else if (s.toLowerCase().equals("sunset")) { scene = Camera.Parameters.SCENE_MODE_SUNSET; break tagLoop;}
        }

        Log.d("Scene",scene);
        //TODO Implement setSceneMode after the rest of CameraActivity is created.
        mCamera.getParameters().setSceneMode(scene);
        ((CameraActivity)mContext).setModeText(scene.toString());
    }
}
