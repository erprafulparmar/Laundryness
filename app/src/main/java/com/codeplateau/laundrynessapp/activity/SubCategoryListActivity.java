package com.codeplateau.laundrynessapp.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
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
import com.codeplateau.laundrynessapp.model.Category_Model;
import com.codeplateau.laundrynessapp.model.SubCategoryList_Model;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

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

public class SubCategoryListActivity extends AppCompatActivity {

    private ProgressDialog pDialog;

    private RecyclerView rc_category_list, rc_subcategory_list;
    private LinearLayout ll_category_no_record, ll_subcategory_no_record;

    private static LinearLayoutManager mLayoutManager_Category, mLayoutManager_SubCategory;
    private static LinearLayout ll_home, ll_profile;
    private static RelativeLayout rel_cartcount;
    private static TextView txt_cartcount;

    public String user_id = "", service_id = "", title = "", cart_data = "";

    ArrayList<Category_Model> category_modelArrayList;
    ArrayList<SubCategoryList_Model> subCategory_List_modelArrayList;
    ArrayList<Cart_Model> cartList_modelArray;
    private Gson gson;

    Category_Adapter category_adapter;
    SubCategory_Adapter subCategory_adapter;

    SharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_category_list);

        initViews();
        setListner();
    }

    private void initViews() {

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            service_id = extras.getString("service_id");
            title = extras.getString("title");
        }

        pref = getApplicationContext().getSharedPreferences(AppConfig.pref, Context.MODE_PRIVATE);
        user_id = pref.getString("userid", "");
        cart_data = pref.getString("cart_data", "");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView mTitle = (TextView) toolbar.findViewById(R.id.toolbar_title);
        ImageView img_back = (ImageView) toolbar.findViewById(R.id.img_back);
        setSupportActionBar(toolbar);

        mTitle.setText(title);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(true);

        rc_category_list = (RecyclerView) findViewById(R.id.rc_category_list);
        ll_category_no_record = (LinearLayout) findViewById(R.id.ll_category_no_record);

        rc_subcategory_list = (RecyclerView) findViewById(R.id.rc_subcategory_list);
        ll_subcategory_no_record = (LinearLayout) findViewById(R.id.ll_subcategory_no_record);

        ll_home = (LinearLayout) findViewById(R.id.ll_home);
        rel_cartcount = (RelativeLayout) findViewById(R.id.rel_cartcount);
        ll_profile = (LinearLayout) findViewById(R.id.ll_profile);

        txt_cartcount = (TextView) findViewById(R.id.txt_cartcount);

        img_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SubCategoryListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void setListner() {

        mLayoutManager_Category = new LinearLayoutManager(getApplicationContext());
        rc_category_list.setHasFixedSize(false);
        mLayoutManager_Category.setOrientation(LinearLayoutManager.HORIZONTAL);
        rc_category_list.setLayoutManager(mLayoutManager_Category);

        mLayoutManager_SubCategory = new LinearLayoutManager(getApplicationContext());
        rc_subcategory_list.setHasFixedSize(false);
        mLayoutManager_SubCategory.setOrientation(LinearLayoutManager.VERTICAL);
        rc_subcategory_list.setLayoutManager(mLayoutManager_SubCategory);

        CartListService(user_id, cart_data);

        ll_home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SubCategoryListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        rel_cartcount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SubCategoryListActivity.this, ReviewBasketActivity.class);
                startActivity(intent);
                finish();
            }
        });

        ll_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(SubCategoryListActivity.this, HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        setupBadge();
    }

    private void setupBadge() {
        BadgeCartList_API(user_id, cart_data);
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
                JSONArray jsonArray = jObj.getJSONArray("Result");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Cart_Model cart_model = new Cart_Model();

                    cart_model.setSubcategory_id(jsonObject.optString("subcategory_id"));
                    cart_model.setQty(jsonObject.optString("qty"));
                    cartList_modelArray.add(cart_model);
                }

                CategoryService(service_id);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
        }
    }

    private void CategoryService(String service_id) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();

        RetroAPI retApi = adapter.create(RetroAPI.class);
        retApi.url_categorylist(service_id, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String responseJson = getStringResponse(response);
                hideDialog();

                handle_category_response(responseJson);
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

    private void handle_category_response(String responseJson) {

        if (responseJson != null) {

            try {

                category_modelArrayList = new ArrayList<Category_Model>();
                category_modelArrayList.clear();

                JSONObject jObj = new JSONObject(responseJson);
                JSONArray jsonArray = jObj.getJSONArray("Results");

                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    Category_Model category_model = new Category_Model();

                    category_model.setId(jsonObject.optString("id"));
                    category_model.setService_id(jsonObject.optString("service_id"));
                    category_model.setStatus(jsonObject.optString("status"));
                    category_model.setCategory_name(jsonObject.optString("category_name"));
                    category_model.setCategory_icon(jsonObject.optString("category_icon"));
                    category_modelArrayList.add(category_model);
                }

                if (!category_modelArrayList.isEmpty()) {

                    rc_category_list.setVisibility(View.VISIBLE);
                    ll_category_no_record.setVisibility(View.GONE);

                    category_adapter = new Category_Adapter(getApplicationContext(), category_modelArrayList);
                    rc_category_list.setAdapter(category_adapter);

                } else {

                    ll_category_no_record.setVisibility(View.VISIBLE);
                    rc_category_list.setVisibility(View.GONE);
                }

                String category_id = category_modelArrayList.get(0).getId();
                GetAllSubCategory_API(category_id, cartList_modelArray);

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public class Category_Adapter extends RecyclerView.Adapter<Category_Adapter.ViewHolder> {
        private ArrayList<Category_Model> category_modelArrayList;
        private Context context;

        public Category_Adapter(Context context, ArrayList<Category_Model> category_modelArrayList) {
            this.category_modelArrayList = category_modelArrayList;
            this.context = context;
        }

        @Override
        public Category_Adapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view;
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_category, viewGroup, false);
            return new Category_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final Category_Adapter.ViewHolder viewHolder, final int position) {

            viewHolder.tv_category.setText(category_modelArrayList.get(position).getCategory_name());

            Picasso.with(context)
                    .load(category_modelArrayList.get(position).getCategory_icon())
                    .error(R.drawable.box)
                    .into(viewHolder.img_categoryicon);

            viewHolder.ll_circle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String category_id = category_modelArrayList.get(position).getId();
                    GetAllSubCategory_API(category_id, cartList_modelArray);
                }
            });
        }

        @Override
        public int getItemCount() {

            return category_modelArrayList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            public LinearLayout ll_circle;
            public ImageView img_categoryicon;
            public TextView tv_category;

            public ViewHolder(View view) {
                super(view);

                ll_circle = (LinearLayout) view.findViewById(R.id.ll_circle);
                img_categoryicon = (ImageView) view.findViewById(R.id.img_categoryicon);
                tv_category = (TextView) view.findViewById(R.id.tv_category);
            }
        }
    }

    public void GetAllSubCategory_API(String category_id, final ArrayList<Cart_Model> cartList_modelArray) {

        showpDialog();

        TrustingOkClient ok = new TrustingOkClient();
        RestAdapter adapter = new RestAdapter.Builder()
                .setEndpoint(AppConfig.url_domain)
                .setClient(ok)
                .build();

        RetroAPI retApi = adapter.create(RetroAPI.class);
        retApi.url_get_subcategory(category_id, new Callback<Response>() {
            @Override
            public void success(Response response, Response response2) {
                String responseJson = getStringResponse(response);
                hideDialog();

                handle_subcategory_response(responseJson, cartList_modelArray);
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

    private void handle_subcategory_response(String responseJson, ArrayList<Cart_Model> cartList_modelArray) {

        if (responseJson != null) {

            try {

                subCategory_List_modelArrayList = new ArrayList<SubCategoryList_Model>();
                subCategory_List_modelArrayList.clear();

                JSONObject jObj = new JSONObject(responseJson);
                String success = jObj.getString("status");

                if (success.equals("Success")) {

                    JSONArray jsonArray = jObj.getJSONArray("Results");

                    for (int i = 0; i < jsonArray.length(); i++) {

                        String qty = "0";

                        JSONObject jsonObject = jsonArray.getJSONObject(i);

                        SubCategoryList_Model subCategory_List_model = new SubCategoryList_Model();

                        for (int j = 0; j < cartList_modelArray.size(); j++) {

                            if (cartList_modelArray.get(j).getSubcategory_id().equals(jsonObject.optString("id"))) {

                                qty = cartList_modelArray.get(j).getQty();

                                break;
                            }
                            continue;
                        }

                        subCategory_List_model.setId(jsonObject.optString("id"));
                        subCategory_List_model.setCategory_id(jsonObject.optString("category_id"));
                        subCategory_List_model.setSubcategory_name(jsonObject.optString("subcategory_name"));
                        subCategory_List_model.setPrice(jsonObject.optString("price"));
                        subCategory_List_model.setStatus(jsonObject.optString("status"));
                        subCategory_List_model.setQty(qty);

                        subCategory_List_modelArrayList.add(subCategory_List_model);
                    }

                    if (!subCategory_List_modelArrayList.isEmpty()) {

                        rc_subcategory_list.setVisibility(View.VISIBLE);
                        ll_subcategory_no_record.setVisibility(View.GONE);

                        subCategory_adapter = new SubCategory_Adapter(getApplicationContext(), subCategory_List_modelArrayList);
                        rc_subcategory_list.setAdapter(subCategory_adapter);

                    } else {

                        ll_subcategory_no_record.setVisibility(View.VISIBLE);
                        rc_subcategory_list.setVisibility(View.GONE);
                    }

                } else {

                    String message = jObj.getString("message");
                    Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();

                    hideDialog();
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

        } else {
            Toast.makeText(this, "Response Getting Empty!!!", Toast.LENGTH_SHORT).show();
        }
    }

    public class SubCategory_Adapter extends RecyclerView.Adapter<SubCategory_Adapter.ViewHolder> {
        private ArrayList<SubCategoryList_Model> subcategory_List_modelArrayList;
        private Context context;
        public String TAG = SubCategory_Adapter.class.getSimpleName();
        private ProgressDialog pDialog;

        public SubCategory_Adapter(Context context, ArrayList<SubCategoryList_Model> leads_modelArrayList) {
            this.subcategory_List_modelArrayList = leads_modelArrayList;
            this.context = context;
        }

        @Override
        public SubCategory_Adapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {

            View view;
            view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_subcategory, viewGroup, false);

            return new SubCategory_Adapter.ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final SubCategory_Adapter.ViewHolder viewHolder, final int position) {

            viewHolder.tv_categoryname.setText(subcategory_List_modelArrayList.get(position).getSubcategory_name());
            viewHolder.tv_price.setText(subcategory_List_modelArrayList.get(position).getPrice());

            viewHolder.tv_qty.setText(subcategory_List_modelArrayList.get(position).getQty());

            viewHolder.btn_add.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    String qty = ((TextView) rc_subcategory_list.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_qty)).getText().toString();

                    //String service_id = "1";
                    String category_id = subcategory_List_modelArrayList.get(position).getCategory_id();
                    String subcategory_id = subcategory_List_modelArrayList.get(position).getId();
                    String price = subcategory_List_modelArrayList.get(position).getPrice();
                    //String user_id = "44";
                    //String id = "123456";

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

                    String qty = ((TextView) rc_subcategory_list.findViewHolderForAdapterPosition(position).itemView.findViewById(R.id.tv_qty)).getText().toString();

                    if (!qty.equals("0")) {

                        //String service_id = "4";
                        String category_id = subcategory_List_modelArrayList.get(position).getCategory_id();
                        String subcategory_id = subcategory_List_modelArrayList.get(position).getId();
                        String price = subcategory_List_modelArrayList.get(position).getPrice();
                        //String user_id = "44";
                        //String id = "123456";

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

            return subcategory_List_modelArrayList.size();
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

        private void Add_subCategory_API(final String service_id, final String category_id, final String subcategory_id, final String price, final String qty, final String user_id, final String cart_id, final SubCategory_Adapter.ViewHolder viewHolder) {
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

        private void handle_add_subcategory_response(String responseJson, String service_id, final SubCategory_Adapter.ViewHolder viewHolder) {
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

                        setupBadge();

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
                NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void showpDialog() {
        // Progress Dialog
        pDialog = new ProgressDialog(SubCategoryListActivity.this);
        pDialog.setMessage("Please wait...");
        pDialog.setCancelable(false);

        if (!pDialog.isShowing())
            pDialog.show();
    }

    public void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }
}
