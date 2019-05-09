package premar.tech.facerecognitionapp.ui.signup;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.samples.facedetect.FdActivity;

import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.model.User;
import premar.tech.facerecognitionapp.utils.AppParentActivity;
import premar.tech.facerecognitionapp.utils.UserDialogs;

public class SignupActivity extends AppParentActivity {

    private SignupViewModel signupViewModel;
    private User user;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        signupViewModel = ViewModelProviders.of(this, new SignupViewModelFactory())
                .get(SignupViewModel.class);

        final EditText nameEditText = findViewById(R.id.name);
        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button signupButton = findViewById(R.id.signup);
//        final ProgressBar sweeAlertDialog = findViewById(R.id.loading);

        signupViewModel.getSignupFormState().observe(this, new Observer<SignupFormState>() {
            @Override
            public void onChanged(@Nullable SignupFormState signupFormState) {
                if (signupFormState == null) {
                    return;
                }
                signupButton.setEnabled(signupFormState.isDataValid());
                if (signupFormState.getUsernameError() != null) {
                    nameEditText.setError(getString(signupFormState.getUsernameError()));
                }
                if (signupFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(signupFormState.getUsernameError()));
                }
                if (signupFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(signupFormState.getPasswordError()));
                }
            }
        });

        signupViewModel.getSignupResult().observe(this, new Observer<SignupResult>() {
            @Override
            public void onChanged(@Nullable SignupResult signupResult) {
                if (signupResult == null) {
                    return;
                }
                sweetAlertDialog.hide();
                if (signupResult.getError() != null) {
                    showLoginFailed(signupResult.getError());
                }
                if (signupResult.getSuccess() != null) {
                    updateUiWithUser(signupResult.getSuccess());
                }
                setResult(Activity.RESULT_OK);

                //Complete and destroy signup activity once successful
                finish();
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                signupViewModel.signupDataChanged(nameEditText.getText().toString(),
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    startActivityForResult(new Intent(SignupActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);

                    user = new User();
                    user.username = usernameEditText.getText().toString();
                    user.password = passwordEditText.getText().toString();
                    user.name = nameEditText.getText().toString();
                }
                return false;
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(SignupActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);

                user = new User();
                user.username = usernameEditText.getText().toString();
                user.password = passwordEditText.getText().toString();
                user.name = nameEditText.getText().toString();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GET_FACE_REQUEST_CODE && resultCode == RESULT_OK) {
            String image = data.getStringExtra(FdActivity.DETECTED_FACE_KEY);
            user.image = image;


            sweetAlertDialog = UserDialogs.getProgressDialog(this, "Loading...");
            sweetAlertDialog.show();
            forwardRequest(user);

        } else if (requestCode == GET_FACE_REQUEST_CODE) {
            Toast.makeText(this, "Could not pick face!", Toast.LENGTH_SHORT).show();
        }
    }

    private void forwardRequest(User user) {
        signupViewModel.signup(user);
    }

    private void updateUiWithUser(RegisteredUserView model) {
        String welcome = model.getResponseMessage();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
