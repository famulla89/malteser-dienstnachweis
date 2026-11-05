package de.famulla.malteser;
import android.app.*;
import android.content.*;
import android.net.Uri;
import android.os.*;
import android.print.*;
import android.speech.RecognizerIntent;
import android.view.*;
import android.webkit.*;
import android.widget.Toast;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import org.json.JSONObject;

public class MainActivity extends Activity {
 private static final String ORIGIN="https://dienstzettel.invalid/";
 private WebView web,printView;
 private ValueCallback<Uri[]> chooser;
 private String pendingExport,speechField;
 private boolean exporting=false;
 private static final int FILE=10,CAMERA=11,EXPORT=12,SPEECH=13;
 private final Uri cameraUri=Uri.parse("content://de.famulla.malteser.camera/signature.jpg");
 @Override public void onCreate(Bundle state){
  super.onCreate(state);web=new WebView(this);setContentView(web);
  web.setOnApplyWindowInsetsListener((v,i)->{if(Build.VERSION.SDK_INT>=30){android.graphics.Insets bars=i.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());v.setPadding(bars.left,bars.top,bars.right,bars.bottom);}else{v.setPadding(i.getSystemWindowInsetLeft(),i.getSystemWindowInsetTop(),i.getSystemWindowInsetRight(),i.getSystemWindowInsetBottom());}return i;});
  WebSettings s=web.getSettings();s.setJavaScriptEnabled(true);s.setDomStorageEnabled(true);s.setAllowFileAccess(false);s.setAllowContentAccess(true);s.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);
  web.setWebViewClient(new WebViewClient(){
   @Override public WebResourceResponse shouldInterceptRequest(WebView v,WebResourceRequest r){
    String url=r.getUrl().toString();
    try{if(url.startsWith(ORIGIN)){String path=r.getUrl().getPath();if(path.equals("/"))path="/index.html";if(!Arrays.asList("/index.html","/icon-192.png","/icon-512.png","/manifest.webmanifest").contains(path))return blocked();String mime=path.endsWith(".html")?"text/html":path.endsWith(".png")?"image/png":"application/manifest+json";return new WebResourceResponse(mime,"UTF-8",getAssets().open(path.substring(1)));}}catch(IOException ignored){}
    return blocked();
   }
   @Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest r){String u=r.getUrl().toString();if(u.equals(ORIGIN)||u.equals(ORIGIN+"index.html"))return false;if(u.startsWith("mailto:")){try{startActivity(new Intent(Intent.ACTION_SENDTO,r.getUrl()));}catch(ActivityNotFoundException e){message("Keine E-Mail-App installiert.");}}return true;}
  });
  web.setWebChromeClient(new WebChromeClient(){
   @Override public boolean onShowFileChooser(WebView v,ValueCallback<Uri[]> callback,FileChooserParams p){
    if(chooser!=null)chooser.onReceiveValue(null);chooser=callback;
    try{if(p.isCaptureEnabled()){
      Intent camera=new Intent("android.media.action.IMAGE_CAPTURE");camera.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,cameraUri);camera.setClipData(ClipData.newRawUri("Unterschrift",cameraUri));camera.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);startActivityForResult(camera,CAMERA);
     }else{Intent pick=new Intent(Intent.ACTION_OPEN_DOCUMENT);pick.addCategory(Intent.CATEGORY_OPENABLE);String[] accept=p.getAcceptTypes();pick.setType(accept.length>0&&accept[0].startsWith("image/")?"image/*":"*/*");startActivityForResult(pick,FILE);}return true;
    }catch(ActivityNotFoundException e){chooser.onReceiveValue(null);chooser=null;message("Keine passende App vorhanden. Bitte Datei auswählen.");return true;}
   }
   @Override public boolean onJsAlert(WebView v,String u,String text,JsResult r){new AlertDialog.Builder(MainActivity.this).setMessage(text).setPositiveButton("OK",(d,w)->r.confirm()).setOnCancelListener(d->r.cancel()).show();return true;}
   @Override public boolean onJsConfirm(WebView v,String u,String text,JsResult r){new AlertDialog.Builder(MainActivity.this).setMessage(text).setPositiveButton("Ja",(d,w)->r.confirm()).setNegativeButton("Abbrechen",(d,w)->r.cancel()).setOnCancelListener(d->r.cancel()).show();return true;}
  });
  web.addJavascriptInterface(new NativeBridge(),"MalteserNative");web.loadUrl(ORIGIN);
 }
 private WebResourceResponse blocked(){return new WebResourceResponse("text/plain","UTF-8",403,"Forbidden",Collections.emptyMap(),new ByteArrayInputStream(new byte[0]));}
 private void message(String text){Toast.makeText(this,text,Toast.LENGTH_LONG).show();}
 public class NativeBridge {
  @JavascriptInterface public void exportBackup(String json,String filename){if(json==null||json.length()>6*1024*1024)return;runOnUiThread(()->{if(exporting)return;exporting=true;pendingExport=json;Intent i=new Intent(Intent.ACTION_CREATE_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("application/json");i.putExtra(Intent.EXTRA_TITLE,filename.replaceAll("[^a-zA-Z0-9._-]","_"));try{startActivityForResult(i,EXPORT);}catch(ActivityNotFoundException e){exporting=false;pendingExport=null;message("Kein Dateidialog verfügbar.");}});}
  @JavascriptInterface public void printDocument(String html){if(html==null||html.length()>6*1024*1024)return;runOnUiThread(()->{if(printView!=null){message("Bitte zuerst den Druckdialog schließen.");return;}printView=new WebView(MainActivity.this);printView.setWebViewClient(new WebViewClient(){@Override public void onPageFinished(WebView v,String u){PrintManager pm=(PrintManager)getSystemService(PRINT_SERVICE);PrintDocumentAdapter original=v.createPrintDocumentAdapter("Malteser-Dienstzettel");pm.print("Malteser-Dienstzettel",new PrintDocumentAdapter(){public void onLayout(PrintAttributes a,PrintAttributes b,CancellationSignal c,LayoutResultCallback d,Bundle e){original.onLayout(a,b,c,d,e);}public void onWrite(PageRange[] a,ParcelFileDescriptor b,CancellationSignal c,WriteResultCallback d){original.onWrite(a,b,c,d);}public void onStart(){original.onStart();}public void onFinish(){original.onFinish();v.destroy();printView=null;}},new PrintAttributes.Builder().setMediaSize(PrintAttributes.MediaSize.ISO_A4).build());}});printView.loadDataWithBaseURL(ORIGIN,html,"text/html","UTF-8",null);});}
  @JavascriptInterface public void speak(String field){if(!Arrays.asList("date","endDate","start","end").contains(field))return;runOnUiThread(()->{speechField=field;Intent i=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"de-DE");i.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE,true);try{startActivityForResult(i,SPEECH);}catch(ActivityNotFoundException e){message("Spracherkennung fehlt. Nutze das Mikrofon deiner Tastatur.");}});}
 }
 @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);
  if(request==FILE||request==CAMERA){if(chooser!=null){chooser.onReceiveValue(result==RESULT_OK?(request==CAMERA?new Uri[]{cameraUri}:WebChromeClient.FileChooserParams.parseResult(result,data)):null);chooser=null;}return;}
  if(request==EXPORT){String json=pendingExport;pendingExport=null;exporting=false;if(result==RESULT_OK&&data!=null&&data.getData()!=null&&json!=null){Uri uri=data.getData();new Thread(()->{try(OutputStream out=getContentResolver().openOutputStream(uri,"wt")){if(out==null)throw new IOException();out.write(json.getBytes(StandardCharsets.UTF_8));runOnUiThread(()->message("Datensicherung gespeichert."));}catch(IOException e){runOnUiThread(()->message("Sicherung fehlgeschlagen. Bitte erneut sichern."));}}).start();}return;}
  if(request==SPEECH&&result==RESULT_OK&&data!=null){ArrayList<String> matches=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);if(matches!=null&&!matches.isEmpty())web.evaluateJavascript("window.malteserSpeechResult("+JSONObject.quote(speechField)+","+JSONObject.quote(matches.get(0))+")",null);}
 }
 @Override public void onBackPressed(){web.evaluateJavascript("(()=>{const p=document.getElementById('pdfPreview');if(!p.classList.contains('sign-hidden')){document.getElementById('closePreview').click();return true}if(document.getElementById('historySection').style.display==='block'){document.getElementById('backFromHistory').click();return true}return false})()",value->{if(!"true".equals(value))new AlertDialog.Builder(this).setMessage("App schließen? Deine gespeicherten Einträge bleiben erhalten.").setPositiveButton("Schließen",(d,w)->finish()).setNegativeButton("Zurück",null).show();});}
 @Override protected void onPause(){web.evaluateJavascript("document.activeElement&&document.activeElement.blur()",null);super.onPause();}
 @Override protected void onDestroy(){if(chooser!=null)chooser.onReceiveValue(null);web.removeJavascriptInterface("MalteserNative");web.destroy();if(printView!=null)printView.destroy();super.onDestroy();}
}
