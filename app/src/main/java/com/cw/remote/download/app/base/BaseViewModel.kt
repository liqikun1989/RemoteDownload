package com.cw.remote.download.app.base

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    fun load(
        block: suspend CoroutineScope.() -> Unit,
        onError: (e: Throwable) -> Unit = {},
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(CoroutineExceptionHandler { _, e ->
            onError(e)
        }) {
            try {
                block.invoke(this)
            } finally {
                onComplete()
            }
        }
    }
}