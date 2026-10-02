package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.local.entity.CashAdvanceType
import com.example.data.local.entity.QueueStatus
import com.example.data.local.entity.SalaryType

class Converters {
    @TypeConverter
    fun fromSalaryType(value: SalaryType?): String = value?.name ?: SalaryType.PERCENTAGE.name

    @TypeConverter
    fun toSalaryType(value: String?): SalaryType =
        value?.let { runCatching { SalaryType.valueOf(it) }.getOrNull() } ?: SalaryType.PERCENTAGE

    @TypeConverter
    fun fromQueueStatus(value: QueueStatus?): String = value?.name ?: QueueStatus.WAITING.name

    @TypeConverter
    fun toQueueStatus(value: String?): QueueStatus =
        value?.let { runCatching { QueueStatus.valueOf(it) }.getOrNull() } ?: QueueStatus.WAITING

    @TypeConverter
    fun fromCashAdvanceType(value: CashAdvanceType?): String = value?.name ?: CashAdvanceType.KASBON.name

    @TypeConverter
    fun toCashAdvanceType(value: String?): CashAdvanceType =
        value?.let { runCatching { CashAdvanceType.valueOf(it) }.getOrNull() } ?: CashAdvanceType.KASBON
}
