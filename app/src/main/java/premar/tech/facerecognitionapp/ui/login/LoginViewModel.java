package premar.tech.facerecognitionapp.ui.login;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Patterns;

import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.APIClient;
import premar.tech.facerecognitionapp.api.APIInterface;
import premar.tech.facerecognitionapp.api.model.BaseLogin;
import premar.tech.facerecognitionapp.api.model.FacialLogin;
import premar.tech.facerecognitionapp.api.model.PasswordLogin;
import premar.tech.facerecognitionapp.api.model.ResponseMessage;
import premar.tech.facerecognitionapp.data.LoginRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class LoginViewModel extends ViewModel {

    private MutableLiveData<LoginFormState> loginFormState = new MutableLiveData<>();
    private MutableLiveData<LoginResult> loginResult = new MutableLiveData<>();
    private LoginRepository loginRepository;

    LoginViewModel(LoginRepository loginRepository) {
        this.loginRepository = loginRepository;
    }

    LiveData<LoginFormState> getLoginFormState() {
        return loginFormState;
    }

    LiveData<LoginResult> getLoginResult() {
        return loginResult;
    }

    public void login(String username, String image) {
        FacialLogin authCredentials = new FacialLogin();
        authCredentials.username = username;
        authCredentials.image = image;
        login(authCredentials);
    }

    public void login(BaseLogin authCredentials) {
        // to be launched in a separate asynchronous job

        sendLoginRequest(authCredentials);
    }

    private void sendLoginRequest(BaseLogin authCredentials) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseMessage> signinAction = null;
        if (authCredentials instanceof FacialLogin) {
            signinAction = apiInterface.authenticateUserFace((FacialLogin) authCredentials);
        } else if (authCredentials instanceof PasswordLogin) {
            signinAction = apiInterface.authenticateUserPassword((PasswordLogin) authCredentials);
        }

        signinAction.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
                ResponseMessage responseMessage = response.body();
                if (responseMessage != null) {
                    loginResult.setValue(new LoginResult(new LoggedInUserView(responseMessage.success, responseMessage.message)));
                    Timber.d("RESPONSE MESSAGE : : " + responseMessage.message);
                } else {
                    loginResult.setValue(new LoginResult(R.string.login_failed));
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                loginResult.setValue(new LoginResult(R.string.connection_error));
            }
        });
    }

    public void loginDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            loginFormState.setValue(new LoginFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            loginFormState.setValue(new LoginFormState(null, R.string.invalid_password));
        } else {
            loginFormState.setValue(new LoginFormState(true));
        }
    }

    // A placeholder username validation check
    private boolean isUserNameValid(String username) {
        if (username == null) {
            return false;
        }
        if (username.contains("@")) {
            return Patterns.EMAIL_ADDRESS.matcher(username).matches();
        } else {
            return username.trim().length() >= 4 && !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }
}
