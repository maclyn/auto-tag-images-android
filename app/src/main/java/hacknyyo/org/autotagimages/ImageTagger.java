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
    public static final int NUM_FILES_PASSED = 7;
    private RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.clarifai.com/v1")
            .build();
    private String accessToken;
    private int tokenExpiration;
    private int timeTokenAccessed;
    private List<String> classes;
    private List<Double> probs;
    //private ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
    private BackGroundTaskListener mListener;
    private CameraActivity cameraActivity;

    public interface BackGroundTaskListener{
        public void setTagInfos();
    }

    public void setAccessToken(){
        ClarifaiTokenService service = restAdapter.create(ClarifaiTokenService.class);
        service.getToken("client_credentials", Constants.clientId, Constants.clientSecret,new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {
                accessToken = token.getAccess_token();
                tokenExpiration = token.getExpires_in();
                //Calendar c = Calendar.getInstance();
                //timeTokenAccessed = c.get(Calendar.SECOND);
                long l = System.currentTimeMillis();
                timeTokenAccessed = (int)(l / 1000);
                Log.d("debug",accessToken);
                Log.d("Expiration","" + tokenExpiration);
                Log.d("Accessed","" + timeTokenAccessed);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("debug", "Unable to get the token");
                Log.d("debug",error.getMessage());
            }
        });
    }

    public void getTag(final Context ctx, final SQLiteDatabase db, final String[] paths,
                       final String[] names, final String[] thumbIds,final int numFiles, final boolean addToDb){
        Log.d("debug","In the get tag");
        //Calendar c = Calendar.getInstance();
        //int currentTime = c.get(Calendar.SECOND);
        long l = System.currentTimeMillis();
        int currentTime = (int)(l / 1000);
        Log.d("DEBUG",""+currentTime);
        Log.d("DEBUG",(currentTime - timeTokenAccessed) + "");
        if(currentTime - timeTokenAccessed > tokenExpiration){
            setAccessToken();
            Log.d("Accesstoken","Getting the token");
        }

        try {
            mListener = (MainActivity) ctx;
        }catch(ClassCastException e){

        }
        if(!addToDb){
            cameraActivity = (CameraActivity) ctx;
        }
        ArrayList<TypedFile> typedFiles = new ArrayList<TypedFile>();
        Log.d("Step one","Made it to here");
        try {
            for(int i = 0; i < numFiles; i ++) {
                Log.d("Step two","Made it to here now");
                File photo = new File(paths[i]);
                Bitmap photoBmp = MediaStore.Images.Media.getBitmap(ctx.getContentResolver(), Uri.fromFile(photo));
                int biggerNum;
                int width = photoBmp.getWidth();
                int height = photoBmp.getHeight();
                if (width > height) {
                    biggerNum = width;
                } else {
                    biggerNum = height;
                }
                float scaleFactor = 1000f / ((float) biggerNum);
                int newWidth = (int) (scaleFactor * ((float) width));
                int newHeight = (int) (scaleFactor * ((float) height));

                Bitmap newBmp = Bitmap.createScaledBitmap(photoBmp, newWidth, newHeight, true);
                File output = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/test.png");
                FileOutputStream fos = new FileOutputStream(output);
                newBmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
                fos.close();

                File outputDone = new File(ctx.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS).getPath() + "/test.png");

                String type = null;
                String extension = MimeTypeMap.getFileExtensionFromUrl(output.getPath());
                if (extension != null) {
                    type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
                }
                if (type == null) {
                    Uri localUri = Uri.fromFile(outputDone);
                    type = ctx.getContentResolver().getType(localUri);
                }
                Log.d(TAG, "MIME type: " + type);
                TypedFile typedFile = new TypedFile(type, outputDone);
                typedFiles.add(typedFile);
                if(newBmp != null){
                    newBmp.recycle();
                    newBmp = null;
                }
            }
            for(int k = numFiles; k < NUM_FILES_PASSED; k ++){
                typedFiles.add(null);
            }
            ClarifaiTagService service = restAdapter.create(ClarifaiTagService.class);
            service.getTag("Bearer " + accessToken,
                    typedFiles.get(0),
                    typedFiles.get(1),
                    typedFiles.get(2),
                    typedFiles.get(3),
                    typedFiles.get(4),
                    typedFiles.get(5),
                    typedFiles.get(6),
                    //typedFiles.get(7),
                    //typedFiles.get(8),
                new Callback<CloudTag>() {
                    @Override
                    public void success(CloudTag cloudTag, Response response) {
                        List<Result> results = cloudTag.getResults();
                        //Looping through the results list should get the tags for EACH of the pictures pushed through
                        for(int k = 0; k < results.size(); k ++) {
                            ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
                            InnerResult innerResult = results.get(k).result;
                            InnerInnerResult innerInnerResult = innerResult.tag;
                            classes = innerInnerResult.classes;
                            probs = innerInnerResult.probs;
                            for (int i = 0; i < classes.size(); i++) {
                                TagInfo temp = new TagInfo();
                                temp.setClasses(classes.get(i));
                                temp.setProbs(probs.get(i));
                                tagInfos.add(temp);
                            }
                            for (String s : innerInnerResult.classes) {
                                Log.d("debug", s);
                            }
                            for (Double d : innerInnerResult.probs) {
                                Log.d("debug", d.toString());
                            }

                            if (addToDb) {
                                DatabaseEditor.addTags(paths[k], names[k], thumbIds[k], tagInfos, db);
                                Log.d("debug", "add to db");

                            }
                            if (!addToDb) {
                                Log.d("debug", "dont add to db");
                                cameraActivity.setData(classes);
                                File f = new File(paths[k]);
                                f.delete();
                            }
                        }
                        if (addToDb){
                            mListener.setTagInfos();
                        }
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("failure",error.getMessage());
                        getTag(ctx,db,paths,names,thumbIds,numFiles,addToDb);
                    }
                });
        } catch (Exception e){
            Log.d("THE FUCKING MESSAGE","there is an error");
        }
    }


}
