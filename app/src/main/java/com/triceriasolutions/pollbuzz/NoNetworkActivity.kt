package com.triceriasolutions.pollbuzz

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.triceriasolutions.pollbuzz.utils.NetworkStatus
import com.triceriasolutions.pollbuzz.utils.NetworkStatusHelper

class NoNetworkActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_network)

        NetworkStatusHelper(context = this).observe(this, {
            when (it) {
                NetworkStatus.Available -> {
                    Log.d("network", "details connected")
                    finish()
                }
                NetworkStatus.Unavailable -> {

                }
            }
        })
    }

    override fun onBackPressed() {
        Toast.makeText(this, "Please check your network connection...", Toast.LENGTH_SHORT).show()
    }
}