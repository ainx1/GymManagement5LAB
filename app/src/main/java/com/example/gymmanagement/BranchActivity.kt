package com.example.gymmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class BranchActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_branch)

        findViewById<Button>(R.id.btnBranchCentral).setOnClickListener {
            returnBranch("Центральный (пр. Мира, 10)")
        }

        findViewById<Button>(R.id.btnBranchNorth).setOnClickListener {
            returnBranch("Северный (ул. Ленина, 45)")
        }

        findViewById<Button>(R.id.btnBranchSouth).setOnClickListener {
            returnBranch("Южный (ул. Гагарина, 108)")
        }
    }

    // T2: Отправка выбранного филиала обратно в MainActivity
    private fun returnBranch(branchName: String) {
        val resultIntent = Intent().apply {
            putExtra("EXTRA_BRANCH_NAME", branchName)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }
}