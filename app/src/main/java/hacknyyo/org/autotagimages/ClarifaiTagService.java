package hacknyyo.org.autotagimages;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.POST;

/**
 * Created by Rushil on 10/18/2014.
 */
public interface ClarifaiTagService {
    @FormUrlEncoded
    @POST("/tag/")
    public void getTag(@Header("Authorization") String accessToken, @Field("url") String url, Callback<CloudTag> response);
}
