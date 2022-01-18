package com.triceriasolutions.pollbuzz.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.paging.map
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.data.models.UserDetails
import com.triceriasolutions.pollbuzz.databinding.FragmentAuthorProfileBinding
import com.triceriasolutions.pollbuzz.utils.ClickListener
import com.triceriasolutions.pollbuzz.utils.checkFollowing
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp
import kotlinx.android.synthetic.main.fragment_author_profile.*

class AuthorProfileFragment : Fragment(), ClickListener {

    private var _binding: FragmentAuthorProfileBinding? = null
    private val binding: FragmentAuthorProfileBinding get() = _binding!!
    private lateinit var adapter: PollsAdapter
    private lateinit var paginatedAdapter: PaginatedPollsAdapter
    private val viewModel: LandingViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val args: AuthorProfileFragmentArgs by navArgs()
        val id = args.id
        viewModel.destination.value = "landing"
        _binding = FragmentAuthorProfileBinding.inflate(inflater)
        viewModel.getAuthorProfile(id)
        adapter = PollsAdapter(this)
        paginatedAdapter = PaginatedPollsAdapter(this)
        binding.authProfileRv.adapter = paginatedAdapter

        viewModel.authorProfile.observe(viewLifecycleOwner, {
            it?.let {
                loadDetails(it)
            }
        })

        binding.apply {

            followingChip.setOnClickListener {
                followingChip.isVisible = false
                unfollowingChip.isVisible = true
                viewModel.authorProfile.value?.let { it1 -> viewModel.unfollowAuthor(it1.id) }
                reloadRecyclerview(false)
            }

            unfollowingChip.setOnClickListener {
                followingChip.isVisible = true
                unfollowingChip.isVisible = false
                viewModel.authorProfile.value?.let { it1 -> viewModel.followAuthor(it1.id) }
                reloadRecyclerview(true)
            }

        }


        viewModel.paginatedAuthorPolls.observe(viewLifecycleOwner, {
            lifecycleScope.launchWhenStarted {
                paginatedAdapter.submitData(it)
            }
//            paginatedAdapter.submitData(viewLifecycleOwner.lifecycle, it)
            binding.authorProfileShimmer.isVisible = false

            Log.d("author", it.toString())
            it.map { poll ->
                Log.d("author", poll.pollText)
                    poll
            }

        })

        paginatedAdapter.addLoadStateListener {
            binding.placeholderText.isVisible = paginatedAdapter.itemCount == 0
        }

        binding.backButton.setOnClickListener {
            findNavController().navigateUp()
        }

        activity?.onBackPressedDispatcher?.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                findNavController().navigateUp()

            }
        })



        return binding.root
    }

    private fun reloadRecyclerview(following: Boolean) {
        viewModel.paginatedAuthorPolls.value?.let {
            paginatedAdapter.submitData(
                viewLifecycleOwner.lifecycle, it.map { poll ->
                    poll.following = following
                    poll
                }
            )
            paginatedAdapter.notifyDataSetChanged()
        }
    }

    private fun loadDetails(userDetails: UserDetails) {
        binding.apply {
            Glide.with(imageView3.context)
                .load(userDetails.image)
                .placeholder(R.drawable.author_profile)
                .error(R.drawable.author_profile)
                .circleCrop()
                .into(imageView3)

            profile_name.text = userDetails.name
            username.text = "@${userDetails.username}"
            followerCount.text = "${userDetails.followers} Follwers"

            if (userDetails.id != FirebaseAuth.getInstance().currentUser!!.uid) {
                lifecycleScope.launchWhenStarted {
                    val following = checkFollowing(userDetails.id)
                    followingChip.isVisible = following
                    unfollowingChip.isVisible = !following
                    reloadRecyclerview(following)
                }
            }

        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
//        viewModel.authorProfile.postValue(null)
    }

    override fun openPoll(poll: Poll) {
        viewModel.destination.postValue("vote")
        viewModel.clickedPoll = poll
        findNavController().navigateUp()
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
                    updateList()
                }

                R.id.unfollow_author_menu_item -> {
                    viewModel.unfollowAuthor(poll.creatorId)
                    updateList()
                }
                R.id.poll_id_menu_item -> {
                    showPollIdPopUp(poll.uid, requireContext(), layoutInflater)
                }
            }
            true
        }
        popUpMenu.show()
    }

    private fun updateList() {
        val list = viewModel.authorPolls.value!!.forEach {
            it.following = !it.following
        }
        adapter.submitList(viewModel.authorPolls.value!!)
    }

}