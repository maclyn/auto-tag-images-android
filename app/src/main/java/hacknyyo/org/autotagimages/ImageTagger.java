package hacknyyo.org.autotagimages;

import android.util.Log;

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
    public void setAccessToken(){
        ClarifaiTokenService service = restAdapter.create(ClarifaiTokenService.class);
        service.getToken(clientId,clientSecret,new Callback<Token>() {
            @Override
            public void success(Token token, Response response) {
                accessToken = token.getAccess_token();
                Log.d("debug",accessToken);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("debug", "Unable to get the token");
            }
        });

    }

}
