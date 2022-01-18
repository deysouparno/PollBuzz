package com.triceriasolutions.pollbuzz.ui.auth

import android.app.Activity
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.triceriasolutions.pollbuzz.R
import kotlinx.android.synthetic.main.activity_forgot_password.*

class ForgotPassword : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forgot_password)
        findViewById<Button>(R.id.submit_forgot).setOnClickListener {
            val email = email_forgot.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Enter email", Toast.LENGTH_SHORT).show()
            } else {
                hideKeyboard()
                forgot_password_placeholder.isVisible = true
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnCompleteListener {
                    if (it.isSuccessful) {
                        Toast.makeText(
                            this,
                            "Email sent successfully to reset your password",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    } else {
                        Toast.makeText(this, it.exception?.localizedMessage, Toast.LENGTH_SHORT)
                            .show()
                    }
                    forgot_password_placeholder.isVisible = false

                }
            }
        }
    }

    private fun hideKeyboard() {

        (this.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager)
            .hideSoftInputFromWindow(email_forgot.windowToken, 0)

    }
}