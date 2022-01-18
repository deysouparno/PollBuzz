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
import androidx.paging.flatMap
import androidx.paging.map
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentHomeBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.checkFollowing
import com.triceriasolutions.pollbuzz.utils.checkVoted
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp
import kotlin.math.exp


class HomeFragment : Fragment(), ClickListener {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LandingViewModel by activityViewModels()
    private lateinit var adapter: PollsAdapter
    private lateinit var paginatedAdapter: PaginatedPollsAdapter
    private lateinit var expiredAdapter: PaginatedPollsAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater)
        adapter = PollsAdapter(this)
        paginatedAdapter = PaginatedPollsAdapter(this)
        expiredAdapter = PaginatedPollsAdapter(this)
        binding.homeRv.adapter = paginatedAdapter
        binding.expiredPolls.adapter = expiredAdapter



        binding.showExpiredPolls.setOnClickListener {
            binding.expiredPolls.isVisible = true
            binding.showExpiredPolls.isVisible = false
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.homeFlag.value = !viewModel.homeFlag.value!!
        }

        viewModel.paginatedPolls.observe(viewLifecycleOwner, {
            paginatedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            binding.homeShimmer.isVisible = false
            binding.swipeRefreshLayout.isRefreshing = false
        })

//        viewModel.paginatedExpiredPolls.observe(viewLifecycleOwner, {
//            expiredAdapter.submitData(viewLifecycleOwner.lifecycle, it)
//            binding.homeShimmer.isVisible = false
//        })

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun openPoll(poll: Poll) {
//        Toast.makeText(context, "isVoted: ")
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