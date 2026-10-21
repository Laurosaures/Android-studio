package com.example.eldorado

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class game1 : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.game1)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val Home = findViewById<ImageView>(R.id.imageView)
        Home.setOnClickListener {
            val intent = Intent(this,home::class.java)
            startActivity(intent)
            finish()
        }
        val Login = findViewById<ImageView>(R.id.imageView2)
        Login.setOnClickListener {
            val intent = Intent(this,login::class.java)
            startActivity(intent)
            finish()
        }
        val Shopingcart = findViewById<ImageView>(R.id.imageView3)
        Shopingcart.setOnClickListener {
            val intent = Intent(this,shopingcart::class.java)
            startActivity(intent)
            finish()
        }
        val CartButtonB = findViewById<ImageView>(R.id.imageView9)
        CartButtonB.setOnClickListener {
            val intent = Intent(this,shopingcart::class.java)
            startActivity(intent)
            finish()
        }
        val CartButton = findViewById<ImageView>(R.id.imageView10)
        CartButton.setOnClickListener {
            val intent = Intent(this,shopingcart::class.java)
            startActivity(intent)
            finish()
        }
    }
}