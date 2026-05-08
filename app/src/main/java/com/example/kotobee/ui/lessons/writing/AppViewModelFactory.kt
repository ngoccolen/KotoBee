package com.example.kotobee.ui.lessons.writing

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.kotobee.data.repository.KanjiRepository
import com.example.kotobee.ui.lessons.writing.KanjiPracticeViewModel

class AppViewModelFactory(private val repository: KanjiRepository) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(KanjiPracticeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return KanjiPracticeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}