package ru.nurdaulet.customview

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.nurdaulet.customview.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var batteryPercent: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.button.setOnClickListener {
            parsePercent()
            binding.batteryCustomView.setBatteryPercent(batteryPercent)
        }
    }

    private fun parsePercent() {
        val percentText = binding.editTextNumberDecimal.text.toString()
        batteryPercent = try {
            percentText.trim().toInt()
        } catch (nfe: NumberFormatException) {
            0
        }
    }
}