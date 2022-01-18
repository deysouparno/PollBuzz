package com.triceriasolutions.pollbuzz.ui.landing.profileScreen

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.paging.filter
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentVotedPollsBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingViewModel
import com.triceriasolutions.pollbuzz.ui.landing.PaginatedPollsAdapter
import com.triceriasolutions.pollbuzz.ui.landing.PollsAdapter
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.checkFollowing
import com.triceriasolutions.pollbuzz.utils.checkVoted
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp

class VotedPolls : Fragment(), ClickListener {

    private lateinit var adapter: PollsAdapter
    private lateinit var paginatedAdapter: PaginatedPollsAdapter
    private var _binding: FragmentVotedPollsBinding? = null
    private val binding: FragmentVotedPollsBinding get() = _binding!!
    private val viewModel: LandingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentVotedPollsBinding.inflate(inflater)
        adapter = PollsAdapter(this)
        paginatedAdapter = PaginatedPollsAdapter(this)
        binding.rvVotedpolls.adapter = paginatedAdapter

        viewModel.paginatedVotedPolls.observe(viewLifecycleOwner, {

            it.filter { poll ->
                checkVoted(poll.uid)
            }.map { poll ->
                poll.following = checkFollowing(poll.creatorId)
                poll.isVoted = true
                poll.expired = System.currentTimeMillis() > poll.creationTime
                poll.created = poll.creatorId == FirebaseAuth.getInstance().currentUser!!.uid
                poll
            }.also { data ->
                paginatedAdapter.submitData(viewLifecycleOwner.lifecycle, data)
                paginatedAdapter.addLoadStateListener {
                    binding.placeholderText.isVisible = paginatedAdapter.itemCount == 0
                }
            }

        })
        return binding.root
    }

    override fun openPoll(poll: Poll) {
        viewModel.clickedPoll = poll
        viewModel.destination.postValue("vote")
    }

    override fun showAuthorProfile(id: String) {

    }

    override fun showPollOptions(root: View, following: Boolean, poll: Poll) {
        val popUpMenu = PopupMenu(root.context, root)
        popUpMenu.inflate(
            if (following) R.menu.unfollow_popup_menu
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}