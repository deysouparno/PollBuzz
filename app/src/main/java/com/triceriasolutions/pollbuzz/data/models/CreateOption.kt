package com.triceriasolutions.pollbuzz.data.models

import android.net.Uri
import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

data class CreateOption(
    var text: String = "",
    var img: Uri? = null,
    val code: Int // code 1 for first two options as they are not removable and code 2 for other options
)

@Parcelize
data class PollOption(
    val text: String = "",
    var img: String? = null,
    var vote: String = "0",
    @get:Exclude var isSelected: Boolean = false
) : Parcelable