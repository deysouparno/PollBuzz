package com.triceriasolutions.pollbuzz.ui.vote

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.data.models.PollOption
import com.triceriasolutions.pollbuzz.databinding.FragmentVoteImagePollBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingViewModel
import com.triceriasolutions.pollbuzz.utils.VoteOptionClickListener
import com.triceriasolutions.pollbuzz.utils.getCreationTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.getExpiryTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp


class VoteImagePollFragment : Fragment(), VoteOptionClickListener {

    private var _binding: FragmentVoteImagePollBinding? = null
    private val binding: FragmentVoteImagePollBinding get() = _binding!!

    private val viewModel: LandingViewModel by activityViewModels()

    private lateinit var adapter: ImageVoteAdapter

    private var singleSelectedOption = -1

    private var numOfOptionsSelected = 0

    private var priorityOptions = mutableListOf<PollOption>()

    private var canBackPress = true


    private lateinit var poll: Poll


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVoteImagePollBinding.inflate(inflater)
        viewModel.destination.postValue("home")

        val args: VoteImagePollFragmentArgs by navArgs()
        poll = args.poll

        adapter = ImageVoteAdapter(poll = poll, listener = this, code = 1)

        var totalVote = poll.totalVote


        when (poll.type) {
            1 -> {
                binding.pollType.text = "Single select"
                adapter.submitList(poll.options)
            }
            2 -> {
                binding.pollType.text = "Multiple select"
                adapter.submitList(poll.options)
            }
            3 -> {
                binding.pollType.text = "Priority Poll"
                priorityOptions = poll.options.map { it.copy() } as MutableList<PollOption>
                adapter.submitList(priorityOptions)
            }
        }

        binding.apply {

            liveText.isVisible = poll.isLive

            pollText.text = poll.pollText

            creatingTime.text = getCreationTimeStringFromMillis(poll.creationTime + 19800000)
            timeLeft.text = getExpiryTimeStringFromMillis(poll.expiryTime)

            voteCount.text = totalVote.toString()

            Glide.with(circleImageView.context)
                .load(poll.creatorImage)
                .placeholder(R.drawable.author_profile)
                .error(R.drawable.author_profile)
                .into(circleImageView)

            profileName.text = poll.creatorName

            imageVoteRv.adapter = adapter

            voteButton.setOnClickListener {
                when (poll.type) {
                    1 -> voteSingleSelectPoll()
                    2 -> voteMultipleSelectPoll()
                    3 -> votePriorityPoll()
                }
            }

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            circleImageView.setOnClickListener {
                viewModel.destination.postValue("profile-$id")
                findNavController().navigateUp()
            }

            setFollowDrawable()

            pollOptions.setOnClickListener {

                val popUpMenu = PopupMenu(pollOptions.context, pollOptions)
                popUpMenu.inflate(
                    if (poll.following) R.menu.unfollow_popup_menu
                    else if (poll.created) R.menu.poll_id_menu
                    else R.menu.follow_popup_menu
                )
                popUpMenu.setOnMenuItemClickListener { item ->
                    when (item.itemId) {
                        R.id.follow_author_menu_item -> {
                            viewModel.followAuthor(poll.creatorId)
                            poll.following = true
                            setFollowDrawable()
                        }

                        R.id.unfollow_author_menu_item -> {
                            viewModel.unfollowAuthor(poll.creatorId)
                            poll.following = false
                            setFollowDrawable()
                        }
                        R.id.poll_id_menu_item -> {
                            showPollIdPopUp(poll.uid, requireContext(), layoutInflater)
                        }
                    }
                    true
                }
                popUpMenu.show()
            }

            viewModel.voteState.observe(viewLifecycleOwner, {
                when (it) {
                    "voting" -> {
                        canBackPress = false
                        votingPlaceholder.isVisible = true
                        voteButton.isVisible = false
                    }
                    "done" -> {
                        canBackPress = true
                        poll.isVoted = true
                        findNavController().navigate(
                            VoteImagePollFragmentDirections
                                .actionVoteImagePollFragmentToResultImagePollFragment(poll)
                        )
                    }
                }
            })

        }

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (canBackPress) findNavController().navigateUp()
                else {
                    Toast.makeText(
                        context,
                        "Please wait for the poll to be submitted...",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })


        return binding.root
    }

    private fun setFollowDrawable() {
        binding.followChip.setImageDrawable(
            context?.getDrawable(
                if (poll.following) R.drawable.following
                else R.drawable.author_follow_button
            )
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun voteSingleSelectPoll() {
        if (singleSelectedOption < 0) {
            Toast.makeText(context, "No option selected...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.vote(poll = poll, updatedOptions = poll.options)
        }
    }

    private fun voteMultipleSelectPoll() {
        if (numOfOptionsSelected == 0) {
            Toast.makeText(context, "No option selected...", Toast.LENGTH_SHORT).show()
        } else {
            viewModel.vote(poll = poll, updatedOptions = poll.options)
        }
    }

    private fun votePriorityPoll() {
        if (numOfOptionsSelected < poll.options.size) {
            Toast.makeText(context, "You have to select all options", Toast.LENGTH_SHORT).show()
            return
        }
        var voteString = ""
        if (priorityOptions[0].vote == "0") {
            priorityOptions.forEach { _ -> voteString += "0 " }
            voteString = voteString.trim()
        }
        for (i in priorityOptions.indices) {
            val option = priorityOptions[i]
            if (option.vote == "0") {
                option.vote = voteString
            }

            val voteArr = option.vote.split(" ").map {
                it.toInt()
            }.toMutableList()
            voteArr[i]++
            option.vote = voteArr.toString().replace(",", "")
                .substringAfter("[").substringBefore("]")
        }

        poll.options = priorityOptions
        viewModel.vote(poll = poll, updatedOptions = poll.options)
    }


    override fun singleSelectOptionClicked(position: Int) {
        if (position == singleSelectedOption) {
            return
        }
        if (singleSelectedOption != -1) {
            poll.options[singleSelectedOption].isSelected = false
            poll.options[singleSelectedOption].vote =
                "${poll.options[singleSelectedOption].vote.toInt() - 1}"
            adapter.notifyItemChanged(singleSelectedOption)
        }
        singleSelectedOption = position
        poll.options[singleSelectedOption].isSelected = true
        poll.options[singleSelectedOption].vote =
            "${poll.options[singleSelectedOption].vote.toInt() + 1}"
        adapter.notifyItemChanged(singleSelectedOption)
    }

    override fun multiSelectOptionClicked(position: Int) {
        when (poll.options[position].isSelected) {
            true -> {
                poll.options[position].isSelected = false
                numOfOptionsSelected--
                poll.options[position].vote =
                    "${poll.options[position].vote.toInt() - 1}"
            }
            else -> {
                poll.options[position].isSelected = true
                numOfOptionsSelected++
                poll.options[position].vote =
                    "${poll.options[position].vote.toInt() + 1}"
            }
        }
    }

    override fun priortySelectOptionClicked(position: Int) {

        val option = priorityOptions[position]

        Log.d(
            "tag",
            "numofoptions is $numOfOptionsSelected option is selected: ${option.isSelected}"
        )

        if (priorityOptions[position].isSelected) {
            option.isSelected = false
            priorityOptions.removeAt(position)
            numOfOptionsSelected--
            priorityOptions.add(numOfOptionsSelected, option)
            adapter.notifyItemMoved(position, numOfOptionsSelected)
        } else {
            option.isSelected = true
            priorityOptions.removeAt(position)
            priorityOptions.add(numOfOptionsSelected, option)
            adapter.notifyItemMoved(position, numOfOptionsSelected)
            numOfOptionsSelected++
        }

    }


}