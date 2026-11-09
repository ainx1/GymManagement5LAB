package com.example.gymmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class TrainerActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_trainer)

        val etTrainerName = findViewById<EditText>(R.id.etTrainerName)
        val etSpecialization = findViewById<EditText>(R.id.etSpecialization)
        val btnApply = findViewById<Button>(R.id.btnApplyTrainer)

        btnApply.setOnClickListener {
            val name = etTrainerName.text.toString().trim()
            val spec = etSpecialization.text.toString().trim()

            if (name.length < 2 || spec.isEmpty()) {
                Toast.makeText(this, "Заполните ФИО и специализацию тренера!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // T2: Упаковываем данные в Intent и возвращаем результат в главное окно
            val resultIntent = Intent().apply {
                putExtra("EXTRA_TRAINER_NAME", name)
                putExtra("EXTRA_TRAINER_SPEC", spec)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            finish() // Закрываем окно и возвращаемся в MainActivity
        }
    }
}