package com.example.tracknscan.helpers

import android.content.Context
import android.widget.Toast

fun throwToast(context: Context, msg: String){
    Toast.makeText(
        context.applicationContext, msg,
        Toast.LENGTH_SHORT
    ).show()
}