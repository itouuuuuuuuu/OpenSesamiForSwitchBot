package com.itouuuuuuuuu.opensesami.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.itouuuuuuuuu.opensesami.R
import com.itouuuuuuuuu.opensesami.SharedPreferencesManager
import kotlinx.android.synthetic.main.activity_setting.*

class SettingActivity : AppCompatActivity() {

    private val prefs by lazy { SharedPreferencesManager(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setting)
        title = getString(R.string.app_name_sub)

        // set setting
        intervalEditText.setText(prefs.takePhotoInterval.toString(), TextView.BufferType.NORMAL)
        maxRetryEditText.setText(prefs.maxRetryCount.toString(), TextView.BufferType.NORMAL)
        confidenceThresholdEditText.setText(prefs.confidenceThreshold.toString(), TextView.BufferType.NORMAL)

        // apply setting
        applyButton.setOnClickListener {
            prefs.apply {
                takePhotoInterval = intervalEditText.text.toString().toLong()
                maxRetryCount = maxRetryEditText.text.toString().toInt()
                confidenceThreshold = confidenceThresholdEditText.text.toString().toInt()
            }
            Toast.makeText(this, getString(R.string.setting_apply_message), Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}