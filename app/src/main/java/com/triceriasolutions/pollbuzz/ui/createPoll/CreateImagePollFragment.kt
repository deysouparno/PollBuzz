package com.triceriasolutions.pollbuzz.ui.createPoll

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.canhub.cropper.CropImage
import com.canhub.cropper.CropImageView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.triceriasolutions.pollbuzz.R
import com.triceriasolutions.pollbuzz.data.models.CreateOption
import com.triceriasolutions.pollbuzz.databinding.FragmentCreateImagePollBinding
import com.triceriasolutions.pollbuzz.databinding.ImagePollOptionItemBinding
import com.triceriasolutions.pollbuzz.utils.DatePickerFragment
import com.triceriasolutions.pollbuzz.utils.OptionClickListener
import com.triceriasolutions.pollbuzz.utils.getMillisFromDate


class CreateImagePollFragment : Fragment(), OptionClickListener {

    private var _binding: FragmentCreateImagePollBinding? = null
    private val binding: FragmentCreateImagePollBinding get() = _binding!!
    var flag = false

    private val cropActivityResultContracts = object : ActivityResultContract<Any?, Uri?>() {
        override fun createIntent(context: Context, input: Any?): Intent {
            return CropImage
                .activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(3, 2)
                .setCropShape(CropImageView.CropShape.RECTANGLE)
                .getIntent(context)
        }

        override fun parseResult(resultCode: Int, intent: Intent?): Uri? {
            return CropImage.getActivityResult(intent)?.uriContent
        }

    }

    private lateinit var cropActivityResultLauncher: ActivityResultLauncher<Any?>

    private lateinit var optionsAdapter: OptionsAdapter
    private var optionCount = 2
    private var clickPos = 0

    private var imagePollOptionItemBinding: ImagePollOptionItemBinding? = null
    private val viewModel: CreatePollViewModel by activityViewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateImagePollBinding.inflate(inflater)

        setOptions()
        handleSwitches()

        val items = listOf("30 seconds", "60 seconds", "90 seconds")
        val adapter = ArrayAdapter(requireContext(), R.layout.list_item, items)

        binding.expiryTime.adapter = adapter

        binding.apply {
            addOption.setOnClickListener {

                viewModel.imgOptions.add(CreateOption(text = "", code = 2))
                optionsAdapter.submitOptions(viewModel.imgOptions)
                optionsAdapter.notifyItemInserted(optionCount++)
            }

            chooseDate.setOnClickListener {
                val newFragment = DatePickerFragment { year, month, day ->
                    var dateString = "$day/$month/$year"
                    if (day - 10 < 0) {
                        dateString = "0$day/$month/$year"
                    }
                    if (month - 10 < 0) {
                        dateString = "$day/0$month/$year"
                    }

                    binding.expiryDate.text = dateString
                    flag = true
                    expiryDate.error = null

                    val time = getMillisFromDate(dateString) - 19800000
                    viewModel.expiryTimeInMillis = time

                    if (time < System.currentTimeMillis()) {
                        flag = false
                        expiryDate.error = "select expiry date"
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
                if (flag) {
                    if (viewModel.createPoll()) {
                        Toast.makeText(context, "please enter poll text", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "poll format incorrect", Toast.LENGTH_SHORT).show()
                }
            }
        }

        cropActivityResultLauncher = registerForActivityResult(cropActivityResultContracts) {
            it?.let {
                viewModel.imgOptions[clickPos].img = it
                Glide.with(imagePollOptionItemBinding!!.optionImg.context)
                    .load(it)
                    .fitCenter()
                    .into(imagePollOptionItemBinding!!.optionImg)
            }
        }

        return binding.root
    }


    private fun setOptions() {
        optionsAdapter = OptionsAdapter(code = 2, this)
        binding.imagePollOptions.apply {
            adapter = optionsAdapter
            layoutManager = GridLayoutManager(context, 2)
        }
        optionsAdapter.submitOptions(viewModel.imgOptions)
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
        viewModel.isTextPoll = false
        viewModel.count++

        Log.d("tag1", "in image ${viewModel.count} text is: ${viewModel.pollText}")


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun chooseImage(binding: ImagePollOptionItemBinding, position: Int) {
        imagePollOptionItemBinding = binding
        clickPos = position
        cropActivityResultLauncher.launch(null)
    }

    override fun updateOptionText(position: Int, text: String) {
        viewModel.imgOptions[position].text = text
    }

    override fun deleteOption(position: Int) {
        viewModel.imgOptions.removeAt(position)
        optionsAdapter.submitOptions(viewModel.imgOptions)
        optionsAdapter.notifyItemRemoved(position)
        optionCount--
    }


}