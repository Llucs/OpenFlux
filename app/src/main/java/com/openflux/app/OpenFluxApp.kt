package com.openflux.app

import android.app.Application

class OpenFluxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        lateinit var instance: OpenFluxApp
    }
}
