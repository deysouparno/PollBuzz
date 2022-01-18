package com.triceriasolutions.pollbuzz.ui.landing

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.PollItemBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.getCreationTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.getExpiryTimeStringFromMillis

class PollsAdapter(
    private val listener: ClickListener,
) : RecyclerView.Adapter<PollViewHolder>() {

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PollViewHolder {
        return PollViewHolder.form(parent, listener, differ)
    }

    override fun onBindViewHolder(holder: PollViewHolder, position: Int) {

        holder.bind(differ.currentList[position])
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<Poll>) {
        differ.submitList(list)
        notifyDataSetChanged()
        Log.d("tag", "submit list called")
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<Poll>() {

    override fun areItemsTheSame(oldItem: Poll, newItem: Poll): Boolean {
        return oldItem.uid == newItem.uid
    }

    override fun areContentsTheSame(oldItem: Poll, newItem: Poll): Boolean {
        return oldItem == newItem
    }

}


class PollViewHolder(val binding: PollItemBinding) : RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun form(
            parent: ViewGroup,
            listener: ClickListener,
            differ: AsyncListDiffer<Poll>
        ): PollViewHolder {
            val binding =
                PollItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

            val viewHolder = PollViewHolder(binding)

            binding.apply {
                pollOptions.setOnClickListener {
                    listener.showPollOptions(
                        binding.pollOptions,
                        differ.currentList[viewHolder.bindingAdapterPosition].following,
                        differ.currentList[viewHolder.bindingAdapterPosition]
                    )
                }
                circleImageView.setOnClickListener {
                    listener.showAuthorProfile(differ.currentList[viewHolder.bindingAdapterPosition].creatorId)
                }
                binding.root.setOnClickListener {
                    listener.openPoll(differ.currentList[viewHolder.adapterPosition])
                }

                pollText.setOnClickListener {
                    listener.openPoll(differ.currentList[viewHolder.adapterPosition])
                }
            }
            return viewHolder
        }
    }

    fun bind(poll: Poll) {
        binding.apply {
            profileName.text = poll.creatorName
            pollText.text = poll.pollText
            liveText.isVisible = poll.isLive
            creatingTime.text = getCreationTimeStringFromMillis(poll.creationTime + 19800000)
            timeLeft.text = getExpiryTimeStringFromMillis(poll.expiryTime)

            voteCountDrawableLeft.setImageDrawable(
                ContextCompat.getDrawable(
                    voteCountDrawableLeft.context,
                    if (poll.isVoted) R.drawable.green_vote_counter
                    else R.drawable.vote_counter
                )
            )

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
                    if (poll.following) R.drawable.following
                    else R.drawable.author_follow_button
                )
            )

            if (poll.created) {
                followChip.isVisible = false
            }

            voteCount.text = poll.totalVote.toString()


            Glide.with(circleImageView.context)
                .load(poll.creatorImage)
                .error(R.drawable.author_profile)
                .into(circleImageView)

        }
    }
}