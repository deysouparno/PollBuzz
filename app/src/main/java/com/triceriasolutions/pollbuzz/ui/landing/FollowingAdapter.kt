package com.triceriasolutions.pollbuzz.ui.landing

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.PollItemBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.getCreationTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.getExpiryTimeStringFromMillis

class FollowingAdapter(private val listener: ClickListener) :
    PagingDataAdapter<Poll, FollowingViewHolder>(Companion) {

    companion object : DiffUtil.ItemCallback<Poll>() {
        override fun areItemsTheSame(oldItem: Poll, newItem: Poll): Boolean {
            return oldItem.uid == newItem.uid
        }

        override fun areContentsTheSame(oldItem: Poll, newItem: Poll): Boolean {
            return oldItem == newItem
        }
    }

    override fun onBindViewHolder(holder: FollowingViewHolder, position: Int) {
        getItem(holder.bindingAdapterPosition)?.let { holder.bind(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FollowingViewHolder {

        val binding =
            PollItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val viewHolder = FollowingViewHolder(binding)


//        Log.d("paging", "poll is adapter is: ${poll.toString()}")


        binding.apply {
            pollOptions.setOnClickListener {
                getItem(viewHolder.bindingAdapterPosition)?.let {
                    listener.showPollOptions(
                        binding.pollOptions,
                        it.following,
                        it
                    )
                }
            }
            circleImageView.setOnClickListener {
                getItem(viewHolder.bindingAdapterPosition)?.let {
                    listener.showAuthorProfile(it.creatorId)
                }
            }
            binding.root.setOnClickListener {
                getItem(viewHolder.bindingAdapterPosition)?.let {
                    listener.openPoll(it)
                }
            }

            pollText.setOnClickListener {
                getItem(viewHolder.bindingAdapterPosition)?.let {
                    listener.openPoll(it)
                }
            }
        }

        return viewHolder

    }


}


class FollowingViewHolder(
    val binding: PollItemBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(poll: Poll) {

        poll.expired = System.currentTimeMillis() > poll.creationTime
        poll.created = poll.creatorId == FirebaseAuth.getInstance().currentUser!!.uid
        poll.following = true

        binding.apply {
            profileName.text = poll.creatorName
            pollText.text = poll.pollText
            liveText.isVisible = poll.isLive
            creatingTime.text = getCreationTimeStringFromMillis(poll.creationTime + 19800000)
            timeLeft.text = getExpiryTimeStringFromMillis(poll.expiryTime)
            poll.expired = timeLeft.text == "expired"

            FirebaseFirestore.getInstance().collection("voted")
                .document(FirebaseAuth.getInstance().currentUser!!.uid)
                .collection("votedPolls").document(poll.uid)
                .get().addOnSuccessListener {
                    if (it.exists()) {
                        poll.isVoted = true
                    }
                    voteCountDrawableLeft.setImageDrawable(
                        ContextCompat.getDrawable(
                            voteCountDrawableLeft.context,
                            if (poll.isVoted) R.drawable.green_vote_counter
                            else R.drawable.vote_counter
                        )
                    )
                }


            binding.root.setCardBackgroundColor(
                ContextCompat.getColor(
                    binding.root.context,
                    if (poll.expired) R.color.expired_color
                    else R.color.white
                )
            )


            followChip.setImageDrawable(
                ContextCompat.getDrawable(
                    followChip.context,
                    R.drawable.following
                )
            )


            if (poll.created) {
                followChip.isVisible = false
            }

            voteCount.text = poll.totalVote.toString()

            FirebaseFirestore.getInstance().collection("users").document(poll.creatorId)
                .get()
                .addOnSuccessListener {
                    Glide.with(circleImageView.context)
                        .load(it.getString("image"))
                        .error(R.drawable.author_profile)
                        .into(circleImageView)
                }
        }
    }

}