package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.content.Intent;
import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends Activity {
    private static final String GEMINI_URL = "https://gemini.google.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        openGemini();
    }

    private void openGemini() {
        try {
            CustomTabsIntent tabs = new CustomTabsIntent.Builder()
                    .setShowTitle(true)
                    .build();
            tabs.launchUrl(this, Uri.parse(GEMINI_URL));
        } catch (Exception error) {
            Intent browser = new Intent(Intent.ACTION_VIEW, Uri.parse(GEMINI_URL));
            startActivity(browser);
        }
        finish();
    }
}
