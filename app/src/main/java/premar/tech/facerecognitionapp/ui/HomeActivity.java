package premar.tech.facerecognitionapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;
import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.api.APIClient;
import premar.tech.facerecognitionapp.api.APIInterface;
import premar.tech.facerecognitionapp.api.model.Article;
import premar.tech.facerecognitionapp.data.StaticData;
import premar.tech.facerecognitionapp.ui.login.LoginActivity;
import premar.tech.facerecognitionapp.utils.AppParentActivity;
import premar.tech.facerecognitionapp.utils.UserDialogs;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

public class HomeActivity extends AppParentActivity implements View.OnClickListener {

    // TODO: get this from backend...
    private static final String APP_DOWNLOAD_URL = "https://premar.tech";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        if (!prefs.getBoolean(StaticData.IS_LOGGED_IN, false)) {
            logout();
        }

        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(R.string.app_name);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                logout();
            }
        });

        FancyButton fbReadInTheBeginning = findViewById(R.id.fb_read_in_the_beginning);
        FancyButton fbReadKujichocha = findViewById(R.id.fb_read_kujichocha);
        FancyButton fbWinning = findViewById(R.id.fb_read_winning);
        FancyButton fbReadAndBeyond = findViewById(R.id.fb_read_and_beyond);

        fbReadInTheBeginning.setOnClickListener(this);
        fbReadKujichocha.setOnClickListener(this);
        fbWinning.setOnClickListener(this);
        fbReadAndBeyond.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fb_read_in_the_beginning:
                Toast.makeText(this, "Loading... fb_read_in_the_beginning", Toast.LENGTH_SHORT).show();
                break;
            case R.id.fb_read_kujichocha:
                Toast.makeText(this, "Loading... fb_read_kujichocha", Toast.LENGTH_SHORT).show();

                break;
            case R.id.fb_read_winning:
                Toast.makeText(this, "Loading... fb_read_winning", Toast.LENGTH_SHORT).show();

                break;
            case R.id.fb_read_and_beyond:
                Toast.makeText(this, "Loading... fb_read_and_beyond", Toast.LENGTH_SHORT).show();

                break;
        }

        startActivity(new Intent(this, WebContentActivity.class));
    }


    // TODO: give credit: Photo by Simon Zhu on Unsplash.com

    private void logout() {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
        editor.remove(StaticData.IS_LOGGED_IN);
        editor.remove(StaticData.LOGGED_IN_USERNAME);
        editor.commit();
        startActivity(new Intent(this, LoginActivity.class));
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        sweetAlertDialog = UserDialogs.getProgressDialog(this, "Loading random content...");
        sweetAlertDialog.show();
        fetchArticles();
    }

    private void fetchArticles() {
        APIInterface apiInterface = APIClient.getClient().create(APIInterface.class);
        Call<List<Article>> articles = apiInterface.listArticles();
        articles.enqueue(new Callback<List<Article>>() {
            @Override
            public void onResponse(Call<List<Article>> call, Response<List<Article>> response) {
                sweetAlertDialog.hide();
                Toast.makeText(HomeActivity.this, "Success: Eureka! \n" + response.message(), Toast.LENGTH_SHORT).show();

                List<Article> articles = response.body();

                for (Article article : articles) {
                    Timber.d("RESPONSE MESSAGE : : " + article.summary);
                }

            }

            @Override
            public void onFailure(Call<List<Article>> call, Throwable t) {
                sweetAlertDialog.hide();
                Toast.makeText(HomeActivity.this, "Failed miserably!\n" + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                shareText("Share Moon Guy Faces with a friend",
                        "Hey check out Moon Guy Faces!",
                        "Use face recognition to see " +
                                " anonymous secrets from random people and share yours. Download it here: " + APP_DOWNLOAD_URL);
                break;
            case R.id.action_random:

                break;
            case R.id.action_settings:
                UserDialogs.showAlertDialog(this,
                        "What do you feel like setting in a dummy app now? Kuwa serious!");
                break;
            case R.id.action_logout:
                logout();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void shareText(String chooserHeading, String title, String message) {
        Intent share = new Intent(android.content.Intent.ACTION_SEND);
        share.setType("text/plain");
        share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);

        // Add data to the intent, the receiving app will decide
        // what to do with it.
        share.putExtra(Intent.EXTRA_SUBJECT, title);
        share.putExtra(Intent.EXTRA_TEXT, message);

        startActivity(Intent.createChooser(share, chooserHeading));
    }
}
