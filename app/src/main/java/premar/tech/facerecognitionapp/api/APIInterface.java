package premar.tech.facerecognitionapp.api;

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

    @POST("/users")
    Call<User> listUsers();

    @POST("/save_user")
    Call<User> createUser(@Body User user);

    @POST("/authenticate_user")
    Call<User> authenticateUser(@Body User user);

//    @GET("/api/users?")
//    Call<UserList> doGetUserList(@Query("page") String page);
//
//    @FormUrlEncoded
//    @POST("/api/users?")
//    Call<UserList> doCreateUserWithField(@Field("name") String name, @Field("job") String job);
}