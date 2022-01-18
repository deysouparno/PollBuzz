package com.triceriasolutions.pollbuzz.utils

import android.view.View
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.data.models.PollOption
import com.triceriasolutions.pollbuzz.databinding.ImagePollOptionItemBinding

interface ClickListener {

    fun openPoll(poll: Poll)
    fun showAuthorProfile(id: String)
    fun showPollOptions(root: View, following: Boolean, poll: Poll)
}

interface OptionClickListener {
    fun chooseImage(binding: ImagePollOptionItemBinding, position: Int)
    fun updateOptionText(position: Int, text: String)
    fun deleteOption(position: Int)
}

interface VoteOptionClickListener {
    fun singleSelectOptionClicked(position: Int)
    fun multiSelectOptionClicked(position: Int)
    fun priortySelectOptionClicked(position: Int)
}