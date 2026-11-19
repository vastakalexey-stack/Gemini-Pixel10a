package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.speech.RecognizerIntent;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
    private static final String GEMINI_URL = "https://gemini.google.com/app";
    private static final int VOICE_REQUEST = 41, DOCUMENT_REQUEST = 42;
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.rgb(13,17,26));
        getWindow().setNavigationBarColor(Color.rgb(13,17,26));
        showHome();
    }

    private void showHome() {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(Color.rgb(13,17,26));
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(24),dp(32),dp(24),dp(24));
        scroll.addView(root);

        TextView logo = new TextView(this);
        logo.setText("✦"); logo.setTextSize(54); logo.setTextColor(Color.rgb(138,180,248));
        logo.setGravity(Gravity.CENTER);
        root.addView(logo,new LinearLayout.LayoutParams(-1,dp(72)));

        TextView title = new TextView(this);
        title.setText("Gemini"); title.setTextSize(30); title.setTypeface(Typeface.DEFAULT,Typeface.BOLD);
        title.setTextColor(Color.WHITE); title.setGravity(Gravity.CENTER);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Ваш помощник на Pixel 10a"); subtitle.setTextSize(14);
        subtitle.setTextColor(Color.rgb(184,194,210)); subtitle.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1,-2); sp.bottomMargin=dp(28);
        root.addView(subtitle,sp);

        addAction(root,"Открыть Gemini","Перейти на официальный сайт",this::openGemini,true);
        addAction(root,"🎙  Голосовой ввод","Распознать речь и скопировать запрос",this::startVoice,false);
        addAction(root,"📄  Документы","Выбрать файл для прикрепления в Gemini",this::chooseDocument,false);
        addAction(root,"📅  Календарь и напоминания","Создать событие в календаре телефона",this::createCalendarEvent,false);
        addAction(root,"⚙  Настройки","Информация о приложении",this::showSettings,false);

        TextView note = new TextView(this);
        note.setText("Gemini открывается в Chrome. Голосовой ввод копирует текст в буфер обмена. Выбранный документ нужно прикрепить в Gemini вручную.");
        note.setTextSize(12); note.setTextColor(Color.rgb(145,155,172));
        note.setGravity(Gravity.CENTER); note.setPadding(0,dp(22),0,0);
        root.addView(note);
        setContentView(scroll);
    }

    private void addAction(LinearLayout root,String heading,String detail,Runnable action,boolean primary) {
        LinearLayout card=new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_VERTICAL); card.setPadding(dp(16),dp(9),dp(16),dp(9));
        card.setBackgroundColor(primary?Color.rgb(42,67,104):Color.rgb(28,36,51));
        TextView h=new TextView(this); h.setText(heading); h.setTextSize(16);
        h.setTypeface(Typeface.DEFAULT,Typeface.BOLD); h.setTextColor(Color.WHITE); card.addView(h);
        TextView d=new TextView(this); d.setText(detail); d.setTextSize(12);
        d.setTextColor(Color.rgb(190,199,214)); card.addView(d);
        LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,dp(68)); p.bottomMargin=dp(11);
        root.addView(card,p); card.setClickable(true); card.setFocusable(true); card.setOnClickListener(v->action.run());
    }

    private void openGemini() {
        try { new CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this,Uri.parse(GEMINI_URL)); }
        catch(Exception e) { Toast.makeText(this,"Не удалось открыть Gemini",Toast.LENGTH_LONG).show(); }
    }
    private void startVoice() {
        Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
        i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Скажите запрос для Gemini");
        try { startActivityForResult(i,VOICE_REQUEST); }
        catch(Exception e) { Toast.makeText(this,"Голосовой ввод недоступен",Toast.LENGTH_LONG).show(); }
    }
    private void chooseDocument() {
        Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT); i.setType("*/*"); i.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(i,DOCUMENT_REQUEST);
    }
    private void createCalendarEvent() {
        Intent i=new Intent(Intent.ACTION_INSERT); i.setData(CalendarContract.Events.CONTENT_URI);
        i.putExtra(CalendarContract.Events.TITLE,"Напоминание");
        i.putExtra(CalendarContract.Events.DESCRIPTION,"Создано из Gemini");
        try { startActivity(i); } catch(Exception e) { Toast.makeText(this,"Календарь недоступен",Toast.LENGTH_LONG).show(); }
    }
    private void showSettings() {
        new AlertDialog.Builder(this).setTitle("Настройки Gemini")
            .setMessage("Приложение открывает официальный сайт Gemini. Фоновое окно и разрешение «Поверх других приложений» удалены.")
            .setPositiveButton("Закрыть",null).show();
    }
    @Override protected void onActivityResult(int requestCode,int resultCode,Intent data) {
        super.onActivityResult(requestCode,resultCode,data);
        if(requestCode==VOICE_REQUEST && resultCode==RESULT_OK && data!=null) {
            ArrayList<String> r=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if(r!=null&&!r.isEmpty()) {
                ClipboardManager cb=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
                cb.setPrimaryClip(ClipData.newPlainText("Запрос для Gemini",r.get(0)));
                Toast.makeText(this,"Текст скопирован. Откройте Gemini и вставьте его.",Toast.LENGTH_LONG).show();
            }
        } else if(requestCode==DOCUMENT_REQUEST && resultCode==RESULT_OK && data!=null && data.getData()!=null) {
            Toast.makeText(this,"Файл выбран. Откройте Gemini и прикрепите его кнопкой +.",Toast.LENGTH_LONG).show();
        }
    }
}