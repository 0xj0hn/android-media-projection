package com.example.application.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.IBinder
import android.media.MediaRecorder
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.application.R

class ScreenRecordingService : Service() {

    private lateinit var mediaProjectionManager: MediaProjectionManager
    private lateinit var mediaProjection: MediaProjection
    private lateinit var mediaRecorder: MediaRecorder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val resultCode = intent?.getIntExtra("resultCode", 0) ?: 0
        val data = intent?.getParcelableExtra<Intent>("data")

        mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        mediaProjection = mediaProjectionManager.getMediaProjection(resultCode, data!!)

        setupMediaRecorder()
        startForeground(1, createNotification())
        startRecording()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        createNotificationChannel()
        return NotificationCompat.Builder(this, "notif")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setShowWhen(false)
            .setContentText("‌")
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel("notif", "Capture screen", NotificationManager.IMPORTANCE_LOW)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun setupMediaRecorder() {
        mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(this)
        }else{
            MediaRecorder()
        }
        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setVideoSource(MediaRecorder.VideoSource.SURFACE)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            setVideoSize(1280, 720)
            setVideoFrameRate(30)
            setVideoEncodingBitRate(512 * 1000)
            setOutputFile("/sdcard/screen_recording.mp4")
            prepare()
        }
    }

    private fun startRecording() {
        mediaProjection.createVirtualDisplay(
            "ScreenRecorder",
            1280,
            720,
            1,
            0,
            mediaRecorder.surface,
            null,
            null,
        )
        mediaRecorder.start()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaRecorder.stop()
        mediaRecorder.release()
        mediaProjection.stop();
    }
}
