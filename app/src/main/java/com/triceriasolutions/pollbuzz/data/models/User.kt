package com.triceriasolutions.pollbuzz.data.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
data class User(
    val id: String = "",
    var img: String? = null,
    var username:String = "",
): Parcelable
