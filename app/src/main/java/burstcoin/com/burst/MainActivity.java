package burstcoin.com.burst;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.api.GoogleApiClient;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, IntProvider {

    DatabaseHandler databaseHandler;
    SQLiteDatabase sqLiteDatabase;
    private GoogleApiClient client;
    private android.support.v4.app.FragmentTransaction fragmentTransaction;
    private NavigationView navigationView;
    private WebView mWebView;
    private DrawerLayout mDrawer;
    private SharedPreferences sharedPreferences;
    ProgressDialog progressDialog;
    private boolean isAtHome=true;
    private String burstID = "";
    private String numericID = "";
    //Toolbar toolbar;

    public final static String NUMERICID = "burstcoin.com.burst.NUMERICID";
    private final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("burstShared", MODE_PRIVATE);
        databaseHandler = new DatabaseHandler(MainActivity.this);
        sqLiteDatabase = databaseHandler.getWritableDatabase();
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        // Load up a Spinner
        progressDialog=new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading");
        progressDialog.show();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.setDrawerListener(toggle);
        toggle.syncState();

        //Context.getApplicationInfo().sourceDir;

        //Context.getPackageManager().getInstallerPackageName(packageName);

        mWebView = (WebView) findViewById(R.id.activity_main_webview);

        String url = "https://mwallet.burst-team.us:8125/index.html";
        String jsInjection = createBurstJSInjection();
        loadSite(url, jsInjection);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                new NavigationView.OnNavigationItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(MenuItem menuItem) {
                        selectDrawerItem(menuItem);
                        return true;
                    }
                });
    }

    public void selectDrawerItem(MenuItem menuItem) {
        // Create a new fragment and specify the planet to show based on
        // position

        switch (menuItem.getItemId()) {

            case R.id.nav_wallet:
                progressDialog.show();
                String jsInjectionWallet = createBurstJSInjection();
                loadSite("https://mwallet.burst-team.us:8125/index.html", jsInjectionWallet);
                isAtHome=true;
                break;
            case R.id.save_passphrase:

                final Dialog dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialog_box_phrase);
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                dialog.setTitle("Save Phrase");

                AppCompatButton buttonSave = (AppCompatButton) dialog.findViewById(R.id.save_phrase);
                AppCompatButton buttonName = (AppCompatButton) dialog.findViewById(R.id.phrase_name);
                AppCompatButton buttonPin = (AppCompatButton) dialog.findViewById(R.id.pin);
                AppCompatButton buttonReenterPin = (AppCompatButton) dialog.findViewById(R.id.reenter_pin);
                ImageView btn_paste = (ImageView)dialog.findViewById(R.id.btn_paste);

                int[] colorList = new int[]{
                        getResources().getColor(R.color.colorAccentPressed),
                        getResources().getColor(R.color.colorAccent)
                };

                int[][] states = new int[][]{
                        new int[]{android.R.attr.state_pressed}, // enabled
                        new int[]{}  // pressed
                };
                ColorStateList csl = new ColorStateList(states, colorList);
                buttonSave.setSupportBackgroundTintList(csl);
                buttonName.setSupportBackgroundTintList(csl);
                buttonPin.setSupportBackgroundTintList(csl);
                buttonReenterPin.setSupportBackgroundTintList(csl);


                final EditText passphrase = (EditText) dialog.findViewById(R.id.edt_passphrase);
                passphrase.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                final EditText namephrase = (EditText) dialog.findViewById(R.id.edt_name);
                final EditText pinphrase = (EditText) dialog.findViewById(R.id.edt_pin);
                final EditText reenter_pin_phrase = (EditText) dialog.findViewById(R.id.edt_reenter_pin);

                btn_paste.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                        String pasteData = "";

                        try {
                            // Examines the item on the clipboard. If getText() does not return null, the clip item contains the
// text. Assumes that this application can only handle one item at a time.
                            ClipData.Item item = clipboard.getPrimaryClip().getItemAt(0);

// Gets the clipboard as text.
                            if(item.getText() != null)
                            pasteData = item.getText().toString();

// If the string contains data, then the paste operation is done
                            if (pasteData != null) {

                                passphrase.setText(pasteData);
                                return;

    // The clipboard does not contain text. If it contains a URI, attempts to get data from it
                            } else {
                                Uri pasteUri = item.getUri();

                                // If the URI contains something, try to get text from it
                                if (pasteUri != null) {

                                    // calls a routine to resolve the URI and get data from it. This routine is not
                                    // presented here.
    //                                pasteData = resolveUri(Uri);
                                    return;
                                } else {

                                    // Something is wrong. The MIME type was plain text, but the clipboard does not contain either
                                    // text or a Uri. Report an error.
                                    Log.e("", "Clipboard contains an invalid data type");
                                    return;
                                }

                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }
                });

                buttonSave.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        if (!passphrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.name_layout).setVisibility(View.VISIBLE);
                            namephrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter passphrase.", Toast.LENGTH_LONG).show();
                        }
