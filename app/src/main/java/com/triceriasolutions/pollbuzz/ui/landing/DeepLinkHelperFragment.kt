package com.triceriasolutions.pollbuzz.ui.landing

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.utils.checkVoted
import kotlinx.coroutines.tasks.await

class DeepLinkHelperFragment : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val args: DeepLinkHelperFragmentArgs by navArgs()

        val pollId = args.pollId

        if (pollId != null) {
            lifecycleScope.launchWhenStarted {
                val snapShot = FirebaseFirestore.getInstance().collection("polls")
                    .document(pollId)
                    .get().await()

                val poll = snapShot.toObject(Poll::class.java)
                if (poll != null) {
                    poll.isVoted = checkVoted(pollId)
                    poll.expired = System.currentTimeMillis() > poll.expiryTime

                    if (poll.expired || poll.isVoted) {
                        if (poll.isTextPoll) {
                            findNavController().navigate(
                                DeepLinkHelperFragmentDirections.actionDeepLinkHelperFragmentToResultTextPollFragment(
                                    poll
                                )
                            )
                        } else {
                            findNavController().navigate(
                                DeepLinkHelperFragmentDirections.actionDeepLinkHelperFragmentToResultImagePollFragment(
                                    poll
                                )
                            )
                        }
                    } else {
                        if (poll.isTextPoll) {
                            findNavController().navigate(
                                DeepLinkHelperFragmentDirections.actionDeepLinkHelperFragmentToVoteTextPollFragment(
                                    poll
                                )
                            )
                        } else {
                            findNavController().navigate(
                                DeepLinkHelperFragmentDirections.actionDeepLinkHelperFragmentToVoteImagePollFragment(
                                    poll
                                )
                            )
                        }
                    }
                }

            }
        }

        return inflater.inflate(R.layout.fragment_deep_link_helper, container, false)
    }


}