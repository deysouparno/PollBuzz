package com.triceriasolutions.pollbuzz.ui.landing

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.triceriasolutions.pollbuzz.NoNetworkActivity
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.ActivityLandingBinding
import com.triceriasolutions.pollbuzz.ui.auth.AuthActivity
import com.triceriasolutions.pollbuzz.utils.NetworkStatus
import com.triceriasolutions.pollbuzz.utils.NetworkStatusHelper
import com.triceriasolutions.pollbuzz.utils.getUser

class LandingActivity : AppCompatActivity() {

    private var _binding: ActivityLandingBinding? = null
    private val binding: ActivityLandingBinding get() = _binding!!
    private val viewModel: LandingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityLandingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FirebaseAuth.getInstance().currentUser?.uid == null) {
            val intent = Intent(this, AuthActivity::class.java)
            startActivity(intent)
            finish()
        }

        handleIntent()

        viewModel.getUserDetails()

        viewModel.userDetails.observe(this, { userDetails ->
            if (userDetails.name == "") {
                val intent = Intent(this, AuthActivity::class.java)
                startActivity(intent)
                finish()
            }

        })


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

    private fun handleIntent() {

        val data = intent.data

        Log.d("deeplink", "intent called")

        if (data != null) {
            val id = data.lastPathSegment
            findNavController(R.id.landingHostFragment).handleDeepLink(intent)
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        handleIntent()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}