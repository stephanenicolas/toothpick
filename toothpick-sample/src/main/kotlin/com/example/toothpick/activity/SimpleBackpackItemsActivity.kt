package com.example.toothpick.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.toothpick.R
import com.example.toothpick.activity.AddNewActivity.Companion.NEW_ITEM_NAME_KEY
import com.example.toothpick.adapter.BackpackAdapter
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.helper.NotificationHelper
import com.example.toothpick.model.Backpack
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.binding.toInstance
import toothpick.ktp.delegate.inject
import toothpick.ktp.delegate.lazy


/**
 * Simple version of the BackpackItemsActivity.
 *
 * Here we do not retain the backpack at all.
 */
class SimpleBackpackItemsActivity : AppCompatActivity() {

    companion object {
        const val ADD_NEW_REQUEST = 1
    }

    val notificationHelper: NotificationHelper by lazy()
    val backpack: Backpack by inject()

    private lateinit var coordinatorLayout: CoordinatorLayout
    private lateinit var viewAdapter: RecyclerView.Adapter<*>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.backpack_list)

        coordinatorLayout = findViewById(R.id.coordinatorLayout)

        // 1. Open Activity scope as child of Application scope
        // 2. Install module inside Activity scope
        // 3. Inject dependencies
        KTP.openScopes(ApplicationScope::class.java, this)
                .installModules(module {
                    bind<AppCompatActivity>().toInstance { this@SimpleBackpackItemsActivity }
                })
                .inject(this)

        viewAdapter = BackpackAdapter(backpack)
        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@SimpleBackpackItemsActivity)
            adapter = viewAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))
        }

        findViewById<Button>(R.id.add_new).setOnClickListener { goToAddNewActivity() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                data?.getStringExtra(NEW_ITEM_NAME_KEY)?.also { itemName ->
                    backpack.addItem(itemName)
                    viewAdapter.notifyDataSetChanged()
                    notificationHelper.showNotification(coordinatorLayout, "New Item added")
                }
            }
        }
    }

    private fun goToAddNewActivity() {
        val intent = Intent(this, AddNewActivity::class.java)
        startActivityForResult(intent, ADD_NEW_REQUEST)
    }
}
