package com.example

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FloatingPanelState {
    private val _isServiceRunning = MutableStateFlow(false)
    val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

    private val _isExpanded = MutableStateFlow(false)
    val isExpanded: StateFlow<Boolean> = _isExpanded.asStateFlow()

    private val _feature1Enabled = MutableStateFlow(false)
    val feature1Enabled: StateFlow<Boolean> = _feature1Enabled.asStateFlow()

    private val _feature2Enabled = MutableStateFlow(false)
    val feature2Enabled: StateFlow<Boolean> = _feature2Enabled.asStateFlow()

    private val _feature3Enabled = MutableStateFlow(false)
    val feature3Enabled: StateFlow<Boolean> = _feature3Enabled.asStateFlow()

    private val _sensitivity = MutableStateFlow(50f) // 0 to 100
    val sensitivity: StateFlow<Float> = _sensitivity.asStateFlow()

    private val _logs = MutableStateFlow<List<String>>(listOf("System Initialized.", "Ready to start overlay service..."))
    val logs: StateFlow<List<String>> = _logs.asStateFlow()

    fun setServiceRunning(running: Boolean) {
        _isServiceRunning.value = running
        addLog(if (running) "Floating Service STARTED." else "Floating Service STOPPED.")
    }

    fun setExpanded(expanded: Boolean) {
        _isExpanded.value = expanded
        addLog(if (expanded) "Panel Expanded." else "Panel Minimized.")
    }

    fun setFeature1(enabled: Boolean) {
        _feature1Enabled.value = enabled
        addLog("Feature 1 (Auto-Target): ${if (enabled) "ON" else "OFF"}")
    }

    fun setFeature2(enabled: Boolean) {
        _feature2Enabled.value = enabled
        addLog("Feature 2 (Macro Clicker): ${if (enabled) "ON" else "OFF"}")
    }

    fun setFeature3(enabled: Boolean) {
        _feature3Enabled.value = enabled
        addLog("Feature 3 (Radar Sync): ${if (enabled) "ON" else "OFF"}")
    }

    fun setSensitivity(value: Float) {
        _sensitivity.value = value
    }

    fun addLog(message: String) {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val formatted = "[$time] $message"
        val currentList = _logs.value.toMutableList()
        currentList.add(0, formatted) // newest on top
        if (currentList.size > 50) {
            currentList.removeAt(currentList.lastIndex)
        }
        _logs.value = currentList
    }

    fun clearLogs() {
        _logs.value = listOf("[${SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())}] Log terminal cleared.")
    }
}
