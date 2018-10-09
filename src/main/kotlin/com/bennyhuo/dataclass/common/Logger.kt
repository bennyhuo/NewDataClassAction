package com.bennyhuo.dataclass.common

import com.intellij.openapi.diagnostic.Logger
import java.util.*

private val cachedLogger = HashMap<String, Logger>()

val Any.logger: Logger
    get() {
        return cachedLogger[this.javaClass.canonicalName] ?: Logger.getInstance(this.javaClass).also {
            cachedLogger[this.javaClass.canonicalName] = it
        }
    }