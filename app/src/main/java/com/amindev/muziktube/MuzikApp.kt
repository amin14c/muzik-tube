package com.amindev.muziktube

import android.app.Application
import org.schabi.newpipe.extractor.NewPipe
import org.schabi.newpipe.extractor.localization.ContentCountry
import org.schabi.newpipe.extractor.localization.Localization

class MuzikApp : Application() {
    override fun onCreate() {
        super.onCreate()
        NewPipe.init(
            YtDownloader.getInstance(),
            Localization("en", "US"),
            ContentCountry("US")
        )
    }
}
