package premar.tech.facerecognitionapp.ui.signup;

import android.app.Activity;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModelProviders;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.APIClient;
import premar.tech.facerecognitionapp.api.APIInterface;
import premar.tech.facerecognitionapp.api.model.User;
import premar.tech.facerecognitionapp.utils.AppParentActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class SignupActivity extends AppParentActivity {

    private SignupViewModel signupViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        signupViewModel = ViewModelProviders.of(this, new SignupViewModelFactory())
                .get(SignupViewModel.class);

        final EditText usernameEditText = findViewById(R.id.username);
        final EditText passwordEditText = findViewById(R.id.password);
        final Button loginButton = findViewById(R.id.login);
        final ProgressBar loadingProgressBar = findViewById(R.id.loading);

        signupViewModel.getSignupFormState().observe(this, new Observer<SignupFormState>() {
            @Override
            public void onChanged(@Nullable SignupFormState signupFormState) {
                if (signupFormState == null) {
                    return;
                }
                loginButton.setEnabled(signupFormState.isDataValid());
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
                loadingProgressBar.setVisibility(View.GONE);
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
                signupViewModel.signupDataChanged(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {

            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    signupViewModel.login(usernameEditText.getText().toString(),
                            passwordEditText.getText().toString());
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                startActivity(new Intent(SignupActivity.this, FdActivity.class));
//                teachPeople(loadingProgressBar);
                loadingProgressBar.setVisibility(View.VISIBLE);
                signupViewModel.login(usernameEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        });



    }

    private void teachPeople(final ProgressBar loadingProgressBar) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<List<User>> users = apiInterface.listUsers();
        users.enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                loadingProgressBar.setVisibility(View.INVISIBLE);
//                Toast.makeText(SignupActivity.this, "Success: Eureka! \n" + response.message(), Toast.LENGTH_SHORT).show();
                List<User> users = response.body();
                for (User user : users) {
                    Toast.makeText(SignupActivity.this, user.name + " : : " + user.password, Toast.LENGTH_SHORT).show();
                    Timber.d("USER -- " + user.name + " : : " + user.password);
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                loadingProgressBar.setVisibility(View.INVISIBLE);
                Toast.makeText(SignupActivity.this, "Failed miserably!\n"+t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUiWithUser(RegisteredUserView model) {
        String welcome = getString(R.string.welcome) + model.getDisplayName();
        // TODO : initiate successful logged in experience
        Toast.makeText(getApplicationContext(), welcome, Toast.LENGTH_LONG).show();
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }
}
