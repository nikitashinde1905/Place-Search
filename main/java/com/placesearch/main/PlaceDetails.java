package com.placesearch.main;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.placesearch.adapter.ViewPagerAdapter;
import com.placesearch.fragments.FavoritesFragment;
import com.placesearch.fragments.Info;
import com.placesearch.fragments.Map;
import com.placesearch.fragments.Photos;
import com.placesearch.fragments.Reviews;
import com.placesearch.fragments.SearchFragment;
import com.placesearch.helper.ProgressDialogManager;
import com.placesearch.helper.ToastManager;
import com.placesearch.storage.StorageManager;
import com.placesearch.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class PlaceDetails extends AppCompatActivity {

    TabLayout tabLayout;
    ViewPager viewPager;
    ToastManager tm;
    ProgressDialogManager pdm;
    JSONObject result;
    static JSONObject placedetails,location;

    static JSONArray photolists,reviewlist,address_components;
    static String placeid,placename,vicinity,website,formatedphonenumber,googlepage,formatted_address;
    static String pricelevel="";
    static double rating;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tm = new ToastManager(this);
        pdm = new ProgressDialogManager(this);
        try{
            if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                result= new JSONObject(extras.getString("PLACE_DETAILS"));

            }else {
                result=  new JSONObject((String) savedInstanceState.getSerializable("PLACE_DETAILS"));
            }
            placeid = result.getString("place_id");
            placename  = result.getString("name");
            vicinity  = result.getString("vicinity");
            pdm.showDialog("Fetching details");
            //String URL = Config.PLACES_DETAILS+"placeid="+placeid+"&key="+Config.API_KEY;
            String URL = Config.PLACES_DETAILS+"placeid="+placeid;
            Log.d("DETAILSURL",URL);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {

                            pdm.dismissDialog();
                            try{
                                Log.d("DETAILSURLRESPOSNE",response);
                                JSONObject resp = new JSONObject(response);
                                if(resp.getString("status").compareToIgnoreCase("OK")==0){
                                    placedetails = new JSONObject(resp.getString("result"));
                                    try{
                                        website = placedetails.getString("website");
                                    }catch(JSONException e){
                                        website="";
                                    }
                                    try{
                                        formatedphonenumber = placedetails.getString("formatted_phone_number");
                                    }catch(JSONException e){
                                        formatedphonenumber="";
                                    }
                                    try{
                                        formatted_address = placedetails.getString("formatted_address");
                                    }catch(JSONException e){
                                        formatted_address="";
                                    }
                                    try{
                                        googlepage = placedetails.getString("url");
                                    }catch(JSONException e){
                                        googlepage="";
                                    }
                                    try{
                                        photolists = placedetails.getJSONArray("photos");
                                    }catch(JSONException e){
                                        photolists= new JSONArray();
                                    }
                                    try{
                                        reviewlist = placedetails.getJSONArray("reviews");
                                    }catch(JSONException e){
                                        reviewlist= new JSONArray();
                                    }
                                    try{
                                        address_components = placedetails.getJSONArray("address_components");
                                    }catch(JSONException e){
                                        address_components = new JSONArray();
                                    }
                                    try{
                                        rating = placedetails.getDouble("rating");
                                    }catch(JSONException e){
                                       rating=0;
                                    }
                                    try{
                                        location = placedetails.getJSONObject("geometry").getJSONObject("location");
                                    }catch(JSONException e){
                                        location = null;
                                    }
                                    setContentView(R.layout.placedetails);
                                    setComponents();
                                    pricelevel = String.valueOf(placedetails.getInt("price_level"));

                                }else{
                                    tm.showToast("No place details found!!", Toast.LENGTH_SHORT);
                                    finish();
                                }

                            }catch(JSONException e) {
                                pricelevel = "";
                                Log.d("Place Details: ", e.getMessage());
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            pdm.dismissDialog();
                            if(error.getMessage().trim().length()>0){
                                tm.showToast(error.getMessage(),Toast.LENGTH_SHORT);
                            }

                        }
                    }
            );
            RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
            requestQueue.add(stringRequest);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setComponents(){

        try{

            Toolbar toolbar = (Toolbar) findViewById(R.id.placedetailstoolbar);
            toolbar.setTitle(result.getString("name"));
            toolbar.setNavigationIcon(R.drawable.backarrow);
            setSupportActionBar(toolbar);
            toolbar.setNavigationOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    finish();
                }
            });

        }catch(Exception e){
            e.printStackTrace();
        }
        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.placedetailstabLayout);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.placedetailspager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Info(), "INFO");
        adapter.addFragment(new Photos(), "PHOTOS");
        adapter.addFragment(new Map(), "MAP");
        adapter.addFragment(new Reviews(), "REVIEWS");
        viewPager.setAdapter(adapter);

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.placedetailstablayout, null, false);

        LinearLayout linearLayout1 = (LinearLayout) headerView.findViewById(R.id.placedetailsll);
        LinearLayout linearLayout2 = (LinearLayout) headerView.findViewById(R.id.placedetailsll2);
        LinearLayout linearLayout3 = (LinearLayout) headerView.findViewById(R.id.placedetailsll3);
        LinearLayout linearLayout4 = (LinearLayout) headerView.findViewById(R.id.placedetailsll4);

        tabLayout.getTabAt(0).setCustomView(linearLayout1);
        tabLayout.getTabAt(1).setCustomView(linearLayout2);
        tabLayout.getTabAt(2).setCustomView(linearLayout3);
        tabLayout.getTabAt(3).setCustomView(linearLayout4);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_placedetails, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        try{
            MenuItem checkable = menu.findItem(R.id.favoriteoption);
            if(result.getBoolean("isliked")){
                checkable.setChecked(true);
                checkable.setIcon(R.drawable.heart_fill_white);
            }else{
                checkable.setChecked(false);
                checkable.setIcon(R.drawable.heart_outline_white);
            }
            return true;

        }catch(Exception e){
         e.printStackTrace();
        }
         return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.shareoption) {
            //share the place content
            String text = "Check out "+placename+" located at "+vicinity + " %0A" +"Website: "+website;
            Intent httpIntent = new Intent(Intent.ACTION_VIEW);
            httpIntent.setData(Uri.parse("https://twitter.com/intent/tweet?text="+text));
            startActivity(httpIntent);
            return true;

        }else if(id == R.id.favoriteoption){
            if(item.isChecked()){

                item.setChecked(false);
                item.setIcon(R.drawable.heart_outline_white);
                try{
                    if( StorageManager.delPlace(placeid,getApplicationContext())){
                        tm.showToast(placename + " was removed from favorites",Toast.LENGTH_SHORT);
                    }

                }catch(Exception e){
                    e.printStackTrace();
                }
                //dis liked the placed. Remove from db;

            }else{
                item.setChecked(true);
                item.setIcon(R.drawable.heart_fill_white);
                try{
                    if( StorageManager.addPlace(placeid,result.put("isliked",true),getApplicationContext())){

                        tm.showToast(placename + " was added to favorites",Toast.LENGTH_SHORT);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
            FavoritesFragment.dataSetChanged();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public static String getVicinity() {
        return vicinity;
    }

    public static String getWebsite() {
        return website;
    }

    public static String getFormatedphonenumber() {
        return formatedphonenumber;
    }

    public static String getGooglepage() {
        return googlepage;
    }

    public static String getPricelevel() {
        return pricelevel;
    }

    public static JSONArray getAddressComponents(){
        return address_components;
    }
    public static String getFormattedAddress(){
        return formatted_address;
    }
    public static String getName(){
        return placename;
    }

    public static double getRating() {
        return rating;
    }

    public static  JSONArray getPhotoLists(){
        return photolists;
    }
    public static  JSONObject getLocation(){
        return location;
    }

    public static String getPlacename(){
        return placename;
    }

    public static JSONArray getReviewLists(){
        return reviewlist;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
            finish();
            return true;
        }
        return false;
    }
}
