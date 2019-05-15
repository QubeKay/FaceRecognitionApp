package premar.tech.facerecognitionapp.ui.login;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import mehdi.sakout.fancybuttons.FancyButton;
import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.model.BaseLogin;
import premar.tech.facerecognitionapp.api.model.FacialLogin;
import premar.tech.facerecognitionapp.api.model.PasswordLogin;
import premar.tech.facerecognitionapp.data.StaticData;
import premar.tech.facerecognitionapp.ui.HomeActivity;
import premar.tech.facerecognitionapp.ui.signup.SignupActivity;
import premar.tech.facerecognitionapp.utils.AppParentActivity;
import premar.tech.facerecognitionapp.utils.UserDialogs;

public class LoginActivity extends AppParentActivity {

    private LoginViewModel loginViewModel;
    private BaseLogin authCredentials;
    private static final int FACIAL_AUTH = 1;
    private static final int PASSWORD_AUTH = 2;
    private static int LOGIN_MODE = FACIAL_AUTH;
    protected static final int SIGNUP_REQUEST_CODE = 245;
    private EditText usernameEditText;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (prefs.getBoolean(StaticData.IS_LOGGED_IN, false)) {
            goToHome();
        }

        setContentView(R.layout.activity_login);
        loginViewModel = ViewModelProviders.of(this, new LoginViewModelFactory())
                .get(LoginViewModel.class);

        usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final FancyButton loginButton = findViewById(R.id.login);
        final FancyButton launchSignupButton = findViewById(R.id.login_launch_signup);

        loginViewModel.getLoginFormState().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());

                // If username is set but password is not, use face recognition auth

                // If username and password are set, use password authentication

                if (loginFormState.getUsernameError() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameError()));
                } else if (loginFormState.getPasswordError() != null) { // If username is set but password is not, use face recognition auth
                    loginButton.setEnabled(true);
                    loginButton.setText(getString(R.string.facial_auth));
                    LOGIN_MODE = FACIAL_AUTH;
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                } else if (loginFormState.getUsernameError() == null) { // If username and password are set, use password authentication
                    loginButton.setEnabled(true);
                    loginButton.setText(getString(R.string.action_sign_in));
                    LOGIN_MODE = PASSWORD_AUTH;
                }
            }
        });

        loginViewModel.getLoginResult().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                sweetAlertDialog.hide();
                if (loginResult.getError() != null) {
                    showLoginFailed(loginResult.getError());
                }
                if (loginResult.getSuccess() != null) {
                    updateUiWithUser(loginResult.getSuccess());
                }
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
                loginViewModel.loginDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    startActivityForResult(new Intent(LoginActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);

                    authCredentials = new FacialLogin();
                    authCredentials.username = usernameEditText.getText().toString();
//                user.password = passwordEditText.getText().toString();

                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if (LOGIN_MODE == FACIAL_AUTH) {
                    authCredentials = new FacialLogin();
                    authCredentials.username = usernameEditText.getText().toString();
                    startActivityForResult(new Intent(LoginActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);
                } else if (LOGIN_MODE == PASSWORD_AUTH) {
                    authCredentials = new PasswordLogin();
                    authCredentials.username = usernameEditText.getText().toString();
                    ((PasswordLogin) authCredentials).password = passwordEditText.getText().toString();
                    forwardRequest(authCredentials);
                }
            }
        });

        launchSignupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startActivityForResult(new Intent(LoginActivity.this, SignupActivity.class), SIGNUP_REQUEST_CODE);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GET_FACE_REQUEST_CODE && resultCode == RESULT_OK) {

//            String image = StaticData.base64ImageData;

            ((FacialLogin) authCredentials).image = StaticData.base64ImageData;


            forwardRequest(authCredentials);

        } else if (requestCode == GET_FACE_REQUEST_CODE) {
            Toast.makeText(this, "Could not pick face!", Toast.LENGTH_SHORT).show();
        } else if (requestCode == SIGNUP_REQUEST_CODE && resultCode == RESULT_OK) {
            usernameEditText.setText(data.getStringExtra(StaticData.LOGGED_IN_USERNAME));
        }
    }

    private void forwardRequest(BaseLogin authCredentials) {
        sweetAlertDialog = UserDialogs.getProgressDialog(this, "Loading...");
        sweetAlertDialog.show();
        loginViewModel.login(authCredentials);
    }

    private void updateUiWithUser(LoggedInUserView model) {
        String welcome = model.getDisplayName();
        // initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        if (model.getSuccess()) {

            SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
            editor.putBoolean(StaticData.IS_LOGGED_IN, true);
            editor.putString(StaticData.LOGGED_IN_USERNAME, model.getDisplayName());
            editor.commit();
            goToHome();
        }
    }

    private void goToHome() {
        startActivity(new Intent(this, HomeActivity.class));
        setResult(Activity.RESULT_OK);
        finish();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
