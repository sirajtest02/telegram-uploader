package com.siraj.telegramuploader

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val correctPin = "1234"
    private val enteredPin = StringBuilder()
    private lateinit var dots: Array<View>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        dots = arrayOf(
            findViewById(R.id.dot1),
            findViewById(R.id.dot2),
            findViewById(R.id.dot3),
            findViewById(R.id.dot4)
        )

        setupKeypad()
    }

    private fun setupKeypad() {
        val numberButtons = intArrayOf(
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        )

        for (id in numberButtons) {
            findViewById<Button>(id).setOnClickListener {
                if (enteredPin.length < 4) {
                    enteredPin.append((it as Button).text)
                    updateDots()
                    if (enteredPin.length == 4) verifyPin()
                }
            }
        }

        findViewById<ImageButton>(R.id.btnDelete).setOnClickListener {
            if (enteredPin.isNotEmpty()) {
                enteredPin.deleteCharAt(enteredPin.length - 1)
                updateDots()
            }
        }
    }

    private fun updateDots() {
        for (i in dots.indices) {
            if (i < enteredPin.length) {
                dots[i].backgroundTintList = getColorStateList(android.R.color.holo_orange_light)
            } else {
                dots[i].backgroundTintList = getColorStateList(android.R.color.darker_gray)
            }
        }
    }

    private fun verifyPin() {
        if (enteredPin.toString() == correctPin) {
            startActivity(Intent(this, DashboardActivity::class.java))
            finish()
        } else {
            Toast.makeText(this, "Wrong PIN! Try again.", Toast.LENGTH_SHORT).show()
            enteredPin.clear()
            updateDots()
        }
    }
}
