package com.placesearch.fragments;

import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.placesearch.adapter.ReviewAdapter;
import com.placesearch.main.PlaceDetails;
import com.placesearch.main.R;
import com.placesearch.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;



public class Reviews extends Fragment {

    TextView noreviewstv;
    Spinner reviewtype,reviewsorttype;
    RecyclerView reviewlist;

    ReviewAdapter reviewAdapter;

    JSONArray googlereviews,yelpreviews = new JSONArray();
    JSONArray relevantArray,address_components;


    String reviewType,sortValue;
    String name, address1, address2, city, state,country;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        googlereviews = PlaceDetails.getReviewLists();
        Log.d("Reviews",googlereviews.toString());
        name = PlaceDetails.getName();
        address_components = PlaceDetails.getAddressComponents();
        if(address_components.length()>0){
            parseAddressComponent();
            fetchYelpReviews( name, address1, address2, city, state,country);
        }
        relevantArray = googlereviews;
        View view =  inflater.inflate(R.layout.review, container, false);
        initialize(view);
        reviewType = reviewtype.getSelectedItem().toString();
        sortValue = reviewsorttype.getSelectedItem().toString();
        checkVisibility();
        setOnSpinnerClickListener();
        return view;
    }

    private void initialize(View view){

        noreviewstv = view.findViewById(R.id.noreviewtv);
        reviewtype = view.findViewById(R.id.reviewtype);
        reviewsorttype = view.findViewById(R.id.reviewsorttype);
        reviewlist = view.findViewById(R.id.reviewlist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        reviewlist.setLayoutManager(linearLayoutManager);
        Log.d("initialrelevantarray",relevantArray.toString());
        reviewAdapter = new ReviewAdapter(relevantArray,getContext());
        reviewlist.setAdapter(reviewAdapter);
    }

    private void checkVisibility(){
        Log.d("Checkrelevantarray: ",String.valueOf(relevantArray.length()));
        if(relevantArray.length()<=0){
            Log.d("Settingvisibility","true");
            noreviewstv.setVisibility(View.VISIBLE);
            reviewlist.setVisibility(View.GONE);
        }else{
            noreviewstv.setVisibility(View.GONE);
            reviewlist.setVisibility(View.VISIBLE);
        }
    }

    private void setOnSpinnerClickListener(){

        reviewtype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                reviewType= adapterView.getItemAtPosition(i).toString();
                Log.d("reviewType",reviewType);
                if(reviewType.equalsIgnoreCase("Google reviews")){
                    relevantArray =  filterReviewBasedOnSelection(googlereviews,sortValue);

                }else{
                    relevantArray =  filterReviewBasedOnSelection(yelpreviews,sortValue);
                }
                Log.d("relevantArray<Review>: ",relevantArray.toString());
                checkVisibility();
                if(relevantArray.length()>0){

                    reviewAdapter.setItems(relevantArray);
                    reviewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        reviewsorttype.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                sortValue = adapterView.getItemAtPosition(i).toString();
                if(reviewType.equalsIgnoreCase("Google reviews")){
                    relevantArray =  filterReviewBasedOnSelection(googlereviews,sortValue);

                }else {
                    relevantArray = filterReviewBasedOnSelection(yelpreviews, sortValue);
                }
                Log.d("relevantArray<SORT>: ",relevantArray.toString());
                checkVisibility();
                if(relevantArray.length()>0){
                    reviewAdapter.setItems(relevantArray);
                    reviewAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

    }

    private JSONArray filterReviewBasedOnSelection(JSONArray items,String sortValue){
         if(sortValue.equalsIgnoreCase("Default order")){
             return items;
         }else if(sortValue.equalsIgnoreCase("Highest rating")){
            return sortJson(items,"rating",false);

         }else if(sortValue.equalsIgnoreCase("Lowest rating")){
             return sortJson(items,"rating",true);
         }else if(sortValue.equalsIgnoreCase("Most recent")){
             return sortJson(items,"time",false);
         }else {
            return sortJson(items,"time",true);
         }
    }

    public static JSONArray sortJson(JSONArray jsonArraylab, final String type, final boolean isascending) {
        Log.d("initialarray",jsonArraylab.toString());
        JSONArray sortedJsonArray = new JSONArray();
        try{

            List<JSONObject> jsonValues = new ArrayList<JSONObject>();
            for (int i = 0; i < jsonArraylab.length(); i++) {
                jsonValues.add(jsonArraylab.getJSONObject(i));
            }
            Collections.sort( jsonValues, new Comparator<JSONObject>() {

                @Override
                public int compare(JSONObject jsonObject, JSONObject t1) {
                    int value =0;
                    if(type.equalsIgnoreCase("rating")){
                        Log.d("sortingontype","rating");
                        try{
                            int rating1 = jsonObject.getInt("rating");
                            int rating2 = t1.getInt("rating");
                            if(isascending){
                               value= rating1-rating2;
                            }else{
                                value= rating2-rating1;
                            }

                        }catch(JSONException e){
                            e.printStackTrace();
                        }

                    }else  if(type.equalsIgnoreCase("time")){
                        Log.d("sortingontype","time");
                        try{
                            long time1 = jsonObject.getLong("time");
                            long time2 = t1.getLong("time");
                            if(isascending){
                                value= (int)(time1-time2);
                            }else{
                                value= (int)(time2-time1);
                            }

                        }catch(JSONException e){
                            e.printStackTrace();
                        }
                    }
                    Log.d("value returned",String.valueOf(value));
                    return value;
                }
            });
            for (int i = 0; i < jsonValues.size(); i++) {
                sortedJsonArray.put(jsonValues.get(i));
            }

        }catch(JSONException e){
            e.printStackTrace();
        }
        Log.d("sortedArray",sortedJsonArray.toString());
        return sortedJsonArray;
    }

    private void fetchYelpReviews(String name,String address1,String address2,String city,String state,String country){

        String url = Uri.parse(Config.REVIEWS_YELP)
                .buildUpon()
                .appendQueryParameter("name", name)
                .appendQueryParameter("address1", address1)
                .appendQueryParameter("address2", address2)
                .appendQueryParameter("city", city)
                .appendQueryParameter("state", state)
                .appendQueryParameter("country", country)
                .build().toString();

            String YELP_URL = Config.REVIEWS_YELP+"name="+name+"&address1="+address1+"&address2="+address2+"&city="+city+"&state="+state+"&country="+country;
            Log.d("YELP_URL",url);
            StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                    new Response.Listener<String>() {

                        @Override
                        public void onResponse(String response) {
                            try{
                                Log.d("YELP_Reviews",response);
                                if(response!=null){
                                    JSONArray tempreviews = new JSONObject(response).getJSONArray("reviews");
                                    setYelpReviews(tempreviews);

                                }else{
                                    yelpreviews = new JSONArray();
                                }

                            }catch(JSONException e){
                                yelpreviews = new JSONArray();
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {

                        }
            });
            RequestQueue requestQueue = Volley.newRequestQueue(getContext());
            requestQueue.add(stringRequest);
    }

    private void setYelpReviews(JSONArray arr){
        JSONObject obj=null,finalObj = null;
        for(int i=0;i<arr.length();i++){

                finalObj = new JSONObject();
                try{
                    obj = arr.getJSONObject(i);
                }catch(JSONException e){
                    e.printStackTrace();
                }
                try{
                    finalObj.put("author_name",obj.getJSONObject("user").getString("name"));
                }catch(JSONException e){
                    try{
                        finalObj.put("author_name","");
                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }
                try{
                    finalObj.put("profile_photo_url",obj.getJSONObject("user").getString("image_url"));
                }catch(JSONException e){
                    try{
                        finalObj.put("profile_photo_url","");
                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }
                try{
                    finalObj.put("rating",obj.getInt("rating"));
                }catch(JSONException e){
                    try{
                        finalObj.put("rating",0);
                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }
                try{
                    finalObj.put("text",obj.getString("text"));
                }catch(JSONException e){
                    try{
                        finalObj.put("text","");
                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }
                try{
                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date date = dateFormat.parse(obj.getString("time_created"));
                    long unixTime = (long)date.getTime()/1000;
                    finalObj.put("time",unixTime);
                }catch(JSONException e){
                    try{
                        finalObj.put("time",0);
                    }catch(JSONException e1){
                        e1.printStackTrace();
                    }
                }catch (ParseException e){

                    e.printStackTrace();
                }
                try{
                    finalObj.put("author_url",obj.getString("url"));
                }catch(JSONException e){
                    e.printStackTrace();
                }
                try{
                    yelpreviews.put(i,finalObj);
                }catch (JSONException e){
                    e.printStackTrace();
                }
        }
    }

    private void parseAddressComponent(){

        System.out.println(address_components);
        String vicinity = PlaceDetails.getVicinity();
        System.out.println("Helloo");
        address1 = vicinity;
        address2 = PlaceDetails.getFormattedAddress();
        System.out.println("FormattedAddress: "+address2);
        System.out.println("Vicinity: "+address1);
        for(int z=0;z<address_components.length();z++){
            try {
                JSONObject obj = address_components.getJSONObject(z);
                String shortname = obj.getString("short_name");
                JSONArray types = obj.getJSONArray("types");
                for (int j = 0; j < types.length(); j++) {
                    if (types.getString(j).equalsIgnoreCase("administrative_area_level_2")) {
                        city = shortname;
                    }
                    if (types.getString(j).equalsIgnoreCase("administrative_area_level_1")) {
                        state = shortname;
                    }
                    if (types.getString(j).equalsIgnoreCase("country"))
                        country = shortname;
                }
            }catch(JSONException e){
                e.printStackTrace();
            }
        }
    }

}
