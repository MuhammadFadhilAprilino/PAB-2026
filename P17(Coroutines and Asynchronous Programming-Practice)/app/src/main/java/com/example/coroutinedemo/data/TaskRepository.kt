package com.example.coroutinedemo.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.IOException

class TaskRepository {

    suspend fun getTasks(shouldFail: Boolean = false): List<Task> =
        withContext(Dispatchers.IO) {
            delay(2000L)
            if (shouldFail) {
                throw IOException("Server tidak dapat dijangkau")
            }
            listOf(
                Task(1, "Menulis laporan praktikum", "Pengembangan Aplikasi Bergerak"),
                Task(2, "Membaca bab 5", "Sains Data"),
                Task(3, "Menyiapkan kuis", "Metodologi Penelitian"),
                Task(4, "Mengumpulkan tugas", "Pembelajaran Mesin"),
                Task(5, "Mempelajari slide", "Grafika dan Multimedia"),
                Task(6, "Memperbaiki laporan bug", "Pengembangan Sistem Informasi"),
                Task(7, "Belajar untuk UTS", "Sains Data"),
                Task(8, "Menyusun proposal proyek", "Metodologi Penelitian")
            )
        }

    suspend fun getAnnouncement(): String =
        withContext(Dispatchers.IO) {
            delay(1000L)
            "Kuis 2 diadakan hari Senin depan"
        }

    // Latihan 1
    suspend fun getCourseCount(): Int =
        withContext(Dispatchers.IO) {
            delay(1500L)
            6
        }

    // Latihan 6: pekerjaan CPU, jadi pakai Dispatchers.Default
    suspend fun sortTasks(tasks: List<Task>): List<Task> =
        withContext(Dispatchers.Default) {
            tasks.sortedBy { it.title.lowercase() }
        }
}