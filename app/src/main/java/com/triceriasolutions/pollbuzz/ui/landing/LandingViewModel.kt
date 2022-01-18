package com.triceriasolutions.pollbuzz.ui.landing

import android.util.Log
import androidx.lifecycle.*
import androidx.paging.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.triceriasolutions.pollbuzz.data.models.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class LandingViewModel : ViewModel() {

    var userDetails = MutableLiveData<UserDetails>()

    var authorProfile = MutableLiveData<UserDetails>()
    val authorPolls = MutableLiveData<MutableList<Poll>>(mutableListOf())

    val votedPolls = MutableLiveData<MutableList<Poll>>(mutableListOf())

    val searchSortPolls = MutableLiveData<MutableList<Poll>>(mutableListOf())

    val destination = MutableLiveData("landing")

    val state = MutableLiveData("init")

    val homeFlag = MutableLiveData(true)

    val followingFlag = MutableLiveData(true)

    val myPollFlag = MutableLiveData(true)

    val voteState = MutableLiveData("init")

    lateinit var clickedPoll: Poll

    var followingSet = HashSet<String>()
    var votedSet = HashSet<String>()

    val paginatedPolls = homeFlag.switchMap {
        Log.d("home", "homeflag changed")
        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { FirestorePagingSource(FirebaseFirestore.getInstance()) }
        ).liveData.cachedIn(viewModelScope)
    }

//    val paginatedExpiredPolls = homeFlag.switchMap {
//        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
//            pagingSourceFactory = { ExpiredPollsPagingSource(FirebaseFirestore.getInstance()) }
//        ).liveData.cachedIn(viewModelScope)
//    }

    val paginatedFollowingPolls = followingFlag.switchMap {
        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { FollowingPagingSource(FirebaseFirestore.getInstance()) }
        ).liveData.cachedIn(viewModelScope)
    }

    val paginatedExpiredFollowingPolls = followingFlag.switchMap {
        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { FollowingExpiredPollsPagingSource(FirebaseFirestore.getInstance()) }
        ).liveData.cachedIn(viewModelScope)
    }

    val paginatedCreatedPolls = myPollFlag.switchMap {
        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = {
                MyPollPagingSource(
                    FirebaseFirestore.getInstance()
                )
            }
        ).liveData.cachedIn(viewModelScope)
    }

    val paginatedVotedPolls = myPollFlag.switchMap {
        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { FirestorePagingSource(FirebaseFirestore.getInstance()) }
        ).liveData.cachedIn(viewModelScope)
    }

    val paginatedAuthorPolls = authorProfile.switchMap {

        Pager(PagingConfig(pageSize = 15, maxSize = 50, enablePlaceholders = false),
            pagingSourceFactory = { AuthorPollsPagingSource(User(
                id = it.id, img = it.image, username = it.username
            ), FirebaseFirestore.getInstance()) }
        ).liveData.cachedIn(viewModelScope)
    }







    fun getUserDetails() {

        FirebaseFirestore.getInstance().collection("users")
            .document(FirebaseAuth.getInstance().uid.toString())
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.w("tag", "Listen failed.", error)
                    return@addSnapshotListener
                }
                if (value == null) {
                    return@addSnapshotListener
                }
                val data = value.toObject(UserDetails::class.java)
                if (data != null) {
                    userDetails.postValue(data!!)
                } else {
                    userDetails.postValue(UserDetails())
                }
            }


    }

    fun getAuthorProfile(id: String) {
        viewModelScope.launch {
            FirebaseFirestore.getInstance().collection("users").document(id)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.w("tag", "Listen failed.", error)
                        state.postValue("invalid")
                        return@addSnapshotListener
                    }
                    if (value == null) {
                        state.postValue("invalid")
                        return@addSnapshotListener
                    }
                    val data = value.toObject(UserDetails::class.java)
                    if (data != null) {
                        authorProfile.postValue(data!!)
                    }
                }

        }
    }

    fun followAuthor(authorId: String) {

        FirebaseFirestore.getInstance().collection("following")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("followingUsers")
            .document(authorId).set(object {
                val following: Boolean = true
            })

        FirebaseFirestore.getInstance().collection("users").document(authorId).get()
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("users").document(authorId)
                    .update("followers", it.get("followers").toString().toLong() + 1)
            }

        homeFlag.value = !homeFlag.value!!
        followingFlag.value = !followingFlag.value!!
    }

    fun unfollowAuthor(authorId: String) {
        FirebaseFirestore.getInstance().collection("following")
            .document(FirebaseAuth.getInstance().currentUser!!.uid)
            .collection("followingUsers")
            .document(authorId).delete()
        homeFlag.value = !homeFlag.value!!
        followingFlag.value = !followingFlag.value!!

        FirebaseFirestore.getInstance().collection("users").document(authorId).get()
            .addOnSuccessListener {
                FirebaseFirestore.getInstance().collection("users").document(authorId)
                    .update("followers", it.get("followers").toString().toLong() - 1)
            }
    }

    fun filterPolls(startDate: Long, endDate: Long) {

        viewModelScope.launch {
            val doc = FirebaseFirestore.getInstance().collection("polls")
                .whereGreaterThanOrEqualTo("creationTime", startDate)
                .whereLessThanOrEqualTo("creationTime", endDate).get().await()

            val polls = mutableListOf<Poll>()
            doc.toList().forEach {
                it.toObject(Poll::class.java)?.let { poll ->
                    poll.isVoted = votedSet.contains(poll.uid)
                    poll.following = followingSet.contains(poll.creatorId)
                    poll.created = poll.creatorId == userDetails.value!!.id
                    polls.add(poll)
                }
            }
            searchSortPolls.postValue(polls)
        }
    }

    fun searchPoll(id: String) {
        FirebaseFirestore.getInstance().collection("polls")
            .document(id)
            .get().addOnSuccessListener {
                it.toObject(Poll::class.java)?.let { poll ->
                    poll.isVoted = votedSet.contains(poll.uid)
                    poll.following = followingSet.contains(poll.creatorId)
                    poll.created = poll.creatorId == userDetails.value!!.id
                    searchSortPolls.postValue(mutableListOf(poll))
                }
            }
    }

    fun vote(poll: Poll, updatedOptions: List<PollOption>) {

        voteState.postValue("voting")

        val votedPolls = votedPolls.value!!.toMutableList()
        votedPolls.add(poll)

        viewModelScope.launch {

            FirebaseFirestore.getInstance().collection("voted")
                .document(FirebaseAuth.getInstance().currentUser!!.uid).collection("votedPolls")
                .document(poll.uid)
                .set(object {
                    val voted: Boolean = true
                }).await()

            val pollRef = FirebaseFirestore.getInstance().collection("polls")
                .document(poll.uid)

            pollRef.update("totalVote", poll.totalVote + 1).await()

            clickedPoll.totalVote++

            pollRef.update("options", updatedOptions).await()

            voteState.postValue("done")
        }

    }


}


