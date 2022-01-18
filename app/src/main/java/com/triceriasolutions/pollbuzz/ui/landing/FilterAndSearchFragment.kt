package com.triceriasolutions.pollbuzz.ui.landing

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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentFilterAndSearchBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.DatePickerFragment
import com.triceriasolutions.pollbuzz.utils.getMillisFromDate
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp
import java.text.SimpleDateFormat


class FilterAndSearchFragment : Fragment(), ClickListener {

    private var _binding: FragmentFilterAndSearchBinding? = null
    private val binding: FragmentFilterAndSearchBinding get() = _binding!!
    private val viewModel: LandingViewModel by activityViewModels()
    private lateinit var adapter: PollsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFilterAndSearchBinding.inflate(inflater)
        adapter = PollsAdapter(this)

        val args: FilterAndSearchFragmentArgs by navArgs()
        val code = args.code

        binding.apply {

            searchLayout.isVisible = code == 2
            filterLayout.isVisible = code == 1

            searchSortRv.adapter = adapter

            filterButton.setOnClickListener {
                searchSortShimmer.isVisible = true
                if (startDate.text.isNullOrEmpty()) {
                    startDate.error = "select starting date"
                }
                if (endDate.text.isNullOrEmpty()) {
                    endDate.error = "select starting date"
                }

                if (!startDate.text.isNullOrEmpty() && !endDate.text.isNullOrEmpty()) {

                    viewModel.filterPolls(
                        SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
                            .parse("${startDate.text.toString()} 00:00:01")
                            .time,
                        getMillisFromDate(endDate.text.toString())
                    )
                }
            }

            searchButton.setOnClickListener {
                searchSortShimmer.isVisible = true
                if (searchEditText.text.isNullOrEmpty()) {
                    searchEditText.error = "enter a poll id"
                } else {
                    viewModel.searchPoll(searchEditText.text.toString())
                }
            }

            startDate.setOnClickListener {
                showDatePickerDialog(1)
            }

            endDate.setOnClickListener {
                showDatePickerDialog(2)
            }

            filterBack.setOnClickListener {
                findNavController().navigateUp()
            }

            searchBack.setOnClickListener {
                findNavController().navigateUp()
            }
        }

        viewModel.searchSortPolls.observe(viewLifecycleOwner, {
            if (it.size > 0) {
                binding.searchSortShimmer.isVisible = false
            }
            Log.d("tag", "searchsort list size: ${it.size}")
            adapter.submitList(it)
        })
        return binding.root
    }

    private fun showDatePickerDialog(code: Int) {
        val newFragment = DatePickerFragment { year, month, day ->
            var dateString = "$day/$month/$year"
            if (day - 10 < 0) {
                dateString = "0$day/$month/$year"
            }
            if (month - 10 < 0) {
                dateString = "$day/0$month/$year"
            }
            when (code) {
                1 -> binding.startDate.setText(dateString)
                2 -> binding.endDate.setText(dateString)
            }

        }
        newFragment.show(childFragmentManager, "datePicker")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun openPoll(poll: Poll) {
        viewModel.destination.postValue("vote")
        viewModel.clickedPoll = poll
        findNavController().navigateUp()
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