package com.codeplateau.laundrynessapp.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.codeplateau.laundrynessapp.R;
import com.codeplateau.laundrynessapp.app.AppConfig;

public class SplashScreenActivity extends AppCompatActivity {

    SharedPreferences sharedPreferences;
    String userid = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        sharedPreferences = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
        userid = sharedPreferences.getString("userid", "");

        new Handler().postDelayed(new Runnable() {
            /*
             * Showing splash screen with a timer. This will be useful when you
             * want to show case your app logo / company
             */
            @Override
            public void run() {
                // This method will be executed once the timer is over
                // Start your app main activity

                if(userid.equals("")){

                    Intent intent = new Intent(SplashScreenActivity.this, LoginActivity.class);
                    startActivity(intent);

                } else {

                    Intent intent = new Intent(SplashScreenActivity.this, HomeActivity.class);
                    startActivity(intent);
                }

                // close this activity
                finish();
            }
        }, 3000);
    }
}
