package premar.tech.facerecognitionapp.ui.signup;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Patterns;

import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.APIClient;
import premar.tech.facerecognitionapp.api.APIInterface;
import premar.tech.facerecognitionapp.api.model.ResponseMessage;
import premar.tech.facerecognitionapp.api.model.User;
import premar.tech.facerecognitionapp.data.SignupRepository;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SignupViewModel extends ViewModel {

    private MutableLiveData<SignupFormState> signupFormState = new MutableLiveData<>();
    private MutableLiveData<SignupResult> signupResult = new MutableLiveData<>();
    private SignupRepository signupRepository;

    SignupViewModel(SignupRepository signupRepository) {
        this.signupRepository = signupRepository;
    }

    LiveData<SignupFormState> getSignupFormState() {
        return signupFormState;
    }

    LiveData<SignupResult> getSignupResult() {
        return signupResult;
    }

    public void signup(User user) {
        // can be launched in a separate asynchronous job

        sendSignupRequest(user);
    }

    private void sendSignupRequest(User user) {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<ResponseMessage> users = apiInterface.createUser(user);
        users.enqueue(new Callback<ResponseMessage>() {
            @Override
            public void onResponse(Call<ResponseMessage> call, Response<ResponseMessage> response) {
//                Toast.makeText(SignupActivity.this, "Success: Eureka! \n" + response.message(), Toast.LENGTH_SHORT).show();
                ResponseMessage responseMessage = response.body();
                if (responseMessage != null) {
                    signupResult.setValue(new SignupResult(new RegisteredUserView(responseMessage.success, responseMessage.message)));
//                    Toast.makeText(context, "Message : : " + responseMessage.message, Toast.LENGTH_SHORT).show();
                    Timber.d("RESPONSE MESSAGE : : " + responseMessage.message);
                } else {
                    signupResult.setValue(new SignupResult(R.string.signup_failed));
                }
            }

            @Override
            public void onFailure(Call<ResponseMessage> call, Throwable t) {
                signupResult.setValue(new SignupResult(R.string.connection_error));
//                Toast.makeText(SignupActivity.this, "Failed miserably!\n"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void signupDataChanged(String name, String username, String password) {
        if (!isUserNameValid(username)) {
            signupFormState.setValue(new SignupFormState(null, R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            signupFormState.setValue(new SignupFormState(null, null, R.string.invalid_password));
        } else if (!isNameValid(name)) {
            signupFormState.setValue(new SignupFormState(R.string.invalid_name, null, null));
        } else {
            signupFormState.setValue(new SignupFormState(true));
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
            return !username.trim().isEmpty();
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    // A placeholder password validation check
    private boolean isNameValid(String name) {
        if (!name.contains(" ")) {
            return false;
        }
        return name != null && name.trim().length() < 5;
    }
}
