package com.placesearch.fragments;

import android.content.Context;
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

import com.placesearch.adapter.RecycleViewAdapter;
import com.placesearch.helper.ToastManager;
import com.placesearch.main.R;
import com.placesearch.main.SearchResult;
import com.placesearch.storage.StorageManager;

import org.json.JSONArray;



public class FavoritesFragment extends Fragment {

    static TextView textView;
    static RecyclerView recyclerView;
    static JSONArray places;
    static  RecycleViewAdapter recyclerViewAdapter;
    static Context context;

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){

            places = StorageManager.getAppPlaces(getContext());
            Log.d("favoritesvisible", String.valueOf(places.length()));
            if(places.length()==0){
                Log.d("recycleview","invsible");
                textView.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.INVISIBLE);
            }else{
                Log.d("recycleview","visible");
                textView.setVisibility(View.INVISIBLE);
                recyclerView.setVisibility(View.VISIBLE);
            }
            recyclerViewAdapter.setItems(places);
            recyclerViewAdapter = new RecycleViewAdapter(getContext(), places,true);
            recyclerView.setAdapter(recyclerViewAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("favorites fragment: ","on resume called");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.favorites, container, false);
        context = getContext();
        textView = view.findViewById(R.id.noplacestv);
        recyclerView  = view.findViewById(R.id.placelist);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        places = StorageManager.getAppPlaces(getContext());
        if(places.length()==0){

            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }else{
            textView.setVisibility(View.INVISIBLE);
            recyclerView.setVisibility(View.VISIBLE);
        }
        recyclerViewAdapter = new RecycleViewAdapter(getContext(), places,true);
        recyclerView.setAdapter(recyclerViewAdapter);
        return view;
    }

    public static void dataSetChanged(){
        places = StorageManager.getAppPlaces(context);
        Log.d("Afterdeleting",String.valueOf(places.length()));
        if(places.length()==0){

            textView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }
        recyclerViewAdapter.setItems(places);
        recyclerViewAdapter.notifyDataSetChanged();
    }
}
