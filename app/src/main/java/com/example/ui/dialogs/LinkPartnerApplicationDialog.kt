package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.DealEntity
import com.example.data.model.InstallmentPartner
import com.example.data.model.InstallmentStatus
import com.example.ui.util.FormattingUtils

/**
 * LinkPartnerApplicationDialog
 *
 * Dialog for linking an existing or new customer to a financing partner (Contact, Drive, Aman, etc.)
 * and updating the application state across: Pending, Approved, Rejected.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkPartnerApplicationDialog(
    initialDeal: DealEntity? = null,
    availableDeals: List<DealEntity> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (
        dealId: Long,
        clientName: String,
        phone: String,
        partner: String,
        status: String,
        carModel: String,
        carPrice: Double,
        downPayment: Double,
        loanAmount: Double,
        notes: String
    ) -> Unit
) {
    var selectedDeal by remember { mutableStateOf(initialDeal) }
    var clientName by remember { mutableStateOf(initialDeal?.clientName ?: "") }
    var phone by remember { mutableStateOf(initialDeal?.phone ?: "") }
    var carModel by remember { mutableStateOf(initialDeal?.carModel ?: "") }
    var carPriceText by remember { mutableStateOf(if ((initialDeal?.amount ?: 0.0) > 0) initialDeal!!.amount.toLong().toString() else "") }
    var downPaymentText by remember { mutableStateOf(if ((initialDeal?.downPayment ?: 0.0) > 0) initialDeal!!.downPayment.toLong().toString() else "") }
    var loanAmountText by remember { mutableStateOf(if ((initialDeal?.loanAmount ?: 0.0) > 0) initialDeal!!.loanAmount.toLong().toString() else "") }
    
    var selectedPartner by remember {
        mutableStateOf(
            if (initialDeal?.installmentPartner.isNullOrBlank()) "كونتاكت - Contact"
            else initialDeal!!.installmentPartner
        )
    }

    var selectedStatus by remember {
        mutableStateOf(
            when (initialDeal?.installmentStatus) {
                InstallmentStatus.APPROVED.name -> "APPROVED"
                InstallmentStatus.REJECTED.name -> "REJECTED"
                else -> "PENDING"
            }
        )
    }

    var notes by remember { mutableStateOf("") }
    var partnerDropdownExpanded by remember { mutableStateOf(false) }
    var dealDropdownExpanded by remember { mutableStateOf(false) }

    val partnersList = listOf(
        "كونتاكت - Contact",
        "درايف - Drive",
        "أمان - Aman",
        "وان فاينانس - One Finance",
        "بداية - Bedaya",
        "تمويل بنكي - Bank"
    )

    // Auto calculate loan amount if car price and down payment are set
    val carPrice = carPriceText.toDoubleOrNull() ?: 0.0
    val downPayment = downPaymentText.toDoubleOrNull() ?: 0.0
    val calculatedLoan = if (loanAmountText.isNotBlank()) {
        loanAmountText.toDoubleOrNull() ?: (carPrice - downPayment).coerceAtLeast(0.0)
    } else {
        (carPrice - downPayment).coerceAtLeast(0.0)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 24.dp)
                .testTag("link_partner_dialog"),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.AccountBalance,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = if (initialDeal == null) "ربط عميل بشريك تقسيط" else "تعديل طلب التقسيط",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Installment Partner Linking & State",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "إغلاق")
                    }
                }

                // Deal / Customer Selection (if not pre-selected)
                if (initialDeal == null && availableDeals.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = if (selectedDeal != null) "${selectedDeal!!.clientName} (${selectedDeal!!.phone})" else "اختر العميل من القائمة...",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("اختر العميل (Select Customer)") },
                            leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                            trailingIcon = { Icon(Icons.Default.ArrowDropDown, contentDescription = null) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { dealDropdownExpanded = true }
                                .testTag("select_deal_dropdown"),
                            shape = RoundedCornerShape(12.dp)
                        )
                        DropdownMenu(
                            expanded = dealDropdownExpanded,
                            onDismissRequest = { dealDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.85f)
                        ) {
                            availableDeals.forEach { deal ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(deal.clientName, fontWeight = FontWeight.Bold)
                                            Text("${deal.phone} • ${deal.carModel}", fontSize = 12.sp, color = Color.Gray)
                                        }
                                    },
                                    onClick = {
                                        selectedDeal = deal
                                        clientName = deal.clientName
                                        phone = deal.phone
                                        carModel = deal.carModel
                                        if (deal.amount > 0) carPriceText = deal.amount.toLong().toString()
                                        if (deal.downPayment > 0) downPaymentText = deal.downPayment.toLong().toString()
                                        if (deal.installmentPartner.isNotBlank()) selectedPartner = deal.installmentPartner
                                        dealDropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Customer Name & Phone
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("اسم العميل *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("partner_client_name"),
                        shape = RoundedCornerShape(12.dp),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("رقم الهاتف *") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("partner_client_phone"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        singleLine = true
                    )
                }

                // Partner Selection
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = selectedPartner,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("شريك التقسيط (Financing Partner) *") },
                        leadingIcon = { Icon(Icons.Default.AccountBalance, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            IconButton(onClick = { partnerDropdownExpanded = true }) {
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { partnerDropdownExpanded = true }
                            .testTag("partner_selector_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    DropdownMenu(
                        expanded = partnerDropdownExpanded,
                        onDismissRequest = { partnerDropdownExpanded = false }
                    ) {
                        partnersList.forEach { partner ->
                            DropdownMenuItem(
                                text = { Text(partner, fontWeight = FontWeight.SemiBold) },
                                onClick = {
                                    selectedPartner = partner
                                    partnerDropdownExpanded = false
                                }
                            )
                        }
                    }
                }

                // Application State Selector (Pending, Approved, Rejected)
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "حالة طلب التقسيط (Application State) *",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Pending
                        FilterChip(
                            selected = selectedStatus == "PENDING",
                            onClick = { selectedStatus = "PENDING" },
                            label = { Text("⏳ قيد المراجعة (Pending)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFF3E0),
                                selectedLabelColor = Color(0xFFE65100)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedStatus == "PENDING",
                                selectedBorderColor = Color(0xFFE65100)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_chip_pending")
                        )

                        // Approved
                        FilterChip(
                            selected = selectedStatus == "APPROVED",
                            onClick = { selectedStatus = "APPROVED" },
                            label = { Text("✅ معتمد (Approved)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF2E7D32)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedStatus == "APPROVED",
                                selectedBorderColor = Color(0xFF2E7D32)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_chip_approved")
                        )

                        // Rejected
                        FilterChip(
                            selected = selectedStatus == "REJECTED",
                            onClick = { selectedStatus = "REJECTED" },
                            label = { Text("❌ مرفوض (Rejected)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = Color(0xFFC62828)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedStatus == "REJECTED",
                                selectedBorderColor = Color(0xFFC62828)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("status_chip_rejected")
                        )
                    }
                }

                // Vehicle Model
                OutlinedTextField(
                    value = carModel,
                    onValueChange = { carModel = it },
                    label = { Text("موديل السيارة (Car Model)") },
                    leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                    placeholder = { Text("مثال: كيا سبورتاج 2024 أو هيونداي إلنترا") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_car_model"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                // Financial Inputs (Price, Down payment, Loan Amount)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = carPriceText,
                        onValueChange = { carPriceText = it },
                        label = { Text("سعر السيارة (EGP)") },
                        leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("partner_car_price"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = downPaymentText,
                        onValueChange = { downPaymentText = it },
                        label = { Text("المقدم (Down Payment)") },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("partner_down_payment"),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }

                // Calculated Financing Amount Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "مبلغ التمويل المطلوب (Loan Amount):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormattingUtils.formatEgp(calculatedLoan),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Approximate Monthly Installment estimate (over 5 years)
                        val estimatedMonthly = if (calculatedLoan > 0) (calculatedLoan * 1.55) / 60 else 0.0
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "القسط التقريبي (5 سنوات):",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "~ ${FormattingUtils.formatEgp(estimatedMonthly)} / شهر",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }

                // Application Notes / Reason / Reference number
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("ملاحظات الطلب أو رقم المعاملة (Notes / Ref No.)") },
                    placeholder = { Text("مثال: تم إرسال كشف الحساب والبطاقة، بانتظار استعلام العمل والـ iScore...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("partner_application_notes"),
                    shape = RoundedCornerShape(12.dp),
                    minLines = 2,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("إلغاء")
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Button(
                        onClick = {
                            val dealId = selectedDeal?.id ?: initialDeal?.id ?: 0L
                            val mappedStatus = when (selectedStatus) {
                                "APPROVED" -> InstallmentStatus.APPROVED.name
                                "REJECTED" -> InstallmentStatus.REJECTED.name
                                else -> InstallmentStatus.PENDING_PAPERS.name
                            }

                            onSave(
                                dealId,
                                clientName.trim(),
                                phone.trim(),
                                selectedPartner,
                                mappedStatus,
                                carModel.trim(),
                                carPrice,
                                downPayment,
                                calculatedLoan,
                                notes.trim()
                            )
                        },
                        enabled = clientName.isNotBlank() && phone.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("save_partner_application_button")
                    ) {
                        Text("حفظ وربط الطلب")
                    }
                }
            }
        }
    }
}
