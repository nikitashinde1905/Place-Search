package com.placesearch.storage;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;



public class StorageManager {


    static HashMap<String,JSONObject> map;
    static SharedPreferences prefs;

    static{
        map = new HashMap<String,JSONObject>();
        Log.d("StaticBLock",String.valueOf(map.size()));
    }
    public static boolean addPlace(String key , JSONObject place,Context context){
        Log.d("adding Key to Stoage: ",key);
        map.put(key,place);
        if(saveSharedPrefStore(map,context)){
            return true;
        }else{
            Log.d("saveSharedPref: ","Failed to store in shared pref");
            map.remove(key);
            return false;
        }
    }
    public static boolean delPlace(String key,Context context){
        JSONObject tempObj = map.remove(key);
        if(saveSharedPrefStore(map,context)){
            return true;
        }else{
            Log.d("saveSharedPref: ","Failed to remove in shared pref");
            map.put(key,tempObj);
            return false;
        }
    }

    public static JSONArray getAppPlaces(Context context){
        JSONArray array = new JSONArray();
        if(map!=null){
            if(map.size()==0){

                Log.d("StorageManager","Getting map from local storage");
                map = getHashMap(context);
            }

            for (Map.Entry<String, JSONObject> entry : map.entrySet())
            {
                array.put(entry.getValue());
            }
        }
        System.out.println(array);
        return array;
    }

    public static boolean isPlacePresent(String key){
        Log.d("StorageManager","isPlacePresent");
        if(map!=null){
            if(map.get(key)!=null){
                return true;
            }
            return false;
        }
       return false;
    }

    private static boolean saveSharedPrefStore(HashMap map, Context context){
        Log.d("StorageManager","savingsharedpref");
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(map);
        editor.putString("PLACES_STORE",json);
        editor.apply();
        return true;
    }

    private static HashMap<String,JSONObject> getHashMap(Context context) {
        Log.d("StorageManager","gettinghashMap");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = prefs.getString("PLACES_STORE","");
        java.lang.reflect.Type type = new TypeToken<HashMap<String,JSONObject>>(){}.getType();
        HashMap<String,JSONObject> obj = gson.fromJson(json, type);
        if(obj==null){
            Log.d("Storagemanager","hashmap is null");
            obj = new HashMap<String, JSONObject>();
        }
        System.out.println(obj);
        return obj;
    }

}