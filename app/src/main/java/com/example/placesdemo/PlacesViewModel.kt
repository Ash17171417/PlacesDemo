package com.example.placesdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlacesViewModel : ViewModel() {

    val markerLocations = MutableLiveData<List<MarkLocation>>()

    fun addMarkerLocation(markerLocation: MarkLocation) {
        val currentList = markerLocations.value.orEmpty().toMutableList()
        currentList.add(markerLocation)
        markerLocations.value = currentList
    }
}