package com.iobits.tech.app.ai_identifier.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.iobits.tech.app.ai_identifier.ui.fragments.HistoryFragment
import com.iobits.tech.app.ai_identifier.ui.fragments.HomeFragment
import com.iobits.tech.app.ai_identifier.ui.fragments.SettingsFragment

class HomeViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int = 6

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> HomeFragment()
            1 -> HistoryFragment()
            2 -> SettingsFragment()
            else -> HomeFragment()
        }
    }

}