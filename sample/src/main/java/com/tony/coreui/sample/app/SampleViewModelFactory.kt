package com.tony.coreui.sample.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

fun <VM : ViewModel> sampleViewModelFactory(initializer: () -> VM): ViewModelProvider.Factory =
    object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val viewModel = initializer()
            require(modelClass.isInstance(viewModel)) {
                "Expected ${modelClass.name}, but created ${viewModel::class.java.name}."
            }

            @Suppress("UNCHECKED_CAST")
            return viewModel as T
        }
    }
