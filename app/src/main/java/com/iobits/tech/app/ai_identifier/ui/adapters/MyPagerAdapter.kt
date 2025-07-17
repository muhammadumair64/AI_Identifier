package com.iobits.tech.app.ai_identifier.ui.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.iobits.tech.app.ai_identifier.ui.fragments.OnBoarding1Fragment
import com.iobits.tech.app.ai_identifier.ui.fragments.OnBoarding2Fragment
import com.iobits.tech.app.ai_identifier.ui.fragments.OnBoarding3Fragment

class MyPagerAdapter(fm: FragmentActivity) : FragmentStateAdapter(fm) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> OnBoarding1Fragment()
            1 -> OnBoarding2Fragment()
            2 -> OnBoarding3Fragment()
            else -> OnBoarding1Fragment()
        }
    }

}
