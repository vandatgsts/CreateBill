package com.vandatgsts.thuyetnguyen.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.vandatgsts.thuyetnguyen.data.model.CompanyProfile

class CompanyProfileRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("company_profile_prefs", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun getProfile(): CompanyProfile {
        val json = prefs.getString(KEY_PROFILE, null)
        return if (json != null) {
            try {
                gson.fromJson(json, CompanyProfile::class.java) ?: CompanyProfile()
            } catch (e: Exception) {
                CompanyProfile()
            }
        } else {
            CompanyProfile()
        }
    }

    fun saveProfile(profile: CompanyProfile) {
        val json = gson.toJson(profile)
        prefs.edit().putString(KEY_PROFILE, json).apply()
    }

    companion object {
        private const val KEY_PROFILE = "key_company_profile"

        @Volatile
        private var instance: CompanyProfileRepository? = null

        fun getInstance(context: Context): CompanyProfileRepository {
            return instance ?: synchronized(this) {
                instance ?: CompanyProfileRepository(context.applicationContext).also { instance = it }
            }
        }
    }
}
