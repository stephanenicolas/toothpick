package com.example.toothpick.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.toothpick.R
import com.example.toothpick.activity.AddNewActivity.NEW_ITEM_NAME_KEY
import com.example.toothpick.adapter.BackpackAdapter
import com.example.toothpick.adapter.IBackpackAdapter
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.helper.NotificationHelper
import com.example.toothpick.model.Backpack
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.delegate.inject
import toothpick.ktp.delegate.lazy
import toothpick.smoothie.lifecycle.closeOnDestroy


/**
 * Simple version of the BackpackItemsActivity.
 *
 * In this example the backpack is not retained on configuration changes
 * as it belongs to the activity scope which follows the lifecycle of activity
 * instances: when an instance is destroyed, its associated scope is closed
 * and a new scope with a new backpack will be created when the new instance
 * of the activity is created.
 */
class SimpleBackpackItemsActivity : AppCompatActivity() {

    companion object {
        const val ADD_NEW_REQUEST = 1
    }

    //will be created in the app scope
    //as it is annotated with @Singleton
    val notificationHelper: NotificationHelper by lazy()
    //will be created in the current scope = activity scope
    //as they are not annotated by a scope annotation.
    val backpack: Backpack by inject()
    //will be injected in the activity scope as the binding
    //is defined there
    val viewAdapter: IBackpackAdapter by inject()

    private lateinit var coordinatorLayout: CoordinatorLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectDependencies()
        setupUIComponents()
    }

    @VisibleForTesting fun injectDependencies() {
        // 1. Open Activity scope as child of Application scope
        // 2. Install module inside Activity scope containing:
        //    2.1 hen injection IBackpackAdapter, use the class BackpackAdapter
        //    2.2 install backpack as a singleton on the Activity scope via binding
        // 3. Close when activity is destroyed
        // 4. Inject dependencies
        KTP.openScopes(ApplicationScope::class.java, this)
            .installModules(module {
                bind<IBackpackAdapter>().toClass<BackpackAdapter>()
                bind<Backpack>().singleton()
            })
            .closeOnDestroy(this)
            .inject(this)
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


    private fun setupUIComponents() {
        setContentView(R.layout.backpack_list)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)


        findViewById<RecyclerView>(R.id.list).apply {
            layoutManager = LinearLayoutManager(this@SimpleBackpackItemsActivity)
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
