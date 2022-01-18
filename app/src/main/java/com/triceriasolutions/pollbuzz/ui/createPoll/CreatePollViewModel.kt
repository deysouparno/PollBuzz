package com.triceriasolutions.pollbuzz.ui.createPoll


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.triceriasolutions.pollbuzz.data.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*


class CreatePollViewModel : ViewModel() {
    var pollText = ""
    var count = 1
    var type = 1
    var isTextPoll = true
    var isLive = false
    var expiryTimeInMillis: Long = 0L
    var expirySeconds: Int = 0
    lateinit var userDetails: UserDetails
    val status = MutableLiveData("init")

    val imgOptions = mutableListOf(
        CreateOption(text = "", code = 1),
        CreateOption(text = "", code = 1)
    )
    val textOptions = mutableListOf(
        CreateOption(text = "", code = 1),
        CreateOption(text = "", code = 1)
    )
    private val pollOptions = mutableListOf<PollOption>()

    fun createLivePoll(seconds: Int) {
        expirySeconds = seconds + 1
        createPoll()
    }


    fun createPoll(): Boolean {
        if (pollText.isEmpty()) {
            return false
        }

        status.postValue("uploading")

        val uuid = UUID.randomUUID().toString().substring(0, 6)
        if (!isTextPoll) {
            imgOptions.forEach {
                pollOptions.add(PollOption(text = it.text))
            }
            viewModelScope.launch {
                uploadImages(uuid)
            }
        } else {
            textOptions.forEach {
                pollOptions.add(PollOption(text = it.text))
            }
            viewModelScope.launch {
                uploadPoll(uuid)
            }
        }

        return true
    }

    private suspend fun uploadImages(uuid: String) {
        for (i in imgOptions.indices) {

            if (imgOptions[i].img != null) {
                val ref = FirebaseStorage.getInstance().getReference("$uuid/option-${i + 1}")
                val imgUrl = ref.putFile(imgOptions[i].img!!).await()
                    .storage
                    .downloadUrl
                    .await()
                    .toString()
                pollOptions[i].img = imgUrl
                if (i == imgOptions.size - 1) {
                    uploadPoll(uuid)
                }

            }
        }
    }

    private suspend fun uploadPoll(uuid: String) {
        val poll = Poll(
            uid = uuid,
            pollText = this.pollText,
            creationTime = System.currentTimeMillis(),
            expiryTime = if (!isLive) expiryTimeInMillis else System.currentTimeMillis() + (expirySeconds * 1000),
            creatorId = userDetails.id,
            creatorImage = userDetails.image,
            creatorName = userDetails.name,
            options = pollOptions,
            type = type,
            isTextPoll = isTextPoll,
            isLive = isLive
        )
        FirebaseFirestore.getInstance().collection("polls").document(uuid)
            .set(poll).await()

//        val ref = FirebaseFirestore.getInstance().collection("users")
//            .document(FirebaseAuth.getInstance().currentUser!!.uid)
//
//        val data = ref.get().await().toObject(UserDetails::class.java)
//        val list = mutableListOf<String>()
//        if (data != null) {
//            list.addAll(data.createdPolls)
//        }
//        list.add(poll.uid)
//        ref.update("createdPolls", list).await()
        status.postValue("uploaded")
    }
}