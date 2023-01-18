package com.mancal.mancal.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mancal.mancal.model.Venue

class VenueViewModel: ViewModel() {
    var listVenue: MutableList<Venue> = mutableListOf<Venue>()
    private val _liveDataVenue = MutableLiveData<MutableList<Venue>>()
    val liveDataVenue: LiveData<MutableList<Venue>> get() = _liveDataVenue

    public fun addVenue(venue: Venue) {
        listVenue.add(venue)
    }
    public fun emptyList() {
        listVenue.clear()
    }
}