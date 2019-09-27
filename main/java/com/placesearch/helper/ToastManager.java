package com.placesearch.helper;

import android.content.Context;
import android.widget.Toast;

public class ToastManager {


    private Context context;
    public ToastManager(Context context){
        this.context=context;
    }

    public void showToast(String message,int flag){
// by default two flags, short: wait for small time and long: longer time
        Toast toast = Toast.makeText(context,message,flag);
       /* View toastview = toast.getView();
        toastview.setBackgroundResource(R.drawable.toast_drawable);
        TextView text = toastview.findViewById(android.R.id.message);
        text.setTextColor(getResources().getColor(android.R.color.black));*/
        toast.show();
    }
}
