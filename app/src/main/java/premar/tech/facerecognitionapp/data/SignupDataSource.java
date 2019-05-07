package premar.tech.facerecognitionapp.data;

import java.io.IOException;

import premar.tech.facerecognitionapp.data.model.LoggedInUser;
import premar.tech.facerecognitionapp.data.model.RegisteredUser;

/**
 * Class that handles authentication w/ signup credentials and retrieves user information.
 */
public class SignupDataSource {

    public Result<RegisteredUser> signup(String username, String password) {

        try {
            // TODO: handle loggedInUser authentication
            LoggedInUser fakeUser =
                    new LoggedInUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Doe");
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public void unregister() {
        // TODO: revoke authentication
    }
}
