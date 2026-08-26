package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.vandatgsts.thuyetnguyen.data.model.CustomerInfo
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceItem
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

class InvoiceRepository(private val context: Context) {
    private val gson = Gson()
    private val storageFile: File by lazy {
        File(context.filesDir, "invoices_store_v5.json")
    }



    private val _invoices = MutableStateFlow<List<InvoiceDocument>>(emptyList())
    val invoices: StateFlow<List<InvoiceDocument>> = _invoices.asStateFlow()

    init {
        loadInvoices()
    }

    private fun loadInvoices() {
        if (!storageFile.exists()) {
            val samples = createSampleInvoices()
            _invoices.value = samples
            saveToFile(samples)
            return
        }

        try {
            val json = storageFile.readText()
            val type = object : TypeToken<List<InvoiceDocument>>() {}.type
            val list: List<InvoiceDocument> = gson.fromJson(json, type) ?: emptyList()
            if (list.isEmpty()) {
                val samples = createSampleInvoices()
                _invoices.value = samples
                saveToFile(samples)
            } else {
                _invoices.value = list.sortedByDescending { it.updatedAt }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            _invoices.value = createSampleInvoices()
        }
    }

    private fun saveToFile(list: List<InvoiceDocument>) {
        try {
            val json = gson.toJson(list)
            storageFile.writeText(json)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    suspend fun saveInvoice(invoice: InvoiceDocument) = withContext(Dispatchers.IO) {
        val current = _invoices.value.toMutableList()
        val index = current.indexOfFirst { it.id == invoice.id }
        val updatedInvoice = invoice.copy(updatedAt = System.currentTimeMillis())
        if (index >= 0) {
            current[index] = updatedInvoice
        } else {
            current.add(0, updatedInvoice)
        }
        val sorted = current.sortedByDescending { it.updatedAt }
        _invoices.value = sorted
        saveToFile(sorted)
    }

    suspend fun deleteInvoice(id: String) = withContext(Dispatchers.IO) {
        val current = _invoices.value.filterNot { it.id == id }
        _invoices.value = current
        saveToFile(current)
    }

    fun getInvoiceById(id: String): InvoiceDocument? {
        return _invoices.value.find { it.id == id }
    }

    suspend fun duplicateInvoice(id: String): InvoiceDocument? = withContext(Dispatchers.IO) {
        val existing = getInvoiceById(id) ?: return@withContext null
        val duplicated = existing.copy(
            id = UUID.randomUUID().toString(),
            title = "${existing.title} (Bản sao)",
            createdAt = System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            items = existing.items.map { it.copy(id = UUID.randomUUID().toString()) }
        )
        saveInvoice(duplicated)
        duplicated
    }

    suspend fun replaceAll(newList: List<InvoiceDocument>) = withContext(Dispatchers.IO) {
        val sorted = newList.sortedByDescending { it.updatedAt }
        _invoices.value = sorted
        saveToFile(sorted)
    }

    suspend fun mergeAll(incomingList: List<InvoiceDocument>) = withContext(Dispatchers.IO) {
        val currentMap = _invoices.value.associateBy { it.id }.toMutableMap()
        for (incoming in incomingList) {
            val existing = currentMap[incoming.id]
            if (existing == null || incoming.updatedAt >= existing.updatedAt) {
                currentMap[incoming.id] = incoming
            }
        }
        val mergedList = currentMap.values.sortedByDescending { it.updatedAt }
        _invoices.value = mergedList
        saveToFile(mergedList)
    }

    private fun createSampleInvoices(): List<InvoiceDocument> {
        // 1. Cửa Hàng Thắng Lợi (img.png) - Đã đóng dấu ĐÃ THANH TOÁN
        val thangLoi = InvoiceDocument(
            id = "inv-default-thang-loi",
            title = "Cửa Hàng Thắng Lợi",
            type = InvoiceType.DELIVERY_DEBT,
            storeOrCompanyName = "Cửa Hàng Thắng Lợi",
            items = listOf(
                InvoiceItem(
                    id = "item-tl-1",
                    stt = 1,
                    date = "29/7/2026",
                    productName = "Máy NLMT 130 lít",
                    quantity = 1.0,
                    unitPrice = 4480000.0,
                    address = "Trung Mỹ Tây (theo định vị)",
                    receiver = "",
                    phone = "",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-tl-2",
                    stt = 2,
                    date = "15/8/2026",
                    productName = "Bồn ĐA 500 nằm",
                    quantity = 1.0,
                    unitPrice = 2150000.0,
                    address = "Trịnh Thị Miếng",
                    receiver = "Chị Lụa",
                    phone = "",
                    paidAmount = 0.0
                )
            ),
            oldDebt = 0.0,
            isPaid = true,
            paidDate = "15/8/2026",
            createdAt = 1785500000000L,
            updatedAt = 1785500000000L
        )

        // 2. Cửa Hàng Tiến Dũng (img_1.png) - Đã đóng dấu ĐÃ THANH TOÁN
        val tienDung = InvoiceDocument(
            id = "inv-default-tien-dung",
            title = "Cửa Hàng Tiến Dũng",
            type = InvoiceType.DELIVERY_DEBT,
            storeOrCompanyName = "Cửa Hàng Tiến Dũng",
            items = listOf(
                InvoiceItem(
                    id = "item-td-1",
                    stt = 1,
                    date = "11/07/2026.",
                    productName = "Máy 12",
                    quantity = 1.0,
                    unitPrice = 2340000.0,
                    address = "Lấy tại kho",
                    receiver = "",
                    phone = "",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-td-2",
                    stt = 2,
                    date = "29/07/2026",
                    productName = "Bồn 500 nằm",
                    quantity = 1.0,
                    unitPrice = 2150000.0,
                    address = "Giao qua cửa hàng",
                    receiver = "",
                    phone = "",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-td-3",
                    stt = 3,
                    date = "29/07/2026",
                    productName = "Máy NLMT 15",
                    quantity = 1.0,
                    unitPrice = 3900000.0,
                    address = "Giao 39/11 Đông Hưng Thuận 11B",
                    receiver = "",
                    phone = "",
                    paidAmount = 4800000.0
                )
            ),
            oldDebt = 0.0,
            isPaid = true,
            paidDate = "29/07/2026",
            createdAt = 1785510000000L,
            updatedAt = 1785510000000L
        )

        // 3. Cửa Hàng Hoàng Văn Thụ (img_2.png) - Đã đóng dấu ĐÃ THANH TOÁN
        val hoangVanThu = InvoiceDocument(
            id = "inv-default-hoang-van-thu",
            title = "Cửa Hàng Hoàng Văn Thụ",
            type = InvoiceType.DELIVERY_DEBT,
            storeOrCompanyName = "Cửa Hàng Hoàng Văn Thụ",
            items = listOf(
                InvoiceItem(
                    id = "item-hvt-1",
                    stt = 1,
                    date = "16/8/2026",
                    productName = "Máy NLMT Bình Minh 130 lít",
                    quantity = 1.0,
                    unitPrice = 3300000.0,
                    address = "Giao tại cửa hàng",
                    receiver = "",
                    phone = "",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-hvt-2",
                    stt = 2,
                    date = "17/8/2026",
                    productName = "Bồn 500 đứng ĐA",
                    quantity = 1.0,
                    unitPrice = 1970000.0,
                    address = "Giao Thới Tứ",
                    receiver = "",
                    phone = "",
                    paidAmount = 0.0
                )
            ),
            oldDebt = 0.0,
            isPaid = true,
            paidDate = "17/8/2026",
            createdAt = 1785520000000L,
            updatedAt = 1785520000000L
        )

        // 4. Cửa Hàng Kiều Phát (img_3.png) - Đã đóng dấu ĐÃ THANH TOÁN
        val kieuPhat = InvoiceDocument(
            id = "inv-default-kieu-phat",
            title = "Cửa Hàng Kiều Phát",
            type = InvoiceType.DELIVERY_DEBT,
            storeOrCompanyName = "Cửa Hàng Kiều Phát",
            items = listOf(
                InvoiceItem(
                    id = "item-kp-1",
                    stt = 1,
                    date = "15/7/2026",
                    productName = "Bồn 500 nằm TP",
                    quantity = 1.0,
                    unitPrice = 2090000.0,
                    address = "Xuân Thới Sơn",
                    receiver = "Chị Oanh",
                    phone = "0984674687",
                    paidAmount = 2000000.0
                ),
                InvoiceItem(
                    id = "item-kp-2",
                    stt = 2,
                    date = "31/7/2026",
                    productName = "Máy NLMT Bình Minh 140 lít",
                    quantity = 1.0,
                    unitPrice = 4020000.0,
                    address = "Đông Thạnh",
                    receiver = "A Hưng",
                    phone = "0914567206",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-kp-3",
                    stt = 3,
                    date = "31/7/2026",
                    productName = "Bình phụ 20 lít",
                    quantity = 1.0,
                    unitPrice = 700000.0,
                    address = "Đông Thạnh",
                    receiver = "A Hưng",
                    phone = "0914567206",
                    paidAmount = 0.0
                )
            ),
            oldDebt = 0.0,
            isPaid = true,
            paidDate = "31/7/2026",
            createdAt = 1785530000000L,
            updatedAt = 1785530000000L
        )

        // 5. Cửa Hàng Trung Tín (img_4.png) - Đã đóng dấu ĐÃ THANH TOÁN
        val trungTin = InvoiceDocument(
            id = "inv-default-trung-tin",
            title = "Cửa Hàng Trung Tín",
            type = InvoiceType.DELIVERY_DEBT,
            storeOrCompanyName = "Cửa Hàng Trung Tín",
            items = listOf(
                InvoiceItem(
                    id = "item-tt-1",
                    stt = 1,
                    date = "16/7/2026",
                    productName = "Bồn 1000 đứng",
                    quantity = 1.0,
                    unitPrice = 2980000.0,
                    address = "Long An",
                    receiver = "A Bình",
                    phone = "0981126294",
                    paidAmount = 0.0
                ),
                InvoiceItem(
                    id = "item-tt-2",
                    stt = 2,
                    date = "16/7/2026",
                    productName = "Máy NLMT 160 lít",
                    quantity = 1.0,
                    unitPrice = 5475000.0,
                    address = "Long An",
                    receiver = "A Bình",
                    phone = "0981126294",
                    paidAmount = 0.0
                )
            ),
            oldDebt = 4565000.0,
            debtPayments = listOf(
                com.vandatgsts.thuyetnguyen.data.model.DebtPayment(
                    id = "p-1",
                    title = "Ck lần 1",
                    amount = 3500000.0,
                    date = "25/7"
                ),
                com.vandatgsts.thuyetnguyen.data.model.DebtPayment(
                    id = "p-2",
                    title = "Ck lần 2",
                    amount = 935000.0,
                    date = "26/7"
                )
            ),
            initialOldDebt = 9000000.0,
            notes = "Nợ cũ: 9,000,000 | Ck lần 1 (25/7): 3,500,000 | Ck lần 2 (26/7): 935,000 -> Nợ cũ còn lại: 4,565,000",
            isPaid = true,
            paidDate = "26/7/2026",
            createdAt = 1785540000000L,
            updatedAt = 1785540000000L
        )

        // 6. Bảng Báo Giá A4 (Mẫu 2 - Công Ty Long Đại Thành) - Đã đóng dấu ĐÃ THANH TOÁN
        val baoGiaA4 = InvoiceDocument(
            id = "inv-default-quotation-a4",
            title = "BẢNG BÁO GIÁ",
            type = InvoiceType.QUOTATION_A4,
            storeOrCompanyName = "Công Ty TNHH Sản Xuất Thương Mại Xây Dựng Long Đại Thành",
            companyAddress = "19C Đường 116, Ấp 4, Xã Phú Hòa Đông, Thành phố Hồ Chí Minh",
            companyTaxCode = "0312203397",
            companyPhone = "0312203397",
            customer = CustomerInfo(
                name = "Công Ty MDK",
                address = "số 9 tô ký , Trung Mỹ Tây , quận 12 , Thành phố Hồ Chí Minh"
            ),
            items = listOf(
                InvoiceItem(
                    id = "item-q-1",
                    stt = 1,
                    productName = "Vệ sinh & Hàn & Phủ silicon cho 1 bồn",
                    unit = "Bồn",
                    quantity = 1.0,
                    unitPrice = 4000000.0
                )
            ),
            notes = "Ghi chú: Đơn giá tính theo dịch vụ trọn gói cho 1 bồn, bao gồm vật tư và nhân công.",
            warranty = "Bảo hành: 12 tháng kể từ ngày hoàn thành nghiệm thu",
            paymentTerms = "THANH TOÁN 100% TRƯỚC KHI THI CÔNG",
            bankAccountNumber = "6320201016734",
            bankName = "AGRIBANK CHI NHÁNH TÂY SÀI GÒN",
            bankAccountHolder = "CÔNG TY TNHH SX TM XD LONG ĐẠI THÀNH",
            isPaid = true,
            paidDate = "26/08/2026",
            createdAt = 1785550000000L,
            updatedAt = 1785550000000L
        )

        return listOf(baoGiaA4, thangLoi, tienDung, hoangVanThu, kieuPhat, trungTin)
    }


    companion object {
        @Volatile
        private var instance: InvoiceRepository? = null

        fun getInstance(context: Context): InvoiceRepository {
            return instance ?: synchronized(this) {
                instance ?: InvoiceRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
