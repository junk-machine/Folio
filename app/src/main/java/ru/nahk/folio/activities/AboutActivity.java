package ru.nahk.folio.activities;

import android.os.Bundle;
import android.support.v4.text.HtmlCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import ru.nahk.folio.BuildConfig;
import ru.nahk.folio.R;

/**
 * Activity that displays information about the application.
 */
public class AboutActivity extends AppCompatActivity {
    /**
     * Initializes the activity.
     * @param savedInstanceState Previous activity state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        ((TextView) findViewById(R.id.about_app_version))
            .setText(getString(R.string.app_version, BuildConfig.VERSION_NAME));

        TextView attributionView = findViewById(R.id.about_attribution);
        attributionView.setText(
            HtmlCompat.fromHtml(
                getString(R.string.data_source_attribution),
                HtmlCompat.FROM_HTML_MODE_LEGACY));
        attributionView.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
