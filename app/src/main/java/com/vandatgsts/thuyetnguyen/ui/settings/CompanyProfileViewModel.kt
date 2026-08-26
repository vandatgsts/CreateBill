package com.vandatgsts.thuyetnguyen.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.CompanyProfile
import com.vandatgsts.thuyetnguyen.data.repository.CompanyProfileRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CompanyProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CompanyProfileRepository.getInstance(application)

    private val _profile = MutableStateFlow(repository.getProfile())
    val profile: StateFlow<CompanyProfile> = _profile.asStateFlow()

    fun updateProfile(updated: CompanyProfile) {
        _profile.value = updated
    }

    fun saveProfile() {
        viewModelScope.launch {
            repository.saveProfile(_profile.value)
        }
    }
}
