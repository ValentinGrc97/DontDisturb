package com.oostaoo.org.dontdisturb

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.provider.Settings
import kotlinx.android.synthetic.main.activity_main.*
import android.hardware.SensorManager
import android.media.AudioManager

class MainActivity : AppCompatActivity() {

    private var sensorManager: SensorManager? = null
    private var accelerometerSensor: Sensor? = null
    private var accelerometerPresent: Boolean = false
    private var needSetModeNotDisturb: Boolean = false
    private var isAppActivate: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        val sensorList = sensorManager!!.getSensorList(Sensor.TYPE_ACCELEROMETER)
        if (sensorList.size > 0) {
            accelerometerPresent = true
            accelerometerSensor = sensorList[0]
        } else {
            accelerometerPresent = false
        }

        toggleButton.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked) {
                isAppActivate = true
                toggleButton.setBackgroundColor(resources.getColor(R.color.green))
            }
            else {
                isAppActivate = false
                toggleButton.setBackgroundColor(resources.getColor(R.color.red))
                needSetModeNotDisturb = false
                setNotificationMode()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        if(accelerometerPresent){
            sensorManager?.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    private val accelerometerListener = object: SensorEventListener {

        override fun onAccuracyChanged(arg0:Sensor, arg1:Int) {
        }

        override fun onSensorChanged(arg0:SensorEvent) {
            val zValue = arg0.values[2]
            if(isAppActivate) {
                if (zValue >= -8) {
                    if (needSetModeNotDisturb) {
                        needSetModeNotDisturb = false
                        setNotificationMode()
                    }
                }
                else {
                    if(!needSetModeNotDisturb) {
                        needSetModeNotDisturb = true
                        setNotificationMode()
                    }
                }
            }
        }
    }

    fun setNotificationMode() {
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if(needSetModeNotDisturb) {
            if (mNotificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_SILENT
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_NONE)
            }
            else {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }
        else {
            if (mNotificationManager.isNotificationPolicyAccessGranted) {
                audioManager.ringerMode = AudioManager.RINGER_MODE_NORMAL
                mNotificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
            }
            else {
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            }
        }
    }
}
