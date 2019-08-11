package com.gy.screencapturedemo


import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.media.MediaPlayer
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.util.Log
import com.gy.screen.ScreenRecordHelper
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private var screenRecordHelper : ScreenRecordHelper? = null

    private val afdd : AssetFileDescriptor by lazy {
        assets.openFd("test.aac")
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        btn_start.setOnClickListener {
            if(screenRecordHelper == null){
                screenRecordHelper = ScreenRecordHelper(this,object : ScreenRecordHelper.OnVideoRecordListener{
                    override fun onBeforeRecord() {

                    }

                    override fun onStartRecord() {
                        play()
                    }

                    override fun onCancelRecord() {
                        releasePlayer()
                    }

                    override fun onEndRecord() {
                        releasePlayer()
                    }
                })
            }

            screenRecordHelper?.apply {
                if(!isRecording){
                    // 如果你想录制音频（一定会有环境音量），你可以打开下面这个限制,并且使用不带参数的 stopRecord()
//                        recordAudio = true
                    startRecord()
                }
            }
        }

        btn_stop.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                screenRecordHelper?.apply {
                    if (isRecording) {
                        if (mediaPlayer != null) {
                            // 如果选择带参数的 stop 方法，则录制音频无效
                            stopRecord(mediaPlayer!!.duration.toLong(), 15 *4* 1000, afdd)
                        } else {
                            stopRecord()
                        }
                    }
                }
            }
        }
    }

    // 音频播放  这个播放背景音乐 如果不需要 就不播放
    private var mediaPlayer: MediaPlayer? = null
    private fun play() {
        mediaPlayer = MediaPlayer()
        try {
            mediaPlayer?.apply {
                this.reset()
                this.setDataSource(afdd.fileDescriptor, afdd.startOffset, afdd.length)
                this.isLooping = true
                this.prepare()
                this.start()
            }
        } catch (e: Exception) {
            Log.d("nanchen2251", "播放音乐失败")
        } finally {

        }
    }

    private fun releasePlayer() {
        mediaPlayer?.apply {
            stop()
            release()
        }
        mediaPlayer = null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && data != null) {
            screenRecordHelper?.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onDestroy() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            screenRecordHelper?.clearAll()
        }
        afdd.close()
        super.onDestroy()
    }
}


