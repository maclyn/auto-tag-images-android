package hacknyyo.org.autotagimages;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.mime.TypedFile;

public class ImageTagger {
    public static final String TAG = "ImageTagger";

    private RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.clarifai.com/v1")
            .build();
    private String accessToken;
    private int tokenExpiration;
    private int timeTokenAccessed;
    private List<String> classes;
    private List<Double> probs;
    private ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
    private BackGroundTaskListener mListener;
    private CameraActivity cameraActivity;

    public interface BackGroundTaskListener{
        public void setTagInfos(ArrayList<TagInfo> tags);
    }

    public void setAccessToken(){
        ClarifaiTokenService service = restAdapter.create(ClarifaiTokenService.class);
        service.getToken("client_credentials", Constants.clientId, Constants.clientSecret,new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {
                accessToken = token.getAccess_token();
                tokenExpiration = token.getExpires_in();
                Calendar c = Calendar.getInstance();
                timeTokenAccessed = c.get(Calendar.SECOND);
                Log.d("debug",accessToken);
                Log.d("debug","" + tokenExpiration);
                Log.d("debug","" + timeTokenAccessed);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("debug", "Unable to get the token");
                Log.d("debug",error.getMessage());
            }
        });
    }

    public void getTag(final Context ctx, final SQLiteDatabase db, final String path,
                       final String name, final String thumbId, final boolean addToDb){
        Log.d("debug","In the get tag");
        Calendar c = Calendar.getInstance();
        int currentTime = c.get(Calendar.SECOND);
        Log.d("DEBUG",(currentTime - timeTokenAccessed) + "");
        if(currentTime - timeTokenAccessed > tokenExpiration){
            setAccessToken();
        }
        try {
            mListener = (MainActivity) ctx;
        }catch(ClassCastException e){

        }
        if(!addToDb){
            cameraActivity = (CameraActivity) ctx;
        }
        File photo = new File(path);
        try {
            Bitmap photoBmp = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), Uri.fromFile(photo));
            int biggerNum;
            int width = photoBmp.getWidth();
            int height = photoBmp.getHeight();
            if(width > height){
                biggerNum = width;
            } else {
                biggerNum = height;
            }
            float scaleFactor = 1000f / ((float)biggerNum);
            int newWidth = (int)(scaleFactor * ((float)width));
            int newHeight = (int)(scaleFactor * ((float)height));

            Bitmap newBmp = Bitmap.createScaledBitmap(photoBmp, newWidth, newHeight, true);
            File output = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/test.png");
            FileOutputStream fos = new FileOutputStream(output);
            newBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();

            File outputDone = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/test.png");

            String type = null;
            String extension = MimeTypeMap.getFileExtensionFromUrl(output.getPath());
            if(extension != null) {
                type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
            }
            if(type == null) {
                Uri localUri = Uri.fromFile(outputDone);
                type = ctx.getContentResolver().getType(localUri);
            }
            Log.d(TAG, "MIME type: " + type);
            TypedFile typedFile = new TypedFile(type, outputDone);

            ClarifaiTagService service = restAdapter.create(ClarifaiTagService.class);
            service.getTag("Bearer " + accessToken, typedFile,
                new Callback<CloudTag>() {
                    @Override
                    public void success(CloudTag cloudTag, Response response) {
                        tagInfos = new ArrayList<TagInfo>();
                        List<Result> results = cloudTag.getResults();
                        InnerResult innerResult = results.get(0).result;
                        InnerInnerResult innerInnerResult = innerResult.tag;
                        classes = innerInnerResult.classes;
                        probs = innerInnerResult.probs;
                        for(int i = 0; i < classes.size(); i ++){
                            TagInfo temp = new TagInfo();
                            temp.setClasses(classes.get(i));
                            temp.setProbs(probs.get(i));
                            tagInfos.add(temp);
                        }
                        for(String s : innerInnerResult.classes){
                            Log.d("debug",s);
                        }
                        for(Double d : innerInnerResult.probs){
                            Log.d("debug",d.toString());
                        }

                        if(addToDb) {
                            DatabaseEditor.addTags(path, name, thumbId, tagInfos, db);
                            Log.d("debug", "add to db");
                            mListener.setTagInfos(tagInfos);
                        }
                        if(!addToDb){
                            Log.d("debug", "dont add to db");
                            cameraActivity.setData(classes);
                            File f = new File(path);
                            f.delete();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("failure",error.getMessage());
                        getTag(ctx,db,path,name,thumbId,addToDb);
                    }
                });
        } catch (Exception e){
        }
    }


}
