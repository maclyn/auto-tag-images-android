package hacknyyo.org.autotagimages;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by Rushil on 10/19/2014.
 */
public class PhotoHandler implements Camera.PictureCallback, ImageTagger.BackGroundTagListener {
    public static final String TAG = "PhotoHandler";

    private final Context context;
    private final Camera camera;
    private boolean isNormal;
    private CameraView cv;

    public PhotoHandler(Context context, Camera c, boolean isNormal, CameraView cameraView) {
        this.context = context;
        this.camera = c;
        this.isNormal = isNormal;
        this.cv = cameraView;
    }

    @Override
    public void onPictureTaken(byte[] bytes, Camera cam) {
        Log.d(TAG, "Bytes received");

        File photoDirectory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
        //Find photo directory within DCIM that contains "camera"
        File subdirs[] = photoDirectory.listFiles();
        File saveLocation = null;
        for(File f : subdirs){
            Log.d(TAG, "F: " + f.getPath());
            if(f.getPath().toLowerCase(Locale.US).contains("camera")){
                Log.d(TAG, "Are you set?");
                saveLocation = f;
            }
            Log.d(TAG, "Save location: " + saveLocation);
        }
        if(saveLocation == null){
            saveLocation = photoDirectory; //Fallback here if we don't have a "Camera" folder
        }
        photoDirectory = saveLocation;
        if(!isNormal){
            photoDirectory = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM)+"/auto_tag_tmp/");
        }
        if (!photoDirectory.exists() && !photoDirectory.mkdirs()) {
            Toast.makeText(context, "R/W Error to photodir: " + photoDirectory.getPath(), Toast.LENGTH_SHORT).show();
            return;
        }

        Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        Matrix matrix = new Matrix();
        matrix.postRotate(90);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bmp, 0, 0, w, h, matrix, true);

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss");
        String date = dateFormat.format(new Date());
        String photoFile = "PHOTO_" + date + ".jpg";
        String filename = photoDirectory.getPath() + File.separator + photoFile;
        File outputFile = new File(filename);
        try {
            FileOutputStream fos = new FileOutputStream(outputFile);
            boolean result = rotatedBitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
            fos.close();
            if (result) {
                Toast.makeText(context, "Took photo", Toast.LENGTH_SHORT).show();
            }
            camera.startPreview();
        } catch (Exception e) {
            Toast.makeText(context, "Couldn't save image: " + e.getMessage(),
                    Toast.LENGTH_LONG).show();
        }
        if(!isNormal) {
            File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM) + "/auto_tag_tmp/");
            if (dir.isDirectory()){
                String child = dir.list()[0];
                File f = new File(dir,child);
                ImageTagger it = new ImageTagger();
                it.getTag(context, null, f.getPath(), null, null, false);
                f.delete();
            }
        }

    }

    @Override
    public void setClasses(List<String> classes) {
        cv.setClasses(classes);
    }
}
