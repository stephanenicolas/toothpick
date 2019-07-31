package com.example.toothpick.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.toothpick.R
import com.example.toothpick.annotation.ApplicationScope
import com.example.toothpick.helper.BackpackItemValidator
import toothpick.ktp.KTP
import toothpick.ktp.delegate.lazy

class AddNewActivity : AppCompatActivity() {

    companion object {
        const val NEW_ITEM_NAME_KEY = "name"
    }

    val backpackItemValidator: BackpackItemValidator by lazy()

    private lateinit var editText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.backpack_new)

        // 1. Open Activity scope as child of Application scope
        // 2. Inject dependencies
        KTP.openScopes(ApplicationScope::class.java, this)
                .inject(this)

        editText = findViewById(R.id.new_name)
        findViewById<Button>(R.id.add_item).setOnClickListener {
            if (backpackItemValidator.isValidName(editText.text.toString())) {
                returnNewElement()
            }
        }
    }

    private fun returnNewElement() {
        val intent = Intent().apply {
            putExtra(NEW_ITEM_NAME_KEY, editText.text.toString())
        }
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}
