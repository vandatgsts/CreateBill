package com.vandatgsts.thuyetnguyen.ui.editor

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add

import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Preview
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.InvoiceDocument
import com.vandatgsts.thuyetnguyen.data.model.InvoiceItem
import com.vandatgsts.thuyetnguyen.data.model.InvoiceType
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import com.vandatgsts.thuyetnguyen.ui.components.AppTextField
import com.vandatgsts.thuyetnguyen.ui.components.CurrencyField
import com.vandatgsts.thuyetnguyen.ui.components.DatePickerField
import com.vandatgsts.thuyetnguyen.ui.components.FormSectionHeader
import com.vandatgsts.thuyetnguyen.ui.components.NumberField

import com.vandatgsts.thuyetnguyen.ui.theme.AccentGreen
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.BorderColor
import com.vandatgsts.thuyetnguyen.ui.theme.DeleteRed
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary


import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Store
import com.vandatgsts.thuyetnguyen.data.model.ProductTemplate
import com.vandatgsts.thuyetnguyen.data.model.StorePartner



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceEditorScreen(
    viewModel: InvoiceEditorViewModel,
    onBack: () -> Unit,
    onNavigateToPreview: (String) -> Unit,
    onNavigateToProducts: () -> Unit,
    onNavigateToStores: () -> Unit
) {
    val invoice by viewModel.invoiceState.collectAsState()
    val isSaving by viewModel.isSaving.collectAsState()
    val availableProducts by viewModel.availableProducts.collectAsState()
    val availableStores by viewModel.availableStores.collectAsState()
    val context = LocalContext.current
    val onSaveError: (Exception) -> Unit = {
        Toast.makeText(context, "Không thể lưu hóa đơn. Vui lòng kiểm tra dung lượng và thử lại.", Toast.LENGTH_LONG).show()
    }
    val scrollState = rememberScrollState()
    var viewportBounds by remember { mutableStateOf(Rect.Zero) }

    var activeItemIndexForProductSelection by remember { mutableStateOf<Int?>(null) }
    var showStorePicker by remember { mutableStateOf(false) }





    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (invoice.type == InvoiceType.DELIVERY_DEBT) "Sổ Giao Hàng & Công Nợ" else "Bảng Báo Giá A4",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                actions = {
                    IconButton(enabled = !isSaving, onClick = {
                        viewModel.saveInvoice(onError = onSaveError) { saved ->
                            onNavigateToPreview(saved.id)
                        }
                    }) {
                        Icon(Icons.Default.Preview, contentDescription = "Xem trước & Xuất", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("TỔNG CỘNG:", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                        Text(
                            "${FormatHelper.formatMoney(invoice.totalAmount)} đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = AccentGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedButton(
                            enabled = !isSaving,
                            onClick = {
                                viewModel.saveInvoice(onError = onSaveError) {
                                    Toast.makeText(context, "Đã lưu hóa đơn thành công!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lưu Lại", fontWeight = FontWeight.Bold, color = PrimaryBlue)
                        }

                        Button(
                            enabled = !isSaving,
                            onClick = {
                                viewModel.saveInvoice(onError = onSaveError) { saved ->
                                    onNavigateToPreview(saved.id)
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                            modifier = Modifier.weight(1f).height(48.dp)
                        ) {
                            Icon(Icons.Default.Preview, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Xem & Xuất", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .onGloballyPositioned { viewportBounds = it.boundsInWindow() }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .padding(16.dp)
            ) {
            // 1. Chuyển đổi Loại Mẫu (Template Selector)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("CHỌN KIỂU MẪU HÓA ĐƠN", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextSecondary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = invoice.type == InvoiceType.DELIVERY_DEBT,
                            onClick = { viewModel.setInvoiceType(InvoiceType.DELIVERY_DEBT) },
                            label = { Text("Mẫu 1: Giao hàng & Nợ", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = invoice.type == InvoiceType.QUOTATION_A4,
                            onClick = { viewModel.setInvoiceType(InvoiceType.QUOTATION_A4) },
                            label = { Text("Mẫu 2: Báo giá A4", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = PrimaryBlue,
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 2. Thông tin Bên Bán / Tiêu đề
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormSectionHeader(title = if (invoice.type == InvoiceType.DELIVERY_DEBT) "1. CỬA HÀNG / ĐẠI LÝ" else "1. THÔNG TIN BÊN BÁN / CÔNG TY")
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (invoice.type == InvoiceType.QUOTATION_A4) {
                                Switch(
                                    checked = invoice.showCompanyInfo,
                                    onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showCompanyInfo = it) } },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = PrimaryBlue
                                    )
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            TextButton(onClick = { showStorePicker = true }) {
                                Icon(Icons.Default.Store, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Chọn", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    if (invoice.showCompanyInfo || invoice.type == InvoiceType.DELIVERY_DEBT) {
                        AppTextField(
                            value = invoice.storeOrCompanyName,
                            onValueChange = { viewModel.updateInvoice { doc -> doc.copy(storeOrCompanyName = it, title = if (doc.type == InvoiceType.DELIVERY_DEBT) it else doc.title) } },
                            label = if (invoice.type == InvoiceType.DELIVERY_DEBT) "Tên Cửa Hàng / Đại Lý" else "Tên Công Ty",
                            placeholder = if (invoice.type == InvoiceType.DELIVERY_DEBT) "Cửa Hàng Kiều Phát" else "Công Ty TNHH..."
                        )

                        if (invoice.type == InvoiceType.QUOTATION_A4) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                FilterChip(
                                    selected = invoice.showCompanyAddress,
                                    onClick = { viewModel.updateInvoice { doc -> doc.copy(showCompanyAddress = !doc.showCompanyAddress) } },
                                    label = { Text("Hiện Địa chỉ", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryBlue
                                    )
                                )
                                FilterChip(
                                    selected = invoice.showCompanyTaxCode,
                                    onClick = { viewModel.updateInvoice { doc -> doc.copy(showCompanyTaxCode = !doc.showCompanyTaxCode) } },
                                    label = { Text("Hiện Mã số thuế", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryBlue
                                    )
                                )
                            }

                            if (invoice.showCompanyAddress) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AppTextField(
                                    value = invoice.companyAddress,
                                    onValueChange = { viewModel.updateInvoice { doc -> doc.copy(companyAddress = it) } },
                                    label = "Địa chỉ công ty",
                                    placeholder = "19C Đường 116, Ấp 4..."
                                )
                            }

                            if (invoice.showCompanyTaxCode) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AppTextField(
                                    value = invoice.companyTaxCode,
                                    onValueChange = { viewModel.updateInvoice { doc -> doc.copy(companyTaxCode = it) } },
                                    label = "Mã số thuế",
                                    placeholder = "0312203397"
                                )
                            }
                        }
                    } else {
                        Text("(Mục thông tin bên bán đang được ẩn trên báo giá)", fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 3. Thông tin Khách Hàng (Mẫu 2)
            if (invoice.type == InvoiceType.QUOTATION_A4) {
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormSectionHeader(title = "2. THÔNG TIN KHÁCH HÀNG (BÊN MUA)")
                            Switch(
                                checked = invoice.showCustomerInfo,
                                onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showCustomerInfo = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue
                                )
                            )
                        }

                        if (invoice.showCustomerInfo) {
                            AppTextField(
                                value = invoice.customer.name,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(customer = doc.customer.copy(name = it)) } },
                                label = "Tên khách hàng / Đối tác",
                                placeholder = "Ví dụ: Công Ty MDK / Anh Nam"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(modifier = Modifier.fillMaxWidth()) {
                                FilterChip(
                                    selected = invoice.showCustomerAddress,
                                    onClick = { viewModel.updateInvoice { doc -> doc.copy(showCustomerAddress = !doc.showCustomerAddress) } },
                                    label = { Text("Hiện Địa chỉ KH", fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryBlue.copy(alpha = 0.15f),
                                        selectedLabelColor = PrimaryBlue
                                    )
                                )
                            }

                            if (invoice.showCustomerAddress) {
                                Spacer(modifier = Modifier.height(8.dp))
                                AppTextField(
                                    value = invoice.customer.address,
                                    onValueChange = { viewModel.updateInvoice { doc -> doc.copy(customer = doc.customer.copy(address = it)) } },
                                    label = "Địa chỉ khách hàng",
                                    placeholder = "Số 9 Tô Ký, Trung Mỹ Tây, Q12..."
                                )
                            }
                        } else {
                            Text("(Mục thông tin khách hàng đang được ẩn trên báo giá)", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }


            // 4. Danh sách Mặt Hàng / Dịch Vụ
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        FormSectionHeader(
                            title = if (invoice.type == InvoiceType.DELIVERY_DEBT) "2. DANH SÁCH GIAO HÀNG (${invoice.items.size})" else "3. DANH MỤC DỊCH VỤ / VẬT TƯ (${invoice.items.size})"
                        )
                        Row {
                            TextButton(onClick = onNavigateToProducts) {
                                Icon(Icons.Default.Inventory2, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Kho", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = { viewModel.addItem() }) {
                                Icon(Icons.Default.Add, contentDescription = "Thêm dòng", tint = PrimaryBlue)
                            }
                        }
                    }

                    // Tùy chỉnh bật/tắt các cột cho Báo Giá A4
                    if (invoice.type == InvoiceType.QUOTATION_A4) {
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("TÙY CHỌN CỘT BẢNG BÁO GIÁ:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextSecondary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    FilterChip(
                                        selected = invoice.showUnitCol,
                                        onClick = { viewModel.updateInvoice { doc -> doc.copy(showUnitCol = !doc.showUnitCol) } },
                                        label = { Text("Cột ĐVT", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = invoice.showQuantityCol,
                                        onClick = { viewModel.updateInvoice { doc -> doc.copy(showQuantityCol = !doc.showQuantityCol) } },
                                        label = { Text("Cột Số Lượng", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                    FilterChip(
                                        selected = invoice.showUnitPriceCol,
                                        onClick = { viewModel.updateInvoice { doc -> doc.copy(showUnitPriceCol = !doc.showUnitPriceCol) } },
                                        label = { Text("Cột Đơn Giá", fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue,
                                            selectedLabelColor = Color.White
                                        ),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Text(
                        "Nhấn giữ nút kéo ở mỗi dòng để đổi thứ tự.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    ReorderableInvoiceItems(
                        items = invoice.items,
                        scrollState = scrollState,
                        viewportBounds = viewportBounds,
                        onMove = viewModel::moveItem
                    ) { index, item, dragHandleModifier ->
                        ItemCard(
                            type = invoice.type,
                            index = index,
                            item = item,
                            showUnitCol = invoice.showUnitCol,
                            showQuantityCol = invoice.showQuantityCol,
                            showUnitPriceCol = invoice.showUnitPriceCol,
                            availableProducts = availableProducts,
                            dragHandleModifier = dragHandleModifier,
                            onUpdate = { updated -> viewModel.updateItem(index, updated) },
                            onDelete = { viewModel.removeItem(index) },
                            onDuplicate = { viewModel.duplicateItem(index) },
                            onOpenProductPicker = { activeItemIndexForProductSelection = index }
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.addItem() },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue.copy(alpha = 0.1f)),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Thêm Dòng Mới", color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 5. Nợ cũ (Mẫu 1) hoặc Ghi chú & Điều khoản (Mẫu 2)
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    if (invoice.type == InvoiceType.DELIVERY_DEBT) {
                        FormSectionHeader(title = "3. CÔNG NỢ & THANH TOÁN")

                        val currentOldDebt = if (invoice.initialOldDebt != 0.0) invoice.initialOldDebt else invoice.oldDebt
                        val oldDebtLabel = if (currentOldDebt < 0) "Tiền khách trả thừa kỳ trước (Khấu trừ) (đ)" else "Nợ cũ kỳ trước chuyển sang (đ)"

                        CurrencyField(
                            value = currentOldDebt,
                            onValueChange = {
                                viewModel.updateInvoice { doc ->
                                    doc.copy(oldDebt = it, initialOldDebt = it)
                                }
                            },
                            label = oldDebtLabel,
                            allowNegative = true
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Danh sách các đợt Chuyển khoản trừ nợ (Mẫu img_4.png)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Các đợt Chuyển Khoản trừ nợ", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            TextButton(onClick = {
                                viewModel.addDebtPayment("Ck lần ${invoice.debtPayments.size + 1}", 0.0)
                            }) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Thêm đợt CK", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        if (invoice.debtPayments.isNotEmpty()) {
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                invoice.debtPayments.forEachIndexed { pIndex, payment ->
                                    Surface(
                                        color = Color(0xFFF8FAFC),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            AppTextField(
                                                value = payment.title,
                                                onValueChange = { newTitle ->
                                                    val updatedList = invoice.debtPayments.toMutableList()
                                                    updatedList[pIndex] = payment.copy(title = newTitle)
                                                    viewModel.updateInvoice { it.copy(debtPayments = updatedList) }
                                                },
                                                label = "Tên đợt (ví dụ: Ck lần 1)",
                                                modifier = Modifier.weight(1.2f)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            CurrencyField(
                                                value = payment.amount,
                                                onValueChange = { newAmount ->
                                                    val updatedList = invoice.debtPayments.toMutableList()
                                                    updatedList[pIndex] = payment.copy(amount = newAmount)
                                                    viewModel.updateInvoice { it.copy(debtPayments = updatedList) }
                                                },
                                                label = "Số tiền (đ)",
                                                modifier = Modifier.weight(1.2f)
                                            )
                                            IconButton(onClick = { viewModel.removeDebtPayment(payment.id) }) {
                                                Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = DeleteRed, modifier = Modifier.size(20.dp))
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    if (invoice.effectiveOldDebt < 0) "Khách trả dư sau khi trừ CK:" else "Nợ cũ còn lại sau khi trừ CK:",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    if (invoice.effectiveOldDebt < 0) "+${FormatHelper.formatMoney(-invoice.effectiveOldDebt)} đ" else "${FormatHelper.formatMoney(invoice.effectiveOldDebt)} đ",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (invoice.effectiveOldDebt > 0) DeleteRed else AccentGreen
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Switch: Đóng dấu ĐÃ THANH TOÁN
                        Surface(
                            color = if (invoice.isPaid) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (invoice.isPaid) Color(0xFFFCA5A5) else BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Đóng Dấu: ĐÃ THANH TOÁN", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (invoice.isPaid) DeleteRed else TextPrimary)
                                        Text(
                                            if (invoice.isPaid) "Hóa đơn đã đóng dấu đã thu tiền (giữ nguyên số tiền thành tiền)" else "Bật để đóng dấu ĐÃ THANH TOÁN lên hóa đơn",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = invoice.isPaid,
                                        onCheckedChange = { viewModel.togglePaidStatus(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = DeleteRed
                                        )
                                    )
                                }

                                if (invoice.isPaid) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DatePickerField(
                                        value = invoice.paidDate,
                                        onValueChange = { viewModel.setPaidDate(it) },
                                        label = "Ngày thanh toán (in trên con dấu)",
                                        placeholder = "dd/MM/yyyy"
                                    )
                                }
                            }
                        }
                    } else {
                        FormSectionHeader(title = "4. GHI CHÚ, BẢO HÀNH & ĐIỀU KHOẢN")

                        // Ghi chú
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Ghi chú báo giá", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = invoice.showNotes,
                                onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showNotes = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue
                                )
                            )
                        }

                        if (invoice.showNotes) {
                            AppTextField(
                                value = invoice.notes,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(notes = it) } },
                                label = "Nội dung ghi chú",
                                placeholder = "Ghi chú: Đơn giá tính theo dịch vụ trọn gói...",
                                singleLine = false,
                                maxLines = 3
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Bảo hành
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Bảo hành", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = invoice.showWarranty,
                                onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showWarranty = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue
                                )
                            )
                        }

                        if (invoice.showWarranty) {
                            AppTextField(
                                value = invoice.warranty,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(warranty = it) } },
                                label = "Thời hạn & Điều kiện bảo hành",
                                placeholder = "ví dụ: Bảo hành 12 tháng kể từ ngày hoàn thành nghiệm thu"
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Điều khoản thanh toán
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Điều khoản thanh toán", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = TextPrimary)
                            Switch(
                                checked = invoice.showPaymentTerms,
                                onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showPaymentTerms = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue
                                )
                            )
                        }

                        if (invoice.showPaymentTerms) {
                            AppTextField(
                                value = invoice.paymentTerms,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(paymentTerms = it) } },
                                label = "Nội dung điều khoản",
                                placeholder = "THANH TOÁN 100% TRƯỚC KHI THI CÔNG"
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // VAT Options Card
                        Surface(
                            color = Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Giá Đã Bao Gồm VAT", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Text(
                                            if (invoice.isVatIncluded) "In trên hóa đơn: 'TỔNG CỘNG (ĐÃ BAO GỒM VAT)'" else "Báo giá chưa tính VAT",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = invoice.isVatIncluded,
                                        onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(isVatIncluded = it) } },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = PrimaryBlue
                                        )
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Text("Thuế suất VAT:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    listOf(0.0 to "Đã gồm trong giá", 8.0 to "VAT 8%", 10.0 to "VAT 10%").forEach { (rate, label) ->
                                        val isSelected = invoice.vatRate == rate
                                        Surface(
                                            color = if (isSelected) PrimaryBlue else Color.White,
                                            shape = RoundedCornerShape(6.dp),
                                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) PrimaryBlue else BorderColor),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable {
                                                    viewModel.updateInvoice { doc -> doc.copy(vatRate = rate, isVatIncluded = true) }
                                                }
                                        ) {
                                            Box(
                                                modifier = Modifier.padding(vertical = 8.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = label,
                                                    fontSize = 11.sp,
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                    color = if (isSelected) Color.White else TextPrimary
                                                )
                                            }
                                        }
                                    }
                                }

                                if (invoice.vatRate > 0.0) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("Tiền thuế GTGT (${if (invoice.vatRate == invoice.vatRate.toLong().toDouble()) invoice.vatRate.toInt() else invoice.vatRate}%):", fontSize = 12.sp, color = TextSecondary)
                                        Text(
                                            "+${FormatHelper.formatMoney(invoice.vatAmount)} đ",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = AccentGreen
                                        )
                                    }
                                }
                            }
                        }


                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch: Đóng dấu ĐÃ THANH TOÁN cho Báo Giá A4
                        Surface(
                            color = if (invoice.isPaid) Color(0xFFFEF2F2) else Color(0xFFF8FAFC),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (invoice.isPaid) Color(0xFFFCA5A5) else BorderColor),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Đóng Dấu: ĐÃ THANH TOÁN", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = if (invoice.isPaid) DeleteRed else TextPrimary)
                                        Text(
                                            if (invoice.isPaid) "Báo giá đã đóng dấu ĐÃ THANH TOÁN (giữ nguyên đơn giá)" else "Bật để đóng dấu ĐÃ THANH TOÁN lên báo giá",
                                            fontSize = 11.sp,
                                            color = TextSecondary
                                        )
                                    }
                                    Switch(
                                        checked = invoice.isPaid,
                                        onCheckedChange = { viewModel.togglePaidStatus(it) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = Color.White,
                                            checkedTrackColor = DeleteRed
                                        )
                                    )
                                }

                                if (invoice.isPaid) {
                                    Spacer(modifier = Modifier.height(10.dp))
                                    DatePickerField(
                                        value = invoice.paidDate,
                                        onValueChange = { viewModel.setPaidDate(it) },
                                        label = "Ngày thanh toán (in trên con dấu)",
                                        placeholder = "dd/MM/yyyy"
                                    )
                                }
                            }
                        }
                    }
                }
            }




            // 6. Tài khoản ngân hàng (Mẫu 2)
            if (invoice.type == InvoiceType.QUOTATION_A4) {
                Spacer(modifier = Modifier.height(14.dp))
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FormSectionHeader(title = "5. TÀI KHOẢN NGÂN HÀNG THANH TOÁN")
                            Switch(
                                checked = invoice.showBankInfo,
                                onCheckedChange = { viewModel.updateInvoice { doc -> doc.copy(showBankInfo = it) } },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = PrimaryBlue
                                )
                            )
                        }

                        if (invoice.showBankInfo) {
                            AppTextField(
                                value = invoice.bankAccountNumber,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(bankAccountNumber = it) } },
                                label = "Số Tài Khoản"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AppTextField(
                                value = invoice.bankName,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(bankName = it) } },
                                label = "Ngân hàng"
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            AppTextField(
                                value = invoice.bankAccountHolder,
                                onValueChange = { viewModel.updateInvoice { doc -> doc.copy(bankAccountHolder = it) } },
                                label = "Tên chủ tài khoản"
                            )
                        } else {
                            Text("(Mục tài khoản ngân hàng đang được ẩn trên báo giá)", fontSize = 12.sp, color = TextSecondary)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Modal Chọn Sản Phẩm Từ Danh Mục
    val selectedItemIndex = activeItemIndexForProductSelection
    if (selectedItemIndex != null) {
        AlertDialog(
            onDismissRequest = { activeItemIndexForProductSelection = null },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chọn Sản Phẩm / Dịch Vụ", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = {
                        activeItemIndexForProductSelection = null
                        onNavigateToProducts()
                    }) {
                        Text("Quản lý", fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (availableProducts.isEmpty()) {
                        Text("Chưa có sản phẩm nào trong danh mục. Hãy bấm 'Quản lý' để thêm.", color = TextSecondary, fontSize = 13.sp)
                    } else {
                        availableProducts.forEach { product ->
                            Card(
                                shape = RoundedCornerShape(8.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.applyProductTemplate(selectedItemIndex, product)
                                        activeItemIndexForProductSelection = null
                                    }
                            ) {

                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(product.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        if (product.unit.isNotBlank()) {
                                            Text("ĐVT: ${product.unit}", fontSize = 12.sp, color = TextSecondary)
                                        }
                                        Text(
                                            "${FormatHelper.formatMoney(product.defaultPrice)} đ",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = AccentGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { activeItemIndexForProductSelection = null }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Modal Chọn Cửa Hàng / Đại Lý
    if (showStorePicker) {

        var storeSearchQuery by remember { mutableStateOf("") }
        val filteredPickerStores = if (storeSearchQuery.isBlank()) {
            availableStores
        } else {
            availableStores.filter {
                it.name.contains(storeSearchQuery, ignoreCase = true) ||
                        it.phone.contains(storeSearchQuery, ignoreCase = true) ||
                        it.address.contains(storeSearchQuery, ignoreCase = true)
            }
        }

        AlertDialog(
            onDismissRequest = { showStorePicker = false },
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Chọn Cửa Hàng / Đại Lý", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    TextButton(onClick = {
                        showStorePicker = false
                        onNavigateToStores()
                    }) {
                        Text("Quản lý", fontSize = 12.sp)
                    }
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    AppTextField(
                        value = storeSearchQuery,
                        onValueChange = { storeSearchQuery = it },
                        label = "Tìm kiếm Cửa Hàng",
                        placeholder = "Tên, SĐT, địa chỉ..."
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (filteredPickerStores.isEmpty()) {
                            Text("Chưa có Cửa Hàng nào. Hãy bấm 'Quản lý' để thêm mới.", color = TextSecondary, fontSize = 13.sp)
                        } else {
                            filteredPickerStores.forEach { store ->
                                Card(
                                    shape = RoundedCornerShape(8.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            viewModel.applyStorePartner(store)
                                            showStorePicker = false
                                        }
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(store.name, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        if (store.contactPerson.isNotBlank() || store.phone.isNotBlank()) {
                                            Text(
                                                "${store.contactPerson} ${if (store.phone.isNotBlank()) "- ${store.phone}" else ""}",
                                                fontSize = 12.sp,
                                                color = TextSecondary
                                            )
                                        }
                                        if (store.address.isNotBlank()) {
                                            Text("Đ/c: ${store.address}", fontSize = 12.sp, color = TextSecondary, maxLines = 1)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showStorePicker = false }) {
                    Text("Đóng")
                }
            }
        )
    }
}



@Composable
private fun ItemCard(
    type: InvoiceType,
    index: Int,
    item: InvoiceItem,
    showUnitCol: Boolean = true,
    showQuantityCol: Boolean = true,
    showUnitPriceCol: Boolean = true,
    availableProducts: List<ProductTemplate>,
    dragHandleModifier: Modifier,
    onUpdate: (InvoiceItem) -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onOpenProductPicker: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, BorderColor, RoundedCornerShape(10.dp))
            .background(Color(0xFFFAFAFA), RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = dragHandleModifier.size(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.DragIndicator,
                            contentDescription = "Nhấn giữ và kéo để đổi vị trí dòng ${index + 1}",
                            tint = PrimaryBlue
                        )
                    }
                    Text(
                        text = "#${index + 1}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = PrimaryBlue
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {

                    TextButton(onClick = onOpenProductPicker) {
                        Icon(Icons.AutoMirrored.Filled.List, contentDescription = null, modifier = Modifier.size(16.dp), tint = PrimaryBlue)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Chọn SP", fontSize = 12.sp, color = PrimaryBlue, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = onDuplicate) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Nhân bản", tint = TextSecondary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Xóa", tint = DeleteRed)
                    }
                }
            }

            if (type == InvoiceType.DELIVERY_DEBT) {
                // Form cho Mẫu 1: Giao hàng & Công nợ
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DatePickerField(
                        value = item.date,
                        onValueChange = { onUpdate(item.copy(date = it)) },
                        label = "Ngày giao",
                        placeholder = "dd/MM/yyyy",
                        modifier = Modifier.weight(0.42f)
                    )
                    AppTextField(
                        value = item.productName,
                        onValueChange = { onUpdate(item.copy(productName = it)) },
                        label = "Tên Sản Phẩm",
                        placeholder = "Bồn 500 nằm TP",
                        modifier = Modifier.weight(0.58f)
                    )
                }


                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    NumberField(
                        value = item.quantity,
                        onValueChange = { onUpdate(item.copy(quantity = it)) },
                        label = "Số lượng",
                        modifier = Modifier.weight(0.35f)
                    )
                    CurrencyField(
                        value = item.unitPrice,
                        onValueChange = { onUpdate(item.copy(unitPrice = it)) },
                        label = "Đơn giá",
                        modifier = Modifier.weight(0.65f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AppTextField(
                        value = item.receiver,
                        onValueChange = { onUpdate(item.copy(receiver = it)) },
                        label = "Người nhận",
                        placeholder = "Chị Oanh",
                        modifier = Modifier.weight(0.45f)
                    )
                    AppTextField(
                        value = item.phone,
                        onValueChange = { onUpdate(item.copy(phone = it)) },
                        label = "Số ĐT",
                        placeholder = "0984674687",
                        modifier = Modifier.weight(0.55f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                AppTextField(
                    value = item.address,
                    onValueChange = { onUpdate(item.copy(address = it)) },
                    label = "Địa chỉ giao",
                    placeholder = "Xuân Thới Sơn"
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CurrencyField(
                        value = item.paidAmount,
                        onValueChange = { onUpdate(item.copy(paidAmount = it)) },
                        label = "Đã thu (Cọc/Trả trước)",
                        modifier = Modifier.weight(0.5f)
                    )
                    Column(
                        modifier = Modifier
                            .weight(0.5f)
                            .padding(top = 8.dp)
                    ) {
                        Text("Thành tiền dòng:", fontSize = 12.sp, color = TextSecondary)
                        Text(
                            "${FormatHelper.formatMoney(item.lineTotalM1)} đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = AccentGreen
                        )
                    }
                }
            } else {
                // Form cho Mẫu 2: Báo giá A4
                AppTextField(
                    value = item.productName,
                    onValueChange = { onUpdate(item.copy(productName = it)) },
                    label = "Nội Dung Dịch Vụ / Hàng Hóa",
                    placeholder = "Vệ sinh & Hàn & Phủ silicon...",
                    singleLine = false,
                    maxLines = 2
                )

                val hasAnyOptionalCol = showUnitCol || showQuantityCol || showUnitPriceCol
                if (hasAnyOptionalCol) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (showUnitCol) {
                            AppTextField(
                                value = item.unit,
                                onValueChange = { onUpdate(item.copy(unit = it)) },
                                label = "ĐVT",
                                placeholder = "Bồn",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (showQuantityCol) {
                            NumberField(
                                value = item.quantity,
                                onValueChange = { onUpdate(item.copy(quantity = it)) },
                                label = "Số lượng",
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (showUnitPriceCol) {
                            CurrencyField(
                                value = item.unitPrice,
                                onValueChange = { onUpdate(item.copy(unitPrice = it)) },
                                label = "Đơn giá",
                                modifier = Modifier.weight(1.3f)
                            )
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(8.dp))
                    CurrencyField(
                        value = item.unitPrice,
                        onValueChange = { onUpdate(item.copy(unitPrice = it)) },
                        label = "Thành tiền dịch vụ (đ)"
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Thành tiền: ", fontSize = 13.sp, color = TextSecondary)
                    Text(
                        "${FormatHelper.formatMoney(item.lineTotalM2)} đ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = AccentGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            AppTextField(
                value = item.note,
                onValueChange = { onUpdate(item.copy(note = it)) },
                label = "Ghi chú dòng",
                placeholder = "Nhập ghi chú cho dòng này",
                singleLine = false,
                maxLines = 3
            )
        }
    }
}

