package com.triceriasolutions.pollbuzz.ui.createPoll

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.CreateOption
import com.triceriasolutions.pollbuzz.databinding.FragmentCreateTextPollBinding
import com.triceriasolutions.pollbuzz.databinding.ImagePollOptionItemBinding
import com.triceriasolutions.pollbuzz.utils.DatePickerFragment
import com.triceriasolutions.pollbuzz.utils.OptionClickListener
import com.triceriasolutions.pollbuzz.utils.getMillisFromDate


class CreateTextPollFragment : Fragment(), OptionClickListener {

    private var _binding: FragmentCreateTextPollBinding? = null
    private val binding: FragmentCreateTextPollBinding get() = _binding!!
    private lateinit var optionsAdapter: OptionsAdapter
    var optionCount = 2
    var flag = false

    private val viewModel: CreatePollViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateTextPollBinding.inflate(inflater)
        setOptions()
        handleSwitches()

        val items = listOf("30 seconds", "60 seconds", "90 seconds")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)

        binding.expiryTime.adapter = adapter


        binding.apply {

            addOption.setOnClickListener {
                viewModel.textOptions.add(CreateOption(text = "", code = 2))
                Log.d("tag", "option added ${viewModel.textOptions}")
                optionsAdapter.submitOptions(viewModel.textOptions)
                optionsAdapter.notifyItemInserted(optionCount++)
            }

            chooseDate.setOnClickListener {
                val newFragment = DatePickerFragment { year, month, day ->
                    var dateString = "$day/$month/$year"
                    Log.d("tag", "datestring is: $dateString")
                    if (day - 10 < 0) {
                        dateString = "0$day/$month/$year"
                    }
                    if (month - 10 < 0) {
                        dateString = "$day/0$month/$year"
                    }

                    binding.expiryDate.text = dateString
                    flag = true

                    Log.d("tag", "datestring is: $dateString")


                    val time = getMillisFromDate(dateString) - 19800000
                    viewModel.expiryTimeInMillis = time

                    Log.d("tag", "time is: $time current is: ${System.currentTimeMillis()}")

                    if (time < System.currentTimeMillis()) {
                        Log.d("tag", "invalid expiry")
                        flag = false
                    }
                }
                newFragment.show(childFragmentManager, "datePicker")
            }

            createPollButton.setOnClickListener {
                if (viewModel.pollText.isEmpty()) {
                    Toast.makeText(context, "Please Enter Poll Text", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                if (viewModel.isLive) {
                    viewModel.createLivePoll(
                        seconds = expiryTime.selectedItem.toString()
                            .split(" ").first().toInt()
                    )
                }
                else if (!flag) {
                    expiryDate.error = "select expiry date"
                } else {
                    viewModel.createPoll()
                }
            }
        }

        return binding.root
    }

    private fun setOptions() {
        optionsAdapter = OptionsAdapter(optionClickListener = this)
        binding.textPollOptions.adapter = optionsAdapter
        optionsAdapter.submitOptions(viewModel.textOptions)
    }

    private fun handleSwitches() {

        binding.apply {

            singleSelectSwitch.isChecked = true

            singleSelectSwitch.setOnClickListener {
                viewModel.type = 1
            }

            multiSelectSwitch.setOnClickListener {
                viewModel.type = 2
            }

            rankBasedSwitch.setOnClickListener {
                viewModel.type = 3
            }

            livePollSwitch.setOnCheckedChangeListener { _, isChecked ->
                viewModel.isLive = isChecked
                binding.chooseTime.isVisible = isChecked
                binding.chooseDate.isVisible = !isChecked
            }
        }
    }

    override fun onResume() {
        super.onResume()

        viewModel.count++
        Log.d("tag1", "in text ${viewModel.count} text is: ${viewModel.pollText}")

        viewModel.isTextPoll = true

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        optionCount = 2
    }

    override fun chooseImage(binding: ImagePollOptionItemBinding, position: Int) = Unit

    override fun updateOptionText(position: Int, text: String) {
        viewModel.textOptions[position].text = text
    }

    override fun deleteOption(position: Int) {
        viewModel.textOptions.removeAt(position)
        optionsAdapter.submitOptions(viewModel.textOptions)
        optionsAdapter.notifyItemRemoved(position)
        optionCount--
    }

}