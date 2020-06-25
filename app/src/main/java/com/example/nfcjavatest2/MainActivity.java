package com.example.nfcjavatest2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.media.session.MediaSession;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.IsoDep;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.android.volley.AuthFailureError;
import com.android.volley.ClientError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Callable;
import java.util.Calendar;


public class MainActivity extends AppCompatActivity implements NfcAdapter.ReaderCallback {

    private NfcAdapter nfcAdapter = null;
    private Double EnergyLeft = 0d;
    private Double MinimumEnergy = 0d;
    private Double EnergyUsed = 0d;
    private String ErrorMessage = "";
    private Double ProposedUsage = 80d;
    private List<Integer> availableChargersForClient = new ArrayList();
    private String InstructionPhase = "";
    private CacheData CacheData = new CacheData();
    // Wep api values
    private String serverUrl = "http://129.232.220.250:5555/";
    private String Token = "";
    private boolean IsConnectedToApiServer = false;
    private String Version = "2020-05-30-a";
    private boolean ThirtySecondsFinishedAfterChargingStopped = true;
    private Timer timer;
    private String ToastMessage;
    private boolean IsClientActive;
    private String SiteAdminContactNumber = "+27 87 551 2600";
    private int EnergySentToCharger;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_logout:

                DeleteDBData();
                DisplayLoginControls();

                return true;

            case R.id.menu_buycredit:

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evcharge.co.za/"));
                startActivity(browserIntent);

                return true;


            case R.id.menu_stats:

                DisplayStatsPage();

                return true;

            case R.id.menu_version:

                Toast toast2 = Toast.makeText(getApplicationContext(), "Version : " + Version, Toast.LENGTH_LONG);
                toast2.show();

                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void DisplayStatsPage() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                HideAllControls();

                final TextView txtStatsHeader = findViewById(R.id.txtStatsHeader);
                txtStatsHeader.setVisibility(TextView.VISIBLE);
                txtStatsHeader.setText("Stats");

                final TextView txtStatsHeader2Left = findViewById(R.id.txtStatsHeaderRow2Left);
                txtStatsHeader2Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader2Left.setText("Left 2");

                final TextView txtStatsHeader2Right = findViewById(R.id.txtStatsHeaderRow2Right);
                txtStatsHeader2Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader2Right.setText("right 2");

                final TextView txtStatsHeader3Left = findViewById(R.id.txtStatsHeaderRow3Left);
                txtStatsHeader3Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader3Left.setText("Left 3");

                final TextView txtStatsHeader3Right = findViewById(R.id.txtStatsHeaderRow3Right);
                txtStatsHeader3Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader3Right.setText("right 3");

                final TextView txtStatsHeader4Left = findViewById(R.id.txtStatsHeaderRow4Left);
                txtStatsHeader4Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader4Left.setText("Left 4");

                final TextView txtStatsHeader4Right = findViewById(R.id.txtStatsHeaderRow4Right);
                txtStatsHeader4Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader4Right.setText("right 4");

                final TextView txtStatsHeader5Left = findViewById(R.id.txtStatsHeaderRow5Left);
                txtStatsHeader5Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader5Left.setText("Left 5");

                final TextView txtStatsHeader5Right = findViewById(R.id.txtStatsHeaderRow5Right);
                txtStatsHeader5Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader5Right.setText("right 5");

                final TextView txtStatsHeader6Left = findViewById(R.id.txtStatsHeaderRow6Left);
                txtStatsHeader6Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader6Left.setText("Left 6");

                final TextView txtStatsHeader6Right = findViewById(R.id.txtStatsHeaderRow6Right);
                txtStatsHeader6Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader6Right.setText("right 6");

                final TextView txtStatsHeader7Left = findViewById(R.id.txtStatsHeaderRow7Left);
                txtStatsHeader7Left.setVisibility(TextView.VISIBLE);
                txtStatsHeader7Left.setText("Left 7");

                final TextView txtStatsHeader7Right = findViewById(R.id.txtStatsHeaderRow7Right);
                txtStatsHeader7Right.setVisibility(TextView.VISIBLE);
                txtStatsHeader7Right.setText("right 7");
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Android initialization
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Toolbar setup
        Toolbar myToolbar = findViewById(R.id.my_toolbar);
        setSupportActionBar(myToolbar);
        myToolbar.setBackgroundColor(Color.parseColor("#ffffff"));
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        myToolbar.setTitle("");

        //NFC Setup
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            SetMyText("", "", "", "", "NFC is disabled or not present");
            return;
        }

        //Check if we have local credentials
        if (LocalCredentialsExistAndSchemaValid()) {
            CheckConnectionToApiServer(); //async call
        } else {
            DeleteCacheTableIfItExists();
            CreateCacheTable();
            SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServer(); //async call
        }
    }

    public void txtMiddle_clicked(View v) {
        if (InstructionPhase.equals("Balance"))
            CheckConnectionToApiServerForBalanceRefresh();
    }

    public void lblBottomTop_clicked(View v) {
        TextView txtBottomTop = findViewById(R.id.lblBottomTop);

         if (txtBottomTop.getText().equals("BUY CREDIT")) {
             Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.evcharge.co.za/"));
             startActivity(browserIntent);
         }
    }

    private void DisplayAccountInactiveControls() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                HideAllControls();
                DisplayRedCircle();
                SetMyText("ACCOUNT", "INACTIVE", "", "CONTACT SITE ADMIN", SiteAdminContactNumber);
            }
        });
    }

    private void DisplayGreenCircle() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.VISIBLE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.GONE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.GONE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.GONE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.GONE);
            }
        });
    }

    private void DisplayYellowCircle() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.GONE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.VISIBLE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.GONE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.GONE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.GONE);
            }
        });
    }

    private void DisplayRedCircle() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.GONE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.GONE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.GONE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.VISIBLE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.GONE);
            }
        });
    }

    private void DisplayBlueCircle() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.GONE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.GONE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.VISIBLE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.GONE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.GONE);
            }
        });
    }

    private void DisplayDarkBlueCircle() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.GONE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.GONE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.GONE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.GONE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.VISIBLE);
            }
        });
    }

    private void DisplayChargingGif() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final pl.droidsonroids.gif.GifImageView chargingGif = findViewById(R.id.gif_power);
                chargingGif.setVisibility(ImageView.VISIBLE);

              /*  final TextView txt_busy = findViewById(R.id.txt_busy);
                txt_busy.setVisibility(TextView.VISIBLE);*/

            }
        });
    }

    private void HideChargingGif() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final pl.droidsonroids.gif.GifImageView chargingGif = findViewById(R.id.gif_power);
                chargingGif.setVisibility(ImageView.GONE);
            }
        });
    }

    private void SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServer() {
        SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServerAsync(new Callable<Void>() {
            public Void call() {
                SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServerAsyncCallback();
                return null;
            }
        });
    }

    private void CreateCacheTable() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.CreateNewCacheTable();
    }

    private void CheckConnectionToApiServerForBalanceRefresh() {
        CheckConnectionToApiServerAsync(new Callable<Void>() {
            public Void call() {
                CheckConnectionToApiServerForBalanceRefreshAsyncCallback();
                return null;
            }
        });
    }

    private void CheckConnectionToApiServer() {
        CheckConnectionToApiServerAsync(new Callable<Void>() {
            public Void call() {
                CheckConnectionToApiServerAsyncCallback();
                return null;
            }
        });
    }

    private boolean LocalCredentialsExistAndSchemaValid() {
        DatabaseHelper db = new DatabaseHelper(this);
        try {
            CacheData data = db.readData();
            if (data == null)
                return false;
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void GetEnergyBalanceFromServer() {
        GetEnergyBalanceFromServerAsync(new Callable<Void>() {
            public Void call() {
                GetEnergyBalanceFromServerAsyncCallback();
                return null;
            }
        });

        availableChargersForClient = GetAvailableChargersForClient();
    }

    private List<Integer> GetAvailableChargersForClient() {
        return new ArrayList<Integer>();
    }

    private void OpenLoginControls() {
        final EditText txtUsername = findViewById(R.id.txtUsername);
        final EditText txtPin = findViewById(R.id.txtPin);
        //final ImageView powerImage = findViewById(R.id.img_power);
        //powerImage.setVisibility(ImageView.GONE);
        txtUsername.setVisibility(EditText.VISIBLE);
        txtPin.setVisibility(EditText.VISIBLE);
    }

    private void HideLoginControls() {
        final EditText txtUsername = findViewById(R.id.txtUsername);
        final EditText txtPin = findViewById(R.id.txtPin);
        final Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setVisibility(Button.GONE);
        txtUsername.setVisibility(EditText.GONE);
        txtPin.setVisibility(EditText.GONE);
    }

    private boolean DoLocalDataExist() {
        try {
            //Do SqLite read here on credentials table to see if creds exist
            DatabaseHelper db = new DatabaseHelper(this);
            CacheData data = db.readData();
            return data != null ? true : false;
        } catch (Exception e) {
            SetMyText("Could not get credentials from database: Error: " + e.getMessage(), "", "", "", "");
            return false;
        }
    }

    private void Login() {
        if (!InstructionPhase.equals("Login"))
            return;

        final EditText txtUsername = findViewById(R.id.txtUsername);
        final EditText txtPin = findViewById(R.id.txtPin);

        String username = txtUsername.getText().toString();
        String password = txtPin.getText().toString();

        CacheData.Username = username;
        CacheData.Password = password;

        VerifyClientCredentialsOnServer(); // async call
    }

    public void CheckConnectionToApiServerForBalanceRefreshAsyncCallback() {
        if (IsConnectedToApiServer) {
            ToastMessage = "Updated Balance";
            Toast();
            GetEnergyBalanceFromServer();
        }
    }

    public void CheckConnectionToApiServerAsyncCallback() {
        if (IsConnectedToApiServer) {
            GetPhoneDBCredentialsIntoCacheGlobalVariable();
            VerifyClientCredentialsOnServer(); // async call
        }
    }

    private void GetPhoneDBCredentialsIntoCacheGlobalVariable() {
        DatabaseHelper db = new DatabaseHelper(this);
        CacheData data = db.readData();

        if (data != null)
            CacheData = data;
    }

    public void SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServerAsyncCallback() {
        if (IsConnectedToApiServer)
            DisplayLoginControls();
    }

    private void DisplayNoConnectionToApiServerErrorMsg() {
        SetMyText("No Internet connection", "", "", "", "");
    }

    private void DisplayLoginControls() {
        HideAllControls();

        final Button btnLogin = findViewById(R.id.btnLogin);
        btnLogin.setVisibility(EditText.VISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Login();
            }
        });

        final EditText txtUsername = findViewById(R.id.txtUsername);
        final EditText txtPin = findViewById(R.id.txtPin);

        txtUsername.setVisibility(EditText.VISIBLE);
        txtPin.setVisibility(EditText.VISIBLE);

        InstructionPhase = "Login";
    }

    private void Toast() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(getApplicationContext(), ToastMessage, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }


    private void HideAllControls() {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                SetMyText("", "", "", "", "");

                /*final TextView txt_busy = findViewById(R.id.txt_busy);
                txt_busy.setVisibility(TextView.GONE);*/

                final TextView txtTop = findViewById(R.id.txtTop);
                txtTop.setVisibility(TextView.GONE);

                final TextView txtMiddle = findViewById(R.id.txtMiddle);
                txtMiddle.setVisibility(TextView.GONE);

                final TextView txtBottom = findViewById(R.id.txtBottom);
                txtBottom.setVisibility(TextView.GONE);

                final EditText txtUsername = findViewById(R.id.txtUsername);
                txtUsername.setVisibility(EditText.GONE);

                final EditText txtPin = findViewById(R.id.txtPin);
                txtPin.setVisibility(EditText.GONE);

                final Button btnLogin = findViewById(R.id.btnLogin);
                btnLogin.setVisibility(EditText.GONE);

                final ImageView greenCircle = findViewById(R.id.green_circle);
                greenCircle.setVisibility(ImageView.GONE);

                final ImageView yellowCircle = findViewById(R.id.yellow_circle);
                yellowCircle.setVisibility(ImageView.GONE);

                final ImageView blueCircle = findViewById(R.id.blue_circle);
                blueCircle.setVisibility(ImageView.GONE);

                final ImageView darkblueCircle = findViewById(R.id.darkblue_circle);
                darkblueCircle.setVisibility(ImageView.GONE);

                final ImageView redCircle = findViewById(R.id.red_circle);
                redCircle.setVisibility(ImageView.GONE);

                HideChargingGif();
            }
        });
    }

    private void DisplayBalanceControls() {
        HideAllControls();

        GetPhoneDBCredentialsIntoCacheGlobalVariable();
        //if (EnergyLeft == 0)
        EnergyLeft = Double.parseDouble(CacheData.EnergyBalance);

        SetMyText("AVAILABLE", "" + EnergyLeft, "kWh", "", "");

        if (!ThirtySecondsFinishedAfterChargingStopped) {
            //blue circle
            DisplayBlueCircle();
            SetMyText("USED", "" + EnergyUsed, "kWh", "", "");
        } else if (EnergyLeft >= 100) {
            //green circle
            DisplayGreenCircle();
        } else if (EnergyLeft < 100 && EnergyLeft > 0) {
            //yellow circle
            DisplayYellowCircle();
        } else if (EnergyLeft <= 0) {
            //red circle
            DisplayRedCircle();
        }

        InstructionPhase = "Balance";
    }

    private void DisplayChargingControls() {
        HideAllControls();
        //SetMyText("", "BUSY", "", "", "");
        DisplayChargingGif();
        InstructionPhase = "Charging";
    }

    private boolean IsCharging() {
        DatabaseHelper db = new DatabaseHelper(this);
        CacheData data = db.readData();

        if (data == null)
            return false;

        if (data.IsCharging)
            return true;
        else
            return false;
    }

    private void VerifyClientCredentialsOnServer() {
        VerifyClientCredentialsOnServerAsync(new Callable<Void>() {
            public Void call() {
                VerifyClientCredentialsOnServerAsyncCallback();
                return null;
            }
        });
    }

    public void VerifyClientCredentialsOnServerAsyncCallback() {
        //If Token != "" , it means that the client credentials were validated successfully on the server.
        if (!Token.equals("")) {
            SaveCredentialsOnPhoneDB();
            IsClientActiveOnServer(); //async call
        }
    }

    private void IsClientActiveOnServer() {
        IsClientActiveOnServerAsync(new Callable<Void>() {
            public Void call() {
                IsClientActiveOnServerAsyncCallback();
                return null;
            }
        });
    }

    private void SaveCredentialsOnPhoneDB() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.saveCredentials(CacheData.Username, CacheData.Password);
    }

    public void IsClientActiveOnServerAsyncCallback() {
        if (IsClientActive)
            GetEnergyBalanceFromServer(); //async call
        else
            GetSiteAdminContactNumberFromServer(); //async call
    }

    private void GetSiteAdminContactNumberFromServer() {
        GetSiteAdminContactNumberFromServerAsync(new Callable<Void>() {
            public Void call() {
                GetSiteAdminContactNumberFromServerAsyncCallback();
                return null;
            }
        });
    }

    public void GetSiteAdminContactNumberFromServerAsyncCallback() {
        DisplayAccountInactiveControls();
    }

    public void GetEnergyBalanceFromServerAsyncCallback() {
        SaveEnergyBalanceOnPhoneDB();
        GetMinimumEnergyFromServer(); //async call
    }

    private void GetMinimumEnergyFromServer() {
        GetMinimumEnergyFromServerAsync(new Callable<Void>() {
            public Void call() {
                GetMinimumEnergyFromServerAsyncCallback();
                return null;
            }
        });
    }

    private void GetMinimumEnergyFromServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "GetClientMinimumEnergy";
        final String token = this.Token;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    try {
                        JSONObject res = new JSONObject(response);
                        String energyWithComma = res.getString("EnergyBalance");
                        String energyWithDecimalPoint = energyWithComma.replace(',', '.');
                        MinimumEnergy = Double.parseDouble(energyWithDecimalPoint);

                        try {
                            callBack.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        queue.add(request);
    }

    private void SchemaInvalidOrNoPhoneDBDataCheckConnectionToApiServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "EVCTest";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        IsConnectedToApiServer = true;
                        try {
                            callBack.call();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        IsConnectedToApiServer = false;
                        DisplayNoConnectionToApiServerErrorMsg();
                    }
                }
        );
        queue.add(request);
    }

    public void GetMinimumEnergyFromServerAsyncCallback() {
        SaveMinimumEnergyOnPhoneDB();

        if (IsCharging())
            DisplayChargingControls();
        else
            DisplayBalanceControls();
    }

    private void SaveMinimumEnergyOnPhoneDB() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.saveMinimumEnergy(MinimumEnergy);
    }

    private void SaveEnergyBalanceOnPhoneDB() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.saveEnergy(EnergyLeft);
    }

    public void DeleteCacheTableIfItExists() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.DeleteCacheTable();
    }

    public void DeleteDBData() {
        DatabaseHelper db = new DatabaseHelper(this);
        db.ClearDatabase();
        CacheData = new CacheData();
        EnergyLeft = 0d;
    }

    @Override
    public void onPause() {
        super.onPause();
        nfcAdapter.disableReaderMode(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        nfcAdapter.enableReaderMode(this, this,
                NfcAdapter.FLAG_READER_NFC_A |
                        NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
                null);
    }

    private void SetMyText(final String msg1, final String msg2, final String msg3, final String msg4, final String msg5) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {

                TextView txtTop = findViewById(R.id.txtTop);

                if (!msg1.equals("")) {
                    txtTop.setVisibility(TextView.VISIBLE);
                    txtTop.setText(msg1);
                } else {
                    txtTop.setVisibility(TextView.GONE);
                }

                TextView txtMiddle = findViewById(R.id.txtMiddle);

                if (!msg2.equals("")) {
                    txtMiddle.setVisibility(TextView.VISIBLE);
                    txtMiddle.setText(msg2);
                } else {
                    txtMiddle.setVisibility(TextView.GONE);
                }

                TextView txtBottom = findViewById(R.id.txtBottom);

                if (!msg3.equals("")) {
                    txtBottom.setVisibility(TextView.VISIBLE);
                    txtBottom.setText(msg3);
                } else {
                    txtBottom.setVisibility(TextView.GONE);
                }

                TextView txtBottomTop = findViewById(R.id.lblBottomTop);

                if (!msg4.equals("")) {
                    txtBottomTop.setVisibility(TextView.VISIBLE);
                    txtBottomTop.setText(msg4);
                } else {
                    txtBottomTop.setVisibility(TextView.GONE);
                }

                TextView txtBottomBottom = findViewById(R.id.lblBottomBottom);

                if (!msg5.equals("")) {
                    txtBottomBottom.setVisibility(TextView.VISIBLE);
                    txtBottomBottom.setText(msg5);
                } else {
                    txtBottomBottom.setVisibility(TextView.GONE);
                }
            }
        });
    }

    private void TogglePowerImageVisibility(final Boolean visible) {
        this.runOnUiThread(new Runnable() {

            @Override
            public void run() {
                if (visible) {
                    //final ImageView powerImage = findViewById(R.id.img_power);
                    //powerImage.setVisibility(ImageView.VISIBLE);

                    final pl.droidsonroids.gif.GifImageView chargingGif = findViewById(R.id.gif_power);
                    chargingGif.setVisibility(ImageView.VISIBLE);


                } else {
                    final pl.droidsonroids.gif.GifImageView chargingGif = findViewById(R.id.gif_power);
                    chargingGif.setVisibility(ImageView.GONE);
                    //final ImageView powerImage = findViewById(R.id.img_power);
                    //powerImage.setVisibility(ImageView.GONE);
                }
            }
        });
    }

    @Override
    public void onTagDiscovered(Tag tag) {

        //the process as follows :
        //1. This method gets called when the phone comes close to the Charger
        //2. I then Send my "Select instruction" based on "InstructionPhase"
        //3. I immediately receive a response.
        //4. I process the response, and send an "authenticate instruction" or nothing
        //   if the charger id does not match the clients id on the phone
        //5. I get another response from the "authenticate instruction" and handle accordingly.

        if (!ThirtySecondsFinishedAfterChargingStopped)
            return;

        if (EnergyLeft <= MinimumEnergy) {
            SetMyText("", "ZERO ENERGY", "AVAILABLE", "BUY CREDIT", "www.evcharge.co.za");
            return;
        }

        APDU selectApdu = null;

        if (InstructionPhase.equals("Balance")) {
            selectApdu = BuildStartChargeSelectAPDU();
        } else if (InstructionPhase.equals("Charging")) {
            selectApdu = BuildEndChargeSelectAPDU();
        } else if (InstructionPhase.equals("Login")) {
            return;
        }

        Log.i("select apdu : ", selectApdu.GetApdu());

        IsoDep isoDep = IsoDep.get(tag);

        String selectResult = "";

        try {
            isoDep.connect();

            byte[] result = isoDep.transceive(HexStringToByteArray(selectApdu.GetApdu()));

            selectResult = ByteArrayToHexString(result);

            Log.i("select result 1 ", selectResult);

            int responseLength = selectResult.length();

            int expectedLength = ConvertHexToInt(selectApdu.LE);

            APDU authApdu = null;

            if (InstructionPhase.equals("Balance")) {

                String chargerIdHex = selectResult.substring(0, 8);
                //int chargerId = ConvertHexToInt(chargerIdHex);

                String transIdHex = selectResult.substring(12, 16);
                //int transId = ConvertHexToInt(transIdHex);

                boolean userCanUseThisCharger = true;
                //Check if chargerId is correct according to phone user id from Server
                //string userChargerHex = GetUserChargerList();
                //Loop through list of user chargers and check to see if chargerId is in userChargerId list
                //if (chargerIdHex != userChargerHex)

                if (userCanUseThisCharger) {
                    authApdu = BuildStartChargeAuthAPDU(transIdHex);

                    Log.i("auth apdu : ", authApdu.GetApdu());

                    String authResult = "";

                    byte[] resultAuth = isoDep.transceive(HexStringToByteArray(authApdu.GetApdu()));

                    authResult = ByteArrayToHexString(resultAuth);

                    Log.i("StartChargeAuthResult", authResult);

                    String responseResult = authResult.substring(8, 12);

                    ToastMessage = responseResult;
                    Toast();


                    if (responseResult.equals("9000")) {
                        DisplayChargingControls();
                        SetStateToChargingOnDB(true);
                    } else if (responseResult.equals("9500")) {
                        DisplayChargingControls();
                    }
                }
            } else if (InstructionPhase.equals("Charging")) {
                String chargerIdHex = selectResult.substring(0, 8);
                //int chargerId = ConvertHexToInt(chargerIdHex);

                String transIdHex = selectResult.substring(12, 16);
                //int transId = ConvertHexToInt(transIdHex);

                boolean UserCanStopThisCharge = true;
                //Check if this phone is allowed to stop this charge

                if (UserCanStopThisCharge) {
                    authApdu = BuildEndChargeAuthAPDU(transIdHex);

                    Log.i("auth apdu : ", authApdu.GetApdu());

                    String authResult = "";

                    byte[] resultAuth = isoDep.transceive(HexStringToByteArray(authApdu.GetApdu()));

                    authResult = ByteArrayToHexString(resultAuth);

                    String responseResult = authResult.substring(8, 12);

                    //SetMyText("", "", "Response : " + responseResult);

                    ToastMessage = responseResult;
                    Toast();

                    if (responseResult.equals("9000")) {

                        timer = new Timer();
                        timer.schedule(new RemindTask(), 5000);
                        ThirtySecondsFinishedAfterChargingStopped = false;

                        SetStateToChargingOnDB(false);

                        String energyLeftFromWhatWasSentHex = authResult.substring(4, 8);

                        Long energyLeftFromWhatWasSentFromNFC = Long.parseLong(energyLeftFromWhatWasSentHex, 16);

                        //ToastMessage = energyLeftFromWhatWasSentFromNFC.toString();
                        //Toast();

                        Long energyUsedLongInTenths = EnergySentToCharger - energyLeftFromWhatWasSentFromNFC;
                        double energyUsedDouble = energyUsedLongInTenths / 10d;

                        String energyUsedString = Double.toString(energyUsedDouble);
                        EnergyUsed = Double.parseDouble(energyUsedString);
                        //Integer energyUsedInt = Integer.parseInt(energyUsed);

                        EnergyLeft = EnergyLeft - energyUsedDouble;

                        if (EnergyLeft <= MinimumEnergy) {
                            EnergyLeft = MinimumEnergy;
                        }

                        //SetMyText("Used\n" + energyUsedLong + " kWh", "Available\n" + EnergyLeft + " kWh", "Response : " + responseResult);
                        //EnergyLeft = EnergyLeft - 0.1;

                        // Update db with new energy balance
                        DatabaseHelper db = new DatabaseHelper(this);
                        db.saveEnergy(EnergyLeft);

                        // Post new balance to server
                        WebApiPostEnergyLeft(EnergyLeft);

                        if (EnergyLeft < ProposedUsage) {
                            ProposedUsage = EnergyLeft;
                            //EnergyLeft = 0;
                        }

                        DisplayBalanceControls();
                    } else if (responseResult.equals("9500")) {
                        DisplayBalanceControls();
                    }
                }
            }
        } catch (IOException ex) {
            //This code happens when there is a communications failure when the charger cannot talk to the phone
            //Ignore this and try again.
            return;
        }
    }

    class RemindTask extends TimerTask {
        public void run() {

            ThirtySecondsFinishedAfterChargingStopped = true;
            DisplayBalanceControls();

            timer.cancel();
        }
    }


    private void SetStateToChargingOnDB(boolean isCharging) {
        DatabaseHelper db = new DatabaseHelper(this);
        db.SetChargingState(isCharging);
    }

    private int ConvertHexToInt(String hexString) {
        return Integer.parseInt(hexString, 16);
    }

    private APDU BuildEndChargeSelectAPDU() {

        APDU apdu = new APDU();
        apdu.CLA = "00";
        apdu.INS = "A4";
        apdu.P1 = "04";
        apdu.P2 = "00";
        apdu.LC = "07";
        apdu.Data = GetEndChargeSelectDataAPDUPart();
        apdu.LE = "08";

        return apdu;
    }

    private String GetEndChargeSelectDataAPDUPart() {

        EndChargeSelectDataAPDUPart data = new EndChargeSelectDataAPDUPart();

        data.Category = "FF";
        data.AID = "0002";
        data.Version = "0001";
        data.Reserved1 = "0000";

        return data.GetEndChargeSelectDataAPDUPart();
    }

    private APDU BuildStartChargeAuthAPDU(String transIdHex) {
        APDU apdu = new APDU();
        apdu.CLA = "00";
        apdu.INS = "86";
        apdu.P1 = "00";
        apdu.P2 = "00";
        apdu.LC = "34"; //52 in plain
        apdu.Data = GetStartChargeAuthDataAPDUPart(transIdHex);
        apdu.LE = "04";

        return apdu;
    }

    private String GetStartChargeAuthDataAPDUPart(String transIdHex) {

        StartChargeAuthDataAPDUPart data = new StartChargeAuthDataAPDUPart();

        data.LengthInitVector = "0000000C";
        data.InitVector = "000000000000000000000000";
        data.ChargerId = "AABBCCDD";
        data.Reserved1 = "0000";
        data.UserId = "0001";
        //data.PhoneTime = "00000000";
        data.PhoneTime = GetPhoneTime();
        data.ChargeTime = "0064";
        //data.KWH = "0000";
        data.KWH = GetCurrentAvailableEnergy();
        data.CurrentTableIndex = "01";
        data.Reserved2 = "00";
        data.TransId = transIdHex;
        data.TagICV = "00000000000000000000000000000000";

        return data.GetStartChargeAuthDataAPDUPart();
    }

    private String GetCurrentAvailableEnergy() {

        int energyToSendToCharger = 0;

        if (EnergyLeft >= 80d)
            energyToSendToCharger = 800;
        else if (EnergyLeft < 80 && EnergyLeft >= 0) {
            if (EnergyLeft + (-1 * MinimumEnergy) >= 80d)
                energyToSendToCharger = 800;
            else {
                Double energyToSend = EnergyLeft + (-1 * MinimumEnergy);
                energyToSendToCharger = energyToSend.intValue() * 10;
            }
        } else if (EnergyLeft < 0) {
            if ((MinimumEnergy * -1) - (-1 * EnergyLeft) > 80d)
                energyToSendToCharger = 800;
            else {
                Double energyToSend = (MinimumEnergy * -1) - (-1 * EnergyLeft);
                energyToSendToCharger = energyToSend.intValue() * 10;
            }
        }

        EnergySentToCharger = energyToSendToCharger;

        String hexString = Integer.toHexString(energyToSendToCharger);

        String hexStringPadded = hexString;

        while (hexStringPadded.length() < 4) {

            hexStringPadded = "0" + hexStringPadded;
        }

        return hexStringPadded;
    }

    private String GetPhoneTime() {
        Date currentTime = Calendar.getInstance().getTime();
        long unixTime = currentTime.getTime() / 1000;
        String hexString = Long.toHexString(unixTime);
        return hexString;
    }

    private APDU BuildStartChargeSelectAPDU() {

        //Example : 00A4040007 FF000100010000 08

        APDU apdu = new APDU();
        apdu.CLA = "00";
        apdu.INS = "A4";
        apdu.P1 = "04";
        apdu.P2 = "00";
        apdu.LC = "07";
        apdu.Data = GetStartChargeSelectDataAPDUPart();
        apdu.LE = "08";

        return apdu;
    }

    private String GetStartChargeSelectDataAPDUPart() {

        //Example : FF000100010000

        StartChargeSelectDataAPDUPart data = new StartChargeSelectDataAPDUPart();

        data.Category = "FF";
        data.AID = "0001";
        data.Version = "0001";
        data.Reserved1 = "0000";

        return data.GetStartChargeSelectDataAPDUPart();
    }

    public static String ByteArrayToHexString(byte[] bytes) {
        final char[] hexArray = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
        int v;
        for (int j = 0; j < bytes.length; j++) {
            v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
            hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
            hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
        }
        return new String(hexChars);
    }

    public static byte[] HexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    private APDU BuildEndChargeAuthAPDU(String transIdHex) {

        APDU apdu = new APDU();
        apdu.CLA = "00";
        apdu.INS = "86";
        apdu.P1 = "00";
        apdu.P2 = "00";
        apdu.LC = "34"; //52 in plain
        apdu.Data = GetEndChargeAuthDataAPDUPart(transIdHex);
        apdu.LE = "04";

        return apdu;
    }

    private String GetEndChargeAuthDataAPDUPart(String transIdHex) {

        EndChargeAuthDataAPDUPart data = new EndChargeAuthDataAPDUPart();

        data.LengthInitVector = "0000000C";
        data.InitVector = "000000000000000000000000";
        data.ChargerId = "AABBCCDD";
        data.Reserved1 = "0000";
        data.UserId = "0001";
        data.PhoneTime = "00000000";
        data.ChargeTime = "0000";
        data.KWH = "0000";
        data.CurrentTableIndex = "00";
        data.Reserved2 = "00";
        data.TransId = transIdHex;
        data.TagICV = "00000000000000000000000000000000";

        return data.GetEndChargeAuthDataAPDUPart();
    }

    private void CheckConnectionToApiServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "EVCTest";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // display response
                        IsConnectedToApiServer = true;
                        try {
                            callBack.call();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        IsConnectedToApiServer = false;

                        ToastMessage = "Not Connected";
                        Toast();

                        if (IsCharging())
                            DisplayChargingControls();
                        else
                            DisplayBalanceControls();
                    }
                }
        );
        queue.add(request);
    }


    private void VerifyClientCredentialsOnServerAsync(final Callable<Void> callBack) {
        final String tokenUrl = serverUrl + "Token";
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, tokenUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    try {
                        JSONObject res = new JSONObject(response);
                        Token = res.getString("access_token");
                        try {
                            callBack.call();

                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                if (EnergyLeft == 0d) {
                    String message = "";
                    if (volleyError instanceof NetworkError) {
                        message = "Cannot connect to the internet. Please check your connection.";
                    } else if (volleyError instanceof ClientError) {
                        message = "Authentication failed";
                    } else if (volleyError instanceof ServerError) {
                        message = "The server could not be found. Please try again after some time.";
                    } else if (volleyError instanceof AuthFailureError) {
                        message = "Cannot connect to the internet. Please check your connection.";
                    } else if (volleyError instanceof ParseError) {
                        message = "Parsing error. Please try again after some time.";
                    } else if (volleyError instanceof NoConnectionError) {
                        message = "Cannot connect to the internet. Please check your connection.";
                    } else if (volleyError instanceof TimeoutError) {
                        message = "Connection time out. Please check your internet connection.";
                    } else {
                        message = "Authentication failed";
                    }
                    SetMyText("", "", "", message, "");
                    //DisplayLoginControls();
                }
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }

            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", CacheData.Username);
                params.put("password", CacheData.Password);
                params.put("grant_type", "password");
                return params;
            }
        };
        queue.add(request);
    }

    private void GetSiteAdminContactNumberFromServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "GetSiteAdminContactNumber";
        final String token = this.Token;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    try {
                        JSONObject res = new JSONObject(response);
                        String siteAdminContactNumber = res.getString("Message");
                        SiteAdminContactNumber = siteAdminContactNumber;
                        try {
                            callBack.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        queue.add(request);
    }


    private void GetEnergyBalanceFromServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "GetEnergyLeft";
        final String token = this.Token;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    try {
                        JSONObject res = new JSONObject(response);
                        String energyWithComma = res.getString("EnergyBalance");
                        String energyWithDecimalPoint = energyWithComma.replace(',', '.');
                        EnergyLeft = Double.parseDouble(energyWithDecimalPoint);
                        ErrorMessage = res.getString("Message");
                        try {
                            callBack.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        queue.add(request);
    }

    private void IsClientActiveOnServerAsync(final Callable<Void> callBack) {
        final String url = serverUrl + "IsClientActive";
        final String token = this.Token;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    try {
                        JSONObject res = new JSONObject(response);
                        String isActiveMessage = res.getString("Message");

                        if (isActiveMessage.equals("Active"))
                            IsClientActive = true;
                        else
                            IsClientActive = false;
                        try {
                            callBack.call();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Log.e("Your Array Response", "Data Null");
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        queue.add(request);
    }

    private void WebApiPostEnergyLeft(final double energy) {
        final String url = serverUrl + "UpdateClientEnergyBalance?newBalance=" + energy;
        final String token = Token;
        RequestQueue queue = Volley.newRequestQueue(this);
        StringRequest request = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (!response.equals(null)) {
                    //TODO Remove when testing is complete
                    //Toast toast = Toast.makeText(getApplicationContext(), "Posted to server", Toast.LENGTH_SHORT);
                    //toast.show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("error is ", "" + error);
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Authorization", "Bearer " + token);
                return params;
            }
        };
        queue.add(request);
    }
}