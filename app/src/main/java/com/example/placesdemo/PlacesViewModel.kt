package com.example.placesdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlacesViewModel : ViewModel() {

    val markerLocations = MutableLiveData<List<MarkerLocation>>()

    fun addMarkerLocation(markerLocation: MarkerLocation) {
        val currentList = markerLocations.value.orEmpty().toMutableList()
        currentList.add(markerLocation)
        markerLocations.value = currentList
    }
}