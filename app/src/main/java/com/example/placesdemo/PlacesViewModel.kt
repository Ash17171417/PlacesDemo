package com.example.placesdemo

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class PlacesViewModel : ViewModel() {

    val markLocations = MutableLiveData<List<MarkLocation>>()

    fun addMarkLocation(markLocation: MarkLocation) {
        val currentList = markLocations.value.orEmpty().toMutableList()
        currentList.add(markLocation)
        markLocations.value = currentList
    }
}