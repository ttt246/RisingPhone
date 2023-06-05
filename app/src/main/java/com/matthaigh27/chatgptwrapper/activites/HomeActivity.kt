package com.matthaigh27.chatgptwrapper.activites

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.matthaigh27.chatgptwrapper.R
import com.matthaigh27.chatgptwrapper.fragments.ChatFragment


class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.frame_container, ChatFragment()).commit()
    }

}


