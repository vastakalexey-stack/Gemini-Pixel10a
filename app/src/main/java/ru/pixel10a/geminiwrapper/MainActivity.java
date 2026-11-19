package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String GEMINI_URL = "https://gemini.google.com/app";
    private static final int VOICE_REQUEST = 41;
    private static final int DOCUMENT_REQUEST = 42;
    private LinearLayout root;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(0xFF101522);
        getWindow().setNavigationBarColor(0xFF101522);
        showHome();
    }

    private void showHome() {
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24), dp(36), dp(24), dp(24));
        root.setBackgroundColor(0xFF101522);
        setContentView(root);

        TextView star = new TextView(this);
        star.setText("✦");
        star.setTextSize(64);
        star.setTextColor(0xFF8AB4F8);
        star.setGravity(Gravity.CENTER);
        root.addView(star, matchWrap());

        TextView title = new TextView(this);
        title.setText("Gemini");
        title.setTextSize(32);
        title.setTextColor(0xFFFFFFFF);
        title.setGravity(Gravity.CENTER);
        root.addView(title, matchWrap());

        TextView subtitle = new TextView(this);
        subtitle.setText("Ваш помощник на Pixel");
        subtitle.setTextSize(15);
        subtitle.setTextColor(0xFFB8C2D6);
        subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sub = matchWrap(); sub.bottomMargin = dp(28);
        root.addView(subtitle, sub);

        addAction("Открыть Gemini", this::openGemini, true);
        addAction("Голосовой ввод", this::startVoice, false);
        addAction("Открыть документ", this::pickDocument, false);
        addAction("Создать событие / напоминание", this::createCalendarEvent, false);
        addAction("Настройки", this::showSettings, false);

        TextView note = new TextView(this);
        note.setText("Вход и работа Gemini выполняются через поддерживаемую вкладку браузера.");
        note.setTextColor(0xFF8995AA);
        note.setTextSize(12);
        note.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams np=matchWrap(); np.topMargin=dp(22);
        root.addView(note,np);
    }

    private void addAction(String label, Runnable action, boolean primary) {
        Button b = new Button(this);
        b.setText(label);
        b.setAllCaps(false);
        b.setTextSize(16);
        b.setTextColor(primary ? 0xFF101522 : 0xFFE8EEFA);
        b.setBackgroundTintList(android.content.res.ColorStateList.valueOf(primary ? 0xFF8AB4F8 : 0xFF263247));
        LinearLayout.LayoutParams p=matchWrap(); p.topMargin=dp(10);
        root.addView(b,p);
        b.setOnClickListener(v -> action.run());
    }

    private void openGemini() {
        new CustomTabsIntent.Builder().setShowTitle(true).build()
                .launchUrl(this, Uri.parse(GEMINI_URL));
    }

    private void startVoice() {
        Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT, "Скажите запрос для Gemini");
        try { startActivityForResult(i, VOICE_REQUEST); }
        catch (Exception e) { Toast.makeText(this, "Голосовой ввод недоступен на устройстве", Toast.LENGTH_LONG).show(); }
    }

    private void pickDocument() {
        Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        i.setType("*/*");
        i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i, DOCUMENT_REQUEST);
    }

    private void createCalendarEvent() {
        Intent i = new Intent(Intent.ACTION_INSERT);
        i.setData(android.provider.CalendarContract.Events.CONTENT_URI);
        i.putExtra(android.provider.CalendarContract.Events.TITLE, "Напоминание");
        i.putExtra(android.provider.CalendarContract.Events.DESCRIPTION, "Создано из приложения Gemini");
        try { startActivity(i); }
        catch (Exception e) { Toast.makeText(this, "Не найдено приложение календаря", Toast.LENGTH_LONG).show(); }
    }

    private void showSettings() {
        new AlertDialog.Builder(this).setTitle("Настройки Gemini")
                .setItems(new String[]{"О приложении", "Очистить подсказку при следующем запуске"}, (d, which) -> {
                    if (which == 0) new AlertDialog.Builder(this).setMessage("Gemini для Pixel 10a\nВерсия 1.1\nОткрытие сайта и вход выполняются через Chrome Custom Tabs.")
                            .setPositiveButton("ОК", null).show();
                    else Toast.makeText(this, "Настройки будут расширены в следующей версии", Toast.LENGTH_SHORT).show();
                }).setNegativeButton("Закрыть", null).show();
    }

    @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK || data == null) return;
        if (requestCode == VOICE_REQUEST) {
            ArrayList<String> results = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (results != null && !results.isEmpty()) {
                String spoken = results.get(0);
                ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE))
                        .setPrimaryClip(android.content.ClipData.newPlainText("Запрос для Gemini", spoken));
                new AlertDialog.Builder(this).setTitle("Текст распознан")
                        .setMessage(spoken + "\n\nТекст скопирован. Откройте Gemini и вставьте его в поле сообщения.")
                        .setPositiveButton("Открыть Gemini", (d,w) -> openGemini())
                        .setNegativeButton("Закрыть", null).show();
            }
        } else if (requestCode == DOCUMENT_REQUEST) {
            Uri uri=data.getData();
            if (uri != null) {
                getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                Toast.makeText(this, "Документ выбран. Откройте Gemini и прикрепите его кнопкой «+».", Toast.LENGTH_LONG).show();
                openGemini();
            }
        }
    }

    private int dp(int v) { return (int)(v * getResources().getDisplayMetrics().density + 0.5f); }
    private LinearLayout.LayoutParams matchWrap() { return new LinearLayout.LayoutParams(-1, -2); }
}