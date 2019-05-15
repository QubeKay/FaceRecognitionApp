package premar.tech.facerecognitionapp.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import premar.tech.facerecognitionapp.utils.AppParentActivity;

public class WebContentActivity extends AppParentActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        WebView wvInTheBeginningSection = new WebView(this);
        setContentView(wvInTheBeginningSection);

        WebSettings webSettings = wvInTheBeginningSection.getSettings();
        webSettings.setJavaScriptEnabled(true);
        wvInTheBeginningSection.setWebViewClient(wvInTheBeginningSectionClient);
        wvInTheBeginningSection.loadUrl("https://premar.tech/site/index.php/2019/04/06/the-cost-of-making-an-app/");
    }

        private WebViewClient wvInTheBeginningSectionClient = new WebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            if (Uri.parse(url).getHost().equals("premar.tech")) {
                // This is my website, so do not override; let my WebView load the page
                return false;
            }
            Toast.makeText(WebContentActivity.this, Uri.parse(url).getHost(), Toast.LENGTH_SHORT).show();
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url)
        {
//            view.loadUrl("javascript:(function() { " +
//                    "document.getElementsByTagName('header')[0].style.display=\"none\"; " +
//                    "})()");
        }
    };
}
