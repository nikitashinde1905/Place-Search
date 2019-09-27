package com.placesearch.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.placesearch.helper.ToastManager;
import com.placesearch.main.R;
import com.placesearch.utils.Config;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

public class PhotoViewAdapter extends RecyclerView.Adapter<PhotoViewAdapter.MyPhotoHolder>{

    JSONArray items =new JSONArray();
    Context context;
    ToastManager tm;

    public PhotoViewAdapter(JSONArray items, Context context) {
        this.items = items;
        this.context = context;
        tm = new ToastManager(context);
    }

    @Override
    public PhotoViewAdapter.MyPhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.photorowlayout, parent, false);
        PhotoViewAdapter.MyPhotoHolder vh = new PhotoViewAdapter.MyPhotoHolder(v);
        return vh;
    }
    @Override
    public int getItemCount() {
        return items.length();
    }

    @Override
    public void onBindViewHolder(final PhotoViewAdapter.MyPhotoHolder holder, int position) {
        try{

            final JSONObject photodetails = items.getJSONObject(position);
            String url = Config.PHOTO_URL+"maxwidth=400&photoreference="+photodetails.getString("photo_reference")+"&key="+Config.API_KEY;
            Log.d("photourl",url);
            Picasso.with(context).load(url).into(holder.photo);

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    public class MyPhotoHolder extends RecyclerView.ViewHolder {
        // init the item view's
        ImageView photo;

        public MyPhotoHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            photo = (ImageView) itemView.findViewById(R.id.photoimage);
        }
    }
}
