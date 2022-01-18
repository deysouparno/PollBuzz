package com.triceriasolutions.pollbuzz.ui.createPoll

import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.triceriasolutions.pollbuzz.NoNetworkActivity
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.databinding.ActivityCreatePollBinding
import com.triceriasolutions.pollbuzz.databinding.CreatePollBackPopUpBinding
import com.triceriasolutions.pollbuzz.utils.NetworkStatus
import com.triceriasolutions.pollbuzz.utils.NetworkStatusHelper
import com.triceriasolutions.pollbuzz.utils.getUser

class CreatePollActivity : AppCompatActivity() {
    private var _binding: ActivityCreatePollBinding? = null
    private val binding: ActivityCreatePollBinding get() = _binding!!
    private val viewModel: CreatePollViewModel by viewModels()
    private var canBackPress = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCreatePollBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.userDetails = getUser(this)

        viewModel.status.observe(this, {
            when (it) {
                "uploading" -> {
                    canBackPress = false
                    binding.votingPlaceholder.isVisible = true
                }
                "uploaded" -> {
                    canBackPress = true
                    finish()
                }
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



        binding.apply {
            createPollViewPager.adapter = CreatePollSwipeViewAdapter(this@CreatePollActivity)
//            createPollTab.tabGravity = TabLayout.GRAVITY_FILL
            createPollTab.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener  {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    tab?.icon?.setTint(getColor(R.color.white))
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                    tab?.icon?.setTint(getColor(R.color.colorPrimary))
                }

                override fun onTabReselected(tab: TabLayout.Tab?) = Unit

            })
            TabLayoutMediator(createPollTab, createPollViewPager) { tab, position ->
                when (position) {
                    0 -> {
                        tab.text = "Text Poll"
                        tab.setIcon(R.drawable.option_text_poll_icon)
                    }
                    1 -> {
                        tab.text = "Image Poll"
                        tab.setIcon(R.drawable.option_image_poll_icon)
                    }
                }
            }.attach()

            backButton.setOnClickListener {
                onBackPressed()
            }

            pollQuery.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    viewModel.pollText = s.toString()

                }

                override fun afterTextChanged(s: Editable?) {
                    viewModel.pollText = s.toString()
                }

            })
        }

    }

    override fun onBackPressed() {

        val builder = AlertDialog.Builder(this)

        val popUpBinding = CreatePollBackPopUpBinding.inflate(layoutInflater)

        var dialog: AlertDialog? = null

        popUpBinding.apply {
            okButton.setOnClickListener {
                dialog?.cancel()
                super.onBackPressed()
            }

            cancelButton.setOnClickListener {
                dialog?.let {
                    it.cancel()
                }
            }
        }

        builder.setView(popUpBinding.root)

        dialog = builder.create()

        dialog.show()

    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }
}