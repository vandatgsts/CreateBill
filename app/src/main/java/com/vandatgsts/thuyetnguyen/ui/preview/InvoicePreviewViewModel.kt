package com.vandatgsts.thuyetnguyen.ui.preview

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.repository.InvoiceRepository
import com.vandatgsts.thuyetnguyen.generator.ImageInvoiceRenderer
import com.vandatgsts.thuyetnguyen.generator.InvoiceExportManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class InvoicePreviewViewModel(application: Application) : AndroidViewModel(application) {
    private val invoiceRepo = InvoiceRepository.getInstance(application)

    private val _invoice = MutableStateFlow<InvoiceDocument?>(null)
    val invoice: StateFlow<InvoiceDocument?> = _invoice.asStateFlow()

    private val _previewBitmap = MutableStateFlow<Bitmap?>(null)
    val previewBitmap: StateFlow<Bitmap?> = _previewBitmap.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadInvoice(invoiceId: String) {
        val doc = invoiceRepo.getInvoiceById(invoiceId)
        _invoice.value = doc
        if (doc != null) {
            viewModelScope.launch {
                _isLoading.value = true
                val bitmap = ImageInvoiceRenderer.renderBitmap(doc)
                _previewBitmap.value = bitmap
                _isLoading.value = false
            }
        } else {
            _isLoading.value = false
        }
    }

    fun sharePdf(onComplete: () -> Unit = {}) {
        val doc = _invoice.value ?: return
        viewModelScope.launch {
            InvoiceExportManager.sharePdf(getApplication(), doc)
            onComplete()
        }
    }

    fun shareImage(onComplete: () -> Unit = {}) {
        val doc = _invoice.value ?: return
        viewModelScope.launch {
            InvoiceExportManager.shareImage(getApplication(), doc)
            onComplete()
        }
    }

    fun viewPdf(onComplete: () -> Unit = {}) {
        val doc = _invoice.value ?: return
        viewModelScope.launch {
            InvoiceExportManager.viewPdf(getApplication(), doc)
            onComplete()
        }
    }

    fun saveImage(onResult: (Boolean) -> Unit) {
        val doc = _invoice.value ?: return
        viewModelScope.launch {
            val success = InvoiceExportManager.saveImageToGallery(getApplication(), doc)
            onResult(success)
        }
    }
}
