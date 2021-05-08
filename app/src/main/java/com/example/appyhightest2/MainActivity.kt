package com.example.appyhightest2

import android.Manifest

import android.content.pm.PackageManager
import android.graphics.PorterDuff
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.ads.MobileAds
import io.agora.rtc.IRtcEngineEventHandler
import io.agora.rtc.RtcEngine
import io.agora.rtc.video.VideoCanvas

class MainActivity : AppCompatActivity() {

    private var mRtcEngine: RtcEngine? = null
    private var timerForRemoteUser: Boolean = false


// Ask for Android device permissions at runtime.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        timer.start()

        // If all the permissions are granted, initialize the RtcEngine object and join a channel.
        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO, PERMISSION_REQ_ID_RECORD_AUDIO) && checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)) {
            initAgoraEngineAndJoinChannel()
        }
    }

    private fun initAgoraEngineAndJoinChannel() {
        initializeAgoraEngine()
        setupLocalVideo()
        joinChannel()
    }

    private fun checkSelfPermission(permission: String, requestCode: Int): Boolean {
        Log.i("permission", "checkSelfPermission $permission $requestCode")
        if (ContextCompat.checkSelfPermission(this,
                permission) != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                arrayOf(permission),
                requestCode)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        Log.i(LOG_TAG, "onRequestPermissionsResult " + grantResults[0] + " " + requestCode)

        when (requestCode) {
            PERMISSION_REQ_ID_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkSelfPermission(Manifest.permission.CAMERA, PERMISSION_REQ_ID_CAMERA)
                } else {
                    showLongToast("No permission for " + Manifest.permission.RECORD_AUDIO)
                    finish()
                }
            }
            PERMISSION_REQ_ID_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initAgoraEngineAndJoinChannel()
                } else {
                    showLongToast("No permission for " + Manifest.permission.CAMERA)
                    finish()
                }
            }
        }
    }

    private fun showLongToast(msg: String) {
        this.runOnUiThread { Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show() }
    }

    //Creating a timer for remote user to get connected
    private val timer = object: CountDownTimer(15000, 1000) {
        override fun onTick(millisUntilFinished: Long) {

        }

        override fun onFinish() {
            if(!timerForRemoteUser){
                Toast.makeText(this@MainActivity, "Retry after some time.",Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }



    // Kotlin
    private val mRtcEventHandler = object : IRtcEngineEventHandler() {

        // Listen for the onUserJoined callback.
        // This callback occurs when the remote user successfully joins the channel.
        // You can call the setupRemoteVideo method in this callback to set up the remote video view.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            timerForRemoteUser = true
            runOnUiThread { setupRemoteVideo(uid) }
        }

        // Listen for the onUserOffline callback.
        // This callback occurs when the remote user leaves the channel or drops offline.
        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread { onRemoteUserLeft() }
        }

    }

    private fun onRemoteUserLeft() {
        Toast.makeText(this, "Remote User Disconnected", Toast.LENGTH_SHORT).show()
    }


    // Initialize the RtcEngine object.
    private fun initializeAgoraEngine() {
        try {
            mRtcEngine = RtcEngine.create(baseContext, getString(R.string.agora_app_id), mRtcEventHandler)
//            Toast.makeText(this, "agora initailized", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e("agora_init", Log.getStackTraceString(e))

            throw RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e))
        }
    }

    private fun setupLocalVideo() {

        // Enable the video module.
        mRtcEngine!!.enableVideo()

        val container = findViewById<FrameLayout>(R.id.local_video_view_container)

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        container.addView(surfaceView)
        // Set the local video view.
        mRtcEngine!!.setupLocalVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, 0))
    }

    private fun joinChannel() {

        // Join a channel with a token.
        mRtcEngine!!.joinChannel(getString(R.string.token), "demoChannel1", "Extra Optional Data", 0)
    }


    private fun setupRemoteVideo(uid: Int) {
        val container = findViewById<RelativeLayout>(R.id.remote_video_view_container)

//        if (container.childCount > 1) {
//            Toast.makeText(this, "agora initailized ${getString(R.string.agora_app_id)}", Toast.LENGTH_SHORT).show()
//            return
//        }

        // Create a SurfaceView object.
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        container.addView(surfaceView)

        // Set the remote video view.
        mRtcEngine!!.setupRemoteVideo(VideoCanvas(surfaceView, VideoCanvas.RENDER_MODE_FIT, uid))
    }

    override fun onDestroy() {
        super.onDestroy()

        leaveChannel()
        RtcEngine.destroy()
        mRtcEngine = null
    }

    private fun leaveChannel() {
        // Leave the current channel.
        mRtcEngine!!.leaveChannel()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun onLocalAudioMuteClicked(view: View) {
        val iv = view as ImageView
        if (iv.isSelected) {
            iv.isSelected = false
            iv.clearColorFilter()
        } else {
            iv.isSelected = true
            iv.setColorFilter(getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY)
        }

        mRtcEngine!!.muteLocalAudioStream(iv.isSelected)
    }
    fun onSwitchCameraClicked(view: View) {
        mRtcEngine!!.switchCamera()
    }
    fun onCallClicked(view: View) {
        finish()
    }

    companion object {
        private val LOG_TAG = MainActivity::class.java.simpleName
        private const val PERMISSION_REQ_ID_RECORD_AUDIO = 22
        private const val  PERMISSION_REQ_ID_CAMERA = PERMISSION_REQ_ID_RECORD_AUDIO + 1
    }
}