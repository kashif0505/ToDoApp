package com.example.todoapp

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import java.util.*

data class Task(val title: String, val description: String, val date: String)

class MainActivity : AppCompatActivity() {

    private val tasks = mutableListOf<Task>()
    private lateinit var adapter: ArrayAdapter<String>
    private lateinit var username: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnAdd = findViewById<Button>(R.id.btnAdd)
        val lvTasks = findViewById<ListView>(R.id.lvTasks)
        val btnLogout = findViewById<Button>(R.id.btnLogout)  // Logout Button

        username = intent.getStringExtra("username") ?: "defaultUser"

        loadTasks()

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, tasks.map { it.title })
        lvTasks.adapter = adapter

        // Add Task
        btnAdd.setOnClickListener {
            showAddTaskDialog()
        }

        // View Task Details on Item Click
        lvTasks.setOnItemClickListener { _, _, position, _ ->
            showTaskDetails(position)
        }

        // Long Click for Edit/Delete Menu
        lvTasks.setOnItemLongClickListener { _, view, position, _ ->
            showPopupMenu(view, position)
            true
        }

        // Logout button click
        btnLogout.setOnClickListener {
            showLogoutConfirmationDialog()
        }
    }

    // Show Dialog to Add a New Task
    private fun showAddTaskDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val btnDatePicker = dialogView.findViewById<Button>(R.id.btnDatePicker)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        btnDatePicker.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(this, { _, year, month, day ->
                val selectedDate = "$day/${month + 1}/$year"
                tvDate.text = selectedDate
            }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
        }

        AlertDialog.Builder(this)
            .setTitle("Add New Task")
            .setView(dialogView)
            .setPositiveButton("Add") { _, _ ->
                val title = etTitle.text.toString()
                val description = etDescription.text.toString()
                val date = tvDate.text.toString()

                if (title.isNotEmpty()) {
                    tasks.add(Task(title, description, date))
                    saveTasks()
                    adapter.clear()
                    adapter.addAll(tasks.map { it.title })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Show Task Details
    private fun showTaskDetails(position: Int) {
        val task = tasks[position]

        AlertDialog.Builder(this)
            .setTitle(task.title)
            .setMessage("Description: ${task.description}\nDue Date: ${task.date}")
            .setPositiveButton("Edit") { _, _ -> showEditDialog(position) }
            .setNegativeButton("Delete") { _, _ ->
                tasks.removeAt(position)
                saveTasks()
                adapter.clear()
                adapter.addAll(tasks.map { it.title })
            }
            .setNeutralButton("Close", null)
            .show()
    }

    // Show Popup Menu for Edit/Delete
    private fun showPopupMenu(view: android.view.View, position: Int) {
        val popupMenu = PopupMenu(this, view)
        popupMenu.menuInflater.inflate(R.menu.task_menu, popupMenu.menu)

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.menu_edit -> {
                    showEditDialog(position)
                    true
                }
                R.id.menu_delete -> {
                    tasks.removeAt(position)
                    saveTasks()
                    adapter.clear()
                    adapter.addAll(tasks.map { it.title })
                    true
                }
                else -> false
            }
        }
        popupMenu.show()
    }

    // Show Dialog to Edit Task
    private fun showEditDialog(position: Int) {
        val task = tasks[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_task, null)
        val etTitle = dialogView.findViewById<EditText>(R.id.etTitle)
        val etDescription = dialogView.findViewById<EditText>(R.id.etDescription)
        val tvDate = dialogView.findViewById<TextView>(R.id.tvDate)

        etTitle.setText(task.title)
        etDescription.setText(task.description)
        tvDate.text = task.date

        AlertDialog.Builder(this)
            .setTitle("Edit Task")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val updatedTitle = etTitle.text.toString()
                val updatedDescription = etDescription.text.toString()
                val updatedDate = tvDate.text.toString()

                if (updatedTitle.isNotEmpty()) {
                    tasks[position] = Task(updatedTitle, updatedDescription, updatedDate)
                    saveTasks()
                    adapter.clear()
                    adapter.addAll(tasks.map { it.title })
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Show Confirmation Dialog for Logout
    private fun showLogoutConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Yes") { _, _ ->
                // Navigate to Login Activity
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish()  // Close current activity
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Load Tasks from SharedPreferences
    private fun loadTasks() {
        val sharedPrefs = getSharedPreferences("UserTasks", Context.MODE_PRIVATE)
        val tasksString = sharedPrefs.getString("tasks_$username", "")
        if (!tasksString.isNullOrEmpty()) {
            tasks.addAll(tasksString.split(";").map {
                val parts = it.split("|")
                Task(parts[0], parts[1], parts[2])
            })
        }
    }

    // Save Tasks to SharedPreferences
    private fun saveTasks() {
        val sharedPrefs = getSharedPreferences("UserTasks", Context.MODE_PRIVATE)
        with(sharedPrefs.edit()) {
            val tasksString = tasks.joinToString(";") { "${it.title}|${it.description}|${it.date}" }
            putString("tasks_$username", tasksString)
            apply()
        }
    }
}
