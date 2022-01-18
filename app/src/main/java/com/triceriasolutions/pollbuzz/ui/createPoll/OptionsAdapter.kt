package com.triceriasolutions.pollbuzz.ui.createPoll

import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.triceriasolutions.pollbuzz.data.models.CreateOption
import com.triceriasolutions.pollbuzz.databinding.ImagePollOptionItemBinding
import com.triceriasolutions.pollbuzz.databinding.TextPollOptionItemBinding
import com.triceriasolutions.pollbuzz.utils.OptionClickListener

class OptionsAdapter(
    private val code: Int = 1,
    private val optionClickListener: OptionClickListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun getItemViewType(position: Int): Int {
        return code
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            1 -> TextOptionsViewHolder.form(parent, optionClickListener)
            else -> ImageOptionViewHolder.form(parent, optionClickListener)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (code) {
            1 -> (holder as TextOptionsViewHolder).bind(differ.currentList[position])
            else -> (holder as ImageOptionViewHolder).bind(differ.currentList[position])
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    private val differ = AsyncListDiffer(this, diffCallback)

    fun submitOptions(list: List<CreateOption>) {
        differ.submitList(list)
    }

}

private val diffCallback = object : DiffUtil.ItemCallback<CreateOption>() {

    override fun areItemsTheSame(oldItem: CreateOption, newItem: CreateOption): Boolean {
        return oldItem.hashCode() == newItem.hashCode()
    }

    override fun areContentsTheSame(oldItem: CreateOption, newItem: CreateOption): Boolean {
        return oldItem == newItem
    }

}

class TextOptionsViewHolder(val binding: TextPollOptionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun form(parent: ViewGroup, optionClickListener: OptionClickListener): TextOptionsViewHolder {
            val binding =
                TextPollOptionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            val viewHolder = TextOptionsViewHolder(binding)

            binding.deleteOption.setOnClickListener {
                optionClickListener.deleteOption(viewHolder.adapterPosition)
            }

            binding.optionText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    optionClickListener.updateOptionText(viewHolder.adapterPosition, s.toString())
                }

                override fun afterTextChanged(s: Editable?) = Unit

            })


            return viewHolder
        }
    }

    fun bind(createOption: CreateOption) {
        binding.apply {
            optionText.setText(createOption.text)
            deleteOption.isVisible = createOption.code == 2
        }
    }

}

class ImageOptionViewHolder(val binding: ImagePollOptionItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    companion object {
        fun form(
            parent: ViewGroup,
            optionClickListener: OptionClickListener,
        ): ImageOptionViewHolder {

            val binding =
                ImagePollOptionItemBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            val viewHolder = ImageOptionViewHolder(binding)

            binding.optionImg.setOnClickListener {
                optionClickListener.chooseImage(binding, position = viewHolder.adapterPosition)
            }

            binding.deleteOption.setOnClickListener {
                optionClickListener.deleteOption(viewHolder.adapterPosition)
            }

            binding.optionText.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) = Unit

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                    optionClickListener.updateOptionText(viewHolder.adapterPosition, s.toString())
                }

                override fun afterTextChanged(s: Editable?) = Unit

            })


            return viewHolder
        }
    }

    fun bind(createOption: CreateOption) {

        binding.apply {
            optionText.setText(createOption.text)
            deleteOption.isVisible = createOption.code == 2
            if (createOption.img != null) {
                Glide.with(binding.optionImg.context)
                    .load(createOption.img)
                    .into(binding.optionImg)
            }
        }
    }

}