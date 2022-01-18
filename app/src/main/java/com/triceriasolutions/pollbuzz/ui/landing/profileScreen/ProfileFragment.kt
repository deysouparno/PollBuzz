package com.triceriasolutions.pollbuzz.ui.landing.profileScreen

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.databinding.FragmentProfileBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingViewModel
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding: FragmentProfileBinding get() = _binding!!
    private val firestore = Firebase.firestore
    private val firebaseUser: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
    private val viewModel: LandingViewModel by activityViewModels()
    private var selectedPhoto: Uri? = null

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }

    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater)

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                selectedPhoto = it
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.imageView3)
                uploadPhoto()
            }
        }

       handleImage()

        viewModel.userDetails.observe(viewLifecycleOwner, { userDetails ->

            Log.d("tag", "photo is ${userDetails.image}")
            Glide.with(binding.imageView3.context)
                .load(userDetails.image)
                .centerCrop()
                .placeholder(R.drawable.author_profile)
                .error(R.drawable.author_profile)
                .into(binding.imageView3)

            binding.textView.text = userDetails.name
            binding.textView3.text = "@${userDetails.username}"
            binding.followerCount.text = "${userDetails.followers} Followers"
        })



        binding.apply {

            tabProfile.addTab(tabProfile.newTab().setText("My Polls"))
            tabProfile.addTab(tabProfile.newTab().setText("Voted Polls"))
            tabProfile.tabGravity = TabLayout.GRAVITY_FILL
            val adapter = FragmentProfileAdapter(
                requireContext(), childFragmentManager,
                tabProfile.tabCount
            )
            pagerProfile.adapter = adapter
            pagerProfile.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabProfile))
            tabProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    pagerProfile.currentItem = tab.position
                }

                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })
        }
        return binding.root
    }

    private fun handleImage() {
        val items = arrayOf("Choose Image", "Remove Image")
        binding.imageView3.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setItems(items) { dialog, which ->
                    when (items[which]) {
                        "Choose Image" -> {
                            cropActivityResultLauncher.launch(null)
                        }
                        else -> {
                            imageView3.setImageDrawable(
                                AppCompatResources.getDrawable(
                                    requireContext(),
                                    R.drawable.author_profile
                                )
                            )
                            GlobalScope.launch {
                                firestore.collection("users")
                                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                                    .update("image", "").await()
                            }
                        }
                    }
                }
                .show()
        }
    }

    private fun uploadPhoto() {
        GlobalScope.launch(Dispatchers.IO) {
            if (selectedPhoto != null) {
                val ref = FirebaseStorage.getInstance()
                    .getReference("profileImages/${FirebaseAuth.getInstance().currentUser!!.uid}")
                val image = ref.putFile(selectedPhoto!!).await()
                    .storage
                    .downloadUrl
                    .await()
                    .toString()

                firestore.collection("users")
                    .document(FirebaseAuth.getInstance().currentUser!!.uid)
                    .update("image", image).await()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}