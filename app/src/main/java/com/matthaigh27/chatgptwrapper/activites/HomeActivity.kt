package com.matthaigh27.chatgptwrapper.activites

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.fragments.ChatFragment
import com.matthaigh27.chatgptwrapper.utils.Constants.REQUEST_PERMISSION_CODE


class HomeActivity : AppCompatActivity() {

    val requestPermissionList = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        if (arePermissionsGranted()) {
            navigateToChatFragment()
        } else {
            requestPermissions()
        }
    }

    private fun arePermissionsGranted(): Boolean {
        requestPermissionList.forEach { permissionName ->
            val permission = ContextCompat.checkSelfPermission(this@HomeActivity, permissionName)
            if (permission != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            requestPermissionList,
            REQUEST_PERMISSION_CODE
        )
    }

    private fun navigateToChatFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, ChatFragment()).commit()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    navigateToChatFragment()
                } else {
                    finish()
                }
            }
        }
    }
}


