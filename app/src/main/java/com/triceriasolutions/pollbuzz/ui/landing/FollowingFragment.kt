package com.triceriasolutions.pollbuzz.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentFollowingBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp


class FollowingFragment : Fragment(), ClickListener {

    private var _binding: FragmentFollowingBinding? = null
    private val binding: FragmentFollowingBinding get() = _binding!!

    private lateinit var paginatedAdapter: FollowingAdapter
    private lateinit var expiredAdapter: FollowingAdapter
    private val viewModel: LandingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFollowingBinding.inflate(inflater)

        paginatedAdapter = FollowingAdapter(this)
        expiredAdapter = FollowingAdapter(this)

        binding.followingRv.adapter = paginatedAdapter
        binding.expiredRv.adapter = expiredAdapter


        viewModel.paginatedFollowingPolls.observe(viewLifecycleOwner, {

            paginatedAdapter.submitData(viewLifecycleOwner.lifecycle, it)

            Log.d("following", "adapter size: ${paginatedAdapter.itemCount}")
        })

        paginatedAdapter.addLoadStateListener {
            binding.placeholderText.isVisible = paginatedAdapter.itemCount == 0
        }

        expiredAdapter.addLoadStateListener {
            binding.placeholderText.isVisible = expiredAdapter.itemCount == 0
        }

        viewModel.paginatedExpiredFollowingPolls.observe(viewLifecycleOwner, {
            expiredAdapter.submitData(viewLifecycleOwner.lifecycle, it)
        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun openPoll(poll: Poll) {
        viewModel.clickedPoll = poll
        viewModel.destination.postValue("vote")
    }

    override fun showAuthorProfile(id: String) {
        viewModel.destination.postValue("profile-$id")
    }

    override fun showPollOptions(root: View, following: Boolean, poll: Poll) {
        val popUpMenu = PopupMenu(root.context, root)
        popUpMenu.inflate(
            if (following) R.menu.unfollow_popup_menu
            else if (poll.created) R.menu.poll_id_menu
            else R.menu.follow_popup_menu
        )
        popUpMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.follow_author_menu_item -> {
                    viewModel.followAuthor(poll.creatorId)
                }

                R.id.unfollow_author_menu_item -> {
                    viewModel.unfollowAuthor(poll.creatorId)
                }
                R.id.poll_id_menu_item -> {
                    showPollIdPopUp(poll.uid, requireContext(), layoutInflater)
                }
            }
            true
        }
        popUpMenu.show()
    }


}