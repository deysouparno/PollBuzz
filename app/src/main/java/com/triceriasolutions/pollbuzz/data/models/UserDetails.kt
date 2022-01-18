package com.triceriasolutions.pollbuzz.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class UserDetails(
    val id: String = "",
    var name: String = "",
    var email: String = "",
    var username: String = name,
    var dob: String = "",
    var gender: String = "",
    var image: String? = null,
    var followers: Long = 0L

): Parcelable
