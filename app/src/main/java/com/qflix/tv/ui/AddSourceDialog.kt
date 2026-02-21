package com.qflix.tv.ui

import android.app.Dialog
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.qflix.tv.R
import com.qflix.tv.model.Source

class AddSourceDialog : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val name = EditText(requireContext()).apply { hint = getString(R.string.source_name) }
        val url = EditText(requireContext()).apply { hint = getString(R.string.source_url) }
        val epg = EditText(requireContext()).apply { hint = "EPG XMLTV (optionnel)" }

        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 32, 48, 8)
            addView(name)
            addView(url)
            addView(epg)
        }

        return AlertDialog.Builder(requireContext())
            .setTitle(getString(R.string.add_source))
            .setView(container)
            .setPositiveButton(getString(R.string.save)) { _, _ ->
                val n = name.text.toString().trim().ifBlank { "Source" }
                val u = url.text.toString().trim()
                val e = epg.text.toString().trim().ifBlank { null }
                if (!Patterns.WEB_URL.matcher(u).matches()) {
                    Toast.makeText(requireContext(), getString(R.string.invalid_url), Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                (parentFragmentManager.fragments.firstOrNull { it is MainBrowseFragment } as? MainBrowseFragment)
                    ?.addSource(Source(n, u, e))
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .create()
    }
}
