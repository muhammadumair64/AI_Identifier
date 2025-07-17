package com.iobits.tech.app.ai_identifier.ui.fragments.bottomSheetFragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iobits.tech.app.ai_identifier.MyApplication
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.databinding.LayoutRateUsBottomSheetBinding
import com.iobits.tech.app.ai_identifier.manager.PreferenceManager
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class RatingBottomSheetFragment : BottomSheetDialogFragment() {
    val binding by lazy {
        LayoutRateUsBottomSheetBinding.inflate(layoutInflater)
    }
    var name : ((String)->Unit)? = null
    private var comment1State = false
    private var comment2State = false
    private var comment3State = false
    private var comment4State = false
    private var comment5State = false
    private var starCount =  5

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View  {
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
         initListeners()
        return binding.root
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme)
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation? {
        return if (enter) {
            AnimationUtils.loadAnimation(context, R.anim.slide_in_bottom_sheet)
        } else {
            AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom_sheet)
        }
    }
    private fun animateStarsToRating(targetRating: Int) {
        lifecycleScope.launch {
            for (i in 1..targetRating) {
                binding.simpleRatingBar.rating = i.toFloat()
                delay(200)
            }
        }
    }
    private fun initListeners() {

        binding.apply {
            simpleRatingBar.rating =  5F
            simpleRatingBar.setOnRatingChangeListener { ratingBar, rating, fromUser ->
                if (fromUser) {
                    starCount = rating.toInt()
                    when(rating){
                        1F ->{
                            binding.emoji.setImageResource(R.drawable.emoji1)
                        }
                        2F ->{
                            binding.emoji.setImageResource(R.drawable.emoji2)
                        }
                        3F ->{
                            binding.emoji.setImageResource(R.drawable.emoji3)
                        }
                        4F ->{
                            binding.emoji.setImageResource(R.drawable.emoji4)
                        }
                        5F ->{
                        binding.emoji.setImageResource(R.drawable.emoji5)
                       }
                    }
                    animateStarsToRating(rating.toInt())
                }
            }

            binding.cross.setSafeOnClickListener {
                dismiss()
            }

            comment1.setSafeOnClickListener {
                if(comment1State){
                    comment1State = false
                    it.setBackgroundResource(R.drawable.stroke_theme_button_bg)
                }else{
                    comment1State = true
                    it.setBackgroundResource(R.drawable.green_gradient_circle)
                }
            }
            comment2.setSafeOnClickListener {
                if(comment2State){
                    comment2State = false
                    it.setBackgroundResource(R.drawable.stroke_theme_button_bg)
                }else{
                    comment2State = true
                    it.setBackgroundResource(R.drawable.green_gradient_circle)
                }
            }
            comment3.setSafeOnClickListener {
                if(comment3State){
                    comment3State = false
                    it.setBackgroundResource(R.drawable.stroke_theme_button_bg)
                }else{
                    comment3State = true
                    it.setBackgroundResource(R.drawable.green_gradient_circle)
                }
            }
            comment4.setSafeOnClickListener {
                if(comment4State){
                    comment4State = false
                    it.setBackgroundResource(R.drawable.stroke_theme_button_bg)
                }else{
                    comment4State = true
                    it.setBackgroundResource(R.drawable.green_gradient_circle)
                }
            }
            comment5.setSafeOnClickListener {
                if(comment5State){
                    comment5State = false
                    it.setBackgroundResource(R.drawable.stroke_theme_button_bg)
                }else{
                    comment5State = true
                    it.setBackgroundResource(R.drawable.green_gradient_circle)
                }
            }

            Start.setSafeOnClickListener {

                if(starCount > 3){
                    dismiss()
                    try {
                        MyApplication.blockOpendAd = true
                        MyApplication.mInstance?.preferenceManager?.put(PreferenceManager.Key.rateUsKey,true)
                        val url = "https://play.google.com/store/apps/details?id=${requireActivity().packageName}"
                        val i = Intent(Intent.ACTION_VIEW)
                        i.setData(Uri.parse(url))
                        startActivity(i)
                    }catch (_: Exception){}
                }else{
                    dismiss()
                }

            }
        }
    }
}
