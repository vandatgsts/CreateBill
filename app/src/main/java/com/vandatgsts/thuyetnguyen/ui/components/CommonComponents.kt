package com.vandatgsts.thuyetnguyen.ui.components

import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.vandatgsts.thuyetnguyen.generator.FormatHelper
import com.vandatgsts.thuyetnguyen.ui.theme.BorderColor
import com.vandatgsts.thuyetnguyen.ui.theme.PrimaryBlue
import com.vandatgsts.thuyetnguyen.ui.theme.TextPrimary
import com.vandatgsts.thuyetnguyen.ui.theme.TextSecondary
import java.util.Calendar

@Composable
fun FormSectionHeader(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = PrimaryBlue,
        modifier = modifier.padding(vertical = 6.dp)
    )
}

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    prefix: @Composable (() -> Unit)? = null,
    suffix: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = if (placeholder.isNotBlank()) { { Text(placeholder, fontSize = 13.sp, color = TextSecondary) } } else null,
        singleLine = singleLine,
        maxLines = maxLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        prefix = prefix,
        suffix = suffix,
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun CurrencyField(
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    allowNegative: Boolean = false
) {
    var rawText by remember(value) {
        mutableStateOf(
            if (value == 0.0) ""
            else if (value < 0) "-${FormatHelper.formatMoney(-value)}"
            else FormatHelper.formatMoney(value)
        )
    }

    OutlinedTextField(
        value = rawText,
        onValueChange = { input ->
            val isNegative = allowNegative && input.startsWith("-")
            val clean = input.replace(".", "").replace(",", "").replace("-", "").filter { it.isDigit() }
            if (clean.isEmpty()) {
                rawText = if (isNegative) "-" else ""
                onValueChange(0.0)
            } else {
                val absNum = clean.toDoubleOrNull() ?: 0.0
                val finalNum = if (isNegative) -absNum else absNum
                rawText = if (isNegative) "-${FormatHelper.formatMoney(absNum)}" else FormatHelper.formatMoney(absNum)
                onValueChange(finalNum)
            }
        },
        label = { Text(label, fontSize = 13.sp) },
        suffix = { Text("đ", fontSize = 13.sp, color = TextSecondary) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = if (allowNegative) KeyboardType.Text else KeyboardType.Number),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun NumberField(
    value: Double,
    onValueChange: (Double) -> Unit,
    label: String,
    modifier: Modifier = Modifier
) {
    var rawText by remember(value) {
        mutableStateOf(FormatHelper.formatQuantity(value))
    }

    OutlinedTextField(
        value = rawText,
        onValueChange = { input ->
            rawText = input
            val num = input.toDoubleOrNull() ?: 0.0
            onValueChange(num)
        },
        label = { Text(label, fontSize = 13.sp) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary
        ),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun DatePickerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    placeholder: String = "dd/MM/yyyy"
) {
    val context = LocalContext.current
    val calendar = Calendar.getInstance()

    // Parse existing date if available (e.g. 29/7/2026 or 29/07/2026 or 29-7-2026)
    val cleaned = value.trim().trimEnd('.')
    val parts = cleaned.split("/", "-")
    val initialDay = parts.getOrNull(0)?.toIntOrNull() ?: calendar.get(Calendar.DAY_OF_MONTH)
    val initialMonth = (parts.getOrNull(1)?.toIntOrNull()?.minus(1)) ?: calendar.get(Calendar.MONTH)
    val initialYear = parts.getOrNull(2)?.toIntOrNull() ?: calendar.get(Calendar.YEAR)

    val datePickerDialog = remember(context, value) {
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val formatted = String.format("%d/%d/%d", dayOfMonth, month + 1, year)
                onValueChange(formatted)
            },
            initialYear,
            initialMonth,
            initialDay
        )
    }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 13.sp) },
        placeholder = { Text(placeholder, fontSize = 13.sp, color = TextSecondary) },
        singleLine = true,
        readOnly = true,
        trailingIcon = {
            IconButton(onClick = { datePickerDialog.show() }) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Chọn ngày",
                    tint = PrimaryBlue,
                    modifier = Modifier.size(18.dp)
                )
            }
        },
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PrimaryBlue,
            unfocusedBorderColor = BorderColor,
            focusedLabelColor = PrimaryBlue,
            unfocusedLabelColor = TextSecondary,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        modifier = modifier
            .fillMaxWidth()
            .clickable { datePickerDialog.show() }
    )
}
