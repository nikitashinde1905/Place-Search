package com.placesearch.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.placesearch.main.PlaceDetails;
import com.placesearch.main.R;



public class Info extends Fragment {

    TextView addresstv,phonennotv,priceleveltv,googlepagetv,websitetv;
    LinearLayout l1,l2,l3,l4,l5,l6;
    RatingBar reviewrb;

    static String vicinity,website,formatedphonenumber,googlepage;
    static String pricelevel;
    static double rating;

    String priceText="";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view =  inflater.inflate(R.layout.info, container, false);
        initialize(view);
        getData();
        setFields();
        handleOnclickListenerOnPhone();
        handleOnclickListenerOnGooglePage();
        handleOnclickListenerOnWebsite();
        return view;
    }

    public void initialize(View view){
        addresstv = view.findViewById(R.id.placeinfoaddress);
        phonennotv = view.findViewById(R.id.placeinfophonenumber);
        priceleveltv = view.findViewById(R.id.placeinfopricelevel);
        googlepagetv = view.findViewById(R.id.placeinfogooglepage);
        websitetv = view.findViewById(R.id.placeinfowebsite);
        reviewrb = view.findViewById(R.id.placeinforating);
        l1 = view.findViewById(R.id.placeinfol1);
        l2 = view.findViewById(R.id.placeinfol2);
        l3 = view.findViewById(R.id.placeinfol3);
        l4 = view.findViewById(R.id.placeinfol4);
        l5 = view.findViewById(R.id.placeinfol5);
        l6 = view.findViewById(R.id.placeinfol6);

    }
    public void getData(){

        vicinity = PlaceDetails.getFormattedAddress();
        website = PlaceDetails.getWebsite();
        formatedphonenumber = PlaceDetails.getFormatedphonenumber();
        googlepage = PlaceDetails.getGooglepage();
        pricelevel=PlaceDetails.getPricelevel();
        rating = PlaceDetails.getRating();
        if(pricelevel.trim().length()>0){

            for(int i=0;i<Integer.parseInt(pricelevel);i++){
                priceText+="$";
            }
        }

    }

    public void setFields() {

        if(vicinity.trim().length()>0){
            addresstv.setText(vicinity);
        }else{
            l1.setVisibility(View.GONE);
        }

        if(website.trim().length()>0){
            websitetv.setText(underlineString(website));
        }else{
            l6.setVisibility(View.GONE);
        }

        if(formatedphonenumber.trim().length()>0){
            phonennotv.setText(underlineString(formatedphonenumber));
        }else{
            l2.setVisibility(View.GONE);
        }

        if(googlepage.trim().length()>0){
            googlepagetv.setText(underlineString(googlepage));
        }else{
            l5.setVisibility(View.GONE);
        }

        if(priceText.trim().length()>0){
            priceleveltv.setText(priceText);
        }else{
            l3.setVisibility(View.GONE);
        }
        if(rating>0){
            Log.d("Rating Set: ",String.valueOf(rating));
            reviewrb.setNumStars((int)Math.ceil((float)rating));
            reviewrb.setRating((float) rating);
        }else{
            l4.setVisibility(View.GONE);
        }
    }

    private SpannableString underlineString(String s){

        SpannableString content = new SpannableString(s);
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        return content;
    }

    private  void handleOnclickListenerOnPhone(){
        phonennotv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:"+formatedphonenumber));
                startActivity(intent);
            }
        });
    }
    private  void handleOnclickListenerOnGooglePage(){
        googlepagetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(googlepage));
                startActivity(intent);
            }
        });
    }
    private  void handleOnclickListenerOnWebsite(){
        websitetv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent httpIntent = new Intent(Intent.ACTION_VIEW);
                httpIntent.setData(Uri.parse(website));
                startActivity(httpIntent);
            }
        });
    }
}
