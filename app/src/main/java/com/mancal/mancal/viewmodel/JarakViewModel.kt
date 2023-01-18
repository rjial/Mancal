package com.mancal.mancal.viewmodel

import android.provider.Settings.Global.getString
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mapbox.geojson.Point
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.turf.TurfMeasurement

class JarakViewModel(): ViewModel() {
    private val liveData = MutableLiveData<Double>()

    public fun getLiveData(): LiveData<Double> {
        return liveData
    }

    public fun setDistance(awal: Point, tujuan: Point) {
        liveData.postValue(TurfMeasurement.distance(awal, tujuan))
    }

    public fun getDistance(): Double? {
        return liveData.value
    }



}