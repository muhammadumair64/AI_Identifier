package com.iobits.tech.app.ai_identifier.ui.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.iobits.tech.app.ai_identifier.database.dataClasses.Collection
import com.iobits.tech.app.ai_identifier.ui.repositories.CollectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionViewModel @Inject constructor(private val repository: CollectionRepository) : ViewModel() {

    val allItems: LiveData<List<Collection>> = repository.allItems.asLiveData()

    fun insertItem(title: String, description: String, image: String, stringToSplit: String, detect: String) {
        val newItem = Collection(
            title = title,
            description = description,
            image = image,
            stringToSplit = stringToSplit,
            detect = detect
        )
        viewModelScope.launch {
            repository.insertItem(newItem)
        }
    }

    fun deleteItem(itemId: Int) {
        viewModelScope.launch {
            repository.deleteItem(itemId)
        }
    }
}
