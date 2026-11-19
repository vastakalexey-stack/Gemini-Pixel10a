package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String GEMINI_URL = "https://gemini.google.com/app";
    private static final String ACTION_VOICE = "ru.pixel10a.geminiwrapper.VOICE";
    private static final String ACTION_DOCUMENT = "ru.pixel10a.geminiwrapper.DOCUMENT";
    private static final String ACTION_CALENDAR = "ru.pixel10a.geminiwrapper.CALENDAR";
    private static final int VOICE_REQUEST = 41, DOCUMENT_REQUEST = 42;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        handleLaunch(getIntent());
    }

    @Override protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleLaunch(intent);
    }

    private void handleLaunch(Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_VOICE.equals(action)) startVoice();
        else if (ACTION_DOCUMENT.equals(action)) chooseDocument();
        else if (ACTION_CALENDAR.equals(action)) createCalendarEvent();
        else openGemini();
    }

    private void openGemini() {
        try {
            new CustomTabsIntent.Builder().setShowTitle(true).build()
                    .launchUrl(this, Uri.parse(GEMINI_URL));
        } catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть Gemini", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    private void startVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите запрос для Gemini");
        try { startActivityForResult(i, VOICE_REQUEST); }
        catch (Exception e) {
            Toast.makeText(this, "Голосовой ввод недоступен", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void chooseDocument() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        try { startActivityForResult(i, DOCUMENT_REQUEST); }
        catch (Exception e) {
            Toast.makeText(this, "Не удалось открыть документы", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void createCalendarEvent() {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setData(CalendarContract.Events.CONTENT_URI);
        i.putExtra(CalendarContract.Events.TITLE, "Напоминание");
        i.putExtra(CalendarContract.Events.DESCRIPTION, "Создано из Gemini");
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "Календарь недоступен", Toast.LENGTH_LONG).show(); }
        finish();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == VOICE_REQUEST && resultCode == RESULT_OK && data != null) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                ClipboardManager clipboard = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                clipboard.setPrimaryClip(ClipData.newPlainText("Запрос для Gemini", results.get(0)));
                Toast.makeText(this, "Текст скопирован. Вставьте его в Gemini.", Toast.LENGTH_LONG).show();
            }
            openGemini();
        } else if (requestCode == DOCUMENT_REQUEST) {
            if (resultCode == RESULT_OK && data != null && data.getData() != null) {
                Toast.makeText(this, "Файл выбран. Прикрепите его в Gemini кнопкой +.", Toast.LENGTH_LONG).show();
                openGemini();
            } else finish();
        }
    }
}