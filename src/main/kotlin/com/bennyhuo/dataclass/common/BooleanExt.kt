package com.bennyhuo.dataclass.common

/**
 * Created by benny on 7/3/17.
 */
sealed class BooleanExt constructor(val boolean: Boolean)

object Otherwise : BooleanExt(true)
class WithData<out T>(val data: T) : BooleanExt(false)

inline fun <T> Boolean.yes(block: () -> T): BooleanExt = when {
    this -> {
        WithData(block())
    }
    else -> Otherwise
}

inline fun <T> Boolean.no(block: () -> T) = when {
    this -> Otherwise
    else -> {
        WithData(block())
    }
}

inline infix fun <T> BooleanExt.otherwise(block: () -> T): T {
    return when (this) {
        is Otherwise -> block()
        is WithData<*> -> this.data as T
    }
}

inline operator fun <T> Boolean.invoke(block: () -> T) = yes(block)