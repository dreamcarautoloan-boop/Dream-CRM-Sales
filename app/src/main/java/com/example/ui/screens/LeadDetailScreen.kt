package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CallLogEntity
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.InterestLevel
import com.example.data.model.QualificationStatus
import com.example.data.model.UserRole
import com.example.ui.CrmUiState
import com.example.ui.CrmViewModel
import com.example.ui.theme.CrmAmber
import com.example.ui.theme.CrmAmberContainer
import com.example.ui.theme.CrmBlue
import com.example.ui.theme.CrmBlueContainer
import com.example.ui.theme.CrmEmerald
import com.example.ui.theme.CrmEmeraldContainer
import com.example.ui.theme.CrmPurple
import com.example.ui.theme.CrmPurpleContainer
import com.example.ui.theme.CrmRose
import com.example.ui.theme.CrmRoseContainer
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeadDetailScreen(
    deal: DealEntity,
    state: CrmUiState,
    viewModel: CrmViewModel,
    onBack: () -> Unit,
    onEditDeal: (DealEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    var showAddNoteSection by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var interactionType by remember { mutableStateOf("Phone Call") }
    var outcomeText by remember { mutableStateOf("Interested - Follow Up Scheduled") }

    var showReassignDialog by remember { mutableStateOf(false) }
    var selectedNewRep by remember { mutableStateOf(deal.salesRep) }

    val currencyFormatter = remember {
        NumberFormat.getNumberInstance(Locale.US).apply { maximumFractionDigits = 0 }
    }

    // Filter relevant call logs for this client
    val clientLogs = remember(state.callLogs, deal.clientName, deal.phone) {
        state.callLogs.filter {
            it.clientName.equals(deal.clientName, ignoreCase = true) ||
                (deal.id > 0 && it.clientId == deal.id)
        }.sortedByDescending { it.timestamp }
    }

    val teamReps = listOf("Mahmoud", "Nahla", "Esraa", "Nada", "Alaa")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = deal.clientName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        Text(
                            text = "Assigned Rep: ${deal.salesRep} • ${deal.leadSource}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("lead_detail_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to CRM Pipeline"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEditDeal(deal) }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Lead Details",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // 1. Header Lead Badges & Quick Action Bar
            LeadHeaderBanner(
                deal = deal,
                onCall = { openPhoneDialer(context, deal.phone) },
                onWhatsApp = { openWhatsApp(context, deal.phone, deal.clientName, deal.carModel) },
                onCopy = {
                    clipboardManager.setText(AnnotatedString(deal.phone))
                    Toast.makeText(context, "Phone number copied: ${deal.phone}", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Interactive Stage Progression Stepper
            StagePipelineCard(
                currentStage = deal.stage,
                onStageSelected = { newStage ->
                    viewModel.updateDealStage(deal.id, newStage)
                    Toast.makeText(context, "Stage updated to $newStage", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Vehicle & Financing Requirements Summary Card
            VehicleAndFinancingCard(
                deal = deal,
                currencyFormatter = currencyFormatter,
                onStatusChange = { newStatus ->
                    viewModel.updateInstallmentStatus(deal.id, newStatus)
                    Toast.makeText(context, "Installment status updated to $newStatus", Toast.LENGTH_SHORT).show()
                }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 4. Contact & Location Information Card
            ContactInformationCard(
                deal = deal,
                onCall = { openPhoneDialer(context, deal.phone) },
                onWhatsApp = { openWhatsApp(context, deal.phone, deal.clientName, deal.carModel) },
                onReassign = {
                    if (state.permissions.canReassignDeals) {
                        showReassignDialog = true
                    } else {
                        Toast.makeText(context, "Reassignment is restricted to Team Leader (مروان) and Manager (علي)", Toast.LENGTH_SHORT).show()
                    }
                },
                canReassign = state.permissions.canReassignDeals
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 5. Interaction Timeline & Historical Notes Card
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "HISTORICAL NOTES & TIMELINE",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Button(
                            onClick = { showAddNoteSection = !showAddNoteSection },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (showAddNoteSection) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.primary,
                                contentColor = if (showAddNoteSection) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(
                                imageVector = if (showAddNoteSection) Icons.Default.CheckCircle else Icons.Default.Add,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (showAddNoteSection) "Cancel" else "Add Note",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Inline Add Note Section
                    AnimatedVisibility(visible = showAddNoteSection) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 12.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "Log New Activity (تسجيل ملاحظة ومتابعة):",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Interaction Type chips
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                listOf("Phone Call", "WhatsApp", "Showroom Meeting", "Bank Follow-up").forEach { type ->
                                    FilterChip(
                                        selected = interactionType == type,
                                        onClick = { interactionType = type },
                                        label = { Text(type, style = MaterialTheme.typography.labelSmall) }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = noteText,
                                onValueChange = { noteText = it },
                                label = { Text("Interaction Details / Notes (تفاصيل المكالمة والملاحظات)") },
                                placeholder = { Text("e.g. Sent Kia Sportage 2024 price quote with 30% down payment plan via Aman...") },
                                minLines = 2,
                                maxLines = 4,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = outcomeText,
                                onValueChange = { outcomeText = it },
                                label = { Text("Call Outcome (النتيجة)") },
                                singleLine = true,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Button(
                                onClick = {
                                    if (noteText.isNotBlank()) {
                                        viewModel.addCallLog(
                                            clientId = deal.id,
                                            clientName = deal.clientName,
                                            salesRep = deal.salesRep,
                                            interactionType = interactionType,
                                            outcome = outcomeText,
                                            notes = noteText
                                        )
                                        noteText = ""
                                        showAddNoteSection = false
                                        Toast.makeText(context, "Note added to timeline successfully", Toast.LENGTH_SHORT).show()
                                    }
                                },
                                enabled = noteText.isNotBlank(),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Save Note to Log")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Initial Creation Deal Note
                    if (deal.notes.isNotBlank()) {
                        TimelineItemView(
                            title = "Initial Lead Note / Requirements",
                            subTitle = "Lead Source: ${deal.leadSource}",
                            notes = deal.notes,
                            date = deal.date.ifBlank { "Initial Creation" },
                            tagColor = CrmBlue,
                            tagContainer = CrmBlueContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Lost Reason Note if applicable
                    if (deal.stage == DealStage.LOST.name && deal.lostReason.isNotBlank()) {
                        TimelineItemView(
                            title = "Lost Deal Reason (سبب فقدان العميل)",
                            subTitle = if (deal.isLostRecycled) "Recycled Lead" else "Closed as Lost",
                            notes = deal.lostReason,
                            date = "Cancellation Record",
                            tagColor = CrmRose,
                            tagContainer = CrmRoseContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Call Logs List
                    if (clientLogs.isEmpty() && deal.notes.isBlank()) {
                        Text(
                            text = "No interaction history logged yet. Use 'Add Note' to record call details.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 12.dp)
                        )
                    } else {
                        clientLogs.forEach { log ->
                            val dateStr = remember(log.timestamp) {
                                SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(log.timestamp))
                            }
                            TimelineItemView(
                                title = "${log.interactionType} - ${log.outcome}",
                                subTitle = "By Rep: ${log.salesRep}",
                                notes = log.notes,
                                date = dateStr,
                                tagColor = CrmEmerald,
                                tagContainer = CrmEmeraldContainer
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Reassignment Dialog
    if (showReassignDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showReassignDialog = false },
            title = { Text("Reassign Lead (إعادة تخصيص السيلز)", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Select a Sales Representative for ${deal.clientName}:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    teamReps.forEach { rep ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { selectedNewRep = rep }
                                .padding(vertical = 8.dp, horizontal = 4.dp)
                        ) {
                            androidx.compose.material3.RadioButton(
                                selected = selectedNewRep == rep,
                                onClick = { selectedNewRep = rep }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = rep,
                                fontWeight = if (selectedNewRep == rep) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedNewRep == rep) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                            )
                            if (deal.salesRep == rep) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("(Current)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.reassignDeal(deal.id, selectedNewRep)
                        showReassignDialog = false
                        Toast.makeText(context, "Lead reassigned to $selectedNewRep", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text("Confirm Reassignment")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReassignDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun LeadHeaderBanner(
    deal: DealEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.clientName,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = deal.phone,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // Qualification & Interest Badges
                Column(horizontalAlignment = Alignment.End) {
                    StatusBadge(
                        text = deal.qualificationStatus,
                        color = when (deal.qualificationStatus) {
                            QualificationStatus.QUALIFIED.name -> CrmEmerald
                            QualificationStatus.PENDING.name -> CrmAmber
                            else -> CrmRose
                        },
                        containerColor = when (deal.qualificationStatus) {
                            QualificationStatus.QUALIFIED.name -> CrmEmeraldContainer
                            QualificationStatus.PENDING.name -> CrmAmberContainer
                            else -> CrmRoseContainer
                        }
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusBadge(
                        text = "Interest: ${deal.interestLevel}",
                        color = when (deal.interestLevel) {
                            InterestLevel.HOT.name -> CrmRose
                            InterestLevel.WARM.name -> CrmAmber
                            else -> MaterialTheme.colorScheme.outline
                        },
                        containerColor = when (deal.interestLevel) {
                            InterestLevel.HOT.name -> CrmRoseContainer
                            InterestLevel.WARM.name -> CrmAmberContainer
                            else -> MaterialTheme.colorScheme.surfaceVariant
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Call, WhatsApp, Copy
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onCall,
                    colors = ButtonDefaults.buttonColors(containerColor = CrmEmerald),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Call Phone", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onWhatsApp,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF25D366)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("WhatsApp", color = Color.White, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onCopy,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy Phone", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun StagePipelineCard(
    currentStage: String,
    onStageSelected: (String) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.SwapHoriz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CURRENT PIPELINE STAGE",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = currentStage,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.ExtraBold,
                    color = when (currentStage) {
                        DealStage.DONE.name -> CrmEmerald
                        DealStage.LOST.name -> CrmRose
                        else -> MaterialTheme.colorScheme.primary
                    }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stage Flow Chips
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DealStage.values().forEach { stage ->
                    val isSelected = stage.name.equals(currentStage, ignoreCase = true)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onStageSelected(stage.name) },
                        label = {
                            Text(
                                text = "${stage.displayName} (${stage.arabicName})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = when (stage) {
                                DealStage.DONE -> CrmEmeraldContainer
                                DealStage.LOST -> CrmRoseContainer
                                else -> MaterialTheme.colorScheme.primaryContainer
                            },
                            selectedLabelColor = when (stage) {
                                DealStage.DONE -> CrmEmerald
                                DealStage.LOST -> CrmRose
                                else -> MaterialTheme.colorScheme.onPrimaryContainer
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleAndFinancingCard(
    deal: DealEntity,
    currencyFormatter: NumberFormat,
    onStatusChange: (String) -> Unit = {}
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "VEHICLE & FINANCING REQUIREMENTS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Car Model & Condition
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Interested Car Model",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deal.carModel.ifBlank { "Not specified" },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                StatusBadge(
                    text = deal.carType,
                    color = CrmBlue,
                    containerColor = CrmBlueContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Pricing Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FinancingMetricColumn(
                    label = "Total Car Price",
                    value = "${currencyFormatter.format(deal.amount)} EGP",
                    color = MaterialTheme.colorScheme.onSurface
                )
                FinancingMetricColumn(
                    label = "Down Payment Available",
                    value = "${currencyFormatter.format(deal.downPayment)} EGP",
                    color = CrmEmerald
                )
                FinancingMetricColumn(
                    label = "Loan / Financing",
                    value = "${currencyFormatter.format(deal.loanAmount)} EGP",
                    color = CrmPurple
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))

            // Installment Partner & Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Installment Partner",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deal.installmentPartner.ifBlank { "Direct Finance" },
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Financing Status",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = deal.installmentStatus,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = when (deal.installmentStatus) {
                            "APPROVED" -> CrmEmerald
                            "REJECTED" -> CrmRose
                            else -> CrmAmber
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick State Switcher for Installment Application
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedButton(
                    onClick = { onStatusChange("PENDING_PAPERS") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (deal.installmentStatus in listOf("PENDING_PAPERS", "PENDING", "SUBMITTED")) Color(0xFFFFF3E0) else Color.Transparent,
                        contentColor = Color(0xFFE65100)
                    )
                ) {
                    Text("⏳ Pending", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onStatusChange("APPROVED") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF2E7D32),
                        contentColor = Color.White
                    )
                ) {
                    Text("✅ Approve", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { onStatusChange("REJECTED") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (deal.installmentStatus in listOf("REJECTED", "CLIENT_CANCELLED")) Color(0xFFFFEBEE) else Color.Transparent,
                        contentColor = Color(0xFFC62828)
                    )
                ) {
                    Text("❌ Reject", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun ContactInformationCard(
    deal: DealEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onReassign: () -> Unit,
    canReassign: Boolean
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "CONTACT & LEAD DETAILS",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            InfoRow(label = "Lead Source", value = deal.leadSource)
            InfoRow(label = "Primary Phone", value = deal.phone)
            InfoRow(label = "Assigned Sales Rep", value = deal.salesRep)
            InfoRow(label = "Expected Close Date", value = deal.expectedCloseDate.ifBlank { "Not set" })
            InfoRow(label = "Follow-Up Reminder", value = deal.followUpDate.ifBlank { "No reminder set" })

            if (canReassign) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onReassign,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Reassign to Another Sales Rep (تحويل العميل لسيلز آخر)")
                }
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun FinancingMetricColumn(label: String, value: String, color: Color) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun StatusBadge(text: String, color: Color, containerColor: Color) {
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = color,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun TimelineItemView(
    title: String,
    subTitle: String,
    notes: String,
    date: String,
    tagColor: Color,
    tagContainer: Color
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .padding(top = 4.dp)
                .size(10.dp)
                .clip(CircleShape)
                .background(tagColor)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = subTitle,
                style = MaterialTheme.typography.labelSmall,
                color = tagColor,
                fontWeight = FontWeight.SemiBold
            )
            if (notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }
    }
}

private fun openPhoneDialer(context: Context, phone: String) {
    try {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${phone.trim()}"))
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "Unable to launch dialer", Toast.LENGTH_SHORT).show()
    }
}

private fun openWhatsApp(context: Context, phone: String, clientName: String, carModel: String) {
    try {
        var cleanPhone = phone.trim().replace(" ", "").replace("-", "")
        if (cleanPhone.startsWith("0")) {
            cleanPhone = "20" + cleanPhone.substring(1)
        }
        val carMention = if (carModel.isNotBlank()) " بخصوص سيارة $carModel" else ""
        val message = "السلام عليكم أ/ $clientName، معاك من شركة دريم كار لتقسيط السيارات$carMention. تشرفنا باستفساركم وحابين نوضح لحضرتك كافة عروض التقسيط المتاحة."
        val uri = Uri.parse("https://api.whatsapp.com/send?phone=$cleanPhone&text=${Uri.encode(message)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        context.startActivity(intent)
    } catch (e: Exception) {
        Toast.makeText(context, "WhatsApp is not installed", Toast.LENGTH_SHORT).show()
    }
}
