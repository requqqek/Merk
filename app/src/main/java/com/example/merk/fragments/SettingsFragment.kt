package com.example.merk.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.merk.R
import com.example.merk.data.api.ChangePasswordRequest
import com.example.merk.data.api.CreateGroupRequest
import com.example.merk.data.api.CreateInviteCodeRequest
import com.example.merk.data.api.GroupItem
import com.example.merk.data.api.RetrofitClient
import com.example.merk.data.models.CreateStudentRequest
import kotlinx.coroutines.launch

class SettingsFragment : Fragment() {

    private var groups: List<GroupItem> = emptyList()

    override fun onCreateView(i: LayoutInflater, c: ViewGroup?, s: Bundle?): View =
        i.inflate(R.layout.fragment_settings, c, false)

    override fun onViewCreated(view: View, s: Bundle?) {
        super.onViewCreated(view, s)
        val prefs = requireContext().getSharedPreferences("merk_session", Context.MODE_PRIVATE)
        val userId = prefs.getInt("userId", 0)
        val role = prefs.getString("role", "") ?: ""

        val btnCreateAssignment = view.findViewById<Button>(R.id.btnCreateAssignment)
        val btnCreateGroup = view.findViewById<Button>(R.id.btnCreateGroup)
        val btnAddStudent = view.findViewById<Button>(R.id.btnAddStudent)
        val btnInviteCodes = view.findViewById<Button>(R.id.btnInviteCodes)
        val btnChangePassword = view.findViewById<Button>(R.id.btnChangePassword)
        val btnManageStudents = view.findViewById<Button>(R.id.btnManageStudents)

        if (role == "Teacher") {
            btnCreateAssignment.visibility = View.VISIBLE
            btnCreateGroup.visibility = View.VISIBLE
            btnAddStudent.visibility = View.VISIBLE
            btnInviteCodes.visibility = View.VISIBLE
            btnManageStudents.visibility = View.VISIBLE
            loadGroups()
        } else {
            btnCreateAssignment.visibility = View.GONE
            btnCreateGroup.visibility = View.GONE
            btnAddStudent.visibility = View.GONE
            btnInviteCodes.visibility = View.GONE
            btnManageStudents.visibility = View.GONE
        }

        btnCreateAssignment.setOnClickListener {
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, CreateAssignmentFragment())
                .addToBackStack(null)
                .commit()
        }
        btnCreateGroup.setOnClickListener { showCreateGroupDialog() }
        btnAddStudent.setOnClickListener { showAddStudentDialog(userId) }
        btnInviteCodes.setOnClickListener { showInviteCodesDialog(userId) }
        btnChangePassword.setOnClickListener { showChangePasswordDialog(userId) }
        btnManageStudents.setOnClickListener { showManageStudentsDialog(userId) }
    }

    private fun isValidEmail(email: String): Boolean =
        email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun isValidPhone(phone: String): Boolean =
        phone.isEmpty() || Patterns.PHONE.matcher(phone).matches()

    private fun loadGroups() {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getGroups()
                if (resp.isSuccessful) groups = resp.body() ?: emptyList()
            } catch (_: Exception) {}
        }
    }

    private fun showCreateGroupDialog() {
        val input = EditText(requireContext()).apply {
            hint = "Например: ИСП-105"
            inputType = InputType.TYPE_CLASS_TEXT
            setPadding(40, 30, 40, 30)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Создать группу")
            .setView(input)
            .setPositiveButton("Создать") { dialog, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) toast("Введите название группы") else createGroup(name)
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun createGroup(name: String) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.createGroup(CreateGroupRequest(name))
                if (resp.isSuccessful) {
                    toast("✓ Группа \"$name\" создана")
                    loadGroups()
                } else toast(resp.errorBody()?.string() ?: "Ошибка создания группы")
            } catch (e: Exception) {
                toast("Ошибка сети: ${e.message}")
            }
        }
    }

    private fun showAddStudentDialog(teacherId: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_add_student, null)
        val etLogin = dialogView.findViewById<EditText>(R.id.etLogin)
        val etEmail = dialogView.findViewById<EditText>(R.id.etEmail)
        val etPhone = dialogView.findViewById<EditText>(R.id.etPhone)
        val etGroupName = dialogView.findViewById<EditText>(R.id.etGroupName)
        val etPassword = dialogView.findViewById<EditText>(R.id.etPassword)
        val etConfirm = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Добавить студента")
            .setView(dialogView)
            .setPositiveButton("Создать", null) // обработчик ниже, чтобы не закрывать при ошибке
            .setNegativeButton("Отмена", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                val login = etLogin.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val groupName = etGroupName.text.toString().trim()
                val pass = etPassword.text.toString().trim()
                val confirm = etConfirm.text.toString().trim()

                when {
                    login.isEmpty() || pass.isEmpty() -> {
                        etLogin.error = if (login.isEmpty()) "Введите логин" else null
                        toast("Заполните обязательные поля")
                    }
                    !isValidEmail(email) -> {
                        etEmail.error = "Некорректный email (пример: name@mail.ru)"
                    }
                    !isValidPhone(phone) -> {
                        etPhone.error = "Некорректный телефон"
                    }
                    pass.length < 6 -> {
                        etPassword.error = "Минимум 6 символов"
                    }
                    pass != confirm -> {
                        etConfirm.error = "Пароли не совпадают"
                    }
                    else -> {
                        val groupId = groups.firstOrNull { it.name.equals(groupName, true) }?.id
                        createStudent(login, email, phone, pass, teacherId, groupId)
                        dialog.dismiss()
                    }
                }
            }
        }
        dialog.show()
    }

    private fun createStudent(login: String, email: String, phone: String, pass: String, teacherId: Int, groupId: Int?) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.createStudent(
                    CreateStudentRequest(
                        login = login,
                        password = pass,
                        email = email.ifEmpty { null },
                        phone = phone.ifEmpty { null },
                        teacherId = teacherId,
                        groupId = groupId,
                        inviteCode = null
                    )
                )
                if (resp.isSuccessful) toast("✓ Студент $login добавлен")
                else toast(resp.errorBody()?.string() ?: "Ошибка")
            } catch (e: Exception) {
                toast("Ошибка сети: ${e.message}")
            }
        }
    }

    private fun showInviteCodesDialog(teacherId: Int) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getInviteCodes(teacherId)
                val codes = if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                val text = if (codes.isEmpty()) "Кодов пока нет"
                else codes.joinToString("\n") { "${it.code}  —  ${it.groupName ?: "все группы"}" }

                AlertDialog.Builder(requireContext())
                    .setTitle("Коды приглашений")
                    .setMessage(text)
                    .setPositiveButton("Создать новый") { _, _ -> createInviteCode(teacherId) }
                    .setNegativeButton("Закрыть", null)
                    .show()
            } catch (e: Exception) {
                toast("Ошибка: ${e.message}")
            }
        }
    }

    private fun createInviteCode(teacherId: Int) {
        val names = (listOf("Все группы") + groups.map { it.name }).toTypedArray()
        var selected = 0
        AlertDialog.Builder(requireContext())
            .setTitle("Группа для кода")
            .setSingleChoiceItems(names, 0) { _, which -> selected = which }
            .setPositiveButton("Создать") { _, _ ->
                val groupId = if (selected == 0) null else groups[selected - 1].id
                lifecycleScope.launch {
                    try {
                        val resp = RetrofitClient.api.createInviteCode(
                            CreateInviteCodeRequest(teacherId = teacherId, groupId = groupId)
                        )
                        if (resp.isSuccessful) {
                            val code = resp.body()?.code ?: "?"
                            AlertDialog.Builder(requireContext())
                                .setTitle("Код создан")
                                .setMessage("Код приглашения: $code\n\nПередайте его студентам для регистрации.")
                                .setPositiveButton("OK", null)
                                .show()
                        } else toast("Ошибка создания кода")
                    } catch (e: Exception) {
                        toast("Ошибка: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun showChangePasswordDialog(userId: Int) {
        val dialogView = LayoutInflater.from(requireContext())
            .inflate(R.layout.dialog_change_password, null)
        val etOldPassword = dialogView.findViewById<EditText>(R.id.etOldPassword)
        val etNewPassword = dialogView.findViewById<EditText>(R.id.etNewPassword)
        val etConfirmPassword = dialogView.findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(requireContext())
            .setTitle("Изменить пароль")
            .setView(dialogView)
            .setPositiveButton("Изменить") { dialog, _ ->
                val oldP = etOldPassword.text.toString().trim()
                val newP = etNewPassword.text.toString().trim()
                val conf = etConfirmPassword.text.toString().trim()
                when {
                    oldP.isEmpty() || newP.isEmpty() || conf.isEmpty() -> toast("Заполните все поля")
                    newP.length < 6 -> toast("Пароль минимум 6 символов")
                    newP != conf -> toast("Пароли не совпадают")
                    else -> changePassword(userId, oldP, newP)
                }
                dialog.dismiss()
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun changePassword(userId: Int, oldPassword: String, newPassword: String) {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.api.changePassword(
                    ChangePasswordRequest(userId, oldPassword, newPassword)
                )
                if (response.isSuccessful) toast("✓ Пароль изменён!")
                else toast(response.errorBody()?.string() ?: "Ошибка")
            } catch (e: Exception) {
                toast("Ошибка сети: ${e.message}")
            }
        }
    }
    private fun showManageStudentsDialog(teacherId: Int) {
        lifecycleScope.launch {
            try {
                val resp = RetrofitClient.api.getTeacherStudents(teacherId)
                val students = if (resp.isSuccessful) resp.body() ?: emptyList() else emptyList()
                if (students.isEmpty()) { toast("Студентов пока нет"); return@launch }

                val labels = students.map { "${it.login} — ${it.groupName ?: "без группы"}" }.toTypedArray()
                AlertDialog.Builder(requireContext())
                    .setTitle("Студенты (нажмите для удаления)")
                    .setItems(labels) { _, which ->
                        val st = students[which]
                        confirmDeleteStudent(st.userId, st.login)
                    }
                    .setNegativeButton("Закрыть", null)
                    .show()
            } catch (e: Exception) {
                toast("Ошибка: ${e.message}")
            }
        }
    }

    private fun confirmDeleteStudent(studentId: Int, login: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить студента?")
            .setMessage("Студент \"$login\" и все его попытки будут удалены безвозвратно.")
            .setPositiveButton("Удалить") { _, _ ->
                lifecycleScope.launch {
                    try {
                        val resp = RetrofitClient.api.deleteStudent(studentId)
                        if (resp.isSuccessful) toast("✓ Студент удалён")
                        else toast("Ошибка: ${resp.code()}")
                    } catch (e: Exception) {
                        toast("Ошибка: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun toast(msg: String) =
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()

}

