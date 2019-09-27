package com.placesearch.utils;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;



public class Config {


    
     public static String API_KEY="AIzaSyCkSEHJZbz2_1eh0b7PcfiUhX0HlHvzCMk";

     //when running on emulator use 10.0.2.2 instead of localhost;
     public static String PLACES_URL = "http://hw9-env.us-west-1.elasticbeanstalk.com/endpoint?";
     public static String PLACES_DETAILS = "http://hw9-env.us-west-1.elasticbeanstalk.com/placedetails?";
     public static String NEXT_PAGE = "http://hw9-env.us-west-1.elasticbeanstalk.com/nextpage?";
     public static String REVIEWS_YELP = "http://hw9-env.us-west-1.elasticbeanstalk.com/reviewspage?";

     //public static String PLACES_URL = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?";
     //public static String PLACES_DETAILS = "https://maps.googleapis.com/maps/api/place/details/json?";

     public static String PHOTO_URL="https://maps.googleapis.com/maps/api/place/photo?";
     public static String DIRECTION_URL ="https://maps.googleapis.com/maps/api/directions/json?";
     public static final LatLngBounds BOUNDS_MOUNTAIN_VIEW = new LatLngBounds(new LatLng(37.398160, -122.180831), new LatLng(37.430610, -121.972090));


}
