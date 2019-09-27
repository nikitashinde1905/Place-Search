package com.placesearch.main;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.placesearch.adapter.RecycleViewAdapter;
import com.placesearch.helper.ProgressDialogManager;
import com.placesearch.helper.ToastManager;
import com.placesearch.utils.Config;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;



public class SearchResult extends AppCompatActivity {

    String result;
    RecyclerView recyclerView;
    Button next,prev;
    RecycleViewAdapter recyclerViewAdapter;
    LinearLayout reccycleViewLayout;
    TextView noresulttv;
    Toolbar mToolbar;

    ProgressDialogManager dialog;
    ToastManager tm;
    JSONArray array;
    int currentItem = 0;

    ArrayList<JSONArray> itemsQueried = new ArrayList<JSONArray>();
    String nextToken = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("OncreateCalled","Search Result");
        setContentView(R.layout.search_result);
        initialize();
        dialog = new ProgressDialogManager(this);
        tm = new ToastManager(this);
        //store data for activity store in instatancestate, as long as alive. null since creating instance when search is clicked else null
        if (savedInstanceState == null) {
                Bundle extras = getIntent().getExtras();
                result= extras.getString("RESULT");
        }else {

                result= (String) savedInstanceState.getSerializable("RESULT");
        }
        if(result.compareToIgnoreCase("ZERO_RESULTS")==0){

                Log.d("Restult","null");
                noresulttv.setVisibility(View.VISIBLE);
                //reccyleview: list of items, holder for results table
                reccycleViewLayout.setVisibility(View.INVISIBLE);

        }else {
                try{
                    addItemToArray(result);
                }catch(JSONException e){
                    nextToken=null;
                }
                checkNextPrevButttonVisibility();
                //passing array to adapter to display
                recyclerViewAdapter = new RecycleViewAdapter(SearchResult.this, array,false);
                recyclerView.setAdapter(recyclerViewAdapter);
        }
        initialize();
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {


                if(currentItem<itemsQueried.size()-1){
                        //load result from local storage
                        currentItem++;
                        array = itemsQueried.get(currentItem);
                        recyclerViewAdapter.setItems(array);
                        recyclerViewAdapter.notifyDataSetChanged();
                        checkNextPrevButttonVisibility();

                    }else{

                        dialog.showDialog("Fetching next page..");
                        //String URL =Config.PLACES_URL+"pagetoken="+nextToken+"&key="+Config.API_KEY;
                        String URL =Config.NEXT_PAGE+"pagetoken="+nextToken;
                        Log.d("URL QUERIED",URL);

                        StringRequest stringRequest = new StringRequest(Request.Method.GET, URL,
                                new Response.Listener<String>() {

                                    @Override
                                    public void onResponse(String response) {

                                        dialog.dismissDialog();

                                      //  Log.d("VolleyResponse",response);
                                        //switch activity to display list of places
                                        try{
                                            JSONObject result = new JSONObject(response);
                                            if(result.getString("status").compareToIgnoreCase("ZERO_RESULTS")==0){
                                                tm.showToast("No results found!!", Toast.LENGTH_SHORT);
                                            }else{
                                                currentItem++;
                                                addItemToArray(response);
                                                recyclerViewAdapter.setItems(array);
                                                recyclerViewAdapter.notifyDataSetChanged();
                                                checkNextPrevButttonVisibility();
                                            }
                                        }catch(JSONException e) {
                                            nextToken=null;
                                            recyclerViewAdapter.setItems(array);
                                            recyclerViewAdapter.notifyDataSetChanged();
                                            checkNextPrevButttonVisibility();

                                            Log.d("Seach Fragment", e.getMessage());
                                        }
                                    }
                                },
                                new Response.ErrorListener() {
                                    @Override
                                    public void onErrorResponse(VolleyError error) {

                                        dialog.dismissDialog();
                                        tm.showToast(error.getMessage(),Toast.LENGTH_SHORT);
                                        Log.d("VollerError",error.getMessage());

                                    }
                                }
                        );
                        RequestQueue requestQueue = Volley.newRequestQueue(getApplicationContext());
                        requestQueue.add(stringRequest);
                    }
            }
        });
        prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                    currentItem--;
                    array = itemsQueried.get(currentItem);
                    recyclerViewAdapter.setItems(array);
                    recyclerViewAdapter.notifyDataSetChanged();
                    checkNextPrevButttonVisibility();
            }
        });

    }

    private void addItemToArray(String result) throws JSONException{

        Log.d("called addItemtoarray","started");

        JSONObject tempObj = new JSONObject(result);
        array = new JSONObject(result).getJSONArray("results");
        for(int i=0;i<array.length();i++){
            array.put(i,array.getJSONObject(i).put("isliked",false));
        }
        itemsQueried.add(array);
        nextToken = tempObj.getString("next_page_token");

        Log.d("called addItemtoarray","finished");
    }
    private void checkNextPrevButttonVisibility(){
        Log.d("nexttoken: ",String.valueOf(nextToken));
        Log.d("currentItem: ",String.valueOf(currentItem));
        //currentItem checks whihc page to set nextprev buttons
        if(currentItem==0){
            prev.setEnabled(false);

        }else{
            prev.setEnabled(true);
        }
        if(nextToken==null && currentItem==itemsQueried.size()-1){
            next.setEnabled(false);

        }else{
            next.setEnabled(true);
        }
    }
    private void initialize(){

            Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mToolbar.setTitle(getString(R.string.app_name));
            //go back arrow
            mToolbar.setNavigationIcon(R.drawable.backarrow);

            recyclerView = findViewById(R.id.placelist);
            next = findViewById(R.id.nextbutton);
            prev = findViewById(R.id.prevbutton);
            reccycleViewLayout =  findViewById(R.id.resultlayout);
        noresulttv = findViewById(R.id.noresulttv);
        //add default vertical manager
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(linearLayoutManager);
        //on click of this end it...
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)  {
        if (Integer.parseInt(android.os.Build.VERSION.SDK) > 5 && keyCode == KeyEvent.KEYCODE_BACK
                && event.getRepeatCount() == 0) {
           finish();
           return true;
        }
        return false;
    }
}
