package com.vandatgsts.thuyetnguyen.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import android.content.Intent
import android.net.Uri
import com.vandatgsts.thuyetnguyen.data.model.AppBackupData
import com.vandatgsts.thuyetnguyen.data.model.ImportMode
import com.vandatgsts.thuyetnguyen.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CompanyProfileRepository.getInstance(application)
    private val backupRepo = BackupRepository.getInstance(application)

    private val _profile = MutableStateFlow(repository.getProfile())
    val profile: StateFlow<CompanyProfile> = _profile.asStateFlow()

    private val _isExporting = MutableStateFlow(false)
    val isExporting: StateFlow<Boolean> = _isExporting.asStateFlow()

    private val _isImporting = MutableStateFlow(false)
    val isImporting: StateFlow<Boolean> = _isImporting.asStateFlow()

    fun updateProfile(updated: CompanyProfile) {
        _profile.value = updated
    }

    fun saveProfile() {
        viewModelScope.launch {
            repository.saveProfile(_profile.value)
        }
    }

    fun exportBackup(onReady: (AppBackupData, Intent) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isExporting.value = true
            try {
                val (backupData, backupFile) = backupRepo.createBackupData()
                val shareIntent = backupRepo.getShareIntent(backupFile)
                onReady(backupData, shareIntent)
            } catch (e: Exception) {
                e.printStackTrace()
                onError("Lỗi khi xuất dữ liệu: ${e.localizedMessage}")
            } finally {
                _isExporting.value = false
            }
        }
    }

    fun inspectBackupFile(
        uri: Uri,
        onResult: (backup: AppBackupData?, isDuplicate: Boolean, errorMsg: String?) -> Unit
    ) {
        viewModelScope.launch {
            val result = backupRepo.parseBackupFromUri(uri)
            if (result.isSuccess) {
                val backup = result.getOrNull()
                if (backup != null) {
                    val isDuplicate = backupRepo.isHashAlreadyImported(backup.fileHash)
                    onResult(backup, isDuplicate, null)
                } else {
                    onResult(null, false, "Không thể đọc dữ liệu từ file")
                }
            } else {
                onResult(null, false, result.exceptionOrNull()?.localizedMessage ?: "File không hợp lệ")
            }
        }
    }

    fun applyImport(
        backup: AppBackupData,
        mode: ImportMode,
        onComplete: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            _isImporting.value = true
            try {
                val result = backupRepo.applyBackupData(backup, mode)
                if (result.isSuccess) {
                    _profile.value = repository.getProfile()
                    onComplete(true, result.getOrDefault("Nhập dữ liệu thành công!"))
                } else {
                    onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Lỗi khi nhập dữ liệu")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                onComplete(false, "Lỗi: ${e.localizedMessage}")
            } finally {
                _isImporting.value = false
            }
        }
    }
}
