package com.itouuuuuuuuu.opensesami.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.itouuuuuuuuu.opensesami.R

class SettingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        title = getString(R.string.app_name_sub)
    }
}