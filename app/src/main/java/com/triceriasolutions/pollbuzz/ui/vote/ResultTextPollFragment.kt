package com.triceriasolutions.pollbuzz.ui.vote

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.firebase.firestore.FirebaseFirestore
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.Poll
import com.triceriasolutions.pollbuzz.databinding.FragmentResultTextPollBinding
import com.triceriasolutions.pollbuzz.ui.landing.LandingViewModel
import com.triceriasolutions.pollbuzz.utils.VoteOptionClickListener
import com.triceriasolutions.pollbuzz.utils.getCreationTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.getExpiryTimeStringFromMillis
import com.triceriasolutions.pollbuzz.utils.showPollIdPopUp
import kotlinx.coroutines.tasks.await


class ResultTextPollFragment : Fragment(), VoteOptionClickListener {

    private val viewModel: LandingViewModel by activityViewModels()

    private var _binding: FragmentResultTextPollBinding? = null
    private val binding: FragmentResultTextPollBinding get() = _binding!!

    private lateinit var poll: Poll

    private lateinit var adapter: TextVoteAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel.destination.postValue("home")
        viewModel.voteState.postValue("init")
        _binding = FragmentResultTextPollBinding.inflate(inflater)

        val args: ResultImagePollFragmentArgs by navArgs()
        poll = args.poll

        Log.d("tag11", "voted check: ${poll.isVoted}")

        adapter = TextVoteAdapter(poll, this, code = 2)
        adapter.submitList(poll.options)


        binding.apply {
            voteCount.text = poll.totalVote.toString()
            totalVoteText.text = "Total Vote: ${poll.totalVote}"
            liveText.isVisible = poll.isLive
            textResultRv.adapter = adapter

            pollText.text = poll.pollText

            if (poll.isVoted) {
                voteCountDrawableLeft.setImageDrawable(
                    voteCountDrawableLeft.context
                        .getDrawable(R.drawable.green_vote_counter)
                )
            }

            creatingTime.text = getCreationTimeStringFromMillis(poll.creationTime + 19800000)
            timeLeft.text = getExpiryTimeStringFromMillis(poll.expiryTime)

            Glide.with(circleImageView.context)
                .load(poll.creatorImage)
                .placeholder(R.drawable.author_profile)
                .error(R.drawable.author_profile)
                .into(circleImageView)

            circleImageView.setOnClickListener {
                viewModel.destination.postValue("profile-$id")
                findNavController().navigateUp()
            }

            profileName.text = poll.creatorName

            statShowButton.setOnClickListener {
                statShowButton.isVisible = false
                statsCard.isVisible = true
                binding.root.fullScroll(View.FOCUS_DOWN)
            }

            hideStatButton.setOnClickListener {
                statShowButton.isVisible = true
                statsCard.isVisible = false
            }

            backButton.setOnClickListener {
                findNavController().navigateUp()
            }

            setFollowDrawable()

            pollOptions.setOnClickListener {

                Log.d("tag11", "option clicked: ${poll.following}")

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
        }

        loadResult()
        refreshPoll()

        return binding.root
    }

    private fun setFollowDrawable() {
        Log.d("tag11", "drawable function: ${poll.following}")
        binding.followChip.setImageDrawable(
            context?.getDrawable(
                if (poll.following) R.drawable.following
                else R.drawable.author_follow_button
            )
        )
    }

    private fun refreshPoll() {
        lifecycleScope.launchWhenStarted {

            FirebaseFirestore.getInstance().collection("polls").document(poll.uid).get().await()
                .toObject(Poll::class.java)?.let {
                    it.following = poll.following
                    poll = it
                    loadResult()
                }
        }
    }

    private fun loadResult() {
        val pieDateset = mutableListOf<PieEntry>()

        binding.apply {
            voteCount.text = poll.totalVote.toString()
            totalVoteText.text = "Total Vote: ${poll.totalVote}"
        }

        when (poll.type) {
            1 -> {
                binding.pollType.text = "Single Select"
                poll.options.forEach {
                    pieDateset.add(PieEntry((it.vote.toFloat() / poll.totalVote) * 100, it.text))
                }
            }

            2 -> {
                binding.pollType.text = "Multiple Select"
                poll.options.forEach {
                    pieDateset.add(PieEntry((it.vote.toFloat() / poll.totalVote) * 100, it.text))
                }
            }
            3 -> {
                binding.pollType.text = "Priority Poll"
                for (i in poll.options.indices) {
                    val option = poll.options[i]
                    val voteArr = option.vote.split(" ").map { it.toInt() }.toIntArray()
                    var score = 0f
                    for (j in voteArr.indices) {
                        score += (voteArr[j] * (poll.options.size - j))
                    }
//                    score = (score / (poll.totalVote * poll.options.size)) * 100
                    pieDateset.add(PieEntry(score, option.text))
                }
            }

        }

        val pieDataSet = PieDataSet(pieDateset, "")
        pieDataSet.apply {
            colors = ColorTemplate.MATERIAL_COLORS.toMutableList()
            valueTextColor = Color.BLACK
            valueTextSize = 14F
        }

        val pieData = PieData(pieDataSet)
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))

        binding.pieChart.apply {

            centerText = "Stats"
            setCenterTextSize(14f)
            setDrawEntryLabels(false)
            setUsePercentValues(true)
            setCenterTextColor(Color.BLACK)
            data = pieData
            binding.pieChart.description.text = ""
            animateY(1400, Easing.EaseInOutQuad)
        }
    }

    override fun singleSelectOptionClicked(position: Int) {
    }

    override fun multiSelectOptionClicked(position: Int) {
    }

    override fun priortySelectOptionClicked(position: Int) {

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}