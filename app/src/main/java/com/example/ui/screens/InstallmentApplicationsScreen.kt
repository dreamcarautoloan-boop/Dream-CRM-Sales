package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.example.data.model.InstallmentPartner
import com.example.data.model.InstallmentStatus
import com.example.data.model.UserRole
import com.example.ui.CrmUiState
import com.example.ui.CrmViewModel
import com.example.ui.dialogs.LinkPartnerApplicationDialog
import com.example.ui.util.FormattingUtils

/**
 * InstallmentApplicationsScreen
 *
 * Dedicated module for managing financing and installment applications:
 * - Links customers to financing partners (Contact, Drive, Aman, One Finance, Bedaya, Bank)
 * - Real-time state updates across: Pending, Approved, Rejected
 * - Live filtering by partner and status, loan volume calculations, and direct communication triggers.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun InstallmentApplicationsScreen(
    state: CrmUiState,
    viewModel: CrmViewModel,
    onLeadClick: (DealEntity) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedPartnerFilter by remember { mutableStateOf<String?>(null) }
    var selectedStateFilter by remember { mutableStateOf<String?>(null) }
    var showLinkDialog by remember { mutableStateOf(false) }
    var dealToEditApplication by remember { mutableStateOf<DealEntity?>(null) }

    // Role-based visibility enforcement
    val allDeals = state.deals
    val visibleDeals = if (state.currentRole == UserRole.SALES_REP) {
        allDeals.filter { it.salesRep.equals(state.activeRepName, ignoreCase = true) }
    } else {
        allDeals
    }

    // Filter deals that are linked or eligible for installment
    val installmentDeals = visibleDeals.filter { deal ->
        val hasPartner = deal.installmentPartner.isNotBlank() && !deal.installmentPartner.contains("كاش", ignoreCase = true)
        hasPartner || deal.loanAmount > 0.0 || deal.downPayment > 0.0
    }

    // Filter by search, partner, and application state
    val filteredApplications = installmentDeals.filter { deal ->
        val matchesSearch = searchQuery.isBlank() ||
                deal.clientName.contains(searchQuery, ignoreCase = true) ||
                deal.phone.contains(searchQuery, ignoreCase = true) ||
                deal.carModel.contains(searchQuery, ignoreCase = true) ||
                deal.salesRep.contains(searchQuery, ignoreCase = true) ||
                deal.installmentPartner.contains(searchQuery, ignoreCase = true) ||
                deal.notes.contains(searchQuery, ignoreCase = true)

        val matchesPartner = selectedPartnerFilter == null ||
                deal.installmentPartner.contains(selectedPartnerFilter!!, ignoreCase = true)

        val matchesState = selectedStateFilter == null || when (selectedStateFilter) {
            "PENDING" -> deal.installmentStatus in listOf(
                InstallmentStatus.PENDING_PAPERS.name,
                InstallmentStatus.SUBMITTED.name,
                InstallmentStatus.PENDING_CONDITIONS.name,
                "PENDING"
            )
            "APPROVED" -> deal.installmentStatus == InstallmentStatus.APPROVED.name || deal.installmentStatus == "APPROVED"
            "REJECTED" -> deal.installmentStatus in listOf(
                InstallmentStatus.REJECTED.name,
                InstallmentStatus.CLIENT_CANCELLED.name,
                "REJECTED"
            )
            else -> true
        }

        matchesSearch && matchesPartner && matchesState
    }

    // KPIs
    val totalAppsCount = installmentDeals.size
    val pendingCount = installmentDeals.count {
        it.installmentStatus in listOf(
            InstallmentStatus.PENDING_PAPERS.name,
            InstallmentStatus.SUBMITTED.name,
            InstallmentStatus.PENDING_CONDITIONS.name,
            "PENDING"
        )
    }
    val approvedCount = installmentDeals.count {
        it.installmentStatus == InstallmentStatus.APPROVED.name || it.installmentStatus == "APPROVED"
    }
    val rejectedCount = installmentDeals.count {
        it.installmentStatus in listOf(
            InstallmentStatus.REJECTED.name,
            InstallmentStatus.CLIENT_CANCELLED.name,
            "REJECTED"
        )
    }
    val totalFinancedVolume = installmentDeals
        .filter { it.installmentStatus == InstallmentStatus.APPROVED.name || it.installmentStatus == "APPROVED" }
        .sumOf { if (it.loanAmount > 0) it.loanAmount else (it.amount - it.downPayment).coerceAtLeast(0.0) }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                // Header Banner
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Receipt,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "طلبات التقسيط والشركاء (Installments)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "إدارة ومتابعة طلبات التمويل مع كونتاكت، درايف، أمان وبداية",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                dealToEditApplication = null
                                showLinkDialog = true
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("link_new_partner_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("ربط شريك", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Summary Metrics Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pending Metric
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFF3E0),
                        border = BorderStroke(1.dp, Color(0xFFF97316).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("⏳ قيد المراجعة", fontSize = 11.sp, color = Color(0xFFC2410C), fontWeight = FontWeight.Bold)
                            Text("$pendingCount طلب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFE65100))
                        }
                    }

                    // Approved Metric
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFE8F5E9),
                        border = BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("✅ معتمد ومقبول", fontSize = 11.sp, color = Color(0xFF047857), fontWeight = FontWeight.Bold)
                            Text("$approvedCount موافقة", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFF2E7D32))
                        }
                    }

                    // Rejected Metric
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE),
                        border = BorderStroke(1.dp, Color(0xFFF43F5E).copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text("❌ مرفوض", fontSize = 11.sp, color = Color(0xFFBE123C), fontWeight = FontWeight.Bold)
                            Text("$rejectedCount طلب", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color(0xFFC62828))
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم العميل، جهة التمويل (كونتاكت، درايف)، أو السيارة...") },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = "بحث", tint = MaterialTheme.colorScheme.primary)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "مسح")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("installments_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // State Filter Chips (Pending, Approved, Rejected)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "فلترة حسب حالة المعاملة (Application State):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStateFilter == null,
                            onClick = { selectedStateFilter = null },
                            label = { Text("جميع الحالات ($totalAppsCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_installment_all")
                        )

                        FilterChip(
                            selected = selectedStateFilter == "PENDING",
                            onClick = {
                                selectedStateFilter = if (selectedStateFilter == "PENDING") null else "PENDING"
                            },
                            label = { Text("⏳ قيد المراجعة Pending ($pendingCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFF3E0),
                                selectedLabelColor = Color(0xFFE65100)
                            ),
                            modifier = Modifier.testTag("filter_installment_pending")
                        )

                        FilterChip(
                            selected = selectedStateFilter == "APPROVED",
                            onClick = {
                                selectedStateFilter = if (selectedStateFilter == "APPROVED") null else "APPROVED"
                            },
                            label = { Text("✅ معتمد Approved ($approvedCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE8F5E9),
                                selectedLabelColor = Color(0xFF2E7D32)
                            ),
                            modifier = Modifier.testTag("filter_installment_approved")
                        )

                        FilterChip(
                            selected = selectedStateFilter == "REJECTED",
                            onClick = {
                                selectedStateFilter = if (selectedStateFilter == "REJECTED") null else "REJECTED"
                            },
                            label = { Text("❌ مرفوض Rejected ($rejectedCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFEBEE),
                                selectedLabelColor = Color(0xFFC62828)
                            ),
                            modifier = Modifier.testTag("filter_installment_rejected")
                        )
                    }
                }
            }

            // Partner Filter Chips Row
            item {
                val partners = listOf(
                    "كونتاكت" to "كونتاكت - Contact",
                    "درايف" to "درايف - Drive",
                    "أمان" to "أمان - Aman",
                    "وان فاينانس" to "وان فاينانس - One Finance",
                    "بداية" to "بداية - Bedaya",
                    "بنك" to "تمويل بنكي - Bank"
                )

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "فلترة حسب شريك التقسيط (Partner):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        partners.forEach { (shortName, fullName) ->
                            val isSelected = selectedPartnerFilter == shortName
                            val count = installmentDeals.count { it.installmentPartner.contains(shortName, ignoreCase = true) }
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPartnerFilter = if (isSelected) null else shortName
                                },
                                label = { Text("$shortName ($count)", fontSize = 12.sp) }
                            )
                        }
                    }
                }
            }

            // Applications List
            if (filteredApplications.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "لا توجد طلبات تقسيط مطابقة للبحث أو الفلتر",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "اضغط على 'ربط شريك' لربط عميل بشريك تمويل مثل كونتاكت أو درايف",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            } else {
                items(filteredApplications, key = { it.id }) { deal ->
                    InstallmentApplicationCard(
                        deal = deal,
                        onCardClick = { onLeadClick(deal) },
                        onEditApplication = {
                            dealToEditApplication = deal
                            showLinkDialog = true
                        },
                        onStatusChange = { newStatus ->
                            viewModel.updateInstallmentStatus(deal.id, newStatus)
                        },
                        onCallClick = {
                            FormattingUtils.dialPhoneNumber(context, deal.phone)
                        },
                        onWhatsAppClick = {
                            FormattingUtils.openWhatsApp(
                                context = context,
                                phone = deal.phone,
                                clientName = deal.clientName
                            )
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Link Customer to Partner Dialog
    if (showLinkDialog) {
        LinkPartnerApplicationDialog(
            initialDeal = dealToEditApplication,
            availableDeals = visibleDeals,
            onDismiss = {
                showLinkDialog = false
                dealToEditApplication = null
            },
            onSave = { dealId, clientName, phone, partner, status, carModel, carPrice, downPayment, loanAmount, notes ->
                if (dealId > 0L) {
                    viewModel.linkCustomerToInstallmentPartner(
                        dealId = dealId,
                        partner = partner,
                        status = status,
                        loanAmount = loanAmount,
                        downPayment = downPayment,
                        notes = notes
                    )
                } else {
                    // Create new deal linked to partner
                    viewModel.saveDeal(
                        id = 0L,
                        clientName = clientName,
                        phone = phone,
                        salesRep = state.activeRepName.ifBlank { "Nada" },
                        stage = if (status == InstallmentStatus.APPROVED.name) "VIEWING" else "PROPOSAL",
                        amount = carPrice,
                        probability = if (status == InstallmentStatus.APPROVED.name) 70 else 50,
                        expectedCloseDate = "",
                        carModel = carModel,
                        carType = "NEW",
                        downPayment = downPayment,
                        loanAmount = loanAmount,
                        installmentPartner = partner,
                        installmentStatus = status,
                        leadSource = "META_ADS",
                        qualificationStatus = "QUALIFIED",
                        interestLevel = "HOT",
                        followUpDate = "Today",
                        lostReason = "",
                        commissionRate = 0.025,
                        commissionAmount = carPrice * 0.025,
                        isCommissionReceived = false,
                        receivedNotes = "",
                        date = "",
                        notes = notes
                    )
                }
                showLinkDialog = false
                dealToEditApplication = null
            }
        )
    }
}

/**
 * InstallmentApplicationCard
 *
 * Card item displaying client details, partner branding, status state badges,
 * vehicle financing breakdown, and fast status transition triggers (Pending, Approved, Rejected).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun InstallmentApplicationCard(
    deal: DealEntity,
    onCardClick: () -> Unit,
    onEditApplication: () -> Unit,
    onStatusChange: (String) -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit
) {
    val displayState = when {
        deal.installmentStatus == InstallmentStatus.APPROVED.name || deal.installmentStatus == "APPROVED" ->
            InstallmentCardDisplayState(Color(0xFFECFDF5), Color(0xFF10B981), Color(0xFF047857), "✅", "موافقة معتمدة Approved")
        deal.installmentStatus in listOf(InstallmentStatus.REJECTED.name, InstallmentStatus.CLIENT_CANCELLED.name, "REJECTED") ->
            InstallmentCardDisplayState(Color(0xFFFFF1F2), Color(0xFFF43F5E), Color(0xFFBE123C), "❌", "مرفوض Rejected")
        else ->
            InstallmentCardDisplayState(Color(0xFFFFF7ED), Color(0xFFF97316), Color(0xFFC2410C), "⏳", "قيد المراجعة Pending")
    }
    val statusBg = displayState.bg
    val statusBorder = displayState.border
    val statusFg = displayState.fg
    val statusIcon = displayState.icon
    val statusLabel = displayState.label

    val partnerColor = when {
        deal.installmentPartner.contains("كونتاكت", ignoreCase = true) || deal.installmentPartner.contains("contact", ignoreCase = true) -> Color(0xFF004B87)
        deal.installmentPartner.contains("درايف", ignoreCase = true) || deal.installmentPartner.contains("drive", ignoreCase = true) -> Color(0xFFE65100)
        deal.installmentPartner.contains("أمان", ignoreCase = true) || deal.installmentPartner.contains("aman", ignoreCase = true) -> Color(0xFF00897B)
        deal.installmentPartner.contains("وان", ignoreCase = true) || deal.installmentPartner.contains("one", ignoreCase = true) -> Color(0xFF7B1FA2)
        deal.installmentPartner.contains("بداية", ignoreCase = true) || deal.installmentPartner.contains("bedaya", ignoreCase = true) -> Color(0xFF00838F)
        else -> Color(0xFF1565C0)
    }

    val calculatedLoan = if (deal.loanAmount > 0) deal.loanAmount else (deal.amount - deal.downPayment).coerceAtLeast(0.0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("installment_card_${deal.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(1.5.dp, statusBorder.copy(alpha = 0.35f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Status Accent Strip
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(statusBorder)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Top Row: Client Avatar + Name + Partner Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(partnerColor.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = deal.clientName.trim().take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = partnerColor
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = deal.clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${deal.phone} • مسؤول: ${deal.salesRep}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Partner Brand Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = partnerColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, partnerColor.copy(alpha = 0.3f))
                    ) {
                        Text(
                            text = if (deal.installmentPartner.isNotBlank()) deal.installmentPartner else "درايف - Drive",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = partnerColor,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Vehicle & Financing Breakdown
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (deal.carModel.isNotBlank()) deal.carModel else "سيارة غير محددة",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Loan Amount Pill
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "تمويل: ${FormattingUtils.formatEgp(calculatedLoan)}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                // Down Payment + Estimated Monthly Installment
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    if (deal.downPayment > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Text(
                                text = "مقدم: ${FormattingUtils.formatEgp(deal.downPayment)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    if (deal.amount > 0.0) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = "سعر السيارة: ${FormattingUtils.formatEgp(deal.amount)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }

                    // Current State Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg,
                        border = BorderStroke(1.dp, statusBorder.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = "${statusIcon} ${statusLabel}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }

                if (deal.notes.isNotBlank()) {
                    Text(
                        text = "ملاحظات: ${deal.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Application State Switcher Actions & Quick triggers
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "تحديث حالة الموافقة (Update State):",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Pending Action Button
                        OutlinedButton(
                            onClick = { onStatusChange(InstallmentStatus.PENDING_PAPERS.name) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_pending_${deal.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (deal.installmentStatus in listOf(InstallmentStatus.PENDING_PAPERS.name, "PENDING")) Color(0xFFFFF3E0) else Color.Transparent,
                                contentColor = Color(0xFFE65100)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFE65100).copy(alpha = 0.5f))
                        ) {
                            Text("⏳ مراجعة", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Approve Action Button
                        Button(
                            onClick = { onStatusChange(InstallmentStatus.APPROVED.name) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_approve_${deal.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF2E7D32),
                                contentColor = Color.White
                            )
                        ) {
                            Text("✅ اعتماد", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }

                        // Reject Action Button
                        OutlinedButton(
                            onClick = { onStatusChange(InstallmentStatus.REJECTED.name) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("btn_reject_${deal.id}"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (deal.installmentStatus in listOf(InstallmentStatus.REJECTED.name, "REJECTED")) Color(0xFFFFEBEE) else Color.Transparent,
                                contentColor = Color(0xFFC62828)
                            ),
                            border = BorderStroke(1.dp, Color(0xFFC62828).copy(alpha = 0.5f))
                        ) {
                            Text("❌ رفض", fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Direct Call & WhatsApp row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = onCallClick,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.testTag("app_call_${deal.id}")
                            ) {
                                Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("اتصال", fontSize = 11.5.sp)
                            }

                            Button(
                                onClick = onWhatsAppClick,
                                shape = RoundedCornerShape(8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF25D366),
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.testTag("app_whatsapp_${deal.id}")
                            ) {
                                Icon(Icons.Default.Message, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("واتساب", fontSize = 11.5.sp)
                            }
                        }

                        IconButton(
                            onClick = onEditApplication,
                            modifier = Modifier.testTag("app_edit_${deal.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "تعديل الشريك",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class InstallmentCardDisplayState(
    val bg: Color,
    val border: Color,
    val fg: Color,
    val icon: String,
    val label: String
)
