package com.triceriasolutions.pollbuzz.data.models

import android.os.Parcelable
import com.google.firebase.firestore.Exclude
import kotlinx.android.parcel.Parcelize

@Parcelize
data class Poll(
    var uid: String = "",
    var creatorId: String = "",
    var creatorImage: String? = null,
    var creatorName: String = "",
    var creationTime: Long = 0L,
    var expiryTime: Long = 0L,
    var pollText: String = "",
    var isLive: Boolean = false,
    var isTextPoll: Boolean = true,
    var type: Int = 1,
    var options: List<PollOption> = emptyList(),
    var totalVote: Int = 0,
    @get:Exclude var following: Boolean = false,
    @get:Exclude var isVoted: Boolean = false,
    @get:Exclude var created: Boolean = false,
    @get:Exclude var expired: Boolean = false
): Parcelable
