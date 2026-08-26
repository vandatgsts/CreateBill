package com.vandatgsts.thuyetnguyen.ui.settings

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.ui.components.AppTextField
import com.vandatgsts.thuyetnguyen.ui.components.FormSectionHeader
import com.vandatgsts.thuyetnguyen.ui.theme.BackgroundLight
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompanyProfileScreen(
    viewModel: CompanyProfileViewModel,
    onBack: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông Tin Shop / Công Ty", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    viewModel.saveProfile()
                    Toast.makeText(context, "Đã lưu thông tin cửa hàng mặc định thành công!", Toast.LENGTH_SHORT).show()
                    onBack()
                },
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.height(8.dp))
                Text("LƯU CẤU HÌNH MẶC ĐỊNH", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}
