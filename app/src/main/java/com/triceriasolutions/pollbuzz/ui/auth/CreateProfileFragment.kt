package com.triceriasolutions.pollbuzz.ui.auth

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.UserDetails
import com.triceriasolutions.pollbuzz.databinding.FragmentCreateProfileBinding
import com.triceriasolutions.pollbuzz.utils.DatePickerFragment
import com.triceriasolutions.pollbuzz.utils.getMillisFromDate
import com.triceriasolutions.pollbuzz.utils.saveData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class CreateProfileFragment : Fragment() {

    private var _binding: FragmentCreateProfileBinding? = null
    private val binding get() = _binding!!
    private val mauth = FirebaseAuth.getInstance()
    private val firestore = Firebase.firestore
    var email: String = ""
    var image: String? = null
    var selectedPhoto: Uri? = null
    private val viewModel: AuthviewModel by activityViewModels()


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
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateProfileBinding.inflate(inflater)
        val args: CreateProfileFragmentArgs by navArgs()
        email = args.email
        image = args.image

        viewModel.flag = false
        val items = listOf("Male", "Female", "Other")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)
        (binding.gender as? AutoCompleteTextView)?.setAdapter(adapter)

        binding.apply {

            textInputLayout3.setEndIconOnClickListener { showDatePickerDialog() }

            birthDate.setOnClickListener { showDatePickerDialog() }

            createProfileBt.setOnClickListener { createProfile() }

            Glide.with(binding.profileImg.context)
                .load(image)
                .centerCrop()
                .circleCrop()
                .error(R.drawable.profile)
                .into(binding.profileImg)
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                selectedPhoto = it
                Glide.with(this)
                    .load(it)
                    .centerCrop()
                    .circleCrop()
                    .into(binding.profileImg)
            }
        }

        binding.profileImg.setOnClickListener {
            cropActivityResultLauncher.launch(null)
        }

        return binding.root
    }

    private fun createProfile() {
        if (binding.fullName.text.isNullOrBlank()) {
            binding.textInputLayout1.error = "Enter name"
        }
        else if (binding.username.text.isNullOrBlank()) {
            binding.textInputLayout2.error = "Enter username"
        }
        else if (binding.birthDate.text.isNullOrBlank()) {
            binding.textInputLayout3.error = "Enter DOB"
        }
        else if (binding.gender.text.isNullOrBlank()) {
            binding.textInputLayout4.error = "Enter gender"
        }
        else {
            GlobalScope.launch(Dispatchers.IO) {
                if (selectedPhoto != null) {
                    val ref = FirebaseStorage.getInstance()
                        .getReference("profileImages/${mauth.currentUser!!.uid}")
                    image = ref.putFile(selectedPhoto!!).await()
                        .storage
                        .downloadUrl
                        .await()
                        .toString()
                }
                val userDetails = UserDetails(
                    id = mauth.currentUser!!.uid,
                    name = binding.fullName.text.toString(),
                    username = binding.username.text.toString(),
                    dob = binding.birthDate.text.toString(),
                    gender = binding.gender.text.toString(),
                    image = image,
                    email = email
                )
                firestore.collection("users").document(mauth.currentUser!!.uid).set(userDetails)
                    .addOnCompleteListener {
                        if (it.isSuccessful) {

                            saveData(
                                context,
                                userDetails = userDetails
                            )

                            findNavController().navigate(
                                CreateProfileFragmentDirections.actionCreateProfileFragmentToLandingActivity()
                            )
                            Toast.makeText(
                                context,
                                "SignUp Successful...",
                                Toast.LENGTH_SHORT
                            ).show()
                            activity?.finish()

                        } else {
                            Toast.makeText(context, "Something went wrong...", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
            }

        }

    }

    private fun showDatePickerDialog() {
        val newFragment = DatePickerFragment { year, month, day ->
            var dateString = "$day/$month/$year"
            if (day - 10 < 0) {
                dateString = "0$day/$month/$year"
            }
            if (month - 10 < 0) {
                dateString = "$day/0$month/$year"
            }
            if (System.currentTimeMillis() < getMillisFromDate(dateString)) {
                binding.textInputLayout3.error = "Invalid Birthdate"
            } else {
                binding.textInputLayout3.error = null
                binding.birthDate.setText("$day/$month/$year")
            }
        }
        newFragment.show(childFragmentManager, "datePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}