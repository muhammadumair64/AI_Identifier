package com.iobits.tech.app.ai_identifier.ui.fragments.bottomSheetFragment

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.iobits.tech.app.ai_identifier.databinding.NoResultFoundBottomSheetBinding
import kotlin.let
import com.iobits.tech.app.ai_identifier.R
import com.iobits.tech.app.ai_identifier.utils.setSafeOnClickListener

class NoResultFoundBottomSheetFragment: BottomSheetDialogFragment() {

    private val binding by lazy {
        NoResultFoundBottomSheetBinding.inflate(
            layoutInflater
        )
    }

    private var onDone: (() -> Unit)? = null

    fun setOnDoneListener(listener: () -> Unit) {
        onDone = listener
    }

    private var selectedString: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        dialog?.setCancelable(false)

        binding.btnScanAgain.setSafeOnClickListener{
            onDone?.invoke()
        }

        return binding.root
    }

    fun hideSoftKeyboard(activity: Activity?) {
        activity?.let {
            val imm = activity.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.let {
                val currentFocus = activity.currentFocus
                currentFocus?.let {
                    imm.hideSoftInputFromWindow(it.windowToken, 0)
                }
            }
        }
    }

    /**if want a static height or change the behavior of the sheet like can drag up down etc*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val screenHeight = resources.displayMetrics.heightPixels
        val desiredHeight = (screenHeight) * 0.50
        val bottomSheetLayout = binding.mainLayout
        val behavior = (dialog as BottomSheetDialog).behavior
        behavior.state = BottomSheetBehavior.STATE_EXPANDED
        behavior.isDraggable = false

        // Set the desired height to the layout
        val layoutParams = bottomSheetLayout.layoutParams
        layoutParams.height = desiredHeight.toInt()
        bottomSheetLayout.layoutParams = layoutParams

        // Apply the rounded corners background
        bottomSheetLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.rounded_bottom_sheet)
    }
    override fun onResume() {
        super.onResume()
    }
}