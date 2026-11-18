package de.famulla.dienstnachweis;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.print.PrintManager;
import android.print.PrintAttributes;
import android.speech.RecognizerIntent;
import android.webkit.*;
import android.widget.Toast;
import androidx.webkit.WebViewAssetLoader;
import org.json.JSONObject;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
public class MainActivity extends Activity {
 private WebView web;
 private ValueCallback<Uri[]> fileCallback;
 private String pendingJson, speechField;
 private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_LONG).show();}
 @Override public void onCreate(Bundle state){
  super.onCreate(state);
  WebViewAssetLoader loader=new WebViewAssetLoader.Builder().addPathHandler("/assets/",new WebViewAssetLoader.AssetsPathHandler(this)).build();
  web=new WebView(this);
  web.getSettings().setJavaScriptEnabled(true);
  web.getSettings().setDomStorageEnabled(true);
  web.getSettings().setAllowFileAccess(false);
  web.getSettings().setAllowContentAccess(true);
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){WebResourceResponse x=loader.shouldInterceptRequest(r.getUrl());return x!=null?x:new WebResourceResponse("text/plain","UTF-8",new java.io.ByteArrayInputStream(new byte[0]));}
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){
    Uri u=r.getUrl();
    if("https".equals(u.getScheme())&&"appassets.androidplatform.net".equals(u.getHost())&&u.getPath()!=null&&u.getPath().startsWith("/assets/"))return false;
    if("mailto".equals(u.getScheme()))try{startActivity(new Intent(Intent.ACTION_SENDTO,u));}catch(Exception e){toast("Keine E-Mail-App verfügbar");}
    return true;
   }
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> cb,FileChooserParams p){
    if(fileCallback!=null)fileCallback.onReceiveValue(null);
    fileCallback=cb;
    try{startActivityForResult(p.createIntent(),400);}catch(Exception e){fileCallback.onReceiveValue(null);fileCallback=null;toast("Dateiauswahl nicht verfügbar");}
    return true;
   }
  });
  web.addJavascriptInterface(new Object(){
   @JavascriptInterface public void print(){runOnUiThread(()->{
    try{PrintManager pm=(PrintManager)getSystemService(PRINT_SERVICE);pm.print("Malteser-Dienstnachweis",web.createPrintDocumentAdapter("Malteser-Dienstnachweis"),new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());}catch(Exception e){toast("PDF-Druck nicht verfügbar");}
   });}
   @JavascriptInterface public void saveJson(String name,String json){runOnUiThread(()->{
    try{pendingJson=json;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,name.replaceAll("[^a-zA-Z0-9._-]","_"));startActivityForResult(i,401);}catch(Exception e){pendingJson=null;toast("Sichern fehlgeschlagen");}
   });}
   @JavascriptInterface public void speech(String field){runOnUiThread(()->{
    try{speechField=field;Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);startActivityForResult(i,402);}catch(Exception e){toast("Spracherkennung nicht verfügbar");}
   });}
  },"DienstNative");
  setContentView(web);
  web.loadUrl("https://appassets.androidplatform.net/assets/index.html");
 }
 @Override protected void onActivityResult(int request,int result,Intent data){
  super.onActivityResult(request,result,data);
  if(request==400&&fileCallback!=null){fileCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(result,data));fileCallback=null;}
  if(request==401){if(result==RESULT_OK&&data!=null&&data.getData()!=null&&pendingJson!=null){try(java.io.OutputStream out=getContentResolver().openOutputStream(data.getData())){if(out==null)throw new java.io.IOException();out.write(pendingJson.getBytes(StandardCharsets.UTF_8));toast("Sicherung gespeichert");}catch(Exception e){toast("Speichern fehlgeschlagen");}}pendingJson=null;}
  if(request==402&&result==RESULT_OK&&data!=null){ArrayList<String> list=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(list!=null&&!list.isEmpty()&&speechField!=null)web.evaluateJavascript("window.nativeSpeechResult("+JSONObject.quote(speechField)+","+JSONObject.quote(list.get(0))+")",null);}
 }
}
