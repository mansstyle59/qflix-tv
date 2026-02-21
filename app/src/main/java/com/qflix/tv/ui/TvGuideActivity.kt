
package com.qflix.tv.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.qflix.tv.R

class TvGuideActivity: FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tv_guide)

        if (savedInstanceState==null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.tvguide_container, TvGuideFragment())
                .commitNow()
        }
    }
}
