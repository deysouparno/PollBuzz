package com.triceriasolutions.pollbuzz.ui.landing

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.databinding.FragmentLandingBinding
import com.triceriasolutions.pollbuzz.utils.NetworkStatus
import com.triceriasolutions.pollbuzz.utils.NetworkStatusHelper


class LandingFragment : Fragment() {

    private var _binding: FragmentLandingBinding? = null
    private val binding: FragmentLandingBinding get() = _binding!!
    private val viewModel: LandingViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLandingBinding.inflate(inflater)
        val navController =
            (childFragmentManager.findFragmentById(R.id.landingFragmentHost) as NavHostFragment).navController
        binding.bottomNav.setupWithNavController(navController)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).setSupportActionBar(binding.landingToolbar)


        binding.landingFab.setOnClickListener {
            findNavController().navigate(
                LandingFragmentDirections.actionLandingFragmentToCreatePollActivity()
            )
        }

        viewModel.destination.observe(viewLifecycleOwner, {
            if (it.startsWith("profile")) {
                findNavController().navigate(
                    LandingFragmentDirections.actionLandingFragmentToAuthorProfileFragment(
                        it.split("-").last()
                    )
                )
            }
            if (it == "vote") {
                val poll = viewModel.clickedPoll

                if (poll.isTextPoll) {
                    if (System.currentTimeMillis() < poll.expiryTime &&
                        poll.creatorId != FirebaseAuth.getInstance().currentUser!!.uid &&
                        !poll.isVoted) {
                        Log.d("tag11", "going to vote text")
                        findNavController().navigate(
                            LandingFragmentDirections.actionLandingFragmentToVoteTextPollFragment(
                                poll
                            )
                        )
                    } else {
                        findNavController().navigate(
                            LandingFragmentDirections.actionLandingFragmentToResultTextPollFragment(
                                poll
                            )
                        )
                    }
                } else {
                    if (System.currentTimeMillis() < poll.expiryTime &&
                        poll.creatorId != FirebaseAuth.getInstance().currentUser!!.uid &&
                        !poll.isVoted) {
                        findNavController().navigate(
                            LandingFragmentDirections.actionLandingFragmentToVoteImagePollFragment(
                                poll
                            )
                        )
                    } else {
                        findNavController().navigate(
                            LandingFragmentDirections.actionLandingFragmentToResultImagePollFragment(
                                poll
                            )
                        )
                    }
                }
            }
        })

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.landing_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.log_out -> {
                FirebaseAuth.getInstance().signOut()
                findNavController().navigate(LandingFragmentDirections.actionLandingFragmentToAuthActivity())
            }

            R.id.search_menu_item -> {
                findNavController().navigate(
                    LandingFragmentDirections.actionLandingFragmentToFilterAndSearchFragment(
                        code = 2
                    )
                )
            }

            R.id.filter_menu_item -> {
                Log.d("tag", "current :${findNavController().currentDestination}")
                findNavController().navigate(
                    LandingFragmentDirections.actionLandingFragmentToFilterAndSearchFragment(
                        code = 1
                    )
                )
            }

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }


}