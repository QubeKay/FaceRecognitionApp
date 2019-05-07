package premar.tech.facerecognitionapp.ui.signup;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.NonNull;

import premar.tech.facerecognitionapp.data.LoginDataSource;
import premar.tech.facerecognitionapp.data.LoginRepository;

/**
 * ViewModel provider factory to instantiate SignupViewModel.
 * Required given SignupViewModel has a non-empty constructor
 */
public class SignupViewModelFactory implements ViewModelProvider.Factory {

    @NonNull
    @Override
    @SuppressWarnings("unchecked")
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SignupViewModel.class)) {
            return (T) new SignupViewModel(LoginRepository.getInstance(new LoginDataSource()));
        } else {
            throw new IllegalArgumentException("Unknown ViewModel class");
        }
    }
}
