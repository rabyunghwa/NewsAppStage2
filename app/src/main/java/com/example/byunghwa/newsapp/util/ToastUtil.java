package com.example.byunghwa.newsapp.util;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by SAMSUNG on 11/22/2017.
 */

public class ToastUtil {

    public static void showToastMessage(Context context,  int stringResId) {
        Toast.makeText(context, stringResId, Toast.LENGTH_SHORT).show();
    }
}
