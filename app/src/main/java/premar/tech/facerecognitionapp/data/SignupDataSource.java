package premar.tech.facerecognitionapp.data;

import java.io.IOException;

import premar.tech.facerecognitionapp.data.model.RegisteredUser;

/**
 * Class that handles authentication w/ signup credentials and retrieves user information.
 */
public class SignupDataSource {

    public Result<RegisteredUser> signup(String username, String password) {

        try {


            // TODO: handle loggedInUser authentication
            RegisteredUser signupResponse =
                    new RegisteredUser(
                            java.util.UUID.randomUUID().toString(),
                            "Jane Dork");

            return new Result.Success<>(signupResponse);
        } catch (Exception e) {
            return new Result.Error(new IOException("Error signing up", e));
        }
    }

    public void unregister() {
        // TODO: revoke authentication
    }
}
