package com.example.toothpick.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.toothpick.R
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.helper.BackpackItemValidator
import toothpick.Toothpick
import toothpick.ktp.KTP
import toothpick.smoothie.lifecycle.closeOnDestroy
import javax.inject.Inject

class AddNewActivity : AppCompatActivity() {

    companion object {
        const val NEW_ITEM_NAME_KEY = "name"
    }

    //will be created as a singleton in the root scope
    //and is releasable under memory pressure
    @Inject
    lateinit var backpackItemValidator: BackpackItemValidator

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Open Activity scope as child of Application scope
        // 2. Inject dependencies
        KTP.openScope(ApplicationScope::class.java)
                .openSubScope(this)
                .closeOnDestroy(this)
                .inject(this)
        setupUIComponents()
    }

    private fun setupUIComponents() {
        setContentView(R.layout.backpack_new)
        val editText = findViewById<EditText>(R.id.new_name)
        val button = findViewById<Button>(R.id.add_item)
        button.setOnClickListener {
            val text = editText.text.toString()
            if (backpackItemValidator.isValidName(text)) {
                val intent = Intent()
                        .putExtra(NEW_ITEM_NAME_KEY, text)
                setResult(RESULT_OK, intent)
                finish()
            }
        }
    }
}
