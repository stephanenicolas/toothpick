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
import com.example.toothpick.adapter.BackpackAdapter
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.helper.NotificationHelper
import com.example.toothpick.model.Backpack
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.binding.toClass
import toothpick.ktp.delegate.inject

/**
 * Advanced version of the BackpackItemsActivity.
 *
 * Here we retain the backpack on configuration changes using the ViewModel Scope.
 */
class AdvancedBackpackItemsActivity : AppCompatActivity() {

    companion object {
        const val ADD_NEW_REQUEST = 1
    }

    //will be created in the app scope
    //as it is annotated with @Singleton
    val notificationHelper: NotificationHelper by toothpick.ktp.delegate.lazy()
    //will be created in the current scope = activity scope
    //as they are not annotated by a scope annotation.
    val backpack: Backpack by inject()
    //will be injected in the activity scope as the binding
    //is defined there
    val viewAdapter: RecyclerView.Adapter<out RecyclerView.ViewHolder> by inject()

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 1. Open Activity scope as child of Application scope
        // 2. Install module inside Activity scope
        // 3. Inject dependencies
        KTP.openScopes(ApplicationScope::class.java)
                .openSubScope()
                .installModules(module {
                    bind<RecyclerView.Adapter<out RecyclerView.ViewHolder>>().toClass<BackpackAdapter>()
                })
                .inject(this)

        setupUIComponents()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                data?.getStringExtra(AddNewActivity.NEW_ITEM_NAME_KEY)?.also { itemName ->
                    backpack.addItem(itemName)
                    viewAdapter.notifyDataSetChanged()
                    notificationHelper.showNotification(coordinatorLayout, "New Item added")
                }
            }
        }
    }


    private fun setupUIComponents() {
        setContentView(R.layout.backpack_list)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)


        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@AdvancedBackpackItemsActivity)
            adapter = viewAdapter
            setHasFixedSize(true)
            addItemDecoration(DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL))
        }

        findViewById<Button>(R.id.add_new).setOnClickListener { goToAddNewActivity() }
    }

    private fun goToAddNewActivity() {
        val intent = Intent(this, AddNewActivity::class.java)
        startActivityForResult(intent, ADD_NEW_REQUEST)
    }
}
