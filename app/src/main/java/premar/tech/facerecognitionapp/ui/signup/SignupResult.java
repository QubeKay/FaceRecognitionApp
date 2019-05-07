package premar.tech.facerecognitionapp.ui.signup;

import android.support.annotation.Nullable;

/**
 * Authentication result : success (user details) or error message.
 */
class SignupResult {
    @Nullable
    private RegisteredUserView success;
    @Nullable
    private Integer error;

    SignupResult(@Nullable Integer error) {
        this.error = error;
    }

    SignupResult(@Nullable RegisteredUserView success) {
        this.success = success;
    }

    @Nullable
    RegisteredUserView getSuccess() {
        return success;
    }

    @Nullable
    Integer getError() {
        return error;
    }
}
