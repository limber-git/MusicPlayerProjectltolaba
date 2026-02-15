package com.limbe.hexamusicplayer.app

import android.app.Application

class MusicApplication : Application() {
    val container: AppContainer by lazy {
        AppContainer(applicationContext)
    }

    override fun onTerminate() {
        container.shutdown()
        super.onTerminate()
    }
}
