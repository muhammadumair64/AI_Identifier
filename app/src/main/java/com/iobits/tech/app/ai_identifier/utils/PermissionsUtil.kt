package com.iobits.tech.app.ai_identifier.utils

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.iobits.tech.app.ai_identifier.databinding.DialogueShutdownBinding

object PermissionsUtil {
    val REQUIRED_PERMISSIONS = arrayOf(
        Manifest.permission.CAMERA)

    val REQUIRED_PERMISSIONS_STORAGE = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE)

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var REQUIRED_PERMISSIONS_13 = arrayOf(
        Manifest.permission.CAMERA
    )

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    var REQUIRED_PERMISSIONS_MEDIA_13 = arrayOf(
        Manifest.permission.READ_MEDIA_IMAGES
    )
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun allPermissionsGranted13(context: Context) = REQUIRED_PERMISSIONS_13.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }
    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    fun allPermissionsGranted13Media(context: Context) = REQUIRED_PERMISSIONS_MEDIA_13.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun allPermissionsGranted(context: Context) = REQUIRED_PERMISSIONS.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun allPermissionsGrantedStorage(context: Context) = REQUIRED_PERMISSIONS_STORAGE.all {
        Log.d("CAMERA_FRAGMENT", "Camera Permission Granted")
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun showCustomDialog(context: Context) {
        val dialogBinding = DialogueShutdownBinding.inflate(LayoutInflater.from(context))
        val dialogView = dialogBinding.root
        val alertDialogBuilder = AlertDialog.Builder(context)
        alertDialogBuilder.setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.show()


        dialogBinding.btn.setOnClickListener {
            alertDialog.dismiss()
            openAppSettings(context)
        }
        dialogBinding.cross.setOnClickListener {
            alertDialog.dismiss()
        }
    }

    private fun openAppSettings(context: Context) {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
    }


}