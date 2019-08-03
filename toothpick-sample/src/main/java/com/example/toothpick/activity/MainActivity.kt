package com.example.toothpick.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.example.toothpick.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)

        findViewById<Button>(R.id.go_to_simple).setOnClickListener {
            startActivity(Intent(this, SimpleBackpackItemsActivity::class.java))
        }
        findViewById<Button>(R.id.go_to_advanced).setOnClickListener {
            startActivity(Intent(this, AdvancedBackpackItemsActivity::class.java))
        }
    }
}
