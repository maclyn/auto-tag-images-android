package hacknyyo.org.autotagimages;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.mime.TypedFile;

public class ImageTagger {
    private static final String clientId = "uXcLjdijZ1EyyV3aP320XFWTeZhJuBT0_RnqOWEn";
    private static final String clientSecret = "jEnJLQ2tPHxrBODPGNYr-EssEO993wo-QKGjUgcw";
    private RestAdapter restAdapter = new RestAdapter.Builder()
            .setEndpoint("https://api.clarifai.com/v1")
            .build();
    private String accessToken;
    private List<String> classes;
    private List<Double> probs;

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

    public void getTag(){
        File photo = new File("/storage/emulated/0/DCIM/Camera/1009141400a.jpg");
        TypedFile typedFile = new TypedFile("application/picture", photo);
        ClarifaiTagService service = restAdapter.create(ClarifaiTagService.class);
        service.getTag("Bearer " + accessToken,typedFile,

                new Callback<CloudTag>() {
                    @Override
                    public void success(CloudTag cloudTag, Response response) {
                        Log.d("debug",cloudTag.getStatus_msg());
                        Log.d("debug",cloudTag.getStatus_code());
                        List<Result> results = cloudTag.getResults();
                        InnerResult innerResult = results.get(0).result;
                        InnerInnerResult innerInnerResult = innerResult.tag;
                        for(String s : innerInnerResult.classes){
                            Log.d("debug",s);
                        }
                        for(Double d : innerInnerResult.probs){
                            Log.d("debug",d.toString());
                        }
                        /*
                        Log.d("debug","it does get here");
                        try {
                            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getBody().in()));
                            StringBuilder sb = new StringBuilder();
                            String line = "";
                            try{
                                while((line = reader.readLine())!= null){
                                    sb.append(line);
                                }
                            }catch (IOException e){

                            }
                            Log.d("debug",sb.toString());
                        }catch(IOException e){

                        }

                        classes = cloudTag.getClasses();
                        probs = cloudTag.getProbs();
                        Log.d("debug",classes.get(0));
                        Log.d("debug",probs.get(0).toString());
                        */
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("failure",error.getMessage());
                    }
                });
    }


}
