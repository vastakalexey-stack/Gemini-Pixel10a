package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;

import androidx.browser.customtabs.CustomTabsIntent;

public class MainActivity extends Activity {
    private static final String GEMINI_URL = "https://gemini.google.com/app";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Google sign-in is not supported in embedded WebViews.
        // Use the user's browser through Android Custom Tabs instead.
        CustomTabsIntent customTabsIntent = new CustomTabsIntent.Builder()
                .setShowTitle(true)
                .build();
        customTabsIntent.launchUrl(this, Uri.parse(GEMINI_URL));
        finish();
    }
}
