package com.placesearch.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.placesearch.adapter.PlaceArrayAdapter;
import com.placesearch.helper.AlertDialogManager;
import com.placesearch.helper.ProgressDialogManager;
import com.placesearch.helper.ToastManager;
import com.placesearch.main.MainActivity;
import com.placesearch.main.R;
import com.placesearch.main.SearchResult;
import com.placesearch.network.ConnectionManager;
import com.placesearch.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;



public class SearchFragment extends Fragment implements  GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private EditText keywordet,distanceet;
    private Spinner categorySpinner;
    private RadioGroup radiogroup;
    private RadioButton radioButton;
    private AutoCompleteTextView locationet;
    private TextView keywordmessagetv,locationmessagetv;
    private Button search ,clear;

    ArrayAdapter<CharSequence> adapter;
    String keyword,category,distance;
    double lat=-1,lon=-1;

    ConnectionManager cm;
    AlertDialogManager adm;
    ProgressDialogManager dialog;
    ToastManager tm;

    private  GoogleApiClient mGoogleApiClient;
    private PlaceArrayAdapter mPlaceArrayAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view  =  inflater.inflate(R.layout.search, container, false);

        cm = new ConnectionManager(getContext());
        adm = new AlertDialogManager();
        dialog = new ProgressDialogManager(getContext());
        tm = new ToastManager(getContext());

        adapter = ArrayAdapter.createFromResource(getContext(), R.array.categories_arrays, android.R.layout.simple_spinner_item);

        //get handler to widgets in view
        initialize(view);

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), 0, this)
                .addConnectionCallbacks(this)
                .build();

        //on search button click
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(checkdataValid()){

                    //get the result from the google api
                    category = category.toLowerCase();
                    if(category.contains(" ")){
                        String[] temp = category.split(" ");
                        category = temp[0].trim().toLowerCase()+"_"+temp[1].trim().toLowerCase();
                    }
                    Log.d("Keyword",keyword);
                    Log.d("category",category);
                    Log.d("distance",distance);
                    Log.d("lat",String.valueOf(lat));
                    Log.d("lon",String.valueOf(lon));

                    if (!cm.isConnectingToInternet()) {

                        //not connected to internet show alert
                        adm.showAlertDialog(getContext(),"Connectivity Problem","No internet connection available!!");

                    }

                    //get palces from google api
                    //String URL = Config.PLACES_URL+"location="+lat+","+lon+"&radius="+String.valueOf(Integer.parseInt(distance)*1609.34)+"&type="+category+"&keyword="+keyword+"&key="+Config.API_KEY;
                    String URL = Config.PLACES_URL+"location="+lat+","+lon+"&radius="+String.valueOf(Integer.parseInt(distance)*1609.34)+"&type="+category+"&keyword="+keyword;

                    dialog.showDialog("Fetching results..");
                    Log.d("URL QUERIED",URL);

                    StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                            new Response.Listener<String>() {

                                @Override
                                public void onResponse(String response) {

                                    dialog.dismissDialog();

                                    Log.d("VolleyResponse",response);
                                    //switch activity to display list of places
                                    try{
                                        JSONObject result = new JSONObject(response);
                                        if(result.getString("status").compareToIgnoreCase("ZERO_RESULTS")==0){

                                            clear();
                                            Intent intent = new Intent(getContext(), SearchResult.class);
                                            intent.putExtra("RESULT", "ZERO_RESULTS");
                                            startActivity(intent);
                                            //tm.showToast("No results found!!",Toast.LENGTH_SHORT);
                                        }else{
                                            clear();
                                            Intent intent = new Intent(getContext(), SearchResult.class);
                                            intent.putExtra("RESULT", response);
                                            startActivity(intent);
                                        }
                                    }catch(JSONException e) {
                                        Log.d("Seach Fragment", e.getMessage());
                                    }
                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {

                                    dialog.dismissDialog();
                                    clear();
                                    if(error.getMessage().trim().length()>0){
                                        tm.showToast(error.getMessage(),Toast.LENGTH_SHORT);
                                    }else{
                                        tm.showToast("Connection to the server not established!!",Toast.LENGTH_SHORT);
                                    }

                                    //   Log.d("VollerError",error.getMessage());

                                }
                            }
                    );
                    RequestQueue requestQueue = Volley.newRequestQueue(getContext());
                    requestQueue.add(stringRequest);

                }else{
                    tm.showToast("Please fix all fields with errors",Toast.LENGTH_SHORT);

                }
            }
        });

        //onclearbuttonclicked
        clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clear();
            }
        });

        //autocomplete text view
        //link for understanding http://www.truiton.com/2015/04/android-places-api-autocomplete-getplacebyid/
        locationet.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                //get the selcted item from adapter
                final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {
                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    lat = queriedLocation.latitude;
                                    lon = queriedLocation.longitude;
                                    Log.d("Latitude(other): ",String.valueOf(lat));
                                    Log.d("longitude(other): ",String.valueOf(lon));
                                }
                                places.release();
                            }
                        });
            }
        });
        mPlaceArrayAdapter = new PlaceArrayAdapter(getContext(), android.R.layout.simple_list_item_1, Config.BOUNDS_MOUNTAIN_VIEW, null);
        locationet.setAdapter(mPlaceArrayAdapter);

        return view;
    }

    private void initialize(View view) {

        keywordet = view.findViewById(R.id.keywordet);
        distanceet = view.findViewById(R.id.distanceet);
        categorySpinner = view.findViewById(R.id.catergoryspinner);
        keywordmessagetv = view.findViewById(R.id.keywordmessagetv);
        radiogroup = view.findViewById(R.id.radioGroup);
        locationmessagetv = view.findViewById(R.id.locationmessagetv);
        locationet = view.findViewById(R.id.locationet);
        search = view.findViewById(R.id.search);
        clear = view.findViewById(R.id.clear);
        keywordet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(keywordmessagetv.isEnabled()){
                    keywordmessagetv.setVisibility(View.INVISIBLE);
                }
            }
        });
        radiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                if(locationmessagetv.isEnabled()){
                    locationmessagetv.setVisibility(View.INVISIBLE);
                }
                if (checkedId == R.id.otherlocation) {
                    lat=-1;
                    lon=-1;
                    locationet.setEnabled(true);
                } else {

                    locationet.setText("");
                    locationet.setEnabled(false);
                }
            }
        });
        locationet.setThreshold(5);

    }


    private boolean checkdataValid(){

        boolean isvalid = false;
        keyword = keywordet.getText().toString().trim();
        category = categorySpinner.getSelectedItem().toString().trim();
        distance = distanceet.getText().toString().trim();
        distance = distance.length()==0?"10":distance;
        if (keyword.length() <= 0) {
            keywordmessagetv.setVisibility(View.VISIBLE);

        }else{
            isvalid = true;
        }
        if (radiogroup.getCheckedRadioButtonId() == -1)
        {
            locationmessagetv.setVisibility(View.VISIBLE);
            isvalid = false;

        }else{

            int selectedId = radiogroup.getCheckedRadioButtonId();
            if(selectedId==R.id.otherlocation){
                //get location (lat,lon) from autocomple textview
                if(lat==-1&&lon==-1){
                    locationmessagetv.setVisibility(View.VISIBLE);
                    isvalid = false;

                }

            }else{

                //get current location (lat,lon) from google api
                lat = MainActivity.getLat();
                lon= MainActivity.getLon();
                if(lat==-1&&lon==-1){
                    tm.showToast("Please ensure location service is enabled",Toast.LENGTH_SHORT);
                }
                Log.d("Latitude: ",String.valueOf(lat));
                Log.d("longitude: ",String.valueOf(lon));
            }
        }
        return isvalid;
    }

    private void clear(){

        keywordet.setText("");
        distanceet.setText("");
        locationet.setText("");
        categorySpinner.setSelection(adapter.getPosition("Default"));
        radiogroup.check(R.id.currentlocation);
        keywordmessagetv.setVisibility(View.INVISIBLE);
        locationmessagetv.setVisibility(View.INVISIBLE);
        locationet.setEnabled(false);
        keyword = "";
        category="";
        distance="";
        lat=0;
        lon=0;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //make connection
        mPlaceArrayAdapter.setGoogleApiClient(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

        mPlaceArrayAdapter.setGoogleApiClient(null);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

        //connection to google api failed
        tm.showToast("Google Places API connection failed with error code:" +
                connectionResult.getErrorCode(),Toast.LENGTH_SHORT);
    }

}