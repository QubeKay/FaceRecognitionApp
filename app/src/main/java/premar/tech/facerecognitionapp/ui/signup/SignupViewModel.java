package premar.tech.facerecognitionapp.ui.signup;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.util.Patterns;

import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.data.Result;
import premar.tech.facerecognitionapp.data.SignupRepository;
import premar.tech.facerecognitionapp.data.model.RegisteredUser;

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

    public void login(String username, String password) {
        // can be launched in a separate asynchronous job
        Result<RegisteredUser> result = signupRepository.signup(username, password);

        if (result instanceof Result.Success) {
            RegisteredUser data = ((Result.Success<RegisteredUser>) result).getData();
            signupResult.setValue(new SignupResult(new RegisteredUserView(data.getDisplayName())));
        } else {
            signupResult.setValue(new SignupResult(R.string.login_failed));
        }
    }

    public void signupDataChanged(String username, String password) {
        if (!isUserNameValid(username)) {
            signupFormState.setValue(new SignupFormState(R.string.invalid_username, null));
        } else if (!isPasswordValid(password)) {
            signupFormState.setValue(new SignupFormState(null, R.string.invalid_password));
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
}
