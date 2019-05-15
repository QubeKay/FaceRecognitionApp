package premar.tech.facerecognitionapp.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import mehdi.sakout.fancybuttons.FancyButton;
import premar.tech.facerecognitionapp.R;
import premar.tech.facerecognitionapp.data.StaticData;
import premar.tech.facerecognitionapp.ui.login.LoginActivity;
import premar.tech.facerecognitionapp.utils.AppParentActivity;

public class HomeActivity extends AppParentActivity {

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
        FancyButton fbReadAndBeyond = findViewById(R.id.fb_read_winning);
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
}
