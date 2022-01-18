package com.triceriasolutions.pollbuzz.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.triceriasolutions.pollbuzz.data.models.UserDetails
import com.triceriasolutions.pollbuzz.databinding.FragmentLoginBinding
import com.triceriasolutions.pollbuzz.utils.getTextWatcher
import com.triceriasolutions.pollbuzz.utils.saveData


class LoginFragment : Fragment() {

    private val RC_SIGN_IN: Int = 34
    private lateinit var googleSignInClient: GoogleSignInClient
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val mauth: FirebaseAuth = FirebaseAuth.getInstance()
    private val mfirebaseStore = Firebase.firestore
    private var photoUrl: String? = null
    private val viewModel: AuthviewModel by activityViewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (mauth.currentUser != null) {
            if (viewModel.flag)
                checkUser()
        }

        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        // Configure Google Sign In
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("870823966992-k79ccskftekt0c6cc9o4etd17en6rc0k.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)
        binding.createAccount.setOnClickListener {
        }
        binding.loginB.setOnClickListener {
            login()
        }
        binding.gsignin.setOnClickListener {
            signInWithGoogle()
        }
        binding.createAccount.setOnClickListener {
            findNavController().navigate(
                LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            )
        }

        binding.apply {
            loginEmail.addTextChangedListener(getTextWatcher(textInputLayout1))
            loginPassword.addTextChangedListener(getTextWatcher(textInputLayout2))
        }
        binding.forgetPassword.setOnClickListener {
            context?.startActivity(Intent(context, ForgotPassword::class.java))
        }
        return binding.root

    }

    private fun signInWithGoogle() {

        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                // Google Sign In was successful, authenticate with Firebase
                val account = task.getResult(ApiException::class.java)!!
                photoUrl = account.photoUrl?.toString()

                firebaseAuthWithGoogle(account.idToken!!)
            } catch (e: ApiException) {
                // Google Sign In failed, update UI appropriately
            }
        }
    }

    private fun firebaseAuthWithGoogle(idToken: String) {
        binding.loginPlaceholder.isVisible = true
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        mauth.signInWithCredential(credential)
            .addOnCompleteListener { task ->

                if (task.isSuccessful && task.result.user != null) {
                    checkUser()
                } else {
                    Toast.makeText(context, task.exception?.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                    binding.loginPlaceholder.isVisible = false
                }
            }
    }


    private fun login() {
        if (binding.loginEmail.text.toString() == "") {
            binding.textInputLayout1.error = "Enter a valid email"
        } else if (binding.loginPassword.text.toString() == "") {
            binding.textInputLayout2.error = "Enter a valid password"
        } else {
            binding.loginPlaceholder.isVisible = true
            mauth.signInWithEmailAndPassword(
                binding.loginEmail.text.toString(),
                binding.loginPassword.text.toString()
            ).addOnCompleteListener {
                if (it.isSuccessful) {
                    checkUser()
                } else {
                    binding.loginPlaceholder.isVisible = false
                    Toast.makeText(context, it.exception?.localizedMessage, Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }
    }

    private fun checkUser() {

        val firebaseUser = mauth.currentUser!!
        mfirebaseStore.collection("users").document(mauth.uid.toString())
            .get().addOnSuccessListener {

                if (it.exists()) {
                    val data = it.toObject(UserDetails::class.java)
                    data?.let { it1 ->
                        saveData(
                            context,
                            userDetails = it1
                        )
                    }

                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToLandingActivity())
                    activity?.finish()

                } else {

                    findNavController().navigate(
                        LoginFragmentDirections.actionLoginFragmentToCreateProfileFragment(
                            email = firebaseUser.email!!,
                            image = photoUrl,
                        )
                    )
                    binding.loginPlaceholder.isVisible = false
                }
            }
            .addOnFailureListener {
                Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
                binding.loginPlaceholder.isVisible = false
            }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}