package xyz.bluspring.kilt.util

import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform

// https://github.com/Kotlin/kotlinx.coroutines/issues/1147

fun <T, R> Flow<T>.mapAsync(transform: suspend (value: T) -> R): Flow<R> =
    channelFlow {
        collect { e -> send(async { transform(e) }) }
    }.map { it.await() }

fun <T> Flow<T>.filterAsync(predicate: suspend (value: T) -> Boolean): Flow<T> =
    mapAsync { it to predicate(it) }.transform { if (it.second) emit(it.first) }

fun <T> Flow<T>.onEachAsync(action: suspend (value: T) -> Unit): Flow<T> =
    mapAsync {
        action(it)
        it
    }

fun <T, R> Flow<T>.flatMapAsync(transform: suspend (value: T) -> Flow<R>): Flow<R> =
    mapAsync { transform(it) }.transform { it.collect { emit(it) } }