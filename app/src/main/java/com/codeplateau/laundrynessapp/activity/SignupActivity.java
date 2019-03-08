package com.codeplateau.laundrynessapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.codeplateau.laundrynessapp.R;
import com.codeplateau.laundrynessapp.app.AppConfig;
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

public class SignupActivity extends AppCompatActivity {


    public NetworkInfo networkInfo;

    private static TextView tv_login;
    private static EditText ed_name, ed_emailid, ed_password, ed_re_enter_password, ed_mobileno;
    private static Button btn_submit;

    private static TextView tvOtpTimer;
    private static EditText edt1, edt2, edt3, edt4;
    private static Button btn_resendOTP;

    private static LinearLayout ll_timer;
    private static ScrollView ScrollView_register;
    private static RelativeLayout otp_relative_layout;

    private ProgressDialog pDialog;
    private Gson gson;

    private static String name,email,mobileno,password,confirmpassword;
    private String otp;
    public int counter = 30;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        isOnline();
        initViews();
        setListener();
    }

    private void initViews() {

        tv_login = (TextView) findViewById(R.id.tv_login);

        ed_name = (EditText) findViewById(R.id.ed_name);
        ed_emailid = (EditText) findViewById(R.id.ed_emailid);
        ed_password = (EditText) findViewById(R.id.ed_password);
        ed_re_enter_password = (EditText) findViewById(R.id.ed_re_enter_password);
        ed_mobileno = (EditText) findViewById(R.id.ed_mobileno);

        btn_submit = (Button) findViewById(R.id.btn_submit);

        edt1 = (EditText) findViewById(R.id.edt1);
        edt2 = (EditText) findViewById(R.id.edt2);
        edt3 = (EditText) findViewById(R.id.edt3);
        edt4 = (EditText) findViewById(R.id.edt4);

        btn_resendOTP = (Button) findViewById(R.id.btn_resendOTP);

        tvOtpTimer = (TextView) findViewById(R.id.tvOtpTimer);
        ll_timer = (LinearLayout) findViewById(R.id.ll_timer);

        otp_relative_layout = (RelativeLayout) findViewById(R.id.OtpRelativeLayout);
        ScrollView_register = (ScrollView) findViewById(R.id.ScrollView_register);

        edt1.addTextChangedListener(new GenericTextWatcher(edt1));
        edt2.addTextChangedListener(new GenericTextWatcher(edt2));
        edt3.addTextChangedListener(new GenericTextWatcher(edt3));
        edt4.addTextChangedListener(new GenericTextWatcher(edt4));
    }

    private void setListener() {

        tv_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

        btn_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                name = ed_name.getText().toString().trim();
                email = ed_emailid.getText().toString().trim();
                mobileno = ed_mobileno.getText().toString();
                password = ed_password.getText().toString().trim();
                confirmpassword = ed_re_enter_password.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty() && !confirmpassword.isEmpty() && !name.isEmpty() && !mobileno.isEmpty()) {

                    if (!isEmailValid(email)) {
                        Toast.makeText(getApplicationContext(), "This email address is not valid!!", Toast.LENGTH_LONG).show();
                    } else {

                        if (mobileno.length() == 10 && (mobileno.startsWith("9") || mobileno.startsWith("8") || mobileno.startsWith("7"))) {
                            if (password.length() > 5) {
                                if (password.equals(confirmpassword)) {
                                    SendOTP_Response(email, mobileno);
                                } else {
                                    Toast.makeText(getApplicationContext(), "Password Do Not Match !", Toast.LENGTH_LONG).show();
                                }

                            } else {

                                Toast.makeText(getApplicationContext(), "Password Length Must Be Atleast 6 Digit", Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "Invalid Mobile No !!", Toast.LENGTH_LONG).show();
                        }

                    }
                } else {
                    Toast.makeText(getApplicationContext(), "Please enter your all details!", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    private void SendOTP_Response(final String email, final String mobile) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();
        RetroAPI retApi = adapter.create(RetroAPI.class);

        retApi.sendOTP(email, mobile, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String responseJson = getStringResponse(response);
                hidepDialog();
                handle_OTP_Response(responseJson, email, mobile);
                System.out.println("test response ===" + responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                hidepDialog();
                System.out.println("test error ==" + error);
                Toast toast = Toast.makeText(getApplicationContext(), "Server error occured", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                        0, 0);
                toast.show();
            }
        });
    }

    private void handle_OTP_Response(String responseJson, final String email, final String mobile) {

        if (responseJson != null) {

            try {

                JSONObject Jobj = new JSONObject(responseJson);
                String status = Jobj.getString("status");
                String message = Jobj.getString("message");

                if (status.equals("success")) {

                    otp = Jobj.getString("otp");

                    ScrollView_register.setVisibility(View.GONE);
                    otp_relative_layout.setVisibility(View.VISIBLE);

                    counter = 30;

                    ll_timer.setVisibility(View.VISIBLE);
                    btn_resendOTP.setVisibility(View.GONE);

                    ed_mobileno.setText(mobile);

                    OTPTimer();

                    btn_resendOTP.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            SendOTP_Response(email, mobile);
                            ClearOTPCode();
                        }
                    });

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                } else {

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {

        }
    }

    private void ClearOTPCode() {

        edt1.setText("");
        edt2.setText("");
        edt3.setText("");
        edt4.setText("");

        edt1.requestFocus();
    }

    public void OTPTimer() {

        new CountDownTimer(30000, 1000) {
            public void onTick(long millisUntilFinished) {
                tvOtpTimer.setVisibility(View.VISIBLE);
                tvOtpTimer.setText(String.valueOf(counter));
                counter--;
            }

            public void onFinish() {
                //tvOtpTimer.setText("FINISH!!");
                ll_timer.setVisibility(View.GONE);
                btn_resendOTP.setVisibility(View.VISIBLE);
            }
        }.start();
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

    public class GenericTextWatcher implements TextWatcher {
        private View view;

        private GenericTextWatcher(View view) {
            this.view = view;
        }

        @Override
        public void afterTextChanged(Editable editable) {


            // TODO Auto-generated method stub
            String text = editable.toString();
            switch (view.getId()) {
                case R.id.edt1:
                    if (text.length() == 1)
                        edt2.requestFocus();
                    break;
                case R.id.edt2:
                    if (text.length() == 1)
                        edt3.requestFocus();
                    break;
                case R.id.edt3:
                    if (text.length() == 1)
                        edt4.requestFocus();
                    break;
                case R.id.edt4:
                    if (text.length() == 1)
                        edt4.requestFocus();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // Actions to do after 5 seconds
                        }
                    }, 5000);

                    String currentOTP = getOTP();

                    if (currentOTP.length() == 4) {
                        // Verify OTP
                        validate_otp(currentOTP);
                    }
                    break;
            }
        }

        @Override
        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
            // TODO Auto-generated method stub
        }
    }

    private boolean isEmailValid(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z0-9]+\\.+[a-z]+";
        if (!email.toString().trim().matches(emailPattern))
            return false;
        else
            return true;
    }

    private String getOTP() {
        String ed1 = edt1.getText().toString();
        String ed2 = edt2.getText().toString();
        String ed3 = edt3.getText().toString();
        String ed4 = edt4.getText().toString();

        otp = ed1 + ed2 + ed3 + ed4;
        return otp;
    }

    private void validate_otp(String currentOTP) {

        if (currentOTP.equals(otp)) {

            Toast.makeText(this, "OTP Verified Successfully.", Toast.LENGTH_SHORT).show();

            signup_Response(email, password, name, mobileno);
            Intent androidsolved_intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(androidsolved_intent);

        } else {

            Toast.makeText(this, "Invalid OTP !", Toast.LENGTH_SHORT).show();
        }
    }

    private void signup_Response(final String email, final String password, final String username, final String mobileno) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();
        gson = new GsonBuilder().setPrettyPrinting().create();

        RetroAPI retApi = adapter.create(RetroAPI.class);

        retApi.url_signup(email, password, username, mobileno, new Callback<Response>() {
            @Override
            public void success(retrofit.client.Response response, retrofit.client.Response response2) {
                String responseJson = getStringResponse(response);
                hidepDialog();

                System.out.println("test response ===" + responseJson);
                handle_Register_Response(responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                Log.d("TEST1", error.toString());
                System.out.println("test error ==" + error);
                hidepDialog();

                Toast.makeText(SignupActivity.this, "Server error occured", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handle_Register_Response(String responseJson) {

        if (responseJson != null) {

            try {

                JSONObject Jobj = new JSONObject(responseJson);

                String status = Jobj.getString("status");
                String message = Jobj.getString("Message");

                if (status.equals("Success")) {

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

                    Intent androidsolved_intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(androidsolved_intent);
                    finish();
                } else {

                    Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            finish();
        }
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
}
