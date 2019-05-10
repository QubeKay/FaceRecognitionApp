package premar.tech.facerecognitionapp.api;

import java.util.List;

import premar.tech.facerecognitionapp.api.model.FacialLogin;
import premar.tech.facerecognitionapp.api.model.PasswordLogin;
import premar.tech.facerecognitionapp.api.model.ResponseMessage;
import premar.tech.facerecognitionapp.api.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface APIInterface {

//    @GET("/api/unknown")
//    Call<MultipleResource> doGetListResources();

    @GET("/facerecognition/users/")
    Call<List<User>> listUsers();

    @POST("/facerecognition/save_user/")
    Call<ResponseMessage> createUser(@Body User user);

    @POST("/facerecognition/authenticate_user/")
    Call<ResponseMessage> authenticateUserFace(@Body FacialLogin user);

    @POST("/facerecognition/authenticate_user/")
    Call<ResponseMessage> authenticateUserPassword(@Body PasswordLogin user);

//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}