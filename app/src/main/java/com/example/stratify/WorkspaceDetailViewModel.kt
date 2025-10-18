package com.example.stratify

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class WorkspaceDetailViewModel : ViewModel() {

    // LiveData holding the list of progress items. It is private to prevent direct modification from the UI.
    private val _progressList = MutableLiveData<List<ProgressItem>>(emptyList())
    // Public LiveData that the UI can observe for changes in the progress list.
    val progressList: LiveData<List<ProgressItem>> = _progressList

    /**
     * Adds a new progress item to the list.
     */
    fun addProgress(progressText: String) {
        // Only add non-empty progress text.
        if (progressText.isNotEmpty()) {
            // Get the current date and format it.
            val currentDate = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date())
            // Create a new ProgressItem.
            val newProgress = ProgressItem(progressText, currentDate)
            // Get the current list, or an empty list if it's null.
            val newList = _progressList.value.orEmpty().toMutableList()
            // Add the new item to the beginning of the list.
            newList.add(0, newProgress)
            // Update the LiveData with the new list.
            _progressList.value = newList
        }
    }

    /**
     * Removes a progress item from the list.
     */
    fun removeProgress(progressItem: ProgressItem) {
        // Get the current list.
        val newList = _progressList.value.orEmpty().toMutableList()
        // Remove the specified item.
        newList.remove(progressItem)
        // Update the LiveData with the modified list.
        _progressList.value = newList
    }
}
