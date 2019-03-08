package com.codeplateau.laundrynessapp.activity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codeplateau.laundrynessapp.R;
import com.codeplateau.laundrynessapp.app.AppConfig;
import com.codeplateau.laundrynessapp.app.RetroAPI;
import com.codeplateau.laundrynessapp.app.TrustingOkClient;
import com.codeplateau.laundrynessapp.utils.ProgressRotateDialog;
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

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    SharedPreferences pref;
    ProgressRotateDialog progressRotateDialog;
    boolean doubleBackToExitPressedOnce = false;

    private static LinearLayout ll_washonly,ll_irononly,ll_ironandwash,ll_dryclean;
    private static LinearLayout ll_home, ll_profile;
    private static RelativeLayout rel_cartcount;

    private static ImageView img_profile;
    private static TextView txt_username, txt_email, txt_cartcount;

    Gson gson;
    Activity mContext;
    private String user_id, cart_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mContext = this;

        progressRotateDialog = new ProgressRotateDialog(mContext);
        gson = new GsonBuilder().setPrettyPrinting().create();

        pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
        user_id = pref.getString("userid", "");
        cart_id = pref.getString("cart_data", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        setSupportActionBar(toolbar);

        mTitle.setText(toolbar.getTitle());
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        img_profile = (ImageView) findViewById(R.id.img_profile);
        txt_username = (TextView) findViewById(R.id.txt_username);
        txt_email = (TextView) findViewById(R.id.txt_email);

        ll_washonly = (LinearLayout) findViewById(R.id.ll_washonly);
        ll_irononly = (LinearLayout) findViewById(R.id.ll_irononly);
        ll_ironandwash = (LinearLayout) findViewById(R.id.ll_ironandwash);
        ll_dryclean = (LinearLayout) findViewById(R.id.ll_dryclean);

        ll_home = (LinearLayout) findViewById(R.id.ll_home);
        rel_cartcount = (RelativeLayout) findViewById(R.id.rel_cartcount);
        txt_cartcount = (TextView) findViewById(R.id.txt_cartcount);
        ll_profile = (LinearLayout) findViewById(R.id.ll_profile);

        ll_washonly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SubCategoryListActivity.class);
                intent.putExtra("service_id", "1");
                intent.putExtra("title", "Wash Only");
                startActivity(intent);
            }
        });

        ll_irononly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SubCategoryListActivity.class);
                intent.putExtra("service_id", "2");
                intent.putExtra("title", "Iron Only");
                startActivity(intent);
            }
        });

        ll_ironandwash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SubCategoryListActivity.class);
                intent.putExtra("service_id", "3");
                intent.putExtra("title", "Iron & Wash");
                startActivity(intent);
            }
        });

        ll_dryclean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, SubCategoryListActivity.class);
                intent.putExtra("service_id", "4");
                intent.putExtra("title", "Dry Clean");
                startActivity(intent);
                finish();
            }
        });

        ll_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        rel_cartcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, ReviewBasketActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ll_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(HomeActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setupBadge();
    }

    private void setupBadge() {
        BadgeCartList_API(user_id, cart_id);
    }

    public void BadgeCartList_API(String user_id, String cart_id) {
        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();

        RetroAPI retApi = adapter.create(RetroAPI.class);
        retApi.url_get_cartlist(user_id, cart_id, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String responseJson = getStringResponse(response);
                handle_cartlist_responseBadge(responseJson);
                Log.e("BadgeResponse ", "===> " + responseJson);
                System.out.println("response ===" + responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                System.out.println("test error ==" + error);
                Toast toast = Toast.makeText(getApplicationContext(), "Server error occured", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
                toast.show();
            }
        });
    }

    private void handle_cartlist_responseBadge(String responseJson) {
        if (responseJson != null) {
            try {
                JSONObject jObj = new JSONObject(responseJson);
                String count = jObj.optString("count");
                txt_cartcount.setText(count);

            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private String getStringResponse(retrofit.client.Response result) {
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
        return sb.toString();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            //super.onBackPressed();

            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
            Intent intent_HomeActivity= new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(intent_HomeActivity);

        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

            Intent intent = new Intent(getApplicationContext(), ReviewBasketActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_logout) {

            pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = pref.edit();
            editor.clear();
            editor.apply();
            editor.commit();

            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivity(intent);
            finish();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }
}
