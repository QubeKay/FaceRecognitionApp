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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.samples.facedetect.FdActivity;

import mehdi.sakout.fancybuttons.FancyButton;
import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.model.User;
import premar.tech.facerecognitionapp.data.StaticData;
import premar.tech.facerecognitionapp.utils.AppParentActivity;
import premar.tech.facerecognitionapp.utils.UserDialogs;

public class SignupActivity extends AppParentActivity {

    private SignupViewModel signupViewModel;
    private final User user = new User();
    private EditText usernameEditText;
    private ImageView ivSelectedImagePreview;
    private FancyButton signupImagePreviewButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        signupViewModel = ViewModelProviders.of(this, new SignupViewModelFactory())
                .get(SignupViewModel.class);


        final EditText nameEditText = findViewById(R.id.name);
        usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final FancyButton signupButton = findViewById(R.id.signup);
        signupImagePreviewButton = findViewById(R.id.fb_signup_image_preview);
        ivSelectedImagePreview = findViewById(R.id.iv_signup_image_preview);

        signupViewModel.getSignupFormState().observe(this, new Observer<SignupFormState>() {
            @Override
            public void onChanged(@Nullable SignupFormState signupFormState) {
                if (signupFormState == null) {
                    return;
                }
                signupButton.setEnabled(signupFormState.isDataValid());
                if (signupFormState.getNameError() != null) {
                    nameEditText.setError(getString(signupFormState.getNameError()));
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
        nameEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {

                    user.username = usernameEditText.getText().toString();
                    user.password = passwordEditText.getText().toString();
                    user.name = nameEditText.getText().toString();
                }
                return false;
            }
        });

        ivSelectedImagePreview.setOnClickListener(imagePreviewClickListener);
        signupImagePreviewButton.setOnClickListener(imagePreviewClickListener);

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (user.image == null || user.image.isEmpty()) { // first get image
                    startActivityForResult(new Intent(SignupActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);
                    return;
                }

                user.username = usernameEditText.getText().toString();
                user.password = passwordEditText.getText().toString();
                user.name = nameEditText.getText().toString();

                sweetAlertDialog = UserDialogs.getProgressDialog(SignupActivity.this, "Loading...");
                sweetAlertDialog.show();
                forwardRequest(user);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == GET_FACE_REQUEST_CODE && resultCode == RESULT_OK) {
//            String image = data.getStringExtra(FdActivity.DETECTED_FACE_KEY);
            user.image = StaticData.base64ImageData;
            ivSelectedImagePreview.setImageBitmap(StaticData.bitmapImageData);
            signupImagePreviewButton.setEnabled(true);
        } else if (requestCode == GET_FACE_REQUEST_CODE) {
            signupImagePreviewButton.setEnabled(true);
            Toast.makeText(this, "Could not pick face!", Toast.LENGTH_SHORT).show();
        }
    }

    private View.OnClickListener imagePreviewClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            startActivityForResult(new Intent(SignupActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if ((user.image == null || user.image.isEmpty()) && !signupImagePreviewButton.isEnabled()) {
            // first get image before showing UI, reduces number of clicks to registration
            startActivityForResult(new Intent(SignupActivity.this, FdActivity.class), GET_FACE_REQUEST_CODE);
        }
    }

    private void forwardRequest(User user) {
        signupViewModel.signup(user);
    }

    private void updateUiWithUser(RegisteredUserView model) {
        String welcome = model.getResponseMessage();

        // initiate successful sign up experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
        if (model.getSuccess()) {

            Intent data = new Intent();
            data.putExtra(StaticData.LOGGED_IN_USERNAME, usernameEditText.getText().toString());
            setResult(Activity.RESULT_OK, data);
            //Complete and destroy signup activity once successful
            finish();
        }
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