//
//                        SharedPreferences.Editor editor = sharedPreferences.edit();
//                        editor.putString("passphrase",savePhrase.getText().toString());
//                        editor.commit();
                    }
                });

                buttonName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!namephrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.pin_layout).setVisibility(View.VISIBLE);
                            pinphrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter name for phrase.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                buttonPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!pinphrase.getText().toString().equalsIgnoreCase("")) {
                            dialog.findViewById(R.id.pin_reenter_layout).setVisibility(View.VISIBLE);
                            reenter_pin_phrase.requestFocus();
                        } else {
                            Toast.makeText(getApplicationContext(), "Please enter pin for phrase.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                buttonReenterPin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (!reenter_pin_phrase.getText().toString().equalsIgnoreCase("")) {
                            if (pinphrase.getText().toString().equalsIgnoreCase(reenter_pin_phrase.getText().toString())) {
                                ContentValues contentValues = new ContentValues();
                                contentValues.put("name", namephrase.getText().toString());
                                contentValues.put("phrase", passphrase.getText().toString());
                                contentValues.put("pin", pinphrase.getText().toString());
                                databaseHandler.insertItem(contentValues, sqLiteDatabase);
                                Toast.makeText(getApplicationContext(), "Your phrase got successfully saved.", Toast.LENGTH_LONG).show();
                                dialog.dismiss();
                            } else {
                                Toast.makeText(getApplicationContext(), "Please enter same pin.", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Please re-enter pin.", Toast.LENGTH_LONG).show();
                        }
                    }
                });

                dialog.show();



                break;
            case R.id.load_passphrase:

                Dialog loadPhraseDialog = new Dialog(MainActivity.this);
                loadPhraseDialog.setContentView(R.layout.dialog_box_load_passphrase);
                loadPhraseDialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                loadPhraseDialog.setTitle("Load Passphrase:");
                RecyclerView recyclerView = (RecyclerView) loadPhraseDialog.findViewById(R.id.recyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));

                ArrayList<DataModel> dataModels = databaseHandler.getItems(sqLiteDatabase);
                if (dataModels != null) {
                    if (dataModels.size() > 0) {
                        if (dataModels != null) {
                            RecyclerViewAdapter recyclerViewAdapter = new RecyclerViewAdapter(MainActivity.this,dataModels,loadPhraseDialog, databaseHandler );
                            recyclerView.setAdapter(recyclerViewAdapter);
                        }
                        loadPhraseDialog.show();
                    } else {
                        Toast.makeText(getApplicationContext(), "You haven't added any passphrases yet.", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), "You haven't added any passphrase yet.", Toast.LENGTH_LONG).show();
                }

                //  fragmentClass = SecondFragment.class;
                break;
            case R.id.burst_crowd:
                progressDialog.show();
                mWebView.loadUrl("https://mwallet.burst-team.us:8125/atcrowdfund_mobile.html");
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressDialog.cancel();
                    }

                });
                // fragmentClass = ThirdFragment.class;
                isAtHome=false;
                break;

            case R.id.fauet:
                progressDialog.show();
                String url = "https://faucet.burst-team.us";
                if(!burstID.isEmpty()) {
                    String jsInjectionFaucet = "javascript: (function(){document.getElementById('accountId').value='"+burstID+"';})();";
                    loadSite(url, jsInjectionFaucet);
                } else {
                    loadSite(url);
                }
                isAtHome=false;
                // fragmentClass = ThirdFragment.class;
                break;

            case R.id.nav_plotting:
                    if (burstID.isEmpty())
                        Toast.makeText(getApplicationContext(),"Please login so we can obtain your Burst ID",Toast.LENGTH_LONG).show();
                    else if (numericID.isEmpty()){
                        Toast.makeText(getApplicationContext(),"Please login so we can obtain your numeric ID for plotting",Toast.LENGTH_LONG).show();
                    } else {
                        // Lets just throw something up
                        Intent plotIntent = new Intent(this, PlotterActivity.class);
                        plotIntent.putExtra(NUMERICID, numericID);
                        startActivity(plotIntent);
                        isAtHome=false;
                    }
                break;

            case R.id.nav_mining:
                    // ToDo: Using this as a Cheap test button while we Design out the Mining Phase
                    BurstUtil x = new BurstUtil(this);
                    if (!burstID.isEmpty())
                        x.getNumericIDFromBurstID(burstID, this);

                    //    Toast.makeText(getApplicationContext(),"Not implemented yet. Under development",Toast.LENGTH_LONG).show();
                    isAtHome=false;
                break;

            default:

        }


        mDrawer.closeDrawers();
    }

    // This is how Data is passed back to main via the IntProvider Class
    @Override
    public void notice(String... args){
        // ToDo: This is where we handle things
        /*
         * args[0] is probably the identified
         * args[1] is probably a status message, SUCCESS or something else, typically an error code
         */
        switch(args[0]) {
            case "GOTBURSTID":
                if (args[1].contains("SUCCESS"))
                    this.burstID = args[2];
                    BurstUtil fetchNumericID = new BurstUtil(this);
                    fetchNumericID.getNumericIDFromBurstID(burstID, this);
                break;
            case "GOTNUMERICID":
                if (args[1].contains("SUCCESS"))
                    this.numericID = args[2];
                else
                    this.numericID = "";
                break;
        }

        String debug = "";
        for (String s : args)
            debug = debug + " " + s;
        Log.d(TAG, debug);
        // This is were we get info back from BurstUtils
        //Toast.makeText(getApplicationContext(),"We got a notice back from BurtUtils: "+BurstUtil.getNumericIDFromLocal(burstID, this),Toast.LENGTH_LONG).show();
    }

    public void showToast(View view) {
      //  Toast.makeText(this, "Passphrase saved", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            if(isAtHome){
                super.onBackPressed();
            }else{
                isAtHome=true;
                progressDialog.show();
                mWebView.loadUrl("https://mwallet.burst-team.us:8125/index.html");
                mWebView.setWebViewClient(new WebViewClient(){
                    @Override
                    public void onPageFinished(WebView view, String url) {
                        super.onPageFinished(view, url);
                        progressDialog.cancel();
                    }

                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //@SuppressWarnings("StatementWithEmptyBody")

    /*
    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://burstcoin.com.burst/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Main Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app deep link URI is correct.
                Uri.parse("android-app://burstcoin.com.burst/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }
*/

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selectDrawerItem(item);
        return true;
    }

    // Generic Site Loader Prototype
    private void loadSite(String url){
        loadSite(url, "");
    }

    // Generic Site Loader Final with JSInjection
    private void loadSite(String url, final String jsInjection) {
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        /*
        ToDo: Enhance this section by SDK Version
        On API Level 19+, use evaluateJavascript().
        On API Level 18 and below, use loadUrl("javascript:"),
            using the same basic syntax used for bookmarklets on desktop browsers.
         */
        mWebView.loadUrl(url);
        mWebView.addJavascriptInterface(new JSInterface(this), "Android");
        mWebView.setWebViewClient(new WebViewClient(){
            @SuppressWarnings("deprecation")
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d(TAG, "ScriptWas:"+jsInjection);
                if (!jsInjection.isEmpty())
                    mWebView.loadUrl(jsInjection);
                //else
                //    mWebView.loadUrl(url);
                progressDialog.cancel();
            }
        });
    }

    private String createBurstJSInjection() {
        String jsInjection = "";
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    // DOM Level 4 - Android 4.4 and greater
        jsInjection = "javascript:var options = {subtree: true, childList: true, attributes: true, characterData: true};" +
                       "try { " +
                         "var account = $('#account_id')[0];" +
                         "var observer = new MutationObserver( function(mutations) {" +
                         "mutations.forEach(function (mutation) {" +
                           "Android.getBurstID(account.innerHTML);" +
                         "})" +
                       "});" +
                       "observer.observe(account, options);" +
                       "} catch (err) { Android.getBurstID('error:' + err) }";
        }
        else {        // ToDo: DOM Level 3 - MutationEvent (DOES NOT WORK YET)
            jsInjection =
                    "javascript:" +
                            "Android.getBurstID('Hello');" +  // <-- This does not work
                    "try { " +
                      "var account = document.getElementById('account_id')[0];"+
                            "Android.getBurstID('Pre 4.4');" +    // <-- This is not ffiring
                      "account.addEventListener('DOMCharacterDataModified', function(e) {" +  // DOMSubtreeModified or DOMAttrModified or DOMCharacterDataModified
                        "Android.getBurstID(document.getElementById('account_id).innerHTML);" +
                      "}, false);" +
                    "} catch (err) { Android.getBurstID('error:' + err) " +
                    "}";
        }
        return jsInjection;
    }
}


