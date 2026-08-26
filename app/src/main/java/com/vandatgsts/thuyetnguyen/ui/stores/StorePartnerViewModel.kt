package com.vandatgsts.thuyetnguyen.ui.stores

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.StorePartner
import com.vandatgsts.thuyetnguyen.data.model.StorePartnerSummary
import com.vandatgsts.thuyetnguyen.data.repository.InvoiceRepository
import com.vandatgsts.thuyetnguyen.data.repository.StorePartnerRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StorePartnerViewModel(application: Application) : AndroidViewModel(application) {
    private val storeRepo = StorePartnerRepository.getInstance(application)
    private val invoiceRepo = InvoiceRepository.getInstance(application)

    val stores: StateFlow<List<StorePartner>> = storeRepo.stores

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val storeSummaries: StateFlow<List<StorePartnerSummary>> = combine(
        storeRepo.stores,
        invoiceRepo.invoices
    ) { storeList, invoiceList ->
        storeList.map { store ->
            val matchingInvoices = invoiceList.filter { doc ->
                val matchesStoreName = doc.storeOrCompanyName.isNotBlank() &&
                        doc.storeOrCompanyName.contains(store.name, ignoreCase = true)
                val matchesTitle = doc.title.isNotBlank() &&
                        doc.title.contains(store.name, ignoreCase = true)
                matchesStoreName || matchesTitle
            }.sortedByDescending { it.updatedAt }

            val totalAmount = matchingInvoices.sumOf { it.totalAmount }
            val totalPaid = matchingInvoices.sumOf { it.totalPaid }
            val latestInvoice = matchingInvoices.firstOrNull()
            
            // Dư nợ hiện tại của Cửa Hàng: Lấy theo dư nợ thực tế cần thu (remainingDebt) của kỳ gần nhất
            val currentDebt = latestInvoice?.remainingDebt ?: 0.0

            StorePartnerSummary(
                store = store,
                invoiceCount = matchingInvoices.size,
                totalAmount = totalAmount,
                totalPaid = totalPaid,
                currentDebt = currentDebt,
                latestInvoice = latestInvoice,
                invoices = matchingInvoices
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val filteredSummaries: StateFlow<List<StorePartnerSummary>> = combine(
        storeSummaries,
        _searchQuery
    ) { list, query ->
        if (query.isBlank()) {
            list
        } else {
            list.filter { summary ->
                summary.store.name.contains(query, ignoreCase = true) ||
                        summary.store.phone.contains(query, ignoreCase = true) ||
                        summary.store.address.contains(query, ignoreCase = true) ||
                        summary.store.contactPerson.contains(query, ignoreCase = true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun saveStore(store: StorePartner) {
        viewModelScope.launch {
            storeRepo.saveStore(store)
        }
    }

    fun deleteStore(id: String) {
        viewModelScope.launch {
            storeRepo.deleteStore(id)
        }
    }

    fun getStoreSummary(storeId: String): StorePartnerSummary? {
        return storeSummaries.value.find { it.store.id == storeId }
    }

    fun getRollingDebtForStore(storeName: String): Double {
        val summary = storeSummaries.value.find { it.store.name.equals(storeName.trim(), ignoreCase = true) }
        return summary?.currentDebt ?: 0.0
    }

    fun recordPaymentForStore(storeId: String, amount: Double, title: String = "") {
        viewModelScope.launch {
            val summary = storeSummaries.value.find { it.store.id == storeId } ?: return@launch
            val latest = summary.latestInvoice ?: return@launch
            val newPayment = com.vandatgsts.thuyetnguyen.data.model.DebtPayment(
                title = title.ifBlank { "Ck lần ${latest.debtPayments.size + 1}" },
                amount = amount,
                date = com.vandatgsts.thuyetnguyen.generator.FormatHelper.formatDate(System.currentTimeMillis())
            )
            val updatedPayments = latest.debtPayments + newPayment
            val isNowFullyPaid = (latest.copy(debtPayments = updatedPayments).effectiveOldDebt <= 0 && latest.items.all { it.paidAmount >= it.quantity * it.unitPrice }) || (amount >= latest.remainingDebt)
            val updated = latest.copy(
                debtPayments = updatedPayments,
                isPaid = isNowFullyPaid || latest.isPaid,
                paidDate = if (isNowFullyPaid) com.vandatgsts.thuyetnguyen.generator.FormatHelper.formatDate(System.currentTimeMillis()) else latest.paidDate
            )
            invoiceRepo.saveInvoice(updated)
        }
    }
}
