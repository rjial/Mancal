package com.mancal.mancal.model

import android.os.Parcelable
import com.mapbox.geojson.Point
import kotlinx.parcelize.Parcelize

@Parcelize
data class Venue(val title: String, val address: String, val bicycleCount: Int, val latitude: Double, val longitude: Double ): Parcelable {
    public fun getPoint(): Point {
        return Point.fromLngLat(longitude, latitude)
    }
}