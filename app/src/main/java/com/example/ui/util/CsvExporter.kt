package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.CallLogEntity
import com.example.data.model.DealEntity
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Exports CRM data as CSV files (UTF-8 with BOM so Excel shows Arabic correctly)
 * and opens the Android share sheet (WhatsApp, Gmail, Drive, ...).
 */
object CsvExporter {

    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US)

    fun exportAndShare(context: Context, deals: List<DealEntity>, callLogs: List<CallLogEntity>) {
        if (deals.isEmpty() && callLogs.isEmpty()) {
            Toast.makeText(context, "لا توجد بيانات للتصدير", Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val dir = File(context.cacheDir, "exports").apply {
                deleteRecursively()
                mkdirs()
            }
            val stamp = SimpleDateFormat("yyyy-MM-dd_HHmm", Locale.US).format(Date())
            val dealsFile = File(dir, "DreamCar_Deals_$stamp.csv").apply { writeCsv(dealsCsv(deals)) }
            val logsFile = File(dir, "DreamCar_Activities_$stamp.csv").apply { writeCsv(activitiesCsv(callLogs)) }

            val authority = "${context.packageName}.fileprovider"
            val uris = arrayListOf<Uri>(
                FileProvider.getUriForFile(context, authority, dealsFile),
                FileProvider.getUriForFile(context, authority, logsFile)
            )
            val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                type = "text/csv"
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris)
                putExtra(Intent.EXTRA_SUBJECT, "Dream Car CRM Export $stamp")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Export (تصدير البيانات)"))
        } catch (e: Exception) {
            Toast.makeText(context, "فشل التصدير: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun dealsCsv(deals: List<DealEntity>): String {
        val header = listOf(
            "ID", "Client (العميل)", "Phone (التليفون)", "Sales Rep (السيلز)", "Stage (المرحلة)",
            "Amount (القيمة)", "Probability %", "Car Model (العربية)", "Car Type", "Down Payment (المقدم)",
            "Loan Amount (التمويل)", "Installment Partner (جهة التقسيط)", "Installment Status (حالة الطلب)",
            "Lead Source (المصدر)", "Qualification", "Interest (الاهتمام)", "Follow-up (المتابعة)",
            "Expected Close", "Lost Reason (سبب الإلغاء)", "Commission Rate", "Commission (العمولة)",
            "Commission Received (استلمت)", "Closure Request (طلب الإغلاق)", "Date", "Notes (ملاحظات)",
            "Created At", "Updated At"
        )
        val rows = deals.map { d ->
            listOf(
                d.id.toString(), d.clientName, d.phone, d.salesRep, d.stage,
                d.amount.toString(), d.probability.toString(), d.carModel, d.carType, d.downPayment.toString(),
                d.loanAmount.toString(), d.installmentPartner, d.installmentStatus,
                d.leadSource, d.qualificationStatus, d.interestLevel, d.followUpDate,
                d.expectedCloseDate, d.lostReason, d.commissionRate.toString(), d.commissionAmount.toString(),
                if (d.isCommissionReceived) "Yes" else "No",
                if (d.closureStatus == "NONE") "" else "${d.closureStatus} ${d.closureRequestedStage}".trim(),
                d.date, d.notes,
                formatTime(d.createdAt), formatTime(d.updatedAt)
            )
        }
        return toCsv(header, rows)
    }

    fun activitiesCsv(logs: List<CallLogEntity>): String {
        val header = listOf(
            "ID", "Client ID", "Client (العميل)", "Sales Rep (السيلز)", "Type (النوع)",
            "Outcome (النتيجة)", "Notes (ملاحظات)", "Month", "Time (الوقت)"
        )
        val rows = logs.map { l ->
            listOf(
                l.id.toString(), l.clientId?.toString().orEmpty(), l.clientName, l.salesRep, l.interactionType,
                l.outcome, l.notes, l.month, formatTime(l.timestamp)
            )
        }
        return toCsv(header, rows)
    }

    private fun formatTime(millis: Long): String = dateTimeFormat.format(Date(millis))

    private fun toCsv(header: List<String>, rows: List<List<String>>): String =
        (listOf(header) + rows).joinToString("\r\n") { row -> row.joinToString(",") { escape(it) } }

    private fun escape(value: String): String {
        // Neutralise spreadsheet formula injection (values starting with =, +, -, @)
        val safe = if (value.isNotEmpty() && value[0] in "=+-@" && value.toDoubleOrNull() == null) "'$value" else value
        return if (safe.any { it == ',' || it == '"' || it == '\n' || it == '\r' }) {
            "\"" + safe.replace("\"", "\"\"") + "\""
        } else {
            safe
        }
    }

    private fun File.writeCsv(content: String) {
        writeText("﻿" + content, Charsets.UTF_8)
    }
}
