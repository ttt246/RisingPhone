package com.matthaigh27.chatgptwrapper.activites

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.tabs.TabLayout.TabGravity
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.dialogs.CommonConfirmDialog
import com.matthaigh27.chatgptwrapper.fragments.ChatFragment
import java.io.File


class HomeActivity : AppCompatActivity() {

    private val PERMISSIONS_REQUEST_CODE = 1

    private val PERMISSIONS = arrayOf(
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS,
        Manifest.permission.CALL_PHONE,
        Manifest.permission.READ_EXTERNAL_STORAGE
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        requestPermission()
    }

    private fun requestPermission() {
        val notGrantedPermissions = PERMISSIONS.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }

        if (notGrantedPermissions.isNotEmpty()) {
            if (shouldShowRequestPermissionRationale(notGrantedPermissions[0])) {
                // show custom permission rationale
                val confirmDialog = CommonConfirmDialog(this)
                confirmDialog.setMessage("This app requires SMS, Contacts and Phone permissions to function properly. Please grant the necessary permissions.")
                confirmDialog.setOnClickListener(object :
                    CommonConfirmDialog.OnConfirmButtonClickListener {
                    override fun onPositiveButtonClick() {
                        requestPermissions(
                            notGrantedPermissions.toTypedArray(), PERMISSIONS_REQUEST_CODE
                        )
                    }

                    override fun onNegativeButtonClick() {
                        finish()
                    }
                })
                confirmDialog.show()
            } else {
                requestPermissions(notGrantedPermissions.toTypedArray(), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            // Permissions already granted, navigate to your desired fragment
            navigateToChatFragment()
        }
    }

    private fun navigateToChatFragment() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, ChatFragment()).commit()
    }

    @SuppressLint("MissingSuperCall")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    // Permissions granted, navigate to your desired fragment
                    navigateToChatFragment()
                } else {
                    requestPermission()
                }
                return
            }
        }
    }
}



