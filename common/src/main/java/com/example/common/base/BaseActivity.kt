package com.example.common.base

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.common.utils.Cons

open class BaseActivity : AppCompatActivity() {

     val TAG = Cons.TAG
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: BaseActivity")
    }
}