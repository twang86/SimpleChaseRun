package com.pandacat.simplechaserun.services.support

import android.content.Context
import android.media.MediaPlayer
import android.util.Log
import com.pandacat.simplechaserun.constants.Constants
import com.pandacat.simplechaserun.data.monsters.MonsterType

class AudioManager(val context: Context): RunManagerBase, MonstersManager.MonsterListener {
    private val TAG = "AudioManager"
    private var mediaPlayer: MediaPlayer? = null
    private var currentMonster: MonsterType? = null

    override fun startRun() {
    }

    override fun pauseRun() {
        clearMediaPlayer()
    }

    override fun stopRun() {
        pauseRun()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onMonsterStarted(type: MonsterType) {

    }

    override fun onMonsterMoved(type: MonsterType, distInSeconds: Long) {
        if (type != currentMonster || mediaPlayer == null)
        {
            clearMediaPlayer()
            mediaPlayer = MediaPlayer.create(context, type.getDangerSound())
            currentMonster = type
        }

        mediaPlayer?.let {
            val volume = (1 * (1 - distInSeconds.toFloat() / Constants.MONSTER_DANGER_CLOSE_SECONDS))
            Log.i(TAG, "onMonsterMoved: volume $volume")
            if (volume > .01)
            {
                it.setVolume(volume, volume)
                Log.i(TAG, "onMonsterMoved: volume $volume has been set")
                it.isLooping = true
                it.start()
            } else {
                clearMediaPlayer()
            }
        }
    }

    override fun onMonsterFinished(type: MonsterType, success: Boolean) {
        mediaPlayer?.stop()
    }

    private fun clearMediaPlayer()
    {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

}