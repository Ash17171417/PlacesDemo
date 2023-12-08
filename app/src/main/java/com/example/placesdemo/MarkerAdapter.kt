package com.example.placesdemo

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.example.placesdemo.databinding.PlacesListItemBinding

class MarkerAdapter : ListAdapter<MarkLocation, MarkerAdapter.MarkerViewHolder>(MarkerDiffCallback()) {

    class MarkerViewHolder(private val binding: PlacesListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(markLocation: MarkLocation) {
            binding.placesLatitude.text = "Latitude: ${markLocation.latitude}"
            binding.placesLongitude.text = "Longitude: ${markLocation.longitude}"
            binding.placesAddress.text = "Address: ${markLocation.address}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MarkerViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PlacesListItemBinding.inflate(inflater, parent, false)
        return MarkerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MarkerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    private class MarkerDiffCallback : DiffUtil.ItemCallback<MarkLocation>() {
        override fun areItemsTheSame(oldItem: MarkLocation, newItem: MarkLocation): Boolean {
            return oldItem.latitude == newItem.latitude && oldItem.longitude == newItem.longitude
        }

        override fun areContentsTheSame(oldItem: MarkLocation, newItem: MarkLocation): Boolean {
            return oldItem == newItem
        }
    }
}
