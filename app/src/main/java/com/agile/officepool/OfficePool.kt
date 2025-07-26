package com.agile.officepool

import android.app.Application
import android.content.Context

class OfficePool : Application() {

    init {
        instance = this
    }

    companion object {
        private lateinit var instance: OfficePool
        fun context(): Context = instance.applicationContext
    }
}