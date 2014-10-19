package hacknyyo.org.autotagimages;

import retrofit.Callback;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Rushil on 10/18/2014.
 */
public interface ClarifaiTokenService {
    @POST("/grant_type=client_credentials&client_id={clientID}&client_secret={clientSecret}")
    public void getToken(@Path("clientID") String clientId, @Path("clientSecret") String clientSecret, Callback<Token> response);
}
