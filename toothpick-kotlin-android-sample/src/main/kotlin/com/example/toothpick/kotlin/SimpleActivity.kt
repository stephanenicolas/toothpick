package com.example.toothpick.kotlin

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.FragmentActivity
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import com.example.toothpick.kotlin.deps.ContextNamer
import javax.inject.Inject
import toothpick.ktp.KTP.openScope
import toothpick.smoothie.lifecycle.closeOnDestroy
import toothpick.smoothie.module.SmoothieAndroidXActivityModule

class SimpleActivity : FragmentActivity() {

    @Inject
    lateinit var backpack: Backpack
    @Inject
    lateinit var viewModel: SimpleActivityViewModel

    @Inject
    lateinit var contextNamer: ContextNamer
    @BindView(R.id.title)
    lateinit var title: TextView
    @BindView(R.id.subtitle)
    lateinit var subTitle: TextView
    @BindView(R.id.hello)
    lateinit var button: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        openScope(application)
                .openBackpackFlowSubScope()
                .openSimpleActivityViewModelSubScope(this)
                .openSubKTPScope(this) { ifScopeCreated ->
                    ifScopeCreated
                            .installModules(SmoothieAndroidXActivityModule(this))
                            .closeOnDestroy(this)
                }
                .inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.simple_activity)
        ButterKnife.bind(this)
        title.setText(contextNamer.applicationName)
        subTitle.setText(contextNamer.activityName)
        button.setText("click me !")
    }

    @OnClick(R.id.hello)
    @SuppressWarnings("unused")
    internal fun startNewActivity() {
        startActivity(Intent(this, RxMVPActivity::class.java))
    }
}
