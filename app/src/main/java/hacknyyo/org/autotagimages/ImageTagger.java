package hacknyyo.org.autotagimages;

import android.util.Log;

import java.util.List;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;
import retrofit.http.POST;
import retrofit.http.Path;

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
        ClarifaiTagService service = restAdapter.create(ClarifaiTagService.class);
        service.getTag("Bearer " + accessToken,"http://assets.worldwildlife.org/photos/2842/images/hero_small/shutterstock_12730534.jpg",
                new Callback<CloudTag>() {
                    @Override
                    public void success(CloudTag cloudTag, Response response) {
                        classes = cloudTag.getClasses();
                        probs = cloudTag.getProbs();
                        Log.d("debug",classes.get(0));
                        Log.d("debug",probs.get(0).toString());
                    }

                    @Override
                    public void failure(RetrofitError error) {
                        Log.d("debug",error.getMessage());
                    }
                });
    }


}
