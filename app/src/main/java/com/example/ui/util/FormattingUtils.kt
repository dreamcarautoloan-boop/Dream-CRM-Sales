package com.example.ui.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import java.text.NumberFormat
import java.util.Locale

object FormattingUtils {
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US).apply {
        maximumFractionDigits = 0
    }

    fun formatCurrency(amount: Double, symbol: String = "£"): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())
        return "$symbol$formatted"
    }

    fun formatCompact(amount: Double, symbol: String = "£"): String {
        return when {
            amount >= 1_000_000 -> String.format(Locale.US, "%s%.2fM", symbol, amount / 1_000_000.0)
            amount >= 1_000 -> String.format(Locale.US, "%s%.1fK", symbol, amount / 1_000.0)
            else -> "$symbol${amount.toLong()}"
        }
    }

    fun formatPercent(rate: Double): String {
        return String.format(Locale.US, "%.2f%%", rate * 100)
    }

    fun formatEgp(amount: Double): String {
        val formatted = NumberFormat.getNumberInstance(Locale.US).format(amount.toLong())
        return "$formatted EGP"
    }

    fun dialPhoneNumber(context: Context, phone: String) {
        if (phone.isBlank()) {
            Toast.makeText(context, "No phone number provided", Toast.LENGTH_SHORT).show()
            return
        }
        val cleanPhone = phone.replace("[^0-9+]".toRegex(), "")
        try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$cleanPhone")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open dialer", Toast.LENGTH_SHORT).show()
        }
    }

    fun openWhatsApp(context: Context, phone: String, clientName: String = "") {
        if (phone.isBlank()) {
            Toast.makeText(context, "No phone number provided", Toast.LENGTH_SHORT).show()
            return
        }
        var cleanPhone = phone.replace("[^0-9]".toRegex(), "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "2$cleanPhone" // Egypt country code default for 010/011/012
        }
        val message = if (clientName.isNotBlank()) "Hello $clientName," else "Hello,"
        val url = "https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}"
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(url)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(context, "Could not open WhatsApp", Toast.LENGTH_SHORT).show()
        }
    }
}
