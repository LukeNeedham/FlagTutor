package com.flagtutor.app.data.local

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Runs [load] the first time [get] is called and keeps the result in memory. Concurrent callers
 * wait for that one load rather than starting their own, so the data is read exactly once.
 * A failed or cancelled load isn't cached; the next caller tries again.
 */
class LoadOnce<T : Any>(private val load: suspend () -> T) {

    private val mutex = Mutex()
    private var value: T? = null

    suspend fun get(): T {
        value?.let { return it }
        return mutex.withLock {
            value ?: load().also { value = it }
        }
    }
}
