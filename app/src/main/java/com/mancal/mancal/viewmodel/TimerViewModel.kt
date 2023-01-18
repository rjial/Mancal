package com.mancal.mancal.viewmodel

import android.os.SystemClock
import android.text.format.DateUtils
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.Timer
import java.util.TimerTask

class TimerViewModel(): ViewModel() {
    private val ONE_SECOND = 1000

    private val elapsedTime = MutableLiveData<Long>()

    private var initialTime: Long = 0

    private lateinit var timer: Timer

    init {
        initialTime = SystemClock.elapsedRealtime()

        timer = Timer()

        timer.scheduleAtFixedRate(object: TimerTask() {

            override fun run() {
                var newValue = (SystemClock.elapsedRealtime() - initialTime) / 1000;
                elapsedTime.postValue(newValue)
            }

        }, ONE_SECOND.toLong(), ONE_SECOND.toLong())
    }

    public fun getElapsedTime(): LiveData<Long> {
        return elapsedTime
    }
    public fun formatDuration(): String = if (elapsedTime.value!! < 60) {
        elapsedTime.value!!.toString() + " detik"
    } else {
        DateUtils.formatElapsedTime(elapsedTime.value!!)
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }
}