package com.codeplateau.laundrynessapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.codeplateau.laundrynessapp.R;
import com.codeplateau.laundrynessapp.app.AppConfig;
import com.codeplateau.laundrynessapp.app.CheckNetwork;
import com.codeplateau.laundrynessapp.app.RetroAPI;
import com.codeplateau.laundrynessapp.app.TrustingOkClient;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

import static android.Manifest.permission.ACCESS_NETWORK_STATE;
import static android.Manifest.permission.ACCESS_WIFI_STATE;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.INTERNET;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.READ_SMS;
import static android.Manifest.permission.RECEIVE_SMS;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class LoginActivity extends AppCompatActivity {

    public NetworkInfo networkInfo;
    private static final int REQUEST_PERMISSION = 12;

    private static EditText txt_username, txt_password;
    private static TextView txt_signup, txt_forgotpassword;
    private static Button btn_login;

    private ProgressDialog pDialog;
    SharedPreferences pref;
    boolean doubleBackToExitPressedOnce = false;

    private String email, password;
    private String TAG = LoginActivity.class.getSimpleName();
    private Gson gson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions();
        }

        isOnline();
        initViews();
        setListener();
    }

    private void initViews() {

        txt_username = (EditText) findViewById(R.id.txt_username);
        txt_password = (EditText) findViewById(R.id.ed_password);

        txt_signup = (TextView) findViewById(R.id.txt_signup);
        txt_forgotpassword = (TextView) findViewById(R.id.txt_forgotpassword);

        btn_login = (Button) findViewById(R.id.btn_login);
    }

    private void setListener() {

        pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);

        txt_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(intent);
            }
        });

        txt_forgotpassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email = txt_username.getText().toString().trim();
                password = txt_password.getText().toString().trim();

                if (CheckNetwork.isInternetAvailable(LoginActivity.this)) {

                    boolean numeric = true;

                    try {
                        Double number = Double.parseDouble(txt_username.getText().toString());
                    } catch (NumberFormatException e) {
                        numeric = false;
                    }

                    if (!numeric) {

                        if (email.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please enter the email address!!", Toast.LENGTH_LONG).show();
                        } else if (!isEmailValid(email)) {
                            Toast.makeText(getApplicationContext(), "This email address is not valid!!", Toast.LENGTH_LONG).show();
                        } else if (password.isEmpty()) {
                            Toast.makeText(getApplicationContext(), "Please enter your Password", Toast.LENGTH_LONG).show();
                        } else {
                            Login_Email_Response(email, password);
                        }
                    } else {
                        String mobileno = email;
                        Login_Mobile_Response(mobileno, password);
                    }

                } else {

                    showToastMessage(3000);
                }
            }
        });
    }

    private void Login_Email_Response(final String email, final String password) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();
        gson = new GsonBuilder().setPrettyPrinting().create();

        RetroAPI restApi = adapter.create(RetroAPI.class);

        restApi.url_loginuser_email(email, password, new Callback<Response>() {
            @Override
            public void success(retrofit.client.Response response, retrofit.client.Response response2) {
                String responseJson = getStringResponse(response);
                hidepDialog();

                System.out.println("test response ===" + responseJson);
                handle_LoginResponse(responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, error.toString());
                System.out.println("test error ==" + error);

                hidepDialog();
                Toast.makeText(LoginActivity.this, "Server error occured", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void Login_Mobile_Response(final String mobileno, final String password) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();
        gson = new GsonBuilder().setPrettyPrinting().create();

        RetroAPI restApi = adapter.create(RetroAPI.class);

        restApi.url_loginuser_mobileno(mobileno, password, new Callback<Response>() {
            @Override
            public void success(retrofit.client.Response response, retrofit.client.Response response2) {
                String responseJson = getStringResponse(response);
                hidepDialog();

                System.out.println("test response ===" + responseJson);
                handle_LoginResponse(responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d(TAG, error.toString());
                System.out.println("test error ==" + error);

                hidepDialog();
                Toast.makeText(LoginActivity.this, "Server error occured", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void handle_LoginResponse(String responseJson) {

        if (responseJson != null) {

            try {

                JSONObject Jobj = new JSONObject(responseJson);

                String status = Jobj.getString("status");
                String Message = Jobj.getString("Message");

                if (status.equals("Success")) {

                    String cart_data = Jobj.getString("cart_data");

                    JSONObject jObject = Jobj.getJSONObject("Results");

                    String userid = jObject.getString("id");
                    String firstname = jObject.getString("firstname");
                    String lastname = jObject.getString("lastname");
                    String middlename = jObject.getString("middlename");
                    String address1 = jObject.getString("address1");
                    String address2 = jObject.getString("address2");
                    String city = jObject.getString("city");
                    String email = jObject.getString("email");
                    String mobileno = jObject.getString("mobileno");
                    String profile_image = jObject.getString("profile_image");
                    String user_role = jObject.getString("user_role");
                    String pan_number = jObject.getString("pan_number");
                    String adhar_number = jObject.getString("adhar_number");

                    pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = pref.edit();
                    editor.putString("userid", userid);
                    editor.putString("firstname", firstname);
                    editor.putString("lastname", lastname);
                    editor.putString("middlename", middlename);
                    editor.putString("address1", address1);
                    editor.putString("address2", address2);
                    editor.putString("city", city);
                    editor.putString("email", email);
                    editor.putString("mobileno", mobileno);
                    editor.putString("profile_image", profile_image);
                    editor.putString("user_role", user_role);
                    editor.putString("pan_number", pan_number);
                    editor.putString("adhar_number", adhar_number);
                    editor.putString("cart_data", cart_data);

                    editor.apply();
                    editor.commit();

                    Intent intent_HomeActivity = new Intent(getApplicationContext(), HomeActivity.class);
                    startActivity(intent_HomeActivity);

                } else {

                    Toast toast = Toast.makeText(getApplicationContext(), Message, Toast.LENGTH_LONG);
                    toast.show();

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private String getStringResponse(retrofit.client.Response result) {

        //Try to get response body
        BufferedReader reader = null;
        StringBuilder sb = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(result.getBody().in()));
            String line;
            try {
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String response = sb.toString();

        return response;
    }

    private boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z0-9]+\\.+[a-z]+";
        if (!email.toString().trim().matches(emailPattern))
            return false;
        else
            return true;
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(LoginActivity.this, new String[]{
                INTERNET
                , ACCESS_NETWORK_STATE
                , ACCESS_WIFI_STATE
                , CAMERA
                , WRITE_EXTERNAL_STORAGE
                , READ_EXTERNAL_STORAGE
                , READ_SMS
                , RECEIVE_SMS
        }, REQUEST_PERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION:
                if (!verifyPermission()) {
                    showPermissionAlert();
                }
                break;
        }
    }


    private void showPermissionAlert() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        requestPermissions();
                        dialog.dismiss();
                        break;
                    case DialogInterface.BUTTON_NEGATIVE:
                        dialog.dismiss();
                        break;
                }
            }
        };


    }

    private boolean verifyPermission() {
        int IN = ContextCompat.checkSelfPermission(this, INTERNET);
        int AN = ContextCompat.checkSelfPermission(this, ACCESS_NETWORK_STATE);
        int AW = ContextCompat.checkSelfPermission(this, ACCESS_WIFI_STATE);
        int CM = ContextCompat.checkSelfPermission(this, CAMERA);
        int WE = ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE);
        int RE = ContextCompat.checkSelfPermission(this, READ_EXTERNAL_STORAGE);
        int RS = ContextCompat.checkSelfPermission(this, READ_SMS);
        int RES = ContextCompat.checkSelfPermission(this, RECEIVE_SMS);
        return (IN == PackageManager.PERMISSION_GRANTED
                && AN == PackageManager.PERMISSION_GRANTED
                && AW == PackageManager.PERMISSION_GRANTED
                && CM == PackageManager.PERMISSION_GRANTED
                && WE == PackageManager.PERMISSION_GRANTED
                && RE == PackageManager.PERMISSION_GRANTED
                && RS == PackageManager.PERMISSION_GRANTED
                && RES == PackageManager.PERMISSION_GRANTED);
    }

    //If Mobile Data Of Net connection code
    public boolean isOnline() {
        ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        networkInfo = connManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            showToastMessage(3000);
            return false;
        }
    }

    public void showToastMessage(int duration) {
        String message = "No Internet Connection at this moment !!";
        final Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        View view = toast.getView();

        //Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);

        //Gets the TextView from the Toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);

        toast.show();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, duration);
    }

    private void showpDialog() {
        // Progress Dialog
        pDialog = new ProgressDialog(this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hidepDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }

    @Override
    public void onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            //super.onBackPressed();
            finish();
            return;
        }

        this.doubleBackToExitPressedOnce = true;
        Toast.makeText(this, "Press back again to exit!", Toast.LENGTH_SHORT).show();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                doubleBackToExitPressedOnce=false;
            }
        }, 2000);
    }
}
