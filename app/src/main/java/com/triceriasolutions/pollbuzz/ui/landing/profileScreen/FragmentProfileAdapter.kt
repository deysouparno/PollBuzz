package com.triceriasolutions.pollbuzz.ui.landing.profileScreen

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
@Suppress("DEPRECATION")
internal class FragmentProfileAdapter(
    var context: Context,
    fm: FragmentManager,
    var totalTabs: Int
) :
    FragmentPagerAdapter(fm) {
    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MyPolls()
            }
            else -> {
                VotedPolls()
            }
        }
    }
    override fun getCount(): Int {
        return totalTabs
    }
}