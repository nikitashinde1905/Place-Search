package com.placesearch.fragments;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Spinner;
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
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.placesearch.adapter.PlaceArrayAdapter;
import com.placesearch.helper.DirectionsJSONParser;
import com.placesearch.helper.ToastManager;
import com.placesearch.main.PlaceDetails;
import com.placesearch.main.R;
import com.placesearch.utils.Config;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;



public class Map extends Fragment implements GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {

    private PlaceArrayAdapter mPlaceArrayAdapter;
    private MapView mMapView;
    private Spinner travelmode;
    private AutoCompleteTextView fromlocation;
    private GoogleMap googleMap;
    private  GoogleApiClient mGoogleApiClient;
    static Polyline mypoliline=null;
    ToastManager tm;

    JSONObject location;
    String placename,fromplace;

    Marker fromMarker,toMarker;

    double lat,lon;
    double fromlat = -1,fromlon = -1;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState)  {

        Log.d("OnMaps","mapsactivity");
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Places.GEO_DATA_API)
                .enableAutoManage(getActivity(), 0, this)
                .addConnectionCallbacks(this)
                .build();

        tm = new ToastManager(getContext());
        View view =  inflater.inflate(R.layout.map, container, false);
        location = PlaceDetails.getLocation();
        try{
            lat = location.getDouble("lat");
            lon = location.getDouble("lng");
        }catch(JSONException e){
            e.printStackTrace();
        }

        placename = PlaceDetails.getPlacename();

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        travelmode = view.findViewById(R.id.travelmodespinner);
        fromlocation = view.findViewById(R.id.fromlocation);
        mPlaceArrayAdapter = new PlaceArrayAdapter(getContext(), android.R.layout.simple_list_item_1, Config.BOUNDS_MOUNTAIN_VIEW, null);
        fromlocation.setAdapter(mPlaceArrayAdapter);
        fromlocation.setThreshold(5);

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {

                Log.d("OnMaps","loadingmaps");
                googleMap = mMap;
                LatLng location = new LatLng(lat,lon);
                toMarker = googleMap.addMarker(new MarkerOptions().position(location).title(placename));
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if(fromlat!=-1&&fromlon!=-1){
                    LatLng fromlocation = new LatLng(fromlat,fromlon);
                    fromMarker = googleMap.addMarker(new MarkerOptions().position(fromlocation).title(fromplace));
                    builder.include(fromMarker.getPosition());
                    builder.include(toMarker.getPosition());
                    LatLngBounds bounds = builder.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds,50);
                    googleMap.moveCamera(cu);

                }else{
                    // builder.include(toMarker.getPosition());
                    // LatLngBounds bounds = builder.build();
                    Log.d("Setting zoom level","16");
                    //CameraPosition cameraPosition = new CameraPosition.Builder().target(location).zoom(15).build();
                    //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                    googleMap.setMaxZoomPreference(15.0f);
                    // googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }

            }
        });

        fromlocation.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {

                final PlaceArrayAdapter.PlaceAutocomplete item = mPlaceArrayAdapter.getItem(position);
                final String placeId = String.valueOf(item.placeId);
                Places.GeoDataApi.getPlaceById(mGoogleApiClient, placeId)
                        .setResultCallback(new ResultCallback<PlaceBuffer>() {
                            @Override
                            public void onResult(PlaceBuffer places) {
                                if (places.getStatus().isSuccess()) {

                                    final Place myPlace = places.get(0);
                                    LatLng queriedLocation = myPlace.getLatLng();
                                    googleMap.clear();

                                    fromlat = queriedLocation.latitude;
                                    fromlon = queriedLocation.longitude;
                                    fromplace = myPlace.getName().toString();
                                    LatLng fromlocation = new LatLng(fromlat,fromlon);
                                    LatLng tolocation =  new LatLng(lat,lon);
                                    toMarker = googleMap.addMarker(new MarkerOptions().position(tolocation).title(placename));
                                    fromMarker = googleMap.addMarker(new MarkerOptions().position(fromlocation).title(fromplace));
                                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
                                    builder.include(fromMarker.getPosition());
                                    builder.include(toMarker.getPosition());
                                    LatLngBounds bounds = builder.build();
                                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                                    googleMap.animateCamera(cu);
                                    showDirectiononMap(fromlat,fromlon,travelmode.getSelectedItem().toString().toLowerCase().trim());

                                }
                                places.release();
                            }
                        });
            }
        });

        travelmode.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                googleMap.clear();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                if(fromlat!=-1&&fromlon!=-1){
                    LatLng fromlocation = new LatLng(fromlat,fromlon);
                    fromMarker = googleMap.addMarker(new MarkerOptions().position(fromlocation).title(fromplace));
                    builder.include(fromMarker.getPosition());
                }
                LatLng tolocation =  new LatLng(lat,lon);
                toMarker = googleMap.addMarker(new MarkerOptions().position(tolocation).title(placename));
                builder.include(toMarker.getPosition());

                LatLngBounds bounds = builder.build();

                CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
                googleMap.animateCamera(cu);
                showDirectiononMap(fromlat,fromlon,adapterView.getItemAtPosition(i).toString().toLowerCase());
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
        mGoogleApiClient.stopAutoManage(getActivity());
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
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
                connectionResult.getErrorCode(), Toast.LENGTH_SHORT);
    }

    private void showDirectiononMap(double fromlat,double fromlon,String travelmode){
        if(fromlat==-1 && fromlon==-1){
            //  tm.showToast("Please select starting location!!",Toast.LENGTH_SHORT);
        }else{
            if(mypoliline!=null){
                Log.d("polyline","removing");
                mypoliline.remove();
            }
            String direction_url = getDirectionUrl(new LatLng(fromlat,fromlon),new LatLng(lat,lon),travelmode);
            downloadDirectionFromServer(direction_url);
        }
    }
    private String getDirectionUrl(LatLng origin,LatLng dest,String travelmode){

        // Origin of route
        String str_origin = "origin="+origin.latitude+","+origin.longitude;
        // Destination of route
        String str_dest = "destination="+dest.latitude+","+dest.longitude;

        String mode="mode="+travelmode;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin+"&"+str_dest+"&"+mode+"&"+sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = Config.DIRECTION_URL+parameters;
        return url;
    }

    private void downloadDirectionFromServer(String URL){

        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                new Response.Listener<String>() {

                    @Override
                    public void onResponse(String response) {

                        Log.d("Direction Response",response);
                        try{
                            if(new JSONObject(response).getJSONArray("routes").length()>0){
                                ParserTask parserTask = new ParserTask();
                                parserTask.execute(response);
                            }else{
                                tm.showToast(travelmode.getSelectedItem().toString().trim()+" option not available for this route!!",Toast.LENGTH_SHORT);
                            }
                        }catch(JSONException e){
                            e.printStackTrace();
                        }


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        tm.showToast("Failed to get direction from google server!!",Toast.LENGTH_SHORT);
                    }
                }
        );
        RequestQueue requestQueue = Volley.newRequestQueue(getContext());
        requestQueue.add(stringRequest);
    }


    /** A class to parse the Google Places in JSON format */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String,String>>> > {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(fromMarker.getPosition());
            builder.include(toMarker.getPosition());

            Log.d("ParserTask","execute");
            ArrayList<LatLng> points = null;
            PolylineOptions lineOptions = null;
            // Traversing through all the routes
            LatLng fromlocation = new LatLng(fromlat,fromlon);
            LatLng tolocation =  new LatLng(lat,lon);
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<LatLng>();
                lineOptions = new PolylineOptions();

                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                points.add(fromlocation);
                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);
                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);
                    builder.include(position);
                    points.add(position);
                }
                points.add(tolocation);
                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(15);
                lineOptions.color(Color.BLUE);
            }

            LatLngBounds bounds = builder.build();
            CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 50);
            // Drawing polyline in the Google Map for the i-th route
            mypoliline = googleMap.addPolyline(lineOptions);
            googleMap.moveCamera(cu);


        }
    }
}