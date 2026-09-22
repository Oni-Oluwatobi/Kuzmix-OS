package com.kreadivegalaxy.kuzmixos.diagnostics

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiagnosticLogsViewerScreen(
    onClose: () -> Unit,
    viewModel: DiagnosticLogsViewModel = viewModel()
) {
    val logs by viewModel.logs.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedLog by remember { mutableStateOf<AppCrashLog?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadLogs()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F1015))
            .systemBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text("Diagnostic Logs", color = Color.White, fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedLog != null) selectedLog = null else onClose()
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearAllLogs() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear all", tint = Color(0xFFFF453A))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF1A1B23))
            )

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFFFF7A00))
                }
            } else if (selectedLog != null) {
                CrashLogDetail(log = selectedLog!!)
            } else if (logs.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFF4CAF50), modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No crash logs recorded", color = Color.White.copy(alpha = 0.7f), fontSize = 16.sp)
                        Text("System is stable", color = Color(0xFF4CAF50), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(logs) { log ->
                        CrashLogCard(log = log, onClick = { selectedLog = log })
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CrashLogCard(log: AppCrashLog, onClick: () -> Unit) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF252630)),
        onClick = onClick
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    log.exceptionClass.substringAfterLast('.'),
                    color = Color(0xFFFF453A),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (!log.isHandled) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFFF453A).copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text("Unhandled", color = Color(0xFFFF453A), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Text(log.message, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 2)
            Text(dateStr, color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
            Text("${log.deviceModel} | Android ${log.androidVersion}", color = Color.White.copy(alpha = 0.4f), fontSize = 11.sp)
        }
    }
}

@Composable
private fun CrashLogDetail(log: AppCrashLog) {
    val dateFormat = remember { SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.getDefault()) }
    val dateStr = remember(log.timestamp) { dateFormat.format(Date(log.timestamp)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF252630)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Exception", color = Color(0xFFFF453A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(log.exceptionClass, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)

                Text("Message", color = Color(0xFFFF7A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(log.message, color = Color.White, fontSize = 13.sp)

                Text("Device", color = Color(0xFFFF7A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("${log.deviceModel} | Android ${log.androidVersion} | App ${log.appVersion}", color = Color.White, fontSize = 13.sp)

                Text("Timestamp", color = Color(0xFFFF7A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(dateStr, color = Color.White, fontSize = 13.sp)
            }
        }

        Card(
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1C1E)),
            shape = RoundedCornerShape(14.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Stack Trace", color = Color(0xFFFF7A00), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    log.stackTrace,
                    color = Color.White.copy(alpha = 0.7f),
                    fontSize = 11.sp,
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                    lineHeight = 16.sp
                )
            }
        }
    }
}
