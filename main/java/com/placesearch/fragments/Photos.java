package com.placesearch.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.placesearch.adapter.PhotoViewAdapter;
import com.placesearch.adapter.RecycleViewAdapter;
import com.placesearch.main.PlaceDetails;
import com.placesearch.main.R;
import com.placesearch.storage.StorageManager;

import org.json.JSONArray;



public class Photos extends Fragment {

    TextView textView;
    RecyclerView recyclerView;
    JSONArray photos;
    PhotoViewAdapter recyclerViewAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.photo, container, false);
        textView = view.findViewById(R.id.nophotostv);
        recyclerView  = view.findViewById(R.id.photolist);
        photos = PlaceDetails.getPhotoLists();
        Log.d("photos", photos.toString());
        if(photos.length()==0){

            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerViewAdapter = new PhotoViewAdapter( photos,getContext());
        recyclerView.setAdapter(recyclerViewAdapter);

        return view;


    }

}
