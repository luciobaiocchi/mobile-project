package com.heard.mobile.ui.screens.addPath

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class AddPathState(
    val destination: String = "",
    val date: String = "",
    val description: String = "",
    val imageUri: Uri = Uri.EMPTY
) {
    val canSubmit get() = destination.isNotBlank() && date.isNotBlank() && description.isNotBlank()
}

interface AddPathActions {
    fun setDestination(destination: String)
    fun setDate(date: String)
    fun setDescription(description: String)
    fun setImageUri(imageUri: Uri)
}

class AddPathViewModel : ViewModel() {

    init {
        println("AddPathViewModel creato!")
    }

    private val _state = MutableStateFlow(AddPathState())
    val state = _state.asStateFlow()

    val actions = object : AddPathActions {
        override fun setDestination(destination: String) {
            _state.update { it.copy(destination = destination) }
        }

        override fun setDate(date: String) {
            _state.update { it.copy(date = date) }
        }

        override fun setDescription(description: String) {
            _state.update { it.copy(description = description) }
        }

        override fun setImageUri(imageUri: Uri) {
            _state.update { it.copy(imageUri = imageUri) }
        }
    }
}
