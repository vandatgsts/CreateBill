package com.vandatgsts.thuyetnguyen.ui.settings

import android.content.Intent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.data.model.AppBackupData
import com.vandatgsts.thuyetnguyen.data.model.ImportMode
import com.vandatgsts.thuyetnguyen.ui.components.AppTextField
import com.vandatgsts.thuyetnguyen.ui.components.FormSectionHeader
import com.vandatgsts.thuyetnguyen.ui.theme.AccentGreen
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.BorderColor
import com.vandatgsts.thuyetnguyen.ui.theme.DeleteRed
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(
    viewModel: CompanyProfileViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val isExporting by viewModel.isExporting.collectAsState()
    val isImporting by viewModel.isImporting.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    var exportResultData by remember { mutableStateOf<Pair<AppBackupData, Intent>?>(null) }
    var pendingImportBackup by remember { mutableStateOf<AppBackupData?>(null) }
    var isPendingDuplicateHash by remember { mutableStateOf(false) }
    var selectedImportMode by remember { mutableStateOf(ImportMode.MERGE) }
    var showImportErrorDialog by remember { mutableStateOf<String?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            viewModel.inspectBackupFile(uri) { backup, isDuplicate, error ->
                if (error != null) {
                    showImportErrorDialog = error
                } else if (backup != null) {
                    pendingImportBackup = backup
                    isPendingDuplicateHash = isDuplicate
                    selectedImportMode = ImportMode.MERGE
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cấu Hình & Sao Lưu Dữ Liệu", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimaryBlue,
                    titleContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundLight)
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            // 1. THÔNG TIN CỬA HÀNG / CÔNG TY
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormSectionHeader(title = "1. THÔNG TIN BÊN BÁN (CỬA HÀNG / CÔNG TY)")

                    AppTextField(
                        value = profile.storeName,
                        onValueChange = { viewModel.updateProfile(profile.copy(storeName = it)) },
                        label = "Tên Cửa Hàng (Mẫu 1)",
                        placeholder = "Ví dụ: Cửa Hàng Kiều Phát"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.companyName,
                        onValueChange = { viewModel.updateProfile(profile.copy(companyName = it)) },
                        label = "Tên Công Ty (Mẫu 2)",
                        placeholder = "Ví dụ: Công Ty TNHH Sản Xuất TM..."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.address,
                        onValueChange = { viewModel.updateProfile(profile.copy(address = it)) },
                        label = "Địa chỉ",
                        placeholder = "Số nhà, tên đường, quận/huyện, TP..."
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.taxCode,
                        onValueChange = { viewModel.updateProfile(profile.copy(taxCode = it)) },
                        label = "Mã số thuế",
                        placeholder = "Ví dụ: 0312203397"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.phone,
                        onValueChange = { viewModel.updateProfile(profile.copy(phone = it)) },
                        label = "Số điện thoại liên hệ",
                        placeholder = "Ví dụ: 0984674687"
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    FormSectionHeader(title = "2. THÔNG TIN TÀI KHOẢN NGÂN HÀNG")

                    AppTextField(
                        value = profile.bankAccountNumber,
                        onValueChange = { viewModel.updateProfile(profile.copy(bankAccountNumber = it)) },
                        label = "Số Tài Khoản",
                        placeholder = "Ví dụ: 6320201016734"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.bankName,
                        onValueChange = { viewModel.updateProfile(profile.copy(bankName = it)) },
                        label = "Tên Ngân Hàng & Chi Nhánh",
                        placeholder = "Ví dụ: AGRIBANK CHI NHÁNH TÂY SÀI GÒN"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.bankAccountHolder,
                        onValueChange = { viewModel.updateProfile(profile.copy(bankAccountHolder = it)) },
                        label = "Tên Chủ Tài Khoản",
                        placeholder = "Ví dụ: CÔNG TY TNHH SX TM XD LONG ĐẠI THÀNH"
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    FormSectionHeader(title = "3. ĐIỀU KHOẢN & GHI CHÚ MẶC ĐỊNH")

                    AppTextField(
                        value = profile.defaultPaymentTerms,
                        onValueChange = { viewModel.updateProfile(profile.copy(defaultPaymentTerms = it)) },
                        label = "Điều khoản thanh toán mặc định",
                        placeholder = "THANH TOÁN 100% TRƯỚC KHI THI CÔNG"
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AppTextField(
                        value = profile.defaultNotes,
                        onValueChange = { viewModel.updateProfile(profile.copy(defaultNotes = it)) },
                        label = "Ghi chú mặc định (Mẫu 2)",
                        placeholder = "Đơn giá tính theo dịch vụ trọn gói...",
                        singleLine = false,
                        maxLines = 3
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    viewModel.saveProfile()
                    Toast.makeText(context, "Đã lưu thông tin cửa hàng mặc định thành công!", Toast.LENGTH_SHORT).show()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("LƯU THÔNG TIN BÊN BÁN", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. KHỐI SAO LƯU & KHÔI PHỤC DỮ LIỆU
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    FormSectionHeader(title = "4. SAO LƯU & KHÔI PHỤC TOÀN BỘ DỮ LIỆU")

                    Text(
                        "Xuất tất cả dữ liệu (Hóa đơn, Cửa hàng, Kho hàng, Khách hàng, Cấu hình) ra file JSON với mã Hash SHA-256 bảo vệ, hoặc nhập dữ liệu từ file sao lưu.",
                        fontSize = 12.sp,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Nút Xuất toàn bộ dữ liệu
                    Button(
                        onClick = {
                            viewModel.exportBackup(
                                onReady = { backup, intent ->
                                    exportResultData = Pair(backup, intent)
                                },
                                onError = { err ->
                                    Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                }
                            )
                        },
                        enabled = !isExporting,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isExporting) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang tạo file sao lưu...", fontSize = 13.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Xuất Toàn Bộ Dữ Liệu (File JSON)", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Nút Nhập dữ liệu từ file
                    OutlinedButton(
                        onClick = {
                            filePickerLauncher.launch("application/json")
                        },
                        enabled = !isImporting,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        if (isImporting) {
                            CircularProgressIndicator(color = PrimaryBlue, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Đang nhập dữ liệu...", fontSize = 13.sp, color = PrimaryBlue)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, tint = PrimaryBlue)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Nhập Dữ Liệu Từ File (Khôi Phục)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = PrimaryBlue)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // Modal Xuất Dữ Liệu Thành Công & Chia Sẻ
    val exportData = exportResultData
    if (exportData != null) {
        val (backup, intent) = exportData
        AlertDialog(
            onDismissRequest = { exportResultData = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Fingerprint, contentDescription = null, tint = AccentGreen, modifier = Modifier.size(26.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Xuất Dữ Liệu Thành Công", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("File sao lưu đã được tạo an toàn với thông tin:", fontSize = 13.sp, color = TextPrimary)
                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("• Thời gian: ${backup.exportedDate}", fontSize = 12.sp, color = TextSecondary)
                            Text("• Hóa đơn: ${backup.invoices.size} hóa đơn", fontSize = 12.sp, color = TextSecondary)
                            Text("• Cửa hàng / Đại lý: ${backup.storePartners.size} đối tác", fontSize = 12.sp, color = TextSecondary)
                            Text("• Sản phẩm trong kho: ${backup.products.size} mặt hàng", fontSize = 12.sp, color = TextSecondary)
                            Text("• Khách hàng: ${backup.customers.size} hồ sơ", fontSize = 12.sp, color = TextSecondary)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Mã Hash (SHA-256):", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text(backup.fileHash, fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = PrimaryBlue)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        exportResultData = null
                        context.startActivity(Intent.createChooser(intent, "Chia sẻ file sao lưu qua:"))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Gửi / Lưu File")
                }
            },
            dismissButton = {
                TextButton(onClick = { exportResultData = null }) {
                    Text("Đóng")
                }
            }
        )
    }

    // Modal Xác Nhận Nhập Dữ Liệu (Import Confirmation)
    val backupToImport = pendingImportBackup
    if (backupToImport != null) {
        AlertDialog(
            onDismissRequest = { pendingImportBackup = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isPendingDuplicateHash) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFD97706), modifier = Modifier.size(24.dp))
                    } else {
                        Icon(Icons.Default.CloudDownload, contentDescription = null, tint = PrimaryBlue, modifier = Modifier.size(24.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        if (isPendingDuplicateHash) "Cảnh Báo File Đã Import" else "Xác Nhận Nhập Dữ Liệu",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    if (isPendingDuplicateHash) {
                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    "⚠️ File sao lưu này (Mã Hash đã tồn tại) từng được import vào máy trước đó.",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF92400E)
                                )
                                Text(
                                    "Nếu tiếp tục, bạn có thể chọn Gộp hoặc Ghi đè để cập nhật.",
                                    fontSize = 11.sp,
                                    color = Color(0xFFB45309)
                                )
                            }
                        }
                    }

                    Surface(
                        color = Color(0xFFF8FAFC),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                            Text("Ngày sao lưu: ${backupToImport.exportedDate}", fontSize = 12.sp, color = TextSecondary)
                            Text("Dữ liệu trong file:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("• ${backupToImport.invoices.size} hóa đơn", fontSize = 12.sp, color = TextSecondary)
                            Text("• ${backupToImport.storePartners.size} cửa hàng / đại lý", fontSize = 12.sp, color = TextSecondary)
                            Text("• ${backupToImport.products.size} sản phẩm", fontSize = 12.sp, color = TextSecondary)
                            Text("• ${backupToImport.customers.size} khách hàng", fontSize = 12.sp, color = TextSecondary)
                            Text("Mã Hash: ${backupToImport.fileHash.take(16)}...", fontSize = 10.sp, fontFamily = FontFamily.Monospace, color = TextSecondary)
                        }
                    }

                    Text("Chọn chế độ nhập dữ liệu:", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)

                    // Lựa chọn 1: Gộp dữ liệu (Merge)
                    Surface(
                        color = if (selectedImportMode == ImportMode.MERGE) PrimaryBlue.copy(alpha = 0.08f) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (selectedImportMode == ImportMode.MERGE) PrimaryBlue else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedImportMode = ImportMode.MERGE }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedImportMode == ImportMode.MERGE,
                                onClick = { selectedImportMode = ImportMode.MERGE },
                                colors = RadioButtonDefaults.colors(selectedColor = PrimaryBlue)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("Gộp Dữ Liệu (Khuyên dùng)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Text("Giữ nguyên dữ liệu hiện có và bổ sung thêm các mục mới từ file.", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }

                    // Lựa chọn 2: Ghi đè toàn bộ (Replace all)
                    Surface(
                        color = if (selectedImportMode == ImportMode.REPLACE_ALL) DeleteRed.copy(alpha = 0.08f) else Color.White,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, if (selectedImportMode == ImportMode.REPLACE_ALL) DeleteRed else BorderColor, RoundedCornerShape(8.dp))
                            .clickable { selectedImportMode = ImportMode.REPLACE_ALL }
                    ) {
                        Row(modifier = Modifier.padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedImportMode == ImportMode.REPLACE_ALL,
                                onClick = { selectedImportMode = ImportMode.REPLACE_ALL },
                                colors = RadioButtonDefaults.colors(selectedColor = DeleteRed)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Column {
                                Text("Ghi Đè Toàn Bộ (Thay thế)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = DeleteRed)
                                Text("Xóa sạch toàn bộ dữ liệu trong máy và thay thế bằng file sao lưu.", fontSize = 11.sp, color = TextSecondary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val backup = backupToImport
                        val mode = selectedImportMode
                        pendingImportBackup = null
                        viewModel.applyImport(backup, mode) { success, msg ->
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = if (selectedImportMode == ImportMode.REPLACE_ALL) DeleteRed else PrimaryBlue),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(if (selectedImportMode == ImportMode.REPLACE_ALL) "Ghi Đè Ngay" else "Tiến Hành Nhập")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingImportBackup = null }) {
                    Text("Hủy")
                }
            }
        )
    }

    // Modal Báo Lỗi Import
    val errorMsg = showImportErrorDialog
    if (errorMsg != null) {
        AlertDialog(
            onDismissRequest = { showImportErrorDialog = null },
            title = { Text("Lỗi Đọc File", fontWeight = FontWeight.Bold, color = DeleteRed) },
            text = { Text(errorMsg, fontSize = 13.sp) },
            confirmButton = {
                TextButton(onClick = { showImportErrorDialog = null }) {
                    Text("Đóng")
                }
            }
        )
    }
}
