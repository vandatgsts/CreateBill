package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.vandatgsts.thuyetnguyen.data.model.AppBackupData
import com.vandatgsts.thuyetnguyen.data.model.ImportMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStreamReader
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BackupRepository(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("app_backup_meta_prefs", Context.MODE_PRIVATE)
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    private val invoiceRepo = InvoiceRepository.getInstance(context)
    private val productRepo = ProductRepository.getInstance(context)
    private val storePartnerRepo = StorePartnerRepository.getInstance(context)
    private val customerRepo = CustomerRepository.getInstance(context)
    private val companyProfileRepo = CompanyProfileRepository.getInstance(context)

    fun getImportedHashes(): Set<String> {
        val json = prefs.getString(KEY_IMPORTED_HASHES, null) ?: return emptySet()
        return try {
            val type = object : TypeToken<Set<String>>() {}.type
            gson.fromJson(json, type) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    fun isHashAlreadyImported(hash: String): Boolean {
        if (hash.isBlank()) return false
        return getImportedHashes().contains(hash.lowercase().trim())
    }

    fun recordImportedHash(hash: String) {
        if (hash.isBlank()) return
        val current = getImportedHashes().toMutableSet()
        current.add(hash.lowercase().trim())
        prefs.edit().putString(KEY_IMPORTED_HASHES, gson.toJson(current)).apply()
    }

    fun calculateSha256(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(text.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    suspend fun createBackupData(): Pair<AppBackupData, File> = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(Date(now))
        val fileDateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
        val fileNameDate = fileDateFormat.format(Date(now))

        val baseBackup = AppBackupData(
            backupVersion = 1,
            exportedAt = now,
            exportedDate = formattedDate,
            companyProfile = companyProfileRepo.getProfile(),
            invoices = invoiceRepo.invoices.value,
            products = productRepo.products.value,
            storePartners = storePartnerRepo.stores.value,
            customers = customerRepo.customers.value,
            fileHash = ""
        )

        val rawJson = gson.toJson(baseBackup)
        val computedHash = calculateSha256(rawJson + "_salt_" + now)

        val finalBackup = baseBackup.copy(fileHash = computedHash)
        val finalJson = gson.toJson(finalBackup)

        val backupDir = File(context.cacheDir, "backups").apply { mkdirs() }
        val backupFile = File(backupDir, "TaoHoaDon_Backup_$fileNameDate.json")
        backupFile.writeText(finalJson, Charsets.UTF_8)

        Pair(finalBackup, backupFile)
    }

    fun getShareIntent(backupFile: File): Intent {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            backupFile
        )
        return Intent(Intent.ACTION_SEND).apply {
            type = "application/json"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Sao lưu dữ liệu Tạo Hóa Đơn")
            putExtra(Intent.EXTRA_TEXT, "File sao lưu toàn bộ dữ liệu ứng dụng Tạo Hóa Đơn & Báo Giá.")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }

    suspend fun parseBackupFromUri(uri: Uri): Result<AppBackupData> = withContext(Dispatchers.IO) {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
                ?: return@withContext Result.failure(Exception("Không thể đọc file từ nguồn chỉ định"))

            val jsonContent = InputStreamReader(inputStream, Charsets.UTF_8).use { it.readText() }
            if (jsonContent.isBlank()) {
                return@withContext Result.failure(Exception("File sao lưu rỗng"))
            }

            val parsed: AppBackupData = gson.fromJson(jsonContent, AppBackupData::class.java)
                ?: return@withContext Result.failure(Exception("Định dạng file không hợp lệ hoặc bị hỏng"))

            val effectiveHash = if (parsed.fileHash.isNotBlank()) {
                parsed.fileHash
            } else {
                calculateSha256(jsonContent)
            }

            val completeData = parsed.copy(fileHash = effectiveHash)
            Result.success(completeData)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Lỗi phân tích file sao lưu: ${e.localizedMessage}"))
        }
    }

    suspend fun applyBackupData(backup: AppBackupData, mode: ImportMode): Result<String> = withContext(Dispatchers.IO) {
        try {
            when (mode) {
                ImportMode.REPLACE_ALL -> {
                    invoiceRepo.replaceAll(backup.invoices)
                    productRepo.replaceAll(backup.products)
                    storePartnerRepo.replaceAll(backup.storePartners)
                    customerRepo.replaceAll(backup.customers)
                    if (backup.companyProfile.storeName.isNotBlank() || backup.companyProfile.companyName.isNotBlank()) {
                        companyProfileRepo.saveProfile(backup.companyProfile)
                    }
                }
                ImportMode.MERGE -> {
                    invoiceRepo.mergeAll(backup.invoices)
                    productRepo.mergeAll(backup.products)
                    storePartnerRepo.mergeAll(backup.storePartners)
                    customerRepo.mergeAll(backup.customers)
                    val currentProfile = companyProfileRepo.getProfile()
                    if (currentProfile.storeName.isBlank() && currentProfile.companyName.isBlank()) {
                        companyProfileRepo.saveProfile(backup.companyProfile)
                    }
                }
            }

            recordImportedHash(backup.fileHash)

            val summaryMsg = "Đã nhập thành công ${backup.invoices.size} hóa đơn, ${backup.storePartners.size} cửa hàng, ${backup.products.size} sản phẩm, ${backup.customers.size} khách hàng."
            Result.success(summaryMsg)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(Exception("Lỗi khi nhập dữ liệu: ${e.localizedMessage}"))
        }
    }

    companion object {
        private const val KEY_IMPORTED_HASHES = "key_imported_backup_hashes_set"

        @Volatile
        private var instance: BackupRepository? = null

        fun getInstance(context: Context): BackupRepository {
            return instance ?: synchronized(this) {
                instance ?: BackupRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
