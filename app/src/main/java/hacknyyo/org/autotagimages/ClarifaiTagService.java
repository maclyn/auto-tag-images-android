package hacknyyo.org.autotagimages;

import retrofit.Callback;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.Header;
import retrofit.http.Multipart;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Part;
import retrofit.mime.TypedFile;

/**
 * Created by Rushil on 10/18/2014.
 */
public interface ClarifaiTagService {
    @Multipart
    //@FormUrlEncoded
    @POST("/tag/")
    public void getTag(@Header("Authorization") String accessToken,
                       @Part("encoded_image")TypedFile photo1,
                       @Part("encoded_image")TypedFile photo2,
                       @Part("encoded_image")TypedFile photo3,
                       @Part("encoded_image")TypedFile photo4,
                       @Part("encoded_image")TypedFile photo5,
                       @Part("encoded_image")TypedFile photo6,
                       @Part("encoded_image")TypedFile photo7,
                       @Part("encoded_image")TypedFile photo8,
                       @Part("encoded_image")TypedFile photo9,
                       Callback<CloudTag> response);
}
