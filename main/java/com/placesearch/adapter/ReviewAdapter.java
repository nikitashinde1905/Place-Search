package com.placesearch.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.placesearch.helper.ToastManager;
import com.placesearch.main.R;
import com.placesearch.utils.Config;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;



public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.MyReviewHolder>{

    JSONArray items =new JSONArray();
    Context context;
    ToastManager tm;

    public ReviewAdapter(JSONArray items,Context context){
        this.items = items;
        this.context = context;
        tm = new ToastManager(context);
    }

    @Override
    public ReviewAdapter.MyReviewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.reviewrowlayout, parent, false);
        ReviewAdapter.MyReviewHolder vh = new ReviewAdapter.MyReviewHolder(v);
        return vh;
    }
    @Override
    public int getItemCount() {
        return items.length();
    }

    public void setItems(JSONArray items){
        this.items = items;
    }

    @Override
    public void onBindViewHolder(final ReviewAdapter.MyReviewHolder holder, int position) {
        try {
            Log.d("Position",String.valueOf(position));
            final JSONObject reviewdetails = items.getJSONObject(position);
            holder.name.setText(reviewdetails.getString("author_name"));
            holder.ratingbar.setNumStars((int)Math.ceil((float)reviewdetails.getDouble("rating")));
            holder.ratingbar.setRating(reviewdetails.getInt("rating"));
           // Log.d("textmessage: ",reviewdetails.getString("text"));
            try{
                Log.d("textmessage: ",reviewdetails.getString("text"));
                holder.review.setText(reviewdetails.getString("text"));
            }catch(JSONException e){
                e.printStackTrace();
                holder.review.setVisibility(View.GONE);
            }

            Date date = new Date(reviewdetails.getLong("time") * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            holder.date.setText(sdf.format(date.getTime()));
            try{
                String photourl = reviewdetails.getString("profile_photo_url");
                Picasso.with(context).load(photourl).into(holder.photo);
            }catch(JSONException e){
                holder.photo.setVisibility(View.INVISIBLE);
               // Picasso.with(context).load("").into(holder.photo);
            }
            holder.reviewrow.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try{
                        String reveiwlink = reviewdetails.getString("author_url");
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(reveiwlink));
                        context.startActivity(intent);
                    }catch(JSONException e){
                        tm.showToast("No author link found for the review", Toast.LENGTH_SHORT);
                        e.printStackTrace();
                    }

                }
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public class MyReviewHolder extends RecyclerView.ViewHolder {
        // init the item view's
        LinearLayout reviewrow;
        ImageView photo;
        TextView name,date,review;
        RatingBar ratingbar;

        public MyReviewHolder(View itemView) {
            super(itemView);
            // get the reference of item view's
            photo =  itemView.findViewById(R.id.reviewphotoimage);
            name =  itemView.findViewById(R.id.reviewname);
            date = itemView.findViewById(R.id.reviewdate);
            review = itemView.findViewById(R.id.reviewmessage);
            ratingbar = itemView.findViewById(R.id.reviewrating);
            reviewrow =itemView.findViewById(R.id.reviewrow);
            this.setIsRecyclable(false);
        }
    }
}
