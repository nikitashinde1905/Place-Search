package com.placesearch.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.placesearch.fragments.FavoritesFragment;
import com.placesearch.helper.ToastManager;
import com.placesearch.main.PlaceDetails;
import com.placesearch.main.R;
import com.placesearch.main.SearchResult;
import com.placesearch.storage.StorageManager;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class RecycleViewAdapter extends RecyclerView.Adapter<RecycleViewAdapter.MyViewHolder> {

    JSONArray items =new JSONArray();
    Context context;
    ToastManager tm;
    boolean flag;


    public RecycleViewAdapter(Context context ,JSONArray items,boolean flag){

        this.context = context;
        this.items= items;
        this.flag=flag;
        tm = new ToastManager(context);
    }

    public JSONArray getItems() {
        return items;
    }

    public void setItems(JSONArray items) {
        this.items = items;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //content for each row
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.rowlayout, parent, false);
        MyViewHolder vh = new MyViewHolder(v);
        return vh;
    }

    @Override
    public int getItemCount() {
        return items.length();
    }


    @Override
    public void onBindViewHolder(final MyViewHolder holder, int position) {
        try{
            //gets placeID(place)
            final JSONObject place = items.getJSONObject(position);
            holder.placename.setText(place.getString("name"));
            holder.placeaddress.setText(place.getString("vicinity"));
            //load image from url
            Picasso.with(context).load(place.getString("icon")).into(holder.categoryimage);
            holder.favorites.setOnCheckedChangeListener(null);
            if(place.getBoolean("isliked")){
                Log.d("placed liked: ",place.getString("place_id"));
                holder.favorites.setChecked(true);
            }else if(StorageManager.isPlacePresent(place.getString("place_id"))){
                items.put(position,place.put("isliked",true));
                holder.favorites.setChecked(true);
            }
            //b=flag,liked/disliked
            holder.favorites.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                    Log.d("checkedtoggled","listenercalled");
                    JSONObject templace=null;
                    String placeid=null;
                    try{
                        templace = items.getJSONObject(holder.getAdapterPosition());
                        placeid = templace.getString("place_id");

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                    Log.d("position checked: ",String.valueOf(holder.getAdapterPosition()));
                    Log.d("adapterpositionchecked:",String.valueOf(holder.getAdapterPosition()));
                    if(b){

                        try{

                            place.put("isliked",true);
                            items.put(holder.getAdapterPosition(),place);
                            Log.d("itemslength",String.valueOf(items.length()));

                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        //add item to local store (memory as of now since wont work on sqlite)
                        StorageManager.addPlace(placeid,place,context);
                        tm.showToast(holder.placename.getText()+" was added to favorites", Toast.LENGTH_SHORT);

                    }else{

                        try{
                            place.put("isliked",false);
                            items.put(holder.getAdapterPosition(),place);
                            Log.d("itemslength1",String.valueOf(items.length()));


                        }catch(Exception e){
                            e.printStackTrace();
                        }
                        Log.d("flagvalue",String.valueOf(flag));
                        if(flag){

                            Log.d("removed item: ","true");
                            items.remove(holder.getAdapterPosition());
                            Log.d("FavoritesFragment: ","calling");
                            StorageManager.delPlace(placeid,context);
                            FavoritesFragment.dataSetChanged();


                        }else{

                            StorageManager.delPlace(placeid,context);
                            notifyDataSetChanged();
                        }
                        tm.showToast(holder.placename.getText()+" was removed from favorites", Toast.LENGTH_SHORT);
                    }
                }
            });

            holder.rowlayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{

                        JSONObject placedetails = items.getJSONObject(holder.getAdapterPosition());
                        Intent intent = new Intent(context, PlaceDetails.class);
                        intent.putExtra("PLACE_DETAILS",placedetails.toString());
                        context.startActivity(intent);

                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            });

        }catch(Exception e){
            Log.d("RecyvleViewAdapter",e.getMessage());
        }
    }
    public class MyViewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        //initialise the widgets for the rows
        LinearLayout rowlayout;
        TextView placename,placeaddress;
        ImageView categoryimage;
        ToggleButton favorites;

        public MyViewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            placename = (TextView) itemView.findViewById(R.id.placename);
            placeaddress = (TextView) itemView.findViewById(R.id.placeaddress);
            categoryimage = (ImageView) itemView.findViewById(R.id.categoryimage);
            favorites = (ToggleButton) itemView.findViewById(R.id.button_favorite);
            rowlayout = itemView.findViewById(R.id.rowlinearlayout);
            this.setIsRecyclable(false);
        }
    }
}
