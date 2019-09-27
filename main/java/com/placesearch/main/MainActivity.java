package com.placesearch.main;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnSuccessListener;
import com.placesearch.adapter.ViewPagerAdapter;
import com.placesearch.fragments.FavoritesFragment;
import com.placesearch.fragments.SearchFragment;
import com.placesearch.helper.ToastManager;

public class MainActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager viewPager;

    ToastManager tm;
    LocationManager mLocManager;
    private static LocationRequest mLocationRequest;
    private static FusedLocationProviderClient mFusedLocationClient;

    private static Location location=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if(ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET ) != PackageManager.PERMISSION_GRANTED) {


            ActivityCompat.requestPermissions(this, new String[]{
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.INTERNET}, 1);

        }
        startLocationUpdates();

        tm = new ToastManager(getApplicationContext());

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing the tablayout
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);

        //Initializing viewPager
        viewPager = (ViewPager) findViewById(R.id.pager);

        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new SearchFragment(), "SEARCH");
        adapter.addFragment(new FavoritesFragment(), "FAVORITES");
        viewPager.setAdapter(adapter);

        //Adding adapter to pager
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);

        View headerView = ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                .inflate(R.layout.tablayout_custom, null, false);

        LinearLayout linearLayoutOne = (LinearLayout) headerView.findViewById(R.id.ll);
        LinearLayout linearLayout2 = (LinearLayout) headerView.findViewById(R.id.ll2);

        tabLayout.getTabAt(0).setCustomView(linearLayoutOne);
        tabLayout.getTabAt(1).setCustomView(linearLayout2);

    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isallpermissiongranted = false;
        System.out.println(requestCode);
        switch (requestCode) {
            case 1: {
                System.out.println(grantResults.length);
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0){

                        for(int a :grantResults){

                            if(a==PackageManager.PERMISSION_GRANTED){
                                isallpermissiongranted = true;
                            }else{
                                isallpermissiongranted = false;
                                break;
                            }
                        }
                        if(isallpermissiongranted){

                            if(ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_COARSE_LOCATION ) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission( this, Manifest.permission.ACCESS_FINE_LOCATION ) == PackageManager.PERMISSION_GRANTED ||
                                    ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET ) == PackageManager.PERMISSION_GRANTED) {

                                startLocationUpdates();
                            }else{
                                tm.showToast("Application cant work without required permissions",Toast.LENGTH_SHORT);
                                Log.d("Permissiongranted","Not set");
                            }

                        }else{
                            Log.d("Starting location: ","true");
                            startLocationUpdates();
                           // tm.showToast("Application cant work without required permissions",Toast.LENGTH_SHORT);
                            Log.d("Permissiongranted","Denied");
                        }
                } else {

                    tm.showToast("Application cant work without required permissions", Toast.LENGTH_SHORT);
                    Log.d("Permissiongranted","False");
                }
                return;
            }
        }
    }

    public static double getLat(){
        if(location!=null){
            return location.getLatitude();
        }
        return -1;
    }
    public static double getLon(){
        if(location!=null) {
            return location.getLongitude();
        }
        return -1;
    }

    @SuppressWarnings({"MissingPermission"})
    // Trigger new location updates at interval
    protected void startLocationUpdates() {

        // Create the location request to start receiving updates
        mLocationRequest = LocationRequest.create();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.requestLocationUpdates(mLocationRequest, new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here
                if(locationResult !=null){
                    Log.d("Location","got location");
                    location = locationResult.getLastLocation();
                }else{
                    ToastManager tm = new ToastManager(MainActivity.this);
                    tm.showToast("Cannot get location. Application cannot work without location.",Toast.LENGTH_SHORT);

                }
            }

        },null);
    }
}
