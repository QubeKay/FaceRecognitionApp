package premar.tech.facerecognitionapp.data;

import premar.tech.facerecognitionapp.data.model.RegisteredUser;

/**
 * Class that requests authentication and user information from the remote data source and
 * maintains an in-memory cache of signup status and user credentials information.
 */
public class SignupRepository {

    private static volatile SignupRepository instance;

    private SignupDataSource dataSource;

    // If user credentials will be cached in local storage, it is recommended it be encrypted
    // @see https://developer.android.com/training/articles/keystore
    private RegisteredUser user = null;

    // private constructor : singleton access
    private SignupRepository(SignupDataSource dataSource) {
        this.dataSource = dataSource;
    }

    public static SignupRepository getInstance(SignupDataSource dataSource) {
        if (instance == null) {
            instance = new SignupRepository(dataSource);
        }
        return instance;
    }

    public boolean isSignedUp() {
        return user != null;
    }

    public void unregister() {
        user = null;
        dataSource.unregister();
    }

    private void setRegisteredUser(RegisteredUser user) {
        this.user = user;
        // If user credentials will be cached in local storage, it is recommended it be encrypted
        // @see https://developer.android.com/training/articles/keystore
    }

    public Result<RegisteredUser> signup(String username, String password) {
        // handle signup

        Result<RegisteredUser> result = dataSource.signup(username, password);
        if (result instanceof Result.Success) {
            setRegisteredUser(((Result.Success<RegisteredUser>) result).getData());
        }
        return result;
    }
}
