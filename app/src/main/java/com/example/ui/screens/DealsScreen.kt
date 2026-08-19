package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.ViewKanban
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.UserRole
import com.example.ui.CrmUiState
import com.example.ui.CrmViewModel
import com.example.ui.dialogs.ReassignDealDialog
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
import com.example.ui.util.FormattingUtils

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DealsScreen(
    state: CrmUiState,
    viewModel: CrmViewModel,
    onAddNewDeal: () -> Unit,
    onEditDeal: (DealEntity) -> Unit,
    onLogInteraction: (DealEntity) -> Unit,
    onSelectDeal: (DealEntity) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var dealToDelete by remember { mutableStateOf<DealEntity?>(null) }
    var dealToReassign by remember { mutableStateOf<DealEntity?>(null) }
    var isRecyclingMode by remember { mutableStateOf(false) }
    var markLostDeal by remember { mutableStateOf<DealEntity?>(null) }
    var lostReasonInput by remember { mutableStateOf("") }

    // 0: Pipeline Kanban, 1: List View, 2: Today's Follow-ups, 3: Lost Deals Recycle
    var activeSubTab by remember { mutableIntStateOf(0) }

    val availableRepNames = state.targets.map { it.name }.ifEmpty { listOf("Nada", "Esraa", "Nahla", "Alaa", "Marwan", "Kholoud") }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 90.dp, top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search & Sub-tabs Header
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        placeholder = {
                            Text(
                                "Search client, phone, car model, rep...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        trailingIcon = {
                            if (state.searchQuery.isNotEmpty()) {
                                IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Clear search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(50.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                            unfocusedBorderColor = Color.Transparent,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_deals_input")
                    )

                    // View Modes Tab Row
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            val tabs = listOf(
                                "Pipeline" to Icons.Default.ViewKanban,
                                "All Deals (${state.filteredDeals.size})" to Icons.Default.ViewList,
                                "Follow-ups (${state.todayFollowUps.size})" to Icons.Default.CalendarToday,
                                "Recycle (${state.lostDeals.size})" to Icons.Default.Refresh
                            )
                            tabs.forEachIndexed { index, (title, icon) ->
                                val selected = activeSubTab == index
                                Surface(
                                    color = if (selected) MaterialTheme.colorScheme.surface else Color.Transparent,
                                    shape = RoundedCornerShape(10.dp),
                                    border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)) else null,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { activeSubTab = index }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            icon,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp),
                                            tint = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Summary Metric Cards
            item {
                PipelineSummaryHeader(state)
            }

            // Filter Chips - Stages & Installment Partners & Reps
            item {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedStageFilter == null,
                                onClick = { viewModel.setStageFilter(null) },
                                label = { Text("All Stages (${state.deals.size})", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(DealStage.entries.toTypedArray()) { stg ->
                            val count = state.deals.count { it.stage.equals(stg.name, ignoreCase = true) }
                            FilterChip(
                                selected = state.selectedStageFilter?.equals(stg.name, ignoreCase = true) == true,
                                onClick = { viewModel.setStageFilter(stg.name) },
                                label = { Text("${stg.arabicName} ($count)", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }

                    // Installment Partner and Rep Filters
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        item {
                            FilterChip(
                                selected = state.selectedPartnerFilter == null,
                                onClick = { viewModel.setPartnerFilter(null) },
                                label = { Text("All Partners", style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(listOf("Drive", "Contact", "Aman", "One Finance", "Bedaya", "Bank")) { partner ->
                            FilterChip(
                                selected = state.selectedPartnerFilter?.contains(partner, ignoreCase = true) == true,
                                onClick = { viewModel.setPartnerFilter(if (state.selectedPartnerFilter == partner) null else partner) },
                                label = { Text(partner, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                        items(state.targets) { target ->
                            FilterChip(
                                selected = state.selectedRepFilter?.equals(target.name, ignoreCase = true) == true,
                                onClick = { viewModel.setRepFilter(target.name) },
                                label = { Text(target.name, style = MaterialTheme.typography.labelSmall) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            )
                        }
                    }
                }
            }

            // Tab Content
            when (activeSubTab) {
                0 -> {
                    // Pipeline Kanban Stages
                    item {
                        PipelineKanbanView(
                            state = state,
                            onCall = { phone -> FormattingUtils.dialPhoneNumber(context, phone) },
                            onWhatsApp = { phone, name -> FormattingUtils.openWhatsApp(context, phone, name) },
                            onEdit = onEditDeal,
                            onSelectDeal = onSelectDeal,
                            onAdvanceStage = { deal, nextStage -> viewModel.updateDealStage(deal.id, nextStage.name, nextStage.defaultProbability) },
                            onMarkLost = { deal ->
                                markLostDeal = deal
                                lostReasonInput = ""
                            },
                            onReassign = { deal ->
                                dealToReassign = deal
                                isRecyclingMode = false
                            }
                        )
                    }
                }
                1 -> {
                    // Full Deals List View
                    if (state.filteredDeals.isEmpty()) {
                        item {
                            EmptyDealsState(onAddNewDeal = onAddNewDeal)
                        }
                    } else {
                        items(
                            items = state.filteredDeals,
                            key = { it.id }
                        ) { deal ->
                            DealCard(
                                deal = deal,
                                currentRole = state.currentRole,
                                onCall = { FormattingUtils.dialPhoneNumber(context, deal.phone) },
                                onWhatsApp = { FormattingUtils.openWhatsApp(context, deal.phone, deal.clientName) },
                                onLog = { onLogInteraction(deal) },
                                onEdit = { onEditDeal(deal) },
                                onSelect = { onSelectDeal(deal) },
                                onDelete = { dealToDelete = deal },
                                onStageChange = { newStage -> viewModel.updateDealStage(deal.id, newStage) },
                                onTogglePaid = { viewModel.toggleCommissionReceived(deal) },
                                onReassign = {
                                    dealToReassign = deal
                                    isRecyclingMode = false
                                },
                                onMarkLost = {
                                    markLostDeal = deal
                                    lostReasonInput = ""
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                2 -> {
                    // Today's Follow-ups Tab
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "TODAY'S SCHEDULED FOLLOW-UPS (مواعيد المتابعة اليومية)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "One-tap direct WhatsApp reminders & call actions for active deals",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (state.todayFollowUps.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CrmEmerald, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("All Follow-ups Completed!", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("No pending follow-up calls or messages for today.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(
                            items = state.todayFollowUps,
                            key = { it.id }
                        ) { deal ->
                            DealCard(
                                deal = deal,
                                currentRole = state.currentRole,
                                onCall = { FormattingUtils.dialPhoneNumber(context, deal.phone) },
                                onWhatsApp = { FormattingUtils.openWhatsApp(context, deal.phone, deal.clientName) },
                                onLog = { onLogInteraction(deal) },
                                onEdit = { onEditDeal(deal) },
                                onSelect = { onSelectDeal(deal) },
                                onDelete = { dealToDelete = deal },
                                onStageChange = { newStage -> viewModel.updateDealStage(deal.id, newStage) },
                                onTogglePaid = { viewModel.toggleCommissionReceived(deal) },
                                onReassign = {
                                    dealToReassign = deal
                                    isRecyclingMode = false
                                },
                                onMarkLost = {
                                    markLostDeal = deal
                                    lostReasonInput = ""
                                },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
                3 -> {
                    // Lost Deals Recycle Sheet
                    item {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Text(
                                text = "LOST DEALS RECYCLING SHEET (شيت الفرص الضائعة)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CrmRose,
                                letterSpacing = 0.5.sp
                            )
                            Text(
                                text = "Review lost deals & reasons, and 1-tap recycle & reassign to another sales rep",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (state.lostDeals.isEmpty()) {
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(24.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text("No Lost Deals", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                    Text("Great job! No cancelled or lost opportunities recorded.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    } else {
                        items(
                            items = state.lostDeals,
                            key = { it.id }
                        ) { deal ->
                            LostDealCard(
                                deal = deal,
                                onRecycle = {
                                    dealToReassign = deal
                                    isRecyclingMode = true
                                },
                                onCall = { FormattingUtils.dialPhoneNumber(context, deal.phone) },
                                onWhatsApp = { FormattingUtils.openWhatsApp(context, deal.phone, deal.clientName) },
                                onEdit = { onEditDeal(deal) },
                                modifier = Modifier.padding(horizontal = 16.dp)
                            )
                        }
                    }
                }
            }
        }

        // FAB
        FloatingActionButton(
            onClick = onAddNewDeal,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            shape = RoundedCornerShape(16.dp),
            elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .size(56.dp)
                .testTag("add_deal_fab")
        ) {
            Icon(Icons.Default.Add, contentDescription = "Add Deal", modifier = Modifier.size(26.dp))
        }

        // Delete Confirmation Dialog
        dealToDelete?.let { deal ->
            AlertDialog(
                onDismissRequest = { dealToDelete = null },
                title = { Text("Delete Deal") },
                text = { Text("Are you sure you want to delete the deal for \"${deal.clientName}\"?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteDeal(deal)
                            dealToDelete = null
                        }
                    ) {
                        Text("Delete", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { dealToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Reassign / Recycle Dialog
        dealToReassign?.let { deal ->
            ReassignDealDialog(
                deal = deal,
                availableReps = availableRepNames,
                isRecyclingLost = isRecyclingMode,
                onDismiss = {
                    dealToReassign = null
                    isRecyclingMode = false
                },
                onConfirmReassign = { newRep ->
                    if (isRecyclingMode) {
                        viewModel.recycleLostDeal(deal, newRep)
                    } else {
                        viewModel.reassignDeal(deal.id, newRep)
                    }
                    dealToReassign = null
                    isRecyclingMode = false
                }
            )
        }

        // Mark as Lost Reason Dialog
        markLostDeal?.let { deal ->
            AlertDialog(
                onDismissRequest = { markLostDeal = null },
                title = { Text("Mark Deal as Lost (سبب الإلغاء)") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Please specify why the deal for ${deal.clientName} was lost:")
                        OutlinedTextField(
                            value = lostReasonInput,
                            onValueChange = { lostReasonInput = it },
                            placeholder = { Text("e.g. رفض ائتماني / فائدة مرتفعة / اشترى من معرض آخر") },
                            singleLine = false,
                            maxLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val reason = if (lostReasonInput.isNotBlank()) lostReasonInput else "Client Cancelled"
                            viewModel.markDealAsLost(deal.id, reason)
                            markLostDeal = null
                        }
                    ) {
                        Text("Mark as Lost", color = CrmRose)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { markLostDeal = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun PipelineSummaryHeader(state: CrmUiState) {
    val activeDealsCount = state.deals.count { it.stage != "DONE" && it.stage != "LOST" }
    val wonDealsCount = state.deals.count { it.stage == "DONE" }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Active Deals
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "ACTIVE PIPELINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeDealsCount.toString(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "deals ($wonDealsCount won)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CrmEmerald,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }

                Text(
                    text = "Forecast: ${FormattingUtils.formatCompact(state.weightedPipelineAmount)}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        // Pipeline Total Value
        Surface(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .weight(1f)
                .height(110.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "TOTAL PIPELINE",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 0.5.sp
                )

                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = FormattingUtils.formatCompact(state.totalPipelineAmount),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Text(
                    text = "Comm: ${FormattingUtils.formatCompact(state.totalCommissionReceived)} collected",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = CrmEmerald
                )
            }
        }
    }
}

@Composable
fun PipelineKanbanView(
    state: CrmUiState,
    onCall: (String) -> Unit,
    onWhatsApp: (String, String) -> Unit,
    onEdit: (DealEntity) -> Unit,
    onSelectDeal: (DealEntity) -> Unit = onEdit,
    onAdvanceStage: (DealEntity, DealStage) -> Unit,
    onMarkLost: (DealEntity) -> Unit,
    onReassign: (DealEntity) -> Unit
) {
    val stages = DealStage.entries.toTypedArray()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (stage in stages) {
            val stageDeals = state.deals.filter { it.stage.equals(stage.name, ignoreCase = true) }
            val stageTotal = stageDeals.sumOf { it.amount }

            val headerBg = when (stage) {
                DealStage.DONE -> CrmEmeraldContainer
                DealStage.LOST -> CrmRoseContainer
                DealStage.NEGOTIATION -> CrmPurpleContainer
                DealStage.VIEWING -> CrmAmberContainer
                else -> MaterialTheme.colorScheme.primaryContainer
            }
            val headerTextColor = when (stage) {
                DealStage.DONE -> CrmEmerald
                DealStage.LOST -> CrmRose
                DealStage.NEGOTIATION -> CrmPurple
                DealStage.VIEWING -> CrmAmber
                else -> MaterialTheme.colorScheme.primary
            }

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Stage Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                color = headerBg,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${stage.arabicName} (${stageDeals.size})",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = headerTextColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "${stage.defaultProbability}% Prob",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Text(
                            text = FormattingUtils.formatCurrency(stageTotal),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (stageDeals.isEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No deals in this stage",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            stageDeals.forEach { deal ->
                                KanbanDealItem(
                                    deal = deal,
                                    onCall = { onCall(deal.phone) },
                                    onWhatsApp = { onWhatsApp(deal.phone, deal.clientName) },
                                    onEdit = { onEdit(deal) },
                                    onSelect = { onSelectDeal(deal) },
                                    onAdvanceStage = {
                                        val nextStage = when (stage) {
                                            DealStage.PROSPECTING -> DealStage.QUALIFICATION
                                            DealStage.QUALIFICATION -> DealStage.PROPOSAL
                                            DealStage.PROPOSAL -> DealStage.VIEWING
                                            DealStage.VIEWING -> DealStage.NEGOTIATION
                                            DealStage.NEGOTIATION -> DealStage.DONE
                                            else -> DealStage.DONE
                                        }
                                        onAdvanceStage(deal, nextStage)
                                    },
                                    onMarkLost = { onMarkLost(deal) },
                                    onReassign = { onReassign(deal) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun KanbanDealItem(
    deal: DealEntity,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    onSelect: () -> Unit = onEdit,
    onAdvanceStage: () -> Unit,
    onMarkLost: () -> Unit,
    onReassign: () -> Unit
) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSelect)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.clientName,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${if (deal.carModel.isNotBlank()) deal.carModel else "General Loan"} • Rep: ${deal.salesRep}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Text(
                    text = FormattingUtils.formatCurrency(deal.amount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Partner & Expected Close Date
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "${deal.installmentPartner} • ${deal.probability}% Win",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                if (deal.expectedCloseDate.isNotBlank()) {
                    Text(
                        text = "Exp: ${deal.expectedCloseDate}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Kanban Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WA", style = MaterialTheme.typography.labelSmall)
                }

                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call", style = MaterialTheme.typography.labelSmall)
                }

                if (deal.stage != DealStage.DONE.name && deal.stage != DealStage.LOST.name) {
                    OutlinedButton(
                        onClick = onAdvanceStage,
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Advance", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, modifier = Modifier.size(12.dp))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DealCard(
    deal: DealEntity,
    currentRole: UserRole,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onLog: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStageChange: (String) -> Unit,
    onTogglePaid: () -> Unit,
    onReassign: () -> Unit,
    onMarkLost: () -> Unit,
    onSelect: () -> Unit = onEdit,
    modifier: Modifier = Modifier
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var stageMenuExpanded by remember { mutableStateOf(false) }

    val stageBgColor = when (deal.stage.uppercase()) {
        "DONE" -> CrmEmeraldContainer
        "NEGOTIATION" -> CrmPurpleContainer
        "VIEWING" -> CrmAmberContainer
        "PROPOSAL" -> CrmBlueContainer
        "LOST" -> CrmRoseContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val stageTextColor = when (deal.stage.uppercase()) {
        "DONE" -> CrmEmerald
        "NEGOTIATION" -> CrmPurple
        "VIEWING" -> CrmAmber
        "PROPOSAL" -> CrmBlue
        "LOST" -> CrmRose
        else -> MaterialTheme.colorScheme.primary
    }

    val stageDisplayName = when (deal.stage.uppercase()) {
        "DONE" -> "Closed Won (تم البيع)"
        "NEGOTIATION" -> "Negotiation (توقيع)"
        "VIEWING" -> "Viewing (معاينة)"
        "PROPOSAL" -> "Proposal (أوراق التقسيط)"
        "QUALIFICATION" -> "Qualified (تأهيل)"
        "LOST" -> "Lost (ملغي)"
        else -> "Lead (ليد جديد)"
    }

    val avatarInitial = deal.clientName.trim().firstOrNull()?.uppercase() ?: "C"

    val (avatarBgColor, avatarTextColor) = when {
        deal.salesRep.startsWith("N", ignoreCase = true) -> Pair(CrmBlueContainer, CrmBlue)
        deal.salesRep.startsWith("E", ignoreCase = true) -> Pair(CrmAmberContainer, CrmAmber)
        deal.salesRep.startsWith("A", ignoreCase = true) -> Pair(CrmEmeraldContainer, CrmEmerald)
        else -> Pair(CrmPurpleContainer, CrmPurple)
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onSelect,
                onLongClick = { menuExpanded = true }
            )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top Row: Avatar, Client Name, Rep Tag, Stage Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(avatarBgColor)
                ) {
                    Text(
                        text = avatarInitial,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = avatarTextColor
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Icon(
                                    Icons.Default.BusinessCenter,
                                    contentDescription = null,
                                    modifier = Modifier.size(11.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = deal.salesRep,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        if (deal.carModel.isNotBlank()) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = deal.carModel,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // Stage Badge
                Box {
                    Surface(
                        color = stageBgColor,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.clickable { stageMenuExpanded = true }
                    ) {
                        Text(
                            text = stageDisplayName,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = stageTextColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = stageMenuExpanded,
                        onDismissRequest = { stageMenuExpanded = false }
                    ) {
                        DealStage.entries.forEach { stage ->
                            DropdownMenuItem(
                                text = { Text("${stage.displayName} (${stage.arabicName})") },
                                onClick = {
                                    onStageChange(stage.name)
                                    stageMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                // More Menu
                Box {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Log Activity / Call") },
                            leadingIcon = { Icon(Icons.Default.Call, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onLog()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Edit Details") },
                            leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onEdit()
                            }
                        )
                        if (currentRole == UserRole.SALES_MANAGER || currentRole == UserRole.TEAM_LEADER) {
                            DropdownMenuItem(
                                text = { Text("Reassign to Rep (تحويل لموظف آخر)") },
                                leadingIcon = { Icon(Icons.Default.SwapHoriz, contentDescription = null) },
                                onClick = {
                                    menuExpanded = false
                                    onReassign()
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Mark as Lost (تسجيل إلغاء)") },
                            leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null, tint = CrmRose) },
                            onClick = {
                                menuExpanded = false
                                onMarkLost()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(if (deal.isCommissionReceived) "Mark Comm. as Pending" else "Mark Comm. as Paid") },
                            leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                            onClick = {
                                menuExpanded = false
                                onTogglePaid()
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Deal", color = MaterialTheme.colorScheme.error) },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                            onClick = {
                                menuExpanded = false
                                onDelete()
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Middle: Deal Amount, Loan, Partner & Probability
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Deal Value (سعر السيارة)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = FormattingUtils.formatCurrency(deal.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Partner: ${deal.installmentPartner}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Win Prob: ${deal.probability}%",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = when {
                                    deal.probability >= 70 -> CrmEmerald
                                    deal.probability >= 40 -> CrmBlue
                                    deal.probability > 0 -> CrmAmber
                                    else -> CrmRose
                                }
                            )
                        }
                    }

                    if (deal.downPayment > 0 || deal.loanAmount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Down Pay: ${FormattingUtils.formatCompact(deal.downPayment)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Loan Financed: ${FormattingUtils.formatCompact(deal.loanAmount)}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Follow-up Date & Expected Close Date Badges
            if (deal.followUpDate.isNotBlank() || deal.expectedCloseDate.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (deal.followUpDate.isNotBlank()) {
                        Surface(
                            color = CrmAmberContainer.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "Follow-up: ${deal.followUpDate}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CrmAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }

                    if (deal.expectedCloseDate.isNotBlank()) {
                        Text(
                            text = "Exp Close: ${deal.expectedCloseDate}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Notes / Received details
            if (deal.notes.isNotBlank() || deal.receivedNotes.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Column(modifier = Modifier.padding(horizontal = 2.dp)) {
                    if (deal.notes.isNotBlank()) {
                        Text(
                            text = deal.notes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    if (deal.receivedNotes.isNotBlank()) {
                        Text(
                            text = "Payment: ${deal.receivedNotes}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action Row: Quick Contact Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (deal.phone.isNotBlank()) {
                    OutlinedButton(
                        onClick = onCall,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Call", style = MaterialTheme.typography.labelMedium)
                    }

                    OutlinedButton(
                        onClick = onWhatsApp,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(15.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("WhatsApp", style = MaterialTheme.typography.labelMedium)
                    }
                }

                OutlinedButton(
                    onClick = onLog,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.SwapHoriz, contentDescription = null, modifier = Modifier.size(15.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Activity", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}

@Composable
fun LostDealCard(
    deal: DealEntity,
    onRecycle: () -> Unit,
    onCall: () -> Unit,
    onWhatsApp: () -> Unit,
    onEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, CrmRose.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = deal.clientName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Phone: ${deal.phone} • Rep: ${deal.salesRep}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    color = CrmRoseContainer,
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "Closed Lost (فرصة ضائعة)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = CrmRose,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Lost Reason Box
            Surface(
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = CrmRose, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Lost Reason (سبب الفقد):",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = CrmRose
                        )
                        Text(
                            text = if (deal.lostReason.isNotBlank()) deal.lostReason else "Not specified",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Actions: Recycle & Contact
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onWhatsApp,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Phone, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("WA")
                }

                OutlinedButton(
                    onClick = onCall,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Call")
                }

                OutlinedButton(
                    onClick = onRecycle,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = CrmEmerald),
                    modifier = Modifier.weight(1.6f)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Recycle Lead", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun EmptyDealsState(onAddNewDeal: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.FilterList,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No Deals Found",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "No clients or deals match your selected filters. Try clearing search filters or create a new deal.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedButton(
                onClick = onAddNewDeal,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Client / Deal")
            }
        }
    }
}
