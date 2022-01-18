package com.triceriasolutions.pollbuzz.utils

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triceriasolutions.pollbuzz.data.models.UserDetails
import com.triceriasolutions.pollbuzz.databinding.PollIdPopUpBinding
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat


fun getCreationTimeStringFromMillis(currentMillis: Long): String {
    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    val dateString = simpleDateFormat.format(currentMillis)
    val creationDay = dateString.split("/").first().toInt()
    val today = simpleDateFormat.format(System.currentTimeMillis()).split("/").first().toInt()
    return when (today-creationDay) {
        0 -> "today"
        1 -> "yesterday"
        2 -> "2 days ago"
        else -> dateString
    }
}

fun getExpiryTimeStringFromMillis(millis: Long): String {
    val simpleDateFormat = SimpleDateFormat("dd/MM/yyyy")
    val dateString = simpleDateFormat.format(millis)
    val expiryDay = dateString.split("/").first().toInt()
    val today = simpleDateFormat.format(System.currentTimeMillis()).split("/").first().toInt()
    if (System.currentTimeMillis() > millis) {
        return "expired"
    }
    return when (expiryDay - today) {
        0 -> "today"
        1 -> "1 day left"
        2 -> "2 days left"
        else -> dateString
    }
}

fun getMillisFromDate(date: String): Long {

    return SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        .parse("$date 23:59:59")
        .time
}

fun saveData(
    context: Context?,
    userDetails: UserDetails
) {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val editor = sharedPref?.edit()
    editor?.apply {
        putString("id", userDetails.id)
        putString("name", userDetails.name)
        putString("username", userDetails.username)
        putString("email", userDetails.email)
        putString("image", userDetails.image)
        putString("gender", userDetails.gender)
        apply()
    }

}

fun getUser(context: Context?): UserDetails {
    val sharedPref = context?.getSharedPreferences("user", Context.MODE_PRIVATE)
    val id = sharedPref?.getString("id", "") ?: ""
    val name = sharedPref?.getString("name", "") ?: ""
    val username = sharedPref?.getString("username", "") ?: ""
    val email = sharedPref?.getString("email", "") ?: ""
    val image = sharedPref?.getString("image", "") ?: ""
    val gender = sharedPref?.getString("gender", "") ?: ""
    return UserDetails(
        id = id,
        name = name,
        email = email,
        image = image,
        username = username,
        gender = gender,
        dob = ""
    )
}

fun showPollIdPopUp(id: String, context: Context, layoutInflater: LayoutInflater) {
    val builder = AlertDialog.Builder(context)

    val popUpBinding = PollIdPopUpBinding.inflate(layoutInflater)

    var dialog: AlertDialog? = null

    popUpBinding.apply {

        pollIdTextView.text = id

        okButton.setOnClickListener {
            dialog?.cancel()
        }

        shareButton.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type="text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, id);
            startActivity(context, Intent.createChooser(shareIntent,"Share Poll Id"), null)
        }

        copyButton.setOnClickListener {

            val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clipData = ClipData.newPlainText("text", id)
            clipboardManager.setPrimaryClip(clipData)
        }
    }

    builder.setView(popUpBinding.root)

    dialog = builder.create()

    dialog.show()
}

suspend fun checkFollowing(id: String): Boolean {
    return FirebaseFirestore.getInstance().collection("following")
        .document(FirebaseAuth.getInstance().currentUser!!.uid)
        .collection("followingUsers").document(id)
        .get().await().exists()
}

suspend fun checkVoted(id: String): Boolean {
    return FirebaseFirestore.getInstance().collection("voted")
        .document(FirebaseAuth.getInstance().currentUser!!.uid)
        .collection("votedPolls").document(id)
        .get().await().exists()
}

fun getTextWatcher(textView: TextInputLayout): TextWatcher {

    return object: TextWatcher {
        override fun beforeTextChanged(
            s: CharSequence?,
            start: Int,
            count: Int,
            after: Int
        ) = Unit

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            textView.error = null
            print("debug --> $s")
        }

        override fun afterTextChanged(s: Editable?) = Unit

    }
}