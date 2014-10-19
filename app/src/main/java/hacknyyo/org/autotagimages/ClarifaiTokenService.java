package hacknyyo.org.autotagimages;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.Headers;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Rushil on 10/18/2014.
 */
public interface ClarifaiTokenService {
    @FormUrlEncoded
    @POST("/token/")
    //@Headers("grant_type: client_credentials")
    //public void getToken(@Path("clientID") String clientId, @Path("clientSecret") String clientSecret, Callback<Token> response);
    //public void getToken(@Header("client_id") String clientId, @Header("client_secret") String clientSecret, Callback<Token> response);
    public void getToken(@Field("grant_type") String type, @Field("client_id") String clientId,
                         @Field("client_secret") String clientSecret, Callback<Token> response);
}
