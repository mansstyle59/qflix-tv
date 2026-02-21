package com.qflix.tv.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import com.qflix.tv.R

class DetailsActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_details)

        if (savedInstanceState == null) {
            val f = DetailsFragment()
            f.arguments = intent.extras
            supportFragmentManager.beginTransaction()
                .replace(R.id.details_container, f)
                .commitNow()
        }
    }
}
