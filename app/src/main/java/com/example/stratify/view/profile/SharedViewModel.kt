package com.example.stratify.view.profile

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    val displayName = MutableLiveData<String>()
    val photoUri = MutableLiveData<Uri>()
}