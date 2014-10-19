package hacknyyo.org.autotagimages;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

public class ImageTagger {
    public static final String TAG = "ImageTagger";

    private static final String clientId = "uXcLjdijZ1EyyV3aP320XFWTeZhJuBT0_RnqOWEn";
    private static final String clientSecret = "jEnJLQ2tPHxrBODPGNYr-EssEO993wo-QKGjUgcw";
    private RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.clarifai.com/v1")
            .build();
    private String accessToken;
    private List<String> classes;
    private List<Double> probs;
    private ArrayList<TagInfo> tagInfos = new ArrayList<TagInfo>();
    private BackGroundTaskListener mListener;

    public interface BackGroundTaskListener{
        public void setTagInfos(ArrayList<TagInfo> tags);
    }

    public void setAccessToken(){
        ClarifaiTokenService service = restAdapter.create(ClarifaiTokenService.class);
        service.getToken("client_credentials",clientId,clientSecret,new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {
                accessToken = token.getAccess_token();
                Log.d("debug",accessToken);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("debug", "Unable to get the token");
                Log.d("debug",error.getMessage());
            }
        });
    }

    public void getTag(final Context ctx, final SQLiteDatabase db, final String path,
                       final String name, final String thumbId){
        mListener = (MainActivity)ctx;
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

                        DatabaseEditor.addTags(path, name, thumbId, tagInfos, db);
                        mListener.setTagInfos(tagInfos);
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("failure",error.getMessage());
                    }
                });
        } catch (Exception e){
        }
    }


}
