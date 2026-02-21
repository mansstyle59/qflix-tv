package com.qflix.tv.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.qflix.tv.R
import com.qflix.tv.data.SettingsStore
import kotlinx.coroutines.runBlocking
import com.qflix.tv.data.ContentStore

class MainActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
setContentView(R.layout.activity_main)

val settings=SettingsStore(this)
val startLive=runBlocking{ kotlinx.coroutines.flow.first(settings.startLiveFlow) }
if(startLive){
    val live=ContentStore.lastLoadedItems.firstOrNull{it.isLive}
    if(live!=null){
        startActivity(android.content.Intent(this, PlaybackActivity::class.java).apply{
            putExtra(DetailsFragment.EXTRA_MEDIA,live)
        })
        finish()
        return
    }
}


        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.main_container, MainBrowseFragment())
                .commitNow()
        }
    }
}
