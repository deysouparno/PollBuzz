package com.triceriasolutions.pollbuzz.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.triceriasolutions.pollbuzz.data.models.UserDetails
import com.triceriasolutions.pollbuzz.databinding.FragmentSignupBinding
import com.triceriasolutions.pollbuzz.utils.getTextWatcher
import com.triceriasolutions.pollbuzz.utils.saveData


class SignUpFragment : Fragment() {

    private var _binding: FragmentSignupBinding? = null

    private val binding get() = _binding!!
    private val mauth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSignupBinding.inflate(inflater, container, false)
        binding.apply {
            signUpButton.setOnClickListener {
                signup()
            }
            loginText.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        binding.apply {
            signUpEmail.addTextChangedListener(getTextWatcher(textInputLayout1))
            signUpPassword.addTextChangedListener(getTextWatcher(textInputLayout2))
            confirmPassword.addTextChangedListener(getTextWatcher(textInputLayout3))
        }

        return binding.root

    }

    private fun signup() {
        if (binding.signUpEmail.text.isNullOrBlank()) {
            binding.textInputLayout1.error = "Enter email"
        } else if (binding.signUpPassword.text.isNullOrBlank()) {
            binding.textInputLayout2.error = "Enter password"
        } else if (binding.confirmPassword.text.isNullOrBlank()) {
            binding.textInputLayout3.error = "confirm password"
        } else if (binding.confirmPassword.text.toString() != binding.signUpPassword.text.toString()) {
            binding.textInputLayout3.error = "Passwords do not match"
        } else if (binding.signUpPassword.text!!.length < 6) {
            binding.textInputLayout3.error = "Password Length should be minimum 6"
        } else if (binding.signUpPassword.text!!.length > 12) {
            binding.textInputLayout3.error = "Password too long"
        } else {
            binding.signPlaceholder.isVisible = true
            binding.signUpButton.isVisible = false
            signUpWithEmailPassword(
                binding.signUpEmail.text.toString(),
                binding.signUpPassword.text.toString()
            )
        }
    }

    private fun signUpWithEmailPassword(email: String, password: String) {

        mauth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {

                saveData(context, UserDetails(id = it.user!!.uid, email = email))
                findNavController().navigate(
                    SignUpFragmentDirections.actionSignUpFragmentToCreateProfileFragment(
                        email,
                        it.user!!.photoUrl.toString()
                    )
                )
                binding.signPlaceholder.isVisible = false
                binding.signUpButton.isVisible = true
                Toast.makeText(
                    context,
                    "SignUp Successful..",
                    Toast.LENGTH_SHORT
                ).show()

            }.addOnFailureListener {
                binding.signPlaceholder.isVisible = false
                binding.signUpButton.isVisible = true
                Toast.makeText(
                    context,
                    "Something went wrong...\n${it.toString()}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}