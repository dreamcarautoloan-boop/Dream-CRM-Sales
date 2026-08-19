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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.data.model.DealStage
import com.example.data.model.InstallmentPartner
import com.example.data.model.InstallmentStatus
import com.example.data.model.InterestLevel
import com.example.data.model.LeadSource
import com.example.data.model.QualificationStatus
import com.example.ui.theme.CrmAmber
import com.example.ui.theme.CrmBlue
import com.example.ui.theme.CrmEmerald
import com.example.ui.theme.CrmRose
import com.example.ui.util.FormattingUtils
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditDealDialog(
    dealToEdit: DealEntity? = null,
    availableReps: List<String>,
    onDismiss: () -> Unit,
    onSave: (
        id: Long,
        clientName: String,
        phone: String,
        salesRep: String,
        stage: String,
        amount: Double,
        probability: Int,
        expectedCloseDate: String,
        carModel: String,
        carType: String,
        downPayment: Double,
        loanAmount: Double,
        installmentPartner: String,
        installmentStatus: String,
        leadSource: String,
        qualificationStatus: String,
        interestLevel: String,
        followUpDate: String,
        lostReason: String,
        commissionRate: Double,
        commissionAmount: Double,
        isCommissionReceived: Boolean,
        receivedNotes: String,
        date: String,
        notes: String
    ) -> Unit
) {
    val isEditMode = dealToEdit != null

    var clientName by remember { mutableStateOf(dealToEdit?.clientName ?: "") }
    var phone by remember { mutableStateOf(dealToEdit?.phone ?: "") }
    var salesRep by remember { mutableStateOf(dealToEdit?.salesRep ?: availableReps.firstOrNull() ?: "Nada") }
    var stage by remember { mutableStateOf(dealToEdit?.stage ?: DealStage.PROSPECTING.name) }
    var carModel by remember { mutableStateOf(dealToEdit?.carModel ?: "") }
    var carType by remember { mutableStateOf(dealToEdit?.carType ?: "NEW") }

    var amountText by remember { mutableStateOf(if (dealToEdit != null && dealToEdit.amount > 0) dealToEdit.amount.toLong().toString() else "") }
    var probability by remember { mutableIntStateOf(dealToEdit?.probability ?: 10) }
    var expectedCloseDate by remember {
        mutableStateOf(
            dealToEdit?.expectedCloseDate?.ifBlank { null }
                ?: SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(System.currentTimeMillis() + 86400000L * 14))
        )
    }

    var downPaymentText by remember { mutableStateOf(if (dealToEdit != null && dealToEdit.downPayment > 0) dealToEdit.downPayment.toLong().toString() else "") }
    var loanAmountText by remember { mutableStateOf(if (dealToEdit != null && dealToEdit.loanAmount > 0) dealToEdit.loanAmount.toLong().toString() else "") }

    var installmentPartner by remember { mutableStateOf(dealToEdit?.installmentPartner ?: InstallmentPartner.DRIVE.displayName) }
    var installmentStatus by remember { mutableStateOf(dealToEdit?.installmentStatus ?: InstallmentStatus.PENDING_PAPERS.name) }
    var leadSource by remember { mutableStateOf(dealToEdit?.leadSource ?: LeadSource.META_ADS.name) }
    var qualificationStatus by remember { mutableStateOf(dealToEdit?.qualificationStatus ?: QualificationStatus.QUALIFIED.name) }
    var interestLevel by remember { mutableStateOf(dealToEdit?.interestLevel ?: InterestLevel.HOT.name) }
    var followUpDate by remember { mutableStateOf(dealToEdit?.followUpDate ?: "Today 2:00 PM") }
    var lostReason by remember { mutableStateOf(dealToEdit?.lostReason ?: "") }

    var commissionRate by remember { mutableDoubleStateOf(dealToEdit?.commissionRate ?: 0.025) }
    var isCommissionReceived by remember { mutableStateOf(dealToEdit?.isCommissionReceived ?: false) }
    var receivedNotes by remember { mutableStateOf(dealToEdit?.receivedNotes ?: "") }
    var date by remember {
        mutableStateOf(
            dealToEdit?.date?.ifBlank { null }
                ?: SimpleDateFormat("dd/MM/yyyy", Locale.US).format(Date())
        )
    }
    var notes by remember { mutableStateOf(dealToEdit?.notes ?: "") }

    var repDropdownExpanded by remember { mutableStateOf(false) }
    var partnerDropdownExpanded by remember { mutableStateOf(false) }
    var nameError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (isEditMode) "Edit Deal / Lead" else "New Car Deal & Lead",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Dream Auto Loan CRM",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // SECTION 1: Contact Information
                    Text(
                        text = "1. CLIENT & VEHICLE INFORMATION",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = {
                            clientName = it
                            if (it.isNotBlank()) nameError = false
                        },
                        label = { Text("Client Full Name (اسم العميل) *") },
                        leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                        isError = nameError,
                        supportingText = if (nameError) { { Text("Client name is required") } } else null,
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deal_input_client_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = phone,
                            onValueChange = { phone = it },
                            label = { Text("Phone / WhatsApp (رقم الهاتف)") },
                            leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1.2f)
                                .testTag("deal_input_phone")
                        )

                        // Assigned Rep Dropdown
                        ExposedDropdownMenuBox(
                            expanded = repDropdownExpanded,
                            onExpandedChange = { repDropdownExpanded = !repDropdownExpanded },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = salesRep,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Assigned Rep") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = repDropdownExpanded,
                                onDismissRequest = { repDropdownExpanded = false }
                            ) {
                                availableReps.forEach { rep ->
                                    DropdownMenuItem(
                                        text = { Text(rep) },
                                        onClick = {
                                            salesRep = rep
                                            repDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // Car Model & Car Type
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = carModel,
                            onValueChange = { carModel = it },
                            label = { Text("Car Model (نوع السيارة المطلوبة)") },
                            placeholder = { Text("e.g. Kia Sportage 2024") },
                            leadingIcon = { Icon(Icons.Default.DirectionsCar, contentDescription = null) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        )

                        Row(
                            modifier = Modifier.weight(0.9f),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            FilterChip(
                                selected = carType == "NEW",
                                onClick = { carType = "NEW" },
                                label = { Text("New (زيرو)", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                            FilterChip(
                                selected = carType == "USED",
                                onClick = { carType = "USED" },
                                label = { Text("Used (مستعمل)", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // SECTION 2: Deal Stage, Probability & Expected Close Date
                    Text(
                        text = "2. DEAL STAGE & CLOSING FORECAST",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    // Sales Stages Pills
                    Column {
                        Text("Sales Stage (مرحلة مسار البيع):", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DealStage.entries.take(4).forEach { stg ->
                                FilterChip(
                                    selected = stage == stg.name,
                                    onClick = {
                                        stage = stg.name
                                        probability = stg.defaultProbability
                                    },
                                    label = { Text(stg.arabicName, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            DealStage.entries.drop(4).forEach { stg ->
                                FilterChip(
                                    selected = stage == stg.name,
                                    onClick = {
                                        stage = stg.name
                                        probability = stg.defaultProbability
                                    },
                                    label = { Text(stg.arabicName, style = MaterialTheme.typography.labelSmall) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = when (stg) {
                                            DealStage.DONE -> CrmEmerald.copy(alpha = 0.2f)
                                            DealStage.LOST -> CrmRose.copy(alpha = 0.2f)
                                            else -> MaterialTheme.colorScheme.primaryContainer
                                        },
                                        selectedLabelColor = when (stg) {
                                            DealStage.DONE -> CrmEmerald
                                            DealStage.LOST -> CrmRose
                                            else -> MaterialTheme.colorScheme.onPrimaryContainer
                                        }
                                    )
                                )
                            }
                        }
                    }

                    // Probability & Expected Close Date
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Win Probability (احتمالية الإغلاق):",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "$probability%",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = when {
                                        probability >= 70 -> CrmEmerald
                                        probability >= 40 -> CrmBlue
                                        probability > 0 -> CrmAmber
                                        else -> CrmRose
                                    }
                                )
                            }
                            Slider(
                                value = probability.toFloat(),
                                onValueChange = { probability = it.toInt() },
                                valueRange = 0f..100f,
                                steps = 9,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = expectedCloseDate,
                                    onValueChange = { expectedCloseDate = it },
                                    label = { Text("Expected Close Date (تاريخ الإغلاق)") },
                                    leadingIcon = { Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp)) },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                                OutlinedTextField(
                                    value = followUpDate,
                                    onValueChange = { followUpDate = it },
                                    label = { Text("Next Follow-up (موعد المتابعة)") },
                                    placeholder = { Text("Today 4:00 PM") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    // If stage is LOST, prompt for lost reason
                    if (stage == DealStage.LOST.name) {
                        OutlinedTextField(
                            value = lostReason,
                            onValueChange = { lostReason = it },
                            label = { Text("Lost Reason (سبب فقد الصفقة) *") },
                            placeholder = { Text("e.g. رفض ائتماني بنكي / سعر فائدة مرتفع / اشترى من معرض آخر") },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrmRose) },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // SECTION 3: Financials & Installment Financing
                    Text(
                        text = "3. FINANCING & COMMISSION (التمويل والعمولة)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 0.5.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = amountText,
                            onValueChange = { amountText = it },
                            label = { Text("Car Price / Deal Value *") },
                            placeholder = { Text("e.g. 1200000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("deal_input_amount")
                        )

                        OutlinedTextField(
                            value = downPaymentText,
                            onValueChange = { downPaymentText = it },
                            label = { Text("Down Payment (المقدم)") },
                            placeholder = { Text("e.g. 350000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Installment Partner & Status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = partnerDropdownExpanded,
                            onExpandedChange = { partnerDropdownExpanded = !partnerDropdownExpanded },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            OutlinedTextField(
                                value = installmentPartner,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Installment Partner (جهة التقسيط)") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = partnerDropdownExpanded) },
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = partnerDropdownExpanded,
                                onDismissRequest = { partnerDropdownExpanded = false }
                            ) {
                                InstallmentPartner.entries.forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text(p.displayName) },
                                        onClick = {
                                            installmentPartner = p.displayName
                                            partnerDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = loanAmountText,
                            onValueChange = { loanAmountText = it },
                            label = { Text("Loan (مبلغ التمويل)") },
                            placeholder = { Text("e.g. 850000") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Commission status card
                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    val calculatedComm = parsedAmount * commissionRate
                    val weightedVal = parsedAmount * (probability / 100.0)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Brokerage Comm (2.5%): ${FormattingUtils.formatCurrency(calculatedComm)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Weighted Forecast: ${FormattingUtils.formatCompact(weightedVal)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Commission Received (تم تحصيل العمولة):",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Switch(
                                    checked = isCommissionReceived,
                                    onCheckedChange = { isCommissionReceived = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = CrmEmerald)
                                )
                            }

                            if (isCommissionReceived) {
                                OutlinedTextField(
                                    value = receivedNotes,
                                    onValueChange = { receivedNotes = it },
                                    label = { Text("Payment / Clearance Details (تفاصيل التحصيل)") },
                                    placeholder = { Text("e.g. Paid in full via bank wire") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }

                    // Notes
                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Notes & Client Requirements (ملاحظات العميل)") },
                        placeholder = { Text("e.g. تفاصيل الاتصال، شروط التقسيط، الأوراق المطلوبة...") },
                        maxLines = 3,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("deal_input_notes")
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isBlank()) {
                                nameError = true
                                return@Button
                            }
                            val finalAmount = amountText.toDoubleOrNull() ?: 0.0
                            val finalDown = downPaymentText.toDoubleOrNull() ?: 0.0
                            val finalLoan = loanAmountText.toDoubleOrNull() ?: (finalAmount - finalDown).coerceAtLeast(0.0)
                            val finalComm = finalAmount * commissionRate

                            onSave(
                                dealToEdit?.id ?: 0L,
                                clientName,
                                phone,
                                salesRep,
                                stage,
                                finalAmount,
                                probability,
                                expectedCloseDate,
                                carModel,
                                carType,
                                finalDown,
                                finalLoan,
                                installmentPartner,
                                installmentStatus,
                                leadSource,
                                qualificationStatus,
                                interestLevel,
                                followUpDate,
                                lostReason,
                                commissionRate,
                                finalComm,
                                isCommissionReceived,
                                receivedNotes,
                                date,
                                notes
                            )
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("deal_save_button")
                    ) {
                        Text(if (isEditMode) "Save Changes" else "Create Deal")
                    }
                }
            }
        }
    }
}
