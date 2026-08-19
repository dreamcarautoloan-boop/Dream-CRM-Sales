package com.example.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PostAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.example.R
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.LeadSource
import com.example.data.model.UserPermissions
import com.example.data.model.UserRole
import com.example.ui.dialogs.AddEditDealDialog
import com.example.ui.dialogs.CommissionCalculatorDialog
import com.example.ui.dialogs.LogInteractionDialog
import com.example.ui.dialogs.MetaLeadIntakeDialog
import com.example.ui.screens.ActivitiesScreen
import com.example.ui.screens.DealsScreen
import com.example.ui.screens.FinancialsScreen
import com.example.ui.screens.InstallmentApplicationsScreen
import com.example.ui.screens.LeadDetailScreen
import com.example.ui.screens.LeadsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.TeamTargetsScreen

enum class CrmTab(val title: String, val icon: ImageVector) {
    LEADS("Leads", Icons.Default.FilterList),
    DEALS("Pipeline", Icons.Default.BusinessCenter),
    INSTALLMENTS("Installments", Icons.Default.Receipt),
    TARGETS("Team & Reps", Icons.Default.Leaderboard),
    FINANCIALS("P&L Financials", Icons.Default.AccountBalance),
    ACTIVITIES("Call Logs", Icons.Default.Call)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CrmMainScreen(
    viewModel: CrmViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    if (!state.isAuthenticated) {
        LoginScreen(state = state, viewModel = viewModel)
        return
    }

    var selectedTab by remember { mutableIntStateOf(0) }

    // Dialog & Detail screen controllers
    var selectedLeadForDetail by remember { mutableStateOf<DealEntity?>(null) }
    var showAddDealDialog by remember { mutableStateOf(false) }
    var showMetaLeadDialog by remember { mutableStateOf(false) }
    var dealToEdit by remember { mutableStateOf<DealEntity?>(null) }
    var showLogDialog by remember { mutableStateOf(false) }
    var logClientName by remember { mutableStateOf("") }
    var logSalesRep by remember { mutableStateOf("Nada") }
    var logClientId by remember { mutableStateOf<Long?>(null) }
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var roleMenuExpanded by remember { mutableStateOf(false) }
    var userMenuExpanded by remember { mutableStateOf(false) }

    val availableReps = remember(state.targets) {
        if (state.targets.isNotEmpty()) state.targets.map { it.name }
        else listOf("Nada", "Esraa", "Nahla", "Alaa", "Mahmoud", "Marwan")
    }

    val visibleTabs = remember(state.permissions) {
        CrmTab.entries.filter { tab ->
            when (tab) {
                CrmTab.FINANCIALS -> state.permissions.canViewFinancials
                else -> true
            }
        }
    }

    val currentTab = visibleTabs.getOrNull(selectedTab.coerceIn(0, (visibleTabs.size - 1).coerceAtLeast(0))) ?: CrmTab.DEALS

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Brand & Role Indicator
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(R.drawable.ic_dreamcar_logo),
                                contentDescription = "Dream Car Logo",
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "DREAM AUTO LOAN",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    letterSpacing = 0.5.sp,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                // Active Role Selector Chip
                                Box {
                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp),
                                        modifier = Modifier.clickable { roleMenuExpanded = true }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${state.currentRole.arabicName}${if (state.currentRole == UserRole.SALES_REP) " (${state.activeRepName})" else ""}",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.primary
                                            )
                                            Icon(
                                                Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                modifier = Modifier.size(16.dp),
                                                tint = MaterialTheme.colorScheme.primary
                                            )
                                        }
                                    }

                                    DropdownMenu(
                                        expanded = roleMenuExpanded,
                                        onDismissRequest = { roleMenuExpanded = false }
                                    ) {
                                        UserRole.entries.forEach { role ->
                                            DropdownMenuItem(
                                                text = {
                                                    Text(
                                                        text = "${role.arabicName} (${role.displayName})",
                                                        fontWeight = if (state.currentRole == role) FontWeight.Bold else FontWeight.Normal
                                                    )
                                                },
                                                onClick = {
                                                    viewModel.setUserRole(role)
                                                    roleMenuExpanded = false
                                                }
                                            )
                                        }
                                        if (state.currentRole == UserRole.SALES_REP) {
                                            availableReps.forEach { rep ->
                                                DropdownMenuItem(
                                                    text = { Text("السيلز: $rep") },
                                                    onClick = {
                                                        viewModel.setActiveRepName(rep)
                                                        roleMenuExpanded = false
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Top Action Icons
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Meta Lead Intake
                            IconButton(
                                onClick = { showMetaLeadDialog = true },
                                modifier = Modifier.testTag("open_meta_intake_button")
                            ) {
                                Icon(
                                    Icons.Default.PostAdd,
                                    contentDescription = "Meta Ads Lead Intake",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }

                            // Loan Calculator
                            IconButton(
                                onClick = { showCalculatorDialog = true },
                                modifier = Modifier.testTag("open_calculator_button")
                            ) {
                                Icon(
                                    Icons.Default.Calculate,
                                    contentDescription = "Commission & Loan Calculator",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // Add Deal Button
                            IconButton(
                                onClick = { showAddDealDialog = true },
                                modifier = Modifier.testTag("top_add_deal_button")
                            ) {
                                Icon(
                                    Icons.Default.Add,
                                    contentDescription = "Add Deal",
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }

                            // User Profile & Sign Out
                            Box {
                                IconButton(
                                    onClick = { userMenuExpanded = true },
                                    modifier = Modifier.testTag("user_profile_button")
                                ) {
                                    Icon(
                                        Icons.Default.AccountCircle,
                                        contentDescription = "User Profile",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }

                                DropdownMenu(
                                    expanded = userMenuExpanded,
                                    onDismissRequest = { userMenuExpanded = false }
                                ) {
                                    DropdownMenuItem(
                                        text = {
                                            Column {
                                                Text(
                                                    text = state.currentUser?.displayName ?: "User",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.bodyMedium
                                                )
                                                Text(
                                                    text = state.currentUser?.email ?: "",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        },
                                        onClick = { userMenuExpanded = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("Sign Out (تسجيل خروج)", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.SemiBold) },
                                        leadingIcon = { Icon(Icons.Default.Logout, contentDescription = null, tint = MaterialTheme.colorScheme.error) },
                                        modifier = Modifier.testTag("sign_out_menu_item"),
                                        onClick = {
                                            userMenuExpanded = false
                                            viewModel.logout(context)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.fillMaxWidth()
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                        .windowInsetsPadding(WindowInsets.navigationBars)
                        .testTag("crm_navigation_bar")
                ) {
                    visibleTabs.forEachIndexed { index, tab ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            icon = { Icon(tab.icon, contentDescription = tab.title) },
                            label = {
                                Text(
                                    text = tab.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.testTag("nav_tab_${tab.name.lowercase()}")
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (currentTab) {
                CrmTab.LEADS -> LeadsScreen(
                    state = state,
                    viewModel = viewModel,
                    onLeadClick = { lead -> selectedLeadForDetail = lead },
                    onAddLeadClick = { showAddDealDialog = true }
                )

                CrmTab.DEALS -> DealsScreen(
                    state = state,
                    viewModel = viewModel,
                    onAddNewDeal = { showAddDealDialog = true },
                    onEditDeal = { deal -> dealToEdit = deal },
                    onSelectDeal = { deal -> selectedLeadForDetail = deal },
                    onLogInteraction = { deal ->
                        logClientId = deal.id
                        logClientName = deal.clientName
                        logSalesRep = deal.salesRep
                        showLogDialog = true
                    }
                )

                CrmTab.INSTALLMENTS -> InstallmentApplicationsScreen(
                    state = state,
                    viewModel = viewModel,
                    onLeadClick = { deal -> selectedLeadForDetail = deal }
                )

                CrmTab.TARGETS -> TeamTargetsScreen(
                    state = state,
                    viewModel = viewModel,
                    onLogCall = { repName ->
                        logClientId = null
                        logClientName = ""
                        logSalesRep = repName
                        showLogDialog = true
                    }
                )

                CrmTab.FINANCIALS -> FinancialsScreen(
                    state = state,
                    viewModel = viewModel
                )

                CrmTab.ACTIVITIES -> ActivitiesScreen(
                    state = state,
                    viewModel = viewModel,
                    onLogNewActivity = {
                        logClientId = null
                        logClientName = ""
                        logSalesRep = availableReps.firstOrNull() ?: "Nada"
                        showLogDialog = true
                    }
                )
            }
        }
    }

    // Add Deal Dialog
    if (showAddDealDialog) {
        AddEditDealDialog(
            dealToEdit = null,
            availableReps = availableReps,
            onDismiss = { showAddDealDialog = false },
            onSave = { id, clientName, phone, salesRep, stage, amount, probability, expectedCloseDate, carModel, carType, downPayment, loanAmount, installmentPartner, installmentStatus, leadSource, qualificationStatus, interestLevel, followUpDate, lostReason, commissionRate, commissionAmount, isCommissionReceived, receivedNotes, date, notes ->
                viewModel.saveDeal(
                    id = id,
                    clientName = clientName,
                    phone = phone,
                    salesRep = salesRep,
                    stage = stage,
                    amount = amount,
                    probability = probability,
                    expectedCloseDate = expectedCloseDate,
                    carModel = carModel,
                    carType = carType,
                    downPayment = downPayment,
                    loanAmount = loanAmount,
                    installmentPartner = installmentPartner,
                    installmentStatus = installmentStatus,
                    leadSource = leadSource,
                    qualificationStatus = qualificationStatus,
                    interestLevel = interestLevel,
                    followUpDate = followUpDate,
                    lostReason = lostReason,
                    commissionRate = commissionRate,
                    commissionAmount = commissionAmount,
                    isCommissionReceived = isCommissionReceived,
                    receivedNotes = receivedNotes,
                    date = date,
                    notes = notes
                )
            }
        )
    }

    // Edit Deal Dialog
    dealToEdit?.let { deal ->
        AddEditDealDialog(
            dealToEdit = deal,
            availableReps = availableReps,
            onDismiss = { dealToEdit = null },
            onSave = { id, clientName, phone, salesRep, stage, amount, probability, expectedCloseDate, carModel, carType, downPayment, loanAmount, installmentPartner, installmentStatus, leadSource, qualificationStatus, interestLevel, followUpDate, lostReason, commissionRate, commissionAmount, isCommissionReceived, receivedNotes, date, notes ->
                viewModel.saveDeal(
                    id = id,
                    clientName = clientName,
                    phone = phone,
                    salesRep = salesRep,
                    stage = stage,
                    amount = amount,
                    probability = probability,
                    expectedCloseDate = expectedCloseDate,
                    carModel = carModel,
                    carType = carType,
                    downPayment = downPayment,
                    loanAmount = loanAmount,
                    installmentPartner = installmentPartner,
                    installmentStatus = installmentStatus,
                    leadSource = leadSource,
                    qualificationStatus = qualificationStatus,
                    interestLevel = interestLevel,
                    followUpDate = followUpDate,
                    lostReason = lostReason,
                    commissionRate = commissionRate,
                    commissionAmount = commissionAmount,
                    isCommissionReceived = isCommissionReceived,
                    receivedNotes = receivedNotes,
                    date = date,
                    notes = notes
                )
            }
        )
    }

    // Meta Ads Lead Intake Dialog with Deduplication
    if (showMetaLeadDialog) {
        MetaLeadIntakeDialog(
            availableReps = availableReps,
            existingDuplicate = state.duplicateCheckResult,
            onCheckDuplicate = { phone -> viewModel.checkDuplicatePhone(phone) },
            onClearDuplicateCheck = { viewModel.clearDuplicateCheck() },
            onDismiss = { showMetaLeadDialog = false },
            onSubmitLead = { clientName, phone, salesRep, carModel, budgetOrPrice, notes ->
                viewModel.saveDeal(
                    id = 0L,
                    clientName = clientName,
                    phone = phone,
                    salesRep = salesRep,
                    stage = DealStage.PROSPECTING.name,
                    amount = budgetOrPrice,
                    probability = 15,
                    expectedCloseDate = "",
                    carModel = carModel,
                    carType = "NEW",
                    downPayment = budgetOrPrice * 0.25,
                    loanAmount = budgetOrPrice * 0.75,
                    installmentPartner = "Drive",
                    installmentStatus = "PENDING_PAPERS",
                    leadSource = LeadSource.META_ADS.name,
                    qualificationStatus = "QUALIFIED",
                    interestLevel = "HOT",
                    followUpDate = "Today (New Lead Contact)",
                    lostReason = "",
                    commissionRate = 0.025,
                    commissionAmount = budgetOrPrice * 0.025,
                    isCommissionReceived = false,
                    receivedNotes = "",
                    date = "",
                    notes = notes
                )
            }
        )
    }

    // Log Activity Dialog
    if (showLogDialog) {
        LogInteractionDialog(
            initialClientName = logClientName,
            initialSalesRep = logSalesRep,
            initialClientId = logClientId,
            availableReps = availableReps,
            onDismiss = { showLogDialog = false },
            onLog = { clientId, clientName, salesRep, interactionType, outcome, notes ->
                viewModel.addCallLog(
                    clientId = clientId,
                    clientName = clientName,
                    salesRep = salesRep,
                    interactionType = interactionType,
                    outcome = outcome,
                    notes = notes
                )
            }
        )
    }

    // Commission Calculator Dialog
    if (showCalculatorDialog) {
        CommissionCalculatorDialog(
            targets = state.targets,
            onDismiss = { showCalculatorDialog = false }
        )
    }

    // Lead Detail Screen Overlay
    selectedLeadForDetail?.let { activeDeal ->
        val currentActiveDeal = state.deals.find { it.id == activeDeal.id } ?: activeDeal
        LeadDetailScreen(
            deal = currentActiveDeal,
            state = state,
            viewModel = viewModel,
            onBack = { selectedLeadForDetail = null },
            onEditDeal = { dealToEdit = it }
        )
    }
}
