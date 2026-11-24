package com.example.coroutinedemo.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coroutinedemo.data.Task
import com.example.coroutinedemo.data.TaskRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.IOException
import kotlin.system.measureTimeMillis

private data class LoadResult(
    val tasks: List<Task>,
    val announcement: String,
    val courseCount: Int
)

class TasksViewModel(
    private val repository: TaskRepository = TaskRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(TasksUiState())
    val uiState: StateFlow<TasksUiState> = _uiState.asStateFlow()

    private var loadJob: Job? = null

    // Mode concurrent: tiga async berjalan bersamaan (Bagian 3.7 dan 5.2)
    private suspend fun loadConcurrent(shouldFail: Boolean): LoadResult =
        coroutineScope {
            val tasks = async { repository.getTasks(shouldFail) }
            val announcement = async { repository.getAnnouncement() }
            val courseCount = async { repository.getCourseCount() }
            LoadResult(tasks.await(), announcement.await(), courseCount.await())
        }

    // Mode sequential: dipanggil satu per satu (Bagian 3.7)
    private suspend fun loadSequential(shouldFail: Boolean): LoadResult {
        val tasks = repository.getTasks(shouldFail)
        val announcement = repository.getAnnouncement()
        val courseCount = repository.getCourseCount()
        return LoadResult(tasks, announcement, courseCount)
    }

    fun setConcurrent(value: Boolean) {
        _uiState.update { it.copy(isConcurrent = value) }
    }

    fun loadData(shouldFail: Boolean = false) {
        loadJob?.cancel()
        loadJob = viewModelScope.launch {
            val concurrent = _uiState.value.isConcurrent
            _uiState.update {
                it.copy(isLoading = true, errorMessage = null, loadTimeMs = null)
            }
            try {
                var result: LoadResult? = null
                val elapsed = measureTimeMillis {
                    result = if (concurrent) {
                        loadConcurrent(shouldFail)
                    } else {
                        loadSequential(shouldFail)
                    }
                }
                val loaded = checkNotNull(result)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        tasks = loaded.tasks,
                        announcement = loaded.announcement,
                        courseCount = loaded.courseCount,
                        loadTimeMs = elapsed,
                        lastLoadWasConcurrent = concurrent
                    )
                }
            } catch (e: IOException) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Gagal memuat: ${e.message}"
                    )
                }
            }
        }
    }

    fun cancelLoading() {
        loadJob?.cancel()
        _uiState.update {
            it.copy(isLoading = false, errorMessage = "Pemuatan dibatalkan")
        }
    }

    // Latihan 6
    fun sortTasks() {
        viewModelScope.launch {
            val sorted = repository.sortTasks(_uiState.value.tasks)
            _uiState.update { it.copy(tasks = sorted) }
        }
    }

    fun onTap() {
        _uiState.update { it.copy(tapCount = it.tapCount + 1) }
    }

    fun blockMainThread() {
        Thread.sleep(3000L)
    }
}