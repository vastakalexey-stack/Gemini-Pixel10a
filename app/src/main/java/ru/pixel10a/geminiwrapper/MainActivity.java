package ru.pixel10a.geminiwrapper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.speech.RecognizerIntent;
import android.widget.Toast;
import androidx.browser.customtabs.CustomTabsIntent;
import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends Activity {
 private static final String URL="https://gemini.google.com/app";
 private static final int VOICE=41,DOC=42;
 @Override public void onCreate(Bundle b){super.onCreate(b);String a=getIntent().getAction();
  if("ru.pixel10a.geminiwrapper.VOICE".equals(a)){voice();return;}
  if("ru.pixel10a.geminiwrapper.DOCUMENT".equals(a)){document();return;}
  if("ru.pixel10a.geminiwrapper.CALENDAR".equals(a)){calendar();finish();return;}
  if("ru.pixel10a.geminiwrapper.SETTINGS".equals(a)){settings();return;}
  launch();}
 private void launch(){
  if(!Settings.canDrawOverlays(this)){new AlertDialog.Builder(this).setTitle("Плавающая шестерёнка")
   .setMessage("Разрешите отображение поверх других приложений, чтобы открыть меню Gemini поверх страницы.")
   .setPositiveButton("Разрешить",(d,w)->startActivity(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,Uri.parse("package:"+getPackageName()))))
   .setNegativeButton("Без меню",(d,w)->open()).show();return;}
  startService(new Intent(this,GeminiOverlayService.class));open();}
 @Override protected void onResume(){super.onResume();if(getIntent()!=null&&getIntent().getAction()==null&&Settings.canDrawOverlays(this)&&!isFinishing()){startService(new Intent(this,GeminiOverlayService.class));}}
 private void open(){try{new CustomTabsIntent.Builder().setShowTitle(true).build().launchUrl(this,Uri.parse(URL));}catch(Exception e){Toast.makeText(this,"Не удалось открыть Gemini",Toast.LENGTH_LONG).show();}finish();}
 private void voice(){Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Скажите запрос для Gemini");try{startActivityForResult(i,VOICE);}catch(Exception e){Toast.makeText(this,"Голосовой ввод недоступен",Toast.LENGTH_LONG).show();finish();}}
 private void document(){Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.setType("*/*");i.addCategory(Intent.CATEGORY_OPENABLE);startActivityForResult(i,DOC);}
 private void calendar(){Intent i=new Intent(Intent.ACTION_INSERT);i.setData(android.provider.CalendarContract.Events.CONTENT_URI);i.putExtra(android.provider.CalendarContract.Events.TITLE,"Напоминание");i.putExtra(android.provider.CalendarContract.Events.DESCRIPTION,"Создано из Gemini");try{startActivity(i);}catch(Exception e){Toast.makeText(this,"Календарь недоступен",Toast.LENGTH_LONG).show();}}
 private void settings(){new AlertDialog.Builder(this).setTitle("Настройки Gemini").setItems(new String[]{"О приложении","Скрыть шестерёнку"},(d,w)->{if(w==0)new AlertDialog.Builder(this).setMessage("Gemini для Pixel 10a").setPositiveButton("ОК",null).show();else stopService(new Intent(this,GeminiOverlayService.class));}).setNegativeButton("Закрыть",(d,w)->finish()).show();}
 @Override protected void onActivityResult(int r,int c,Intent data){super.onActivityResult(r,c,data);if(r==VOICE){if(c==RESULT_OK&&data!=null){ArrayList<String>x=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(x!=null&&!x.isEmpty()){String s=x.get(0);((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(android.content.ClipData.newPlainText("Запрос для Gemini",s));Toast.makeText(this,"Распознано и скопировано. Вставьте текст в Gemini.",Toast.LENGTH_LONG).show();}}finish();}else if(r==DOC){if(c==RESULT_OK&&data!=null&&data.getData()!=null){try{getContentResolver().takePersistableUriPermission(data.getData(),Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}Toast.makeText(this,"Файл выбран. Прикрепите его в Gemini кнопкой +.",Toast.LENGTH_LONG).show();}finish();}}
}