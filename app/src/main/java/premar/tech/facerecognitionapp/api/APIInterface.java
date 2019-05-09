package premar.tech.facerecognitionapp.api;

import java.util.List;

import premar.tech.facerecognitionapp.api.model.FacialLogin;
import premar.tech.facerecognitionapp.api.model.ResponseMessage;
import premar.tech.facerecognitionapp.api.model.User;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface APIInterface {

//    @GET("/api/unknown")
//    Call<MultipleResource> doGetListResources();

    @GET("/facerecognition/users/")
    Call<List<User>> listUsers();

    @POST("/facerecognition/save_user/")
    Call<ResponseMessage> createUser(@Body User user);

    @POST("/facerecognition/authenticate_user/")
    Call<ResponseMessage> authenticateUser(@Body FacialLogin user);

//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}