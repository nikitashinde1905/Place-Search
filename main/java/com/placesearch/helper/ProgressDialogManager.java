package com.placesearch.helper;

import android.content.Context;


public class ProgressDialogManager {

    Context context;
    android.app.ProgressDialog pDialog;

    public ProgressDialogManager(Context context){
        this.context =context;
    }

    public void showDialog(String message){
        pDialog = new android.app.ProgressDialog(context);
        pDialog.setMessage(message);
        pDialog.setIndeterminate(false);
        pDialog.setCancelable(false);
        pDialog.show();
    }

    public void dismissDialog(){
        if(pDialog!=null){
            pDialog.dismiss();
        }
    }
}
