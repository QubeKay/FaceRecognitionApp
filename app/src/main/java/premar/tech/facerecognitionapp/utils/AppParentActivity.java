package premar.tech.facerecognitionapp.utils;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import premar.tech.facerecognitionapp.BuildConfig;
import premar.tech.facerecognitionapp.R;
import timber.log.Timber;

public class AppParentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_parent);

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new CrashReportingTree());
        }
    }

    /** A tree which logs important information for crash reporting. */
    private static class CrashReportingTree extends Timber.Tree {
        @Override protected void log(int priority, String tag, @NonNull String message, Throwable t) {
            if (priority == Log.VERBOSE || priority == Log.DEBUG) {
                return;
            }

            CrashReporter.log(priority, tag, message);

            if (t != null) {
                if (priority == Log.ERROR) {
                    CrashReporter.logError(t);
                } else if (priority == Log.WARN) {
                    CrashReporter.logWarning(t);
                }
            }
        }
    }
}
