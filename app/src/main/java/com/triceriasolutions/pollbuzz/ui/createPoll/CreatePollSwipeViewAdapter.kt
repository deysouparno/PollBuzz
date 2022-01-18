package com.triceriasolutions.pollbuzz.ui.createPoll

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter

class CreatePollSwipeViewAdapter(activity: AppCompatActivity) :
    FragmentStateAdapter(activity) {

    override fun getItemCount(): Int = 2

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            1 -> CreateImagePollFragment()
            else -> CreateTextPollFragment()
        }
    }
}