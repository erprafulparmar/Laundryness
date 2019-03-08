package com.codeplateau.laundrynessapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codeplateau.laundrynessapp.R;
import com.codeplateau.laundrynessapp.app.AppConfig;
import com.codeplateau.laundrynessapp.app.RetroAPI;
import com.codeplateau.laundrynessapp.app.TrustingOkClient;
import com.codeplateau.laundrynessapp.model.Cart_Model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import retrofit.Callback;
import retrofit.RestAdapter;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class ReviewBasketActivity extends AppCompatActivity {

    private ProgressDialog pDialog;

    private RecyclerView rc_review_basket;
    private LinearLayout ll_review_basket_no_record;

    private static TextView txt_totalprice;
    private static LinearLayout ll_next;

    SharedPreferences pref;
    public String user_id = "", cart_data = "";

    private static LinearLayoutManager mLayoutManager_ReviewBasket;

    ArrayList<Cart_Model> cartList_modelArray;
    ReviewBasket_Adapter reviewBasket_adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reviewbasket);

        initViews();
        setListener();
    }

    private void initViews(){

        pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
        user_id = pref.getString("userid", "");
        cart_data = pref.getString("cart_data", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        ImageView img_back = (ImageView) toolbar.findViewById(R.id.img_back);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        rc_review_basket = (RecyclerView) findViewById(R.id.rc_review_basket);
        ll_review_basket_no_record = (LinearLayout) findViewById(R.id.ll_review_basket_no_record);

        txt_totalprice = (TextView) findViewById(R.id.txt_totalprice);
        ll_next = (LinearLayout) findViewById(R.id.ll_next);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(ReviewBasketActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setListener(){

        mLayoutManager_ReviewBasket = new LinearLayoutManager(getApplicationContext());
        rc_review_basket.setHasFixedSize(false);
        mLayoutManager_ReviewBasket.setOrientation(LinearLayoutManager.VERTICAL);
        rc_review_basket.setLayoutManager(mLayoutManager_ReviewBasket);

        CartListService(user_id, cart_data);
    }

    public void CartListService(String user_id, String cart_data) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();

        RetroAPI retApi = adapter.create(RetroAPI.class);
        retApi.url_get_cartlist(user_id, cart_data, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String responseJson = getStringResponse(response);
                hideDialog();

                handle_cartlist_response(responseJson);
                System.out.println("response ===" + responseJson);
            }

            @Override
            public void failure(RetrofitError error) {
                hideDialog();
                System.out.println("test error ==" + error);
                Toast toast = Toast.makeText(getApplicationContext(), "Server error occured", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                        0, 0);
                toast.show();
            }
        });
    }

    private void handle_cartlist_response(String responseJson) {

        if (responseJson != null) {

            try {

                cartList_modelArray = new ArrayList<Cart_Model>();
                cartList_modelArray.clear();

                JSONObject jObj = new JSONObject(responseJson);

                String count = jObj.optString("count");
                String totalbill = jObj.optString("Total_Bill");
                txt_totalprice.setText(totalbill);

                JSONArray jsonArray = jObj.getJSONArray("Result");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Cart_Model cart_model = new Cart_Model();

                    cart_model.setService_id(jsonObject.optString("service_id"));
                    cart_model.setCategory_id(jsonObject.optString("category_id"));
                    cart_model.setSubcategory_id(jsonObject.optString("subcategory_id"));
                    cart_model.setPrice(jsonObject.optString("price"));
                    cart_model.setQty(jsonObject.optString("qty"));

                    JSONObject servicejsonobject=jsonObject.getJSONObject("service");
                    cart_model.setService_name(servicejsonobject.getString("service_name"));

                    JSONObject subcategoryjsonobject=jsonObject.getJSONObject("subcategory");
                    cart_model.setSubcategory_name(subcategoryjsonobject.getString("subcategory_name"));

                    JSONObject categorysjsonobject=jsonObject.getJSONObject("categorys");
                    cart_model.setCategory_name(categorysjsonobject.getString("category_name"));

                    cartList_modelArray.add(cart_model);
                }

                if (!cartList_modelArray.isEmpty()) {

                    rc_review_basket.setVisibility(View.VISIBLE);
                    ll_review_basket_no_record.setVisibility(View.GONE);

                    reviewBasket_adapter = new ReviewBasket_Adapter(getApplicationContext(), cartList_modelArray);
                    rc_review_basket.setAdapter(reviewBasket_adapter);

                } else {

                    ll_review_basket_no_record.setVisibility(View.VISIBLE);
                    rc_review_basket.setVisibility(View.GONE);
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public class ReviewBasket_Adapter extends RecyclerView.Adapter<ReviewBasket_Adapter.ViewHolder> {
        private ArrayList<Cart_Model> cart_modelArrayList;
        private Context context;

        public ReviewBasket_Adapter(Context context, ArrayList<Cart_Model> cart_modelArrayList) {
            this.cart_modelArrayList = cart_modelArrayList;
            this.context = context;
        }

        @Override
        public ReviewBasket_Adapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view;
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_reviewbasket, viewGroup, false);

            return new ReviewBasket_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final ReviewBasket_Adapter.ViewHolder viewHolder, final int position) {

            viewHolder.tv_categoryname.setText(cart_modelArrayList.get(position).getSubcategory_name());
            viewHolder.tv_price.setText(cart_modelArrayList.get(position).getPrice());

            viewHolder.tv_qty.setText(cart_modelArrayList.get(position).getQty());

            viewHolder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String qty = ((TextView) rc_review_basket.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_qty)).getText().toString();

                    String service_id = cart_modelArrayList.get(position).getService_id();
                    String category_id = cart_modelArrayList.get(position).getCategory_id();
                    String subcategory_id = cart_modelArrayList.get(position).getSubcategory_id();
                    String price = cart_modelArrayList.get(position).getPrice();

                    if (qty.equals("0")) {

                        qty = "1";
                        Add_subCategory_API(service_id, category_id, subcategory_id, price, qty, user_id, cart_data, viewHolder);

                    } else {

                        qty = String.valueOf(Integer.valueOf(qty) + 1);
                        Add_subCategory_API(service_id, category_id, subcategory_id, price, qty, user_id, cart_data, viewHolder);

                    }
                }
            });

            viewHolder.btn_minus.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String qty = ((TextView) rc_review_basket.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_qty)).getText().toString();

                    if (!qty.equals("0")) {

                        String service_id = cart_modelArrayList.get(position).getService_id();
                        String category_id = cart_modelArrayList.get(position).getCategory_id();
                        String subcategory_id = cart_modelArrayList.get(position).getSubcategory_id();
                        String price = cart_modelArrayList.get(position).getPrice();

                        int int_qty = Integer.valueOf(qty);

                        if (int_qty > 0) {

                            qty = String.valueOf(Integer.valueOf(qty) - 1);
                            Add_subCategory_API(service_id, category_id, subcategory_id, price, qty, user_id, cart_data, viewHolder);

                        } else {

                            viewHolder.tv_qty.setText("0");
                        }
                    }
                }
            });
        }

        @Override
        public int getItemCount() {

            return cart_modelArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public TextView tv_categoryname, tv_price, tv_qty;
            public RelativeLayout btn_add, btn_minus;

            public ViewHolder(View view) {
                super(view);

                tv_categoryname = (TextView) view.findViewById(R.id.tv_categoryname);
                tv_price = (TextView) view.findViewById(R.id.tv_price);
                tv_qty = (TextView) view.findViewById(R.id.tv_qty);

                btn_add = (RelativeLayout) view.findViewById(R.id.btn_add);
                btn_minus = (RelativeLayout) view.findViewById(R.id.btn_minus);
            }
        }

        private void Add_subCategory_API(final String service_id, final String category_id, final String subcategory_id, final String price, final String qty, final String user_id, final String cart_id, final ReviewBasket_Adapter.ViewHolder viewHolder) {
            showpDialog();

            TrustingOkClient ok = new TrustingOkClient();
            RestAdapter adapter = new RestAdapter.Builder()
                    .setEndpoint(AppConfig.url_domain)
                    .setClient(ok)
                    .build();

            RetroAPI retApi = adapter.create(RetroAPI.class);
            retApi.url_card(service_id, category_id, subcategory_id, price, qty, user_id, cart_id, new Callback<Response>() {
                @Override
                public void success(Response response, Response response2) {
                    String responseJson = getStringResponse(response);
                    hideDialog();

                    handle_add_subcategory_response(responseJson, service_id, viewHolder);
                    System.out.println("response ===" + responseJson);
                }

                @Override
                public void failure(RetrofitError error) {
                    hideDialog();
                    System.out.println("test error ==" + error);
                    Toast toast = Toast.makeText(context, "Server error occured", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
                            0, 0);
                    toast.show();
                }
            });
        }

        private void handle_add_subcategory_response(String responseJson, String service_id, final ReviewBasket_Adapter.ViewHolder viewHolder) {
            if (responseJson != null) {

                try {

                    JSONObject jObj = new JSONObject(responseJson);
                    String success = jObj.getString("status");

                    if (success.equals("Success")) {

                        JSONObject jsonObject = jObj.getJSONObject("Result");

                        String qty = jsonObject.getString("qty");
                        String subcategory_id = jsonObject.getString("subcategory_id");
                        String cart_id = jsonObject.getString("id");

                        viewHolder.tv_qty.setText(qty);

                        //viewHolder.tv_qty.setText(qty);

                        //GetAllSubCategory_API(service_id);

                        CartListService(user_id, cart_data);

                    } else {

                        String message = jObj.getString("message");
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();

                        hideDialog();
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Toast.makeText(context, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
            }
        }

        private String getStringResponse(Response result) {

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
    }

    private String getStringResponse(Response result) {

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

    public void showpDialog() {
        // Progress Dialog
        pDialog = new ProgressDialog(ReviewBasketActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        if (!pDialog.isShowing())
            pDialog.show();
    }

    public void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
    @Override
    public void onBackPressed() {

        Intent intent = new Intent(getApplicationContext(), HomeActivity.class);
        startActivity(intent);
        finish();
    }
}
