package com.example.gymmanagement

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private val registrationService = GymRegistrationService()
    private val memberList = mutableListOf<GymMember>()
    private lateinit var adapter: GymMemberAdapter

    // данные из других окон (Лаб 4)
    private var assignedTrainer: String = "Не назначен"
    private var selectedBranch: String = "Не выбран"

    // приемник тренера (Лаб 4)
    private val trainerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val name = result.data?.getStringExtra("EXTRA_TRAINER_NAME") ?: ""
            val spec = result.data?.getStringExtra("EXTRA_TRAINER_SPEC") ?: ""
            assignedTrainer = "$name ($spec)"
            Toast.makeText(this, "Выбран тренер: $assignedTrainer", Toast.LENGTH_SHORT).show()
        }
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_member
    }

    // приемник филиала (Лаб 4)
    private val branchLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedBranch = result.data?.getStringExtra("EXTRA_BRANCH_NAME") ?: "Не выбран"
            Toast.makeText(this, "Выбран филиал: $selectedBranch", Toast.LENGTH_SHORT).show()
        }
        findViewById<BottomNavigationView>(R.id.bottomNavigation).selectedItemId = R.id.nav_member
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // начальные карточки для демонстрации списка
        initInitialData()

        val rvMembers = findViewById<RecyclerView>(R.id.rvMembers) //recycler

        val btnAddMember = findViewById<Button>(R.id.btnAddMember)
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        // инициализация адаптера с обработкой U2 (Редактировать) и D2 (Удалить)
        adapter = GymMemberAdapter(
            members = memberList,
            onEditClick = { position, member ->
                // U2: Редактирование через контекстное меню
                showMemberDialog(memberToEdit = member, position = position)
            },
            onDeleteClick = { position, member ->
                // D2: Удаление через контекстное меню
                memberList.removeAt(position)
                adapter.notifyItemRemoved(position) // с анимацией удаляет
                Toast.makeText(this, "Клиент ${member.fullName} удалён", Toast.LENGTH_SHORT).show()
            }
        )

        // O1 (Вертикальная) + N2 (2 элемента в строке):
        rvMembers.layoutManager = GridLayoutManager(this, 2, RecyclerView.VERTICAL, false)
        // false первый элемент наверху, а не внизу
        rvMembers.adapter = adapter

        // C1: Добавление нового элемента по нажатию на отдельную кнопку
        btnAddMember.setOnClickListener {
            showMemberDialog(memberToEdit = null, position = null)
        }

        // навигация нижнего меню (Лаб 4)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_member -> true
                R.id.nav_trainer -> {
                    val intent = Intent(this, TrainerActivity::class.java)
                    trainerLauncher.launch(intent)
                    true
                }
                R.id.nav_branch -> {
                    val intent = Intent(this, BranchActivity::class.java)
                    branchLauncher.launch(intent)
                    true
                }
                else -> false
            }
        }
    }

    // Диалог ввода для C1 (Добавление) и U2 (Редактирование)
    private fun showMemberDialog(memberToEdit: GymMember?, position: Int?) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_member, null)
        val etName = dialogView.findViewById<EditText>(R.id.etDialogName)
        val etAge = dialogView.findViewById<EditText>(R.id.etDialogAge)
        val etPhone = dialogView.findViewById<EditText>(R.id.etDialogPhone)
        val etMonths = dialogView.findViewById<EditText>(R.id.etDialogMonths)

        val isEditMode = memberToEdit != null
        if (isEditMode) {
            etName.setText(memberToEdit.fullName)
            etAge.setText(memberToEdit.age.toString())
            etPhone.setText(memberToEdit.phone)
            etMonths.setText(memberToEdit.durationMonths.toString())
        }

        AlertDialog.Builder(this)
            .setTitle(if (isEditMode) "Редактировать клиента" else "Добавить клиента")
            .setView(dialogView)
            .setPositiveButton(if (isEditMode) "Сохранить" else "Добавить") { _, _ ->
                val result = registrationService.registerMember(
                    nameInput = etName.text.toString(),
                    ageInput = etAge.text.toString(),
                    phoneInput = etPhone.text.toString(),
                    monthsInput = etMonths.text.toString()
                )

                when (result) {
                        is RegistrationResult.Success -> {
                            if (isEditMode && position != null) {
                                // U2: При редактировании сохраняем того же тренера и филиал, что уже были
                                val updatedMember = result.member.copy(
                                    trainer = memberToEdit.trainer,
                                    branch = memberToEdit.branch
                                )
                                memberList[position] = updatedMember
                                adapter.notifyItemChanged(position)
                                Toast.makeText(this, "Данные обновлены", Toast.LENGTH_SHORT).show()
                            }

                            else {
                                // C1: При добавлении НОВОГО клиента — прикрепляем текущего выбранного тренера и филиал!
                                val newMember = result.member.copy(
                                    trainer = assignedTrainer,
                                    branch = selectedBranch
                                )
                                memberList.add(newMember)
                                adapter.notifyItemInserted(memberList.size - 1)
                                Toast.makeText(this, "Клиент добавлен (Тренер: $assignedTrainer)", Toast.LENGTH_SHORT).show()
                            }
                        }
                        is RegistrationResult.Error -> {
                            Toast.makeText(this, result.errorMessage, Toast.LENGTH_LONG).show()
                        }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun initInitialData() {
        val samples = listOf(
            Triple("Иванов Иван", "25", "3"),
            Triple("Смирнова Ольга", "62", "12"),
            Triple("Петров Алексей", "16", "1"),
            Triple("Кузнецов Денис", "34", "6"),
            Triple("Бобов Борис", "21", "4"),
            Triple("Фазанова Кристина", "32", "14"),
            Triple("Дамасков Андрей", "19", "7"),
            Triple("Колымко Данила", "21", "6"),
            Triple("Цаль Виталий", "15", "1"),
            Triple("Новиков Руслан", "22", "9")
        )
        for (s in samples) {
            val res = registrationService.registerMember(s.first, s.second, "+79991234567", s.third)
            if (res is RegistrationResult.Success) {
                memberList.add(res.member.copy(trainer = "Алексеев (Бокс)", branch = "Центральный"))
            }
        }
    }
}