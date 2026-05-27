package com.amindev.muziktube

import android.app.Application
import org.schabi.newpipe.extractor.NewPipe

class MuzikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(YtDownloader.getInstance())
    }
}
