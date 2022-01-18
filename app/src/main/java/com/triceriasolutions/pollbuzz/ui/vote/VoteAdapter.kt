package com.triceriasolutions.pollbuzz.ui.vote

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.data.models.PollOption
import com.triceriasolutions.pollbuzz.databinding.VoteImageItemBinding
import com.triceriasolutions.pollbuzz.databinding.VoteTextItemBinding
import com.triceriasolutions.pollbuzz.utils.VoteOptionClickListener

//code 1 means adapter for vote ... 2 means adapter for result
class ImageVoteAdapter(val poll: Poll, private val listener: VoteOptionClickListener, val code: Int) :
    RecyclerView.Adapter<ImageVoteViewHolder>() {

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageVoteViewHolder {
        return ImageVoteViewHolder.form(parent, poll, listener)
    }

    override fun onBindViewHolder(holder: ImageVoteViewHolder, position: Int) {
        holder.bind(poll = poll, pollOption = differ.currentList[position], code)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<PollOption>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }
}

class TextVoteAdapter(val poll: Poll, private val listener: VoteOptionClickListener, val code: Int) :
    RecyclerView.Adapter<TextVoteViewHolder>() {

    private val differ = AsyncListDiffer(this, diffCallback)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TextVoteViewHolder {
        return TextVoteViewHolder.form(parent, poll, listener)
    }

    override fun onBindViewHolder(holder: TextVoteViewHolder, position: Int) {
        holder.bind(poll = poll, pollOption = differ.currentList[position], code)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    fun submitList(list: List<PollOption>) {
        differ.submitList(list)
        notifyDataSetChanged()
    }
}

private val diffCallback = object : DiffUtil.ItemCallback<PollOption>() {

    override fun areItemsTheSame(oldItem: PollOption, newItem: PollOption): Boolean {
        return oldItem.text == newItem.text
    }

    override fun areContentsTheSame(oldItem: PollOption, newItem: PollOption): Boolean {
        return oldItem == newItem
    }

}

class TextVoteViewHolder(val binding: VoteTextItemBinding): RecyclerView.ViewHolder(binding.root) {
    companion object {
        fun form(
            parent: ViewGroup,
            poll: Poll,
            listener: VoteOptionClickListener
        ): TextVoteViewHolder {
            val binding = VoteTextItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            val viewHolder = TextVoteViewHolder(binding)

            binding.apply {

                radioButton.setOnClickListener {
                    listener.singleSelectOptionClicked(viewHolder.adapterPosition)
                }

                checkbox.setOnClickListener {
                    when (poll.type) {
                        2 -> listener.multiSelectOptionClicked(viewHolder.adapterPosition)
                        3 -> listener.priortySelectOptionClicked(viewHolder.adapterPosition)
                    }

                }

            }

            return viewHolder
        }
    }

    fun bind(poll: Poll, pollOption: PollOption, code: Int) {
        binding.apply {

            radioButton.isChecked = pollOption.isSelected
            checkbox.isChecked = pollOption.isSelected

            if (code == 2) {
                radioButton.isEnabled = false
                checkbox.isEnabled = false
            }

            when (poll.type) {
                1 -> {
                    radioButton.isVisible = true
                    checkbox.isVisible = false
                    radioButton.text = pollOption.text
                }

                else -> {
                    checkbox.isVisible = true
                    radioButton.isVisible = false
                    checkbox.text = pollOption.text
                }
            }
        }
    }
}


class ImageVoteViewHolder(val binding: VoteImageItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun form(
            parent: ViewGroup,
            poll: Poll,
            listener: VoteOptionClickListener
        ): ImageVoteViewHolder {
            val binding = VoteImageItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )

            val viewHolder = ImageVoteViewHolder(binding)

            binding.apply {

                radioButton.setOnClickListener {
                    listener.singleSelectOptionClicked(viewHolder.adapterPosition)
                }

                checkbox.setOnClickListener {
                    when (poll.type) {
                        2 -> listener.multiSelectOptionClicked(viewHolder.adapterPosition)
                        3 -> listener.priortySelectOptionClicked(viewHolder.adapterPosition)
                    }

                }

            }
            return viewHolder
        }
    }

    fun bind(poll: Poll, pollOption: PollOption, code: Int) {
        binding.apply {

            radioButton.isChecked = pollOption.isSelected
            checkbox.isChecked = pollOption.isSelected

            if (code == 2) {
                radioButton.isEnabled = false

                checkbox.isEnabled = false
            }

            when (poll.type) {
                1 -> {
                    radioButton.isVisible = true
                    checkbox.isVisible = false
                    radioButton.text = pollOption.text
                }

                else -> {
                    checkbox.isVisible = true
                    radioButton.isVisible = false
                    checkbox.text = pollOption.text
                }
            }

            Glide.with(optionImg.context)
                .load(pollOption.img)

                .into(optionImg)
        }
    }
}