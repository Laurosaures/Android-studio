package com.example.eldorado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast

class MainActivity3 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main3)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val Home = findViewById<ImageView>(R.id.imageView)
        Home.setOnClickListener {
            val intent = Intent(this,MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        val Login = findViewById<ImageView>(R.id.imageView2)
        Login.setOnClickListener {
            val intent = Intent(this,MainActivity3::class.java)
            startActivity(intent)
            finish()
        }
        val Shopingcart = findViewById<ImageView>(R.id.imageView3)
        Shopingcart.setOnClickListener {
            val intent = Intent(this,MainActivity3::class.java)
            startActivity(intent)
            finish()
        }
        val BackButton = findViewById<Button>(R.id.button)
        BackButton.setOnClickListener {
            val intent = Intent(this,MainActivity2::class.java)
            startActivity(intent)
            finish()
        }
        val NextButton = findViewById<Button>(R.id.button2)
        NextButton.setOnClickListener {
            val intent = Intent(this,MainActivity4::class.java)
            startActivity(intent)
            finish()
        }
    }
}