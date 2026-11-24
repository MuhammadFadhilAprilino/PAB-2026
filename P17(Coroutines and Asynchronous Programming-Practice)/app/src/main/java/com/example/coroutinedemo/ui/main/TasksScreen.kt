package com.example.coroutinedemo.ui.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TasksScreen(
    modifier: Modifier = Modifier,
    viewModel: TasksViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    TasksContent(
        state = state,
        onLoad = { viewModel.loadData() },
        onLoadWithError = { viewModel.loadData(shouldFail = true) },
        onCancel = viewModel::cancelLoading,
        onRetry = { viewModel.loadData() },
        onSort = viewModel::sortTasks,
        onModeChange = viewModel::setConcurrent,
        onTap = viewModel::onTap,
        onBlock = viewModel::blockMainThread,
        modifier = modifier
    )
}

@Composable
fun TasksContent(
    state: TasksUiState,
    onLoad: () -> Unit,
    onLoadWithError: () -> Unit,
    onCancel: () -> Unit,
    onRetry: () -> Unit,
    onSort: () -> Unit,
    onModeChange: (Boolean) -> Unit,
    onTap: () -> Unit,
    onBlock: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var showHint by remember { mutableStateOf(true) }

    // Bagian 4.3: petunjuk hilang otomatis setelah tiga detik
    LaunchedEffect(Unit) {
        delay(3000L)
        showHint = false
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column {
                Text(
                    "Coroutine Demo",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "Pemuat tugas dengan async, withContext, dan Job",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (showHint) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        "Tips: ganti mode pemuatan, lalu tekan Load untuk membandingkan waktunya.",
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Pilihan mode pemuatan
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            "Mode pemuatan",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (state.isConcurrent) {
                                "Konkuren: 3 async berjalan bersamaan (sekitar 2 detik)"
                            } else {
                                "Sekuensial: satu per satu (sekitar 4,5 detik)"
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = state.isConcurrent,
                        onCheckedChange = onModeChange,
                        enabled = !state.isLoading
                    )
                }
            }
        }

        // Tombol aksi
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onLoad,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load")
                }
                OutlinedButton(
                    onClick = onLoadWithError,
                    enabled = !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Load (fail)")
                }
            }
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    enabled = state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                OutlinedButton(
                    onClick = onSort,
                    enabled = state.tasks.isNotEmpty() && !state.isLoading,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Sort by title")
                }
            }
        }

        // Kartu status: loading, error + Retry, atau hasil
        item {
            val error = state.errorMessage
            val loadTime = state.loadTimeMs
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    when {
                        state.isLoading -> {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    strokeWidth = 2.dp
                                )
                                Text("Memuat data...")
                            }
                        }
                        error != null -> {
                            // Latihan 2: Retry hanya muncul kalau errorMessage tidak null
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    error,
                                    color = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedButton(onClick = onRetry) {
                                    Text("Retry")
                                }
                            }
                        }
                        loadTime != null -> {
                            val mode =
                                if (state.lastLoadWasConcurrent) "konkuren" else "sekuensial"
                            Text(
                                "Selesai dimuat dalam $loadTime ms ($mode)",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Pengumuman: ${state.announcement}")
                            Text("Total mata kuliah: ${state.courseCount}")
                        }
                        else -> {
                            Text("Tekan Load untuk memuat daftar tugas.")
                        }
                    }
                }
            }
        }

        // Uji responsivitas layar
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "Uji responsivitas",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        "Ketuk tombol saat data dimuat, penghitung tetap bertambah. " +
                                "Block UI membuat layar macet selama 3 detik.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onTap, modifier = Modifier.weight(1f)) {
                            Text("Tap me: ${state.tapCount}")
                        }
                        OutlinedButton(
                            onClick = onBlock,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Block UI")
                        }
                    }
                }
            }
        }

        item {
            Text(
                "Daftar tugas (${state.tasks.size})",
                style = MaterialTheme.typography.titleMedium
            )
        }

        items(state.tasks, key = { it.id }) { task ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        task.title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        task.course,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // Bagian 4.3: rememberCoroutineScope untuk scroll ke atas
        if (state.tasks.size > 3) {
            item {
                OutlinedButton(
                    onClick = { scope.launch { listState.animateScrollToItem(0) } },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Back to top")
                }
            }
        }
    }
}