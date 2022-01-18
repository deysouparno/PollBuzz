package com.triceriasolutions.pollbuzz.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity
import com.triceriasolutions.pollbuzz.NoNetworkActivity

import com.triceriasolutions.pollbuzz.databinding.ActivityAuthBinding
import com.triceriasolutions.pollbuzz.utils.NetworkStatus
import com.triceriasolutions.pollbuzz.utils.NetworkStatusHelper

class AuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAuthBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        NetworkStatusHelper(context = this).observe(this, {
            when (it) {
                NetworkStatus.Available -> {
                    Log.d("network", "details connected")
                }
                NetworkStatus.Unavailable -> {
                    startActivity(Intent(this, NoNetworkActivity::class.java))
                }
            }
        })

    }


}