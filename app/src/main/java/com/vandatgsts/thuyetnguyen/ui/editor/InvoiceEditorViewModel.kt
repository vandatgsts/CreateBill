package com.vandatgsts.thuyetnguyen.ui.editor

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vandatgsts.thuyetnguyen.data.model.CustomerInfo
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceItem
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.data.model.ProductTemplate
import com.vandatgsts.thuyetnguyen.data.model.StorePartner
import com.vandatgsts.thuyetnguyen.data.repository.CompanyProfileRepository
import com.vandatgsts.thuyetnguyen.data.repository.InvoiceRepository
import com.vandatgsts.thuyetnguyen.data.repository.ProductRepository
import com.vandatgsts.thuyetnguyen.data.repository.StorePartnerRepository
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class InvoiceEditorViewModel(application: Application) : AndroidViewModel(application) {

    private val invoiceRepo = InvoiceRepository.getInstance(application)
    private val profileRepo = CompanyProfileRepository.getInstance(application)
    private val productRepo = ProductRepository.getInstance(application)
    private val storePartnerRepo = StorePartnerRepository.getInstance(application)

    val availableProducts: StateFlow<List<ProductTemplate>> = productRepo.products
    val availableStores: StateFlow<List<StorePartner>> = storePartnerRepo.stores

    private val _invoiceState = MutableStateFlow(InvoiceDocument())
    val invoiceState: StateFlow<InvoiceDocument> = _invoiceState.asStateFlow()

    fun initInvoice(invoiceId: String?, initialType: InvoiceType = InvoiceType.QUOTATION_A4) {
        if (invoiceId != null) {
            val existing = invoiceRepo.getInvoiceById(invoiceId)
            if (existing != null) {
                _invoiceState.value = existing
                return
            }
        }

        // Tạo mới với cấu hình profile mặc định
        val profile = profileRepo.getProfile()
        val defaultTitle = when (initialType) {
            InvoiceType.DELIVERY_DEBT -> profile.storeName
            InvoiceType.QUOTATION_A4 -> "BẢNG BÁO GIÁ"
        }

        _invoiceState.value = InvoiceDocument(
            id = UUID.randomUUID().toString(),
            title = defaultTitle,
            type = initialType,
            storeOrCompanyName = if (initialType == InvoiceType.DELIVERY_DEBT) profile.storeName else profile.companyName,
            companyAddress = profile.address,
            companyTaxCode = profile.taxCode,
            companyPhone = profile.phone,
            customer = CustomerInfo(),
            items = listOf(
                InvoiceItem(
                    id = UUID.randomUUID().toString(),
                    stt = 1,
                    date = FormatHelper.formatDate(System.currentTimeMillis()),
                    productName = "",
                    unit = if (initialType == InvoiceType.QUOTATION_A4) "Bồn" else "",
                    quantity = 1.0,
                    unitPrice = 0.0
                )
            ),
            oldDebt = 0.0,
            notes = if (initialType == InvoiceType.QUOTATION_A4) profile.defaultNotes else "",
            paymentTerms = if (initialType == InvoiceType.QUOTATION_A4) profile.defaultPaymentTerms else "",
            bankAccountNumber = profile.bankAccountNumber,
            bankName = profile.bankName,
            bankAccountHolder = profile.bankAccountHolder
        )
    }

    fun initInvoiceForStorePartner(store: StorePartner, rollingDebt: Double = 0.0) {
        val profile = profileRepo.getProfile()
        val defaultTitle = when (store.defaultType) {
            InvoiceType.DELIVERY_DEBT -> store.name
            InvoiceType.QUOTATION_A4 -> "BẢNG BÁO GIÁ"
        }

        _invoiceState.value = InvoiceDocument(
            id = UUID.randomUUID().toString(),
            title = defaultTitle,
            type = store.defaultType,
            storeOrCompanyName = store.name,
            companyAddress = if (store.defaultType == InvoiceType.QUOTATION_A4 && store.address.isNotBlank()) store.address else profile.address,
            companyTaxCode = profile.taxCode,
            companyPhone = if (store.phone.isNotBlank()) store.phone else profile.phone,
            customer = CustomerInfo(),
            items = listOf(
                InvoiceItem(
                    id = UUID.randomUUID().toString(),
                    stt = 1,
                    date = FormatHelper.formatDate(System.currentTimeMillis()),
                    productName = "",
                    unit = if (store.defaultType == InvoiceType.QUOTATION_A4) "Bồn" else "",
                    quantity = 1.0,
                    unitPrice = 0.0
                )
            ),
            oldDebt = rollingDebt,
            notes = if (store.defaultType == InvoiceType.QUOTATION_A4) profile.defaultNotes else "",
            paymentTerms = if (store.defaultType == InvoiceType.QUOTATION_A4) profile.defaultPaymentTerms else "",
            bankAccountNumber = profile.bankAccountNumber,
            bankName = profile.bankName,
            bankAccountHolder = profile.bankAccountHolder
        )
    }

    fun applyStorePartner(store: StorePartner) {
        val current = _invoiceState.value
        val latestDebt = invoiceRepo.invoices.value
            .filter { it.storeOrCompanyName.contains(store.name, ignoreCase = true) || it.title.contains(store.name, ignoreCase = true) }
            .maxByOrNull { it.updatedAt }?.totalAmount ?: current.oldDebt

        _invoiceState.value = current.copy(
            storeOrCompanyName = store.name,
            title = if (current.type == InvoiceType.DELIVERY_DEBT) store.name else current.title,
            companyAddress = if (store.address.isNotBlank()) store.address else current.companyAddress,
            companyPhone = if (store.phone.isNotBlank()) store.phone else current.companyPhone,
            oldDebt = if (current.oldDebt == 0.0 && latestDebt > 0.0) latestDebt else current.oldDebt
        )
    }

    fun setInvoiceType(newType: InvoiceType) {
        val current = _invoiceState.value
        if (current.type == newType) return

        val profile = profileRepo.getProfile()
        val defaultTitle = when (newType) {
            InvoiceType.DELIVERY_DEBT -> profile.storeName
            InvoiceType.QUOTATION_A4 -> "BẢNG BÁO GIÁ"
        }

        _invoiceState.value = current.copy(
            type = newType,
            title = defaultTitle,
            storeOrCompanyName = if (newType == InvoiceType.DELIVERY_DEBT) profile.storeName else profile.companyName,
            notes = if (newType == InvoiceType.QUOTATION_A4 && current.notes.isBlank()) profile.defaultNotes else current.notes,
            paymentTerms = if (newType == InvoiceType.QUOTATION_A4 && current.paymentTerms.isBlank()) profile.defaultPaymentTerms else current.paymentTerms
        )
    }

    fun updateInvoice(modifier: (InvoiceDocument) -> InvoiceDocument) {
        _invoiceState.value = modifier(_invoiceState.value)
    }

    fun addItem() {
        val current = _invoiceState.value
        val nextStt = current.items.size + 1
        val newItem = InvoiceItem(
            id = UUID.randomUUID().toString(),
            stt = nextStt,
            date = FormatHelper.formatDate(System.currentTimeMillis()),
            productName = "",
            unit = if (current.type == InvoiceType.QUOTATION_A4) "Bồn" else "",
            quantity = 1.0,
            unitPrice = 0.0
        )
        _invoiceState.value = current.copy(items = current.items + newItem)
    }

    fun removeItem(index: Int) {
        val current = _invoiceState.value
        if (index in current.items.indices) {
            val list = current.items.toMutableList()
            list.removeAt(index)
            val reIndexed = list.mapIndexed { i, item -> item.copy(stt = i + 1) }
            _invoiceState.value = current.copy(items = reIndexed)
        }
    }

    fun updateItem(index: Int, updatedItem: InvoiceItem) {
        val current = _invoiceState.value
        if (index in current.items.indices) {
            val list = current.items.toMutableList()
            list[index] = updatedItem
            _invoiceState.value = current.copy(items = list)
        }
    }

    fun duplicateItem(index: Int) {
        val current = _invoiceState.value
        if (index in current.items.indices) {
            val itemToDuplicate = current.items[index]
            val list = current.items.toMutableList()
            list.add(index + 1, itemToDuplicate.copy(id = UUID.randomUUID().toString()))
            val reIndexed = list.mapIndexed { i, item -> item.copy(stt = i + 1) }
            _invoiceState.value = current.copy(items = reIndexed)
        }
    }

    fun applyProductTemplate(itemIndex: Int, product: ProductTemplate) {
        val current = _invoiceState.value
        val list = current.items.toMutableList()
        if (itemIndex in list.indices) {
            val existing = list[itemIndex]
            val updated = existing.copy(
                productName = product.name,
                unit = if (product.unit.isNotBlank()) product.unit else existing.unit,
                unitPrice = if (product.defaultPrice > 0) product.defaultPrice else existing.unitPrice
            )
            list[itemIndex] = updated
            _invoiceState.value = current.copy(items = list)
        }
    }

    fun addDebtPayment(title: String, amount: Double, date: String = "") {
        val current = _invoiceState.value
        val newPayment = com.vandatgsts.thuyetnguyen.data.model.DebtPayment(
            title = title.ifBlank { "CK lần ${current.debtPayments.size + 1}" },
            amount = amount,
            date = date
        )
        _invoiceState.value = current.copy(debtPayments = current.debtPayments + newPayment)
    }

    fun removeDebtPayment(paymentId: String) {
        val current = _invoiceState.value
        _invoiceState.value = current.copy(debtPayments = current.debtPayments.filterNot { it.id == paymentId })
    }

    fun togglePaidStatus(isPaid: Boolean) {
        val current = _invoiceState.value
        _invoiceState.value = current.copy(
            isPaid = isPaid,
            paidDate = if (isPaid) com.vandatgsts.thuyetnguyen.generator.FormatHelper.formatDate(System.currentTimeMillis()) else ""
        )
    }

    fun setPaidDate(date: String) {
        val current = _invoiceState.value
        _invoiceState.value = current.copy(paidDate = date)
    }




    fun saveInvoice(onSuccess: (InvoiceDocument) -> Unit) {
        viewModelScope.launch {
            val invoice = _invoiceState.value
            invoiceRepo.saveInvoice(invoice)

            // Tự động lưu/cập nhật Cửa Hàng / Đại Lý vào danh mục
            if (invoice.storeOrCompanyName.isNotBlank()) {
                storePartnerRepo.saveStore(
                    StorePartner(
                        name = invoice.storeOrCompanyName,
                        phone = invoice.companyPhone,
                        address = invoice.companyAddress,
                        defaultType = invoice.type
                    )
                )
            }

            onSuccess(invoice)
        }
    }
}
