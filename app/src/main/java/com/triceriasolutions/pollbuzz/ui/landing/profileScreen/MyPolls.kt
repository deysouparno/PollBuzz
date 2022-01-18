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
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentMyPollsBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingViewModel
import com.triceriasolutions.pollbuzz.ui.landing.PaginatedPollsAdapter
import com.triceriasolutions.pollbuzz.ui.landing.PollsAdapter
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp


class MyPolls : Fragment(), ClickListener {

    private lateinit var adapter: PollsAdapter
    private lateinit var paginatedAdapter: PaginatedPollsAdapter
    private var _binding: FragmentMyPollsBinding? = null
    private val binding: FragmentMyPollsBinding get() = _binding!!
    private val viewModel: LandingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMyPollsBinding.inflate(inflater)
        adapter = PollsAdapter(this)
        paginatedAdapter = PaginatedPollsAdapter(this)
        binding.rvMypoll.adapter = paginatedAdapter

        viewModel.myPollFlag.postValue(true)


        viewModel.paginatedCreatedPolls.observe(viewLifecycleOwner, {
            paginatedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            paginatedAdapter.addLoadStateListener {
                binding.placeholderText.isVisible = paginatedAdapter.itemCount == 0
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
        popUpMenu.inflate(R.menu.poll_id_menu)
        popUpMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
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