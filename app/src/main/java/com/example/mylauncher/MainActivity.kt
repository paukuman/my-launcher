package com.example.mylauncher

import android.app.Activity
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

data class AppInfo(val label: String, val packageName: String, val activityName: String)

class MainActivity : Activity() {

    private lateinit var adapter: AppAdapter
    private lateinit var search: EditText
    private lateinit var list: RecyclerView
    private var allApps: List<AppInfo> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        search = findViewById(R.id.search)
        list = findViewById(R.id.app_list)

        adapter = AppAdapter(packageManager, ::launch, ::showOptions)
        list.layoutManager = GridLayoutManager(this, 4)
        list.adapter = adapter

        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
            override fun afterTextChanged(s: Editable?) {
                applyFilter(s?.toString().orEmpty())
            }
        })

        // Tekan "Go" di keyboard -> buka hasil pertama
        search.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                adapter.items.firstOrNull()?.let { launch(it) }
                true
            } else false
        }
    }

    override fun onResume() {
        super.onResume()
        loadApps()
        applyFilter(search.text.toString())
    }

    // Dipanggil saat tombol Home ditekan ketika launcher sudah terbuka
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        search.setText("")
        search.clearFocus()
        list.scrollToPosition(0)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        // Launcher tidak boleh tertutup oleh tombol Back
        search.setText("")
        list.scrollToPosition(0)
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        allApps = packageManager.queryIntentActivities(intent, 0)
            .map {
                AppInfo(
                    label = it.loadLabel(packageManager).toString(),
                    packageName = it.activityInfo.packageName,
                    activityName = it.activityInfo.name
                )
            }
            .filter { it.packageName != packageName } // sembunyikan launcher ini sendiri
            .sortedBy { it.label.lowercase() }
    }

    private fun applyFilter(query: String) {
        val q = query.trim().lowercase()
        adapter.items = if (q.isEmpty()) allApps
        else allApps.filter { it.label.lowercase().contains(q) }
    }

    private fun launch(app: AppInfo) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setComponent(ComponentName(app.packageName, app.activityName))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED)
        try {
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal membuka ${app.label}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showOptions(app: AppInfo) {
        AlertDialog.Builder(this)
            .setTitle(app.label)
            .setItems(arrayOf("Info aplikasi", "Copot pemasangan")) { _, which ->
                val uri = Uri.parse("package:${app.packageName}")
                when (which) {
                    0 -> startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, uri))
                    1 -> startActivity(Intent(Intent.ACTION_DELETE, uri))
                }
            }
            .show()
    }
}
