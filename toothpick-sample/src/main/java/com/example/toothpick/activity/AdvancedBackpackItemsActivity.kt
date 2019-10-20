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
import com.example.toothpick.adapter.BackpackAdapter
import com.example.toothpick.adapter.IBackpackAdapter
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.annotation.ViewModelScope
import com.example.toothpick.helper.NotificationHelper
import com.example.toothpick.model.Backpack
import com.example.toothpick.viewmodel.BackpackViewModel
import toothpick.Scope
import toothpick.ktp.KTP
import toothpick.ktp.binding.bind
import toothpick.ktp.binding.module
import toothpick.ktp.delegate.inject
import toothpick.ktp.delegate.lazy
import toothpick.smoothie.lifecycle.closeOnDestroy
import toothpick.smoothie.viewmodel.closeOnViewModelCleared
import toothpick.smoothie.viewmodel.installViewModelBinding

/**
 * Advanced version of the BackpackItemsActivity.
 *
 * In this example, the backpack is retained on configuration
 * changes as it belongs to the view model scope which follows
 * the lifecycle of view model instances: when an instance is
 * destroyed, and later recreated, the scope remains unchanged
 * and the backpack instance will be the same.
 */
class AdvancedBackpackItemsActivity : AppCompatActivity() {

    companion object {
        const val ADD_NEW_REQUEST = 1
    }

    //will be created in the app scope
    //as it is annotated with @Singleton
    val notificationHelper: NotificationHelper by lazy()
    //will be created in the ViewModelScope as we have installed
    //the binding on that scope using `installViewModelBinding`.
    val viewModel: BackpackViewModel by inject()
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
        // 1. Open Application scope
        // 2. Open ViewModelScope as child of Application scope and
        //    2.1 install the viewmodel
        //    2.2 close when viewmodel is cleared
        //    2.3 install backpack as a singleton via binding
        // 3. Open activity scope as child of ViewModelScope scope
        // 4. Install module inside Activity scope containing:
        //    4.1 when injection IBackpackAdapter, use the class BackpackAdapter
        // 5. Close activity scope when activity is destroyed
        // 6. Inject dependencies
        KTP.openScopes(ApplicationScope::class.java)
            .openSubScope(ViewModelScope::class.java) { scope: Scope ->
                scope.installViewModelBinding<BackpackViewModel>(this)
                    .closeOnViewModelCleared(this)
                    .installModules(module {
                        bind<Backpack>().singleton()
                    })
            }
            .openSubScope(this)
            .installModules(module {
                bind<IBackpackAdapter>().toClass<BackpackAdapter>()
            })
            .closeOnDestroy(this)
            .inject(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == ADD_NEW_REQUEST) {
            if (resultCode == RESULT_OK) {
                data?.getStringExtra(AddNewActivity.NEW_ITEM_NAME_KEY)?.also { itemName ->
                    viewModel.backpack.addItem(itemName)
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
