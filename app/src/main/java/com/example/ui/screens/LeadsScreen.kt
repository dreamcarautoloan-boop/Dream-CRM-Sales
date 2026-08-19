package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
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
import com.example.data.model.DealStage
import com.example.data.model.InterestLevel
import com.example.data.model.LeadSource
import com.example.data.model.UserRole
import com.example.ui.CrmUiState
import com.example.ui.CrmViewModel
import com.example.ui.util.FormattingUtils

/**
 * LeadsScreen
 *
 * Dedicated Incoming Leads screen that lists all incoming leads from Firestore & Meta Campaigns.
 * Implements clean Card UI displaying customer names, phone numbers, lead sources, car models,
 * assigned sales rep, and comprehensive status filters matching PRD specifications.
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LeadsScreen(
    state: CrmUiState,
    viewModel: CrmViewModel,
    onLeadClick: (DealEntity) -> Unit,
    onAddLeadClick: () -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedStatusFilter by remember { mutableStateOf<String?>(null) }
    var selectedSourceFilter by remember { mutableStateOf<String?>(null) }
    var selectedRepFilter by remember { mutableStateOf<String?>(null) }

    // Role-based visibility enforcement
    val allDeals = state.deals
    val visibleDeals = if (state.currentRole == UserRole.SALES_REP) {
        allDeals.filter { it.salesRep.equals(state.activeRepName, ignoreCase = true) }
    } else {
        allDeals
    }

    // Filter leads
    val filteredLeads = visibleDeals.filter { lead ->
        val matchesSearch = searchQuery.isBlank() ||
                lead.clientName.contains(searchQuery, ignoreCase = true) ||
                lead.phone.contains(searchQuery, ignoreCase = true) ||
                lead.carModel.contains(searchQuery, ignoreCase = true) ||
                lead.salesRep.contains(searchQuery, ignoreCase = true) ||
                lead.notes.contains(searchQuery, ignoreCase = true)

        val matchesStatus = selectedStatusFilter == null || lead.stage.equals(selectedStatusFilter, ignoreCase = true)
        val matchesSource = selectedSourceFilter == null || lead.leadSource.equals(selectedSourceFilter, ignoreCase = true)
        val matchesRep = selectedRepFilter == null || lead.salesRep.equals(selectedRepFilter, ignoreCase = true)

        matchesSearch && matchesStatus && matchesSource && matchesRep
    }

    val totalCount = visibleDeals.size
    val newCount = visibleDeals.count { it.stage == DealStage.PROSPECTING.name }
    val qualifiedCount = visibleDeals.count { it.stage == DealStage.QUALIFICATION.name }
    val inProgressCount = visibleDeals.count { it.stage in listOf(DealStage.PROPOSAL.name, DealStage.VIEWING.name, DealStage.NEGOTIATION.name) }
    val wonCount = visibleDeals.count { it.stage == DealStage.DONE.name }
    val lostCount = visibleDeals.count { it.stage == DealStage.LOST.name }

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
                                    imageVector = Icons.Outlined.Campaign,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "الوارد من الإعلانات (Incoming Leads)",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "مزامنة لحظية من فيسبوك والحملات الإعلانية • ${filteredLeads.size} عميل محتمل",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = onAddLeadClick,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("add_lead_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إضافة ليد", fontSize = 13.sp)
                        }
                    }
                }
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("بحث باسم العميل، السيلز (نهلة، محمود...)، السيارة أو الهاتف...") },
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
                        .testTag("leads_search_input"),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )
            }

            // Sales Representative Filter Chips Row
            item {
                val availableReps = remember(visibleDeals) {
                    val fromDeals = visibleDeals.map { it.salesRep.trim() }.filter { it.isNotBlank() }
                    val defaultReps = listOf("Ali", "Marwan", "Mahmoud", "Nahla", "Esraa", "Nada", "Alaa")
                    (fromDeals + defaultReps).distinct()
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "فلترة حسب السيلز المسؤول (Sales Representative):",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (selectedRepFilter != null || selectedStatusFilter != null || selectedSourceFilter != null || searchQuery.isNotBlank()) {
                            Text(
                                text = "مسح الفلاتر",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable {
                                        searchQuery = ""
                                        selectedRepFilter = null
                                        selectedStatusFilter = null
                                        selectedSourceFilter = null
                                    }
                                    .padding(4.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedRepFilter == null,
                            onClick = { selectedRepFilter = null },
                            label = { Text("جميع الممثلين (${visibleDeals.size})") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_rep_all")
                        )

                        availableReps.forEach { rep ->
                            val isSelected = selectedRepFilter.equals(rep, ignoreCase = true)
                            val repCount = visibleDeals.count { it.salesRep.equals(rep, ignoreCase = true) }
                            val repArabic = when (rep.lowercase()) {
                                "ali" -> "علي"
                                "marwan" -> "مروان"
                                "mahmoud" -> "محمود"
                                "nahla" -> "نهلة"
                                "esraa", "israa" -> "إسراء"
                                "nada" -> "ندى"
                                "alaa" -> "علاء"
                                else -> rep
                            }

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedRepFilter = if (isSelected) null else rep
                                },
                                label = {
                                    Text(
                                        text = "$repArabic ($repCount)",
                                        fontSize = 12.5.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.secondary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onSecondary
                                ),
                                modifier = Modifier.testTag("filter_rep_${rep.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Status Filter Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "فلترة حسب حالة العميل (Lead Status):",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedStatusFilter == null,
                            onClick = { selectedStatusFilter = null },
                            label = { Text("كل الحالات ($totalCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("filter_status_all")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.PROSPECTING.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.PROSPECTING.name) null else DealStage.PROSPECTING.name
                            },
                            label = { Text("🎯 جديد Prospecting ($newCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF3F51B5),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_prospecting")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.QUALIFICATION.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.QUALIFICATION.name) null else DealStage.QUALIFICATION.name
                            },
                            label = { Text("📋 مؤهل Qualified ($qualifiedCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF00897B),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_qualified")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.PROPOSAL.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.PROPOSAL.name) null else DealStage.PROPOSAL.name
                            },
                            label = {
                                val count = visibleDeals.count { it.stage == DealStage.PROPOSAL.name }
                                Text("📄 أوراق Papers ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFE65100),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_proposal")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.VIEWING.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.VIEWING.name) null else DealStage.VIEWING.name
                            },
                            label = {
                                val count = visibleDeals.count { it.stage == DealStage.VIEWING.name }
                                Text("🚗 معاينة Viewing ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFF57F17),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_viewing")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.NEGOTIATION.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.NEGOTIATION.name) null else DealStage.NEGOTIATION.name
                            },
                            label = {
                                val count = visibleDeals.count { it.stage == DealStage.NEGOTIATION.name }
                                Text("🤝 مفاوضات Negotiation ($count)")
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF0288D1),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_negotiation")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.DONE.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.DONE.name) null else DealStage.DONE.name
                            },
                            label = { Text("🏆 تم البيع Won ($wonCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFF2E7D32),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_won")
                        )

                        FilterChip(
                            selected = selectedStatusFilter == DealStage.LOST.name,
                            onClick = {
                                selectedStatusFilter = if (selectedStatusFilter == DealStage.LOST.name) null else DealStage.LOST.name
                            },
                            label = { Text("❌ ضائع Lost ($lostCount)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFC62828),
                                selectedLabelColor = Color.White
                            ),
                            modifier = Modifier.testTag("filter_status_lost")
                        )
                    }
                }
            }

            // Lead Source Filter Chips Row
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "فلترة حسب مصدر العميل (Lead Source):",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        LeadSource.values().forEach { source ->
                            val isSelected = selectedSourceFilter == source.name
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedSourceFilter = if (isSelected) null else source.name
                                },
                                label = {
                                    val icon = when (source) {
                                        LeadSource.META_ADS -> "📘 "
                                        LeadSource.REFERRAL -> "👥 "
                                        LeadSource.WALK_IN -> "🏢 "
                                        LeadSource.COLD_CALL -> "📞 "
                                        LeadSource.RETURNING -> "🔄 "
                                    }
                                    Text(icon + source.arabicName, fontSize = 12.sp)
                                }
                            )
                        }
                    }
                }
            }

            // Lead Cards List
            if (filteredLeads.isEmpty()) {
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
                                text = "لا توجد نتائج مطابقة للبحث أو الفلتر",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "جرّب تغيير حالة العميل أو مسح نص البحث",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            OutlinedButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedStatusFilter = null
                                    selectedSourceFilter = null
                                    selectedRepFilter = null
                                }
                            ) {
                                Text("إعادة ضبط الفلاتر")
                            }
                        }
                    }
                }
            } else {
                items(filteredLeads, key = { it.id }) { lead ->
                    IncomingLeadCard(
                        lead = lead,
                        onCardClick = { onLeadClick(lead) },
                        onCallClick = {
                            if (lead.phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${lead.phone}"))
                                context.startActivity(intent)
                            }
                        },
                        onWhatsAppClick = {
                            if (lead.phone.isNotBlank()) {
                                val cleanPhone = lead.phone.replace("+", "").replace(" ", "").trim()
                                val formatted = if (cleanPhone.startsWith("0")) "2$cleanPhone" else cleanPhone
                                val msg = "مرحباً أستاذ ${lead.clientName}، بخصوص اهتمامك بسيارة ${lead.carModel} من دريم كار أوتو لون"
                                val url = "https://wa.me/$formatted?text=${Uri.encode(msg)}"
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                try {
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "واتساب غير مثبت", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onStageAdvance = { nextStage ->
                            viewModel.updateDealStage(lead.id, nextStage)
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

/**
 * IncomingLeadCard
 *
 * Polished Material 3 Card Component displaying customer name, contact details,
 * lead source badge, car requirements, assigned sales representative, and quick triggers.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun IncomingLeadCard(
    lead: DealEntity,
    onCardClick: () -> Unit,
    onCallClick: () -> Unit,
    onWhatsAppClick: () -> Unit,
    onStageAdvance: (String) -> Unit
) {
    val stageEnum = try {
        DealStage.valueOf(lead.stage)
    } catch (_: Exception) {
        DealStage.PROSPECTING
    }

    // Color definitions for status indicators and badges
    val statusStyle = when (stageEnum) {
        DealStage.PROSPECTING -> LeadCardStatusStyle(
            bg = Color(0xFFEEF2FF),
            border = Color(0xFF6366F1),
            fg = Color(0xFF3730A3),
            icon = "🎯",
            label = "Prospecting • ليد جديد"
        )
        DealStage.QUALIFICATION -> LeadCardStatusStyle(
            bg = Color(0xFFE6FFFA),
            border = Color(0xFF14B8A6),
            fg = Color(0xFF0F766E),
            icon = "📋",
            label = "Qualified • مؤهل"
        )
        DealStage.PROPOSAL -> LeadCardStatusStyle(
            bg = Color(0xFFFFF7ED),
            border = Color(0xFFF97316),
            fg = Color(0xFFC2410C),
            icon = "📄",
            label = "Papers • أوراق التقسيط"
        )
        DealStage.VIEWING -> LeadCardStatusStyle(
            bg = Color(0xFFFEFCE8),
            border = Color(0xFFEAB308),
            fg = Color(0xFFA16207),
            icon = "🚗",
            label = "Viewing • معاينة"
        )
        DealStage.NEGOTIATION -> LeadCardStatusStyle(
            bg = Color(0xFFF0F9FF),
            border = Color(0xFF0284C7),
            fg = Color(0xFF0369A1),
            icon = "🤝",
            label = "Negotiation • مفاوضات"
        )
        DealStage.DONE -> LeadCardStatusStyle(
            bg = Color(0xFFECFDF5),
            border = Color(0xFF10B981),
            fg = Color(0xFF047857),
            icon = "🏆",
            label = "Won • تم البيع"
        )
        DealStage.LOST -> LeadCardStatusStyle(
            bg = Color(0xFFFFF1F2),
            border = Color(0xFFF43F5E),
            fg = Color(0xFFBE123C),
            icon = "❌",
            label = "Lost • فرصة ضائعة"
        )
    }
    val statusBg = statusStyle.bg
    val statusBorder = statusStyle.border
    val statusFg = statusStyle.fg
    val statusIcon = statusStyle.icon
    val statusLabel = statusStyle.label

    val sourceBadgeColor = when (lead.leadSource) {
        LeadSource.META_ADS.name -> Color(0xFF1877F2)
        LeadSource.REFERRAL.name -> Color(0xFF00897B)
        LeadSource.WALK_IN.name -> Color(0xFF7B1FA2)
        LeadSource.COLD_CALL.name -> Color(0xFFF57C00)
        else -> Color(0xFF546E7A)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
            .testTag("lead_card_${lead.id}"),
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
                // Top Row: Customer Avatar & Name + Prominent Status Badge
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
                                .background(statusBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = lead.clientName.trim().take(1).uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = statusFg
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = lead.clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = lead.phone,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Prominent Status Badge
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = statusBg,
                        border = BorderStroke(1.dp, statusBorder.copy(alpha = 0.5f)),
                        modifier = Modifier.testTag("lead_status_badge_${lead.id}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(statusBorder)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "$statusIcon $statusLabel",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusFg,
                                fontSize = 11.5.sp
                            )
                        }
                    }
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // Vehicle & Price Details
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
                            text = if (lead.carModel.isNotBlank()) lead.carModel else "سيارة غير محددة",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (lead.amount > 0.0) {
                        Text(
                            text = FormattingUtils.formatEgp(lead.amount),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                // Lead Source + Sales Rep + Down Payment Badges
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Lead Source Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = sourceBadgeColor.copy(alpha = 0.12f),
                        border = BorderStroke(1.dp, sourceBadgeColor.copy(alpha = 0.3f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(sourceBadgeColor)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            val sourceName = when (lead.leadSource) {
                                LeadSource.META_ADS.name -> "إعلانات فيسبوك (Meta)"
                                LeadSource.REFERRAL.name -> "ترشيح عميل (Referral)"
                                LeadSource.WALK_IN.name -> "زيارة المعرض (Walk-in)"
                                LeadSource.COLD_CALL.name -> "اتصال خارجي (Outbound)"
                                LeadSource.RETURNING.name -> "عميل مكرر (Returning)"
                                else -> lead.leadSource
                            }
                            Text(
                                text = sourceName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Medium,
                                color = sourceBadgeColor
                            )
                        }
                    }

                    // Interest Level Badge
                    val (intBg, intFg, intLabel) = when (lead.interestLevel) {
                        InterestLevel.HOT.name -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "🔥 مهتم جداً (Hot)")
                        InterestLevel.WARM.name -> Triple(Color(0xFFFFF8E1), Color(0xFFF57F17), "⚡ بيفكر (Warm)")
                        else -> Triple(Color(0xFFECEFF1), Color(0xFF546E7A), "❄️ عادي (Cold)")
                    }
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = intBg
                    ) {
                        Text(
                            text = intLabel,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = intFg,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }

                    // Sales Rep Badge
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Person,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "المسؤول: ${lead.salesRep}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Financing Partner / Down Payment
                    if (lead.downPayment > 0.0 || lead.installmentPartner.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFF3E5F5)
                        ) {
                            Text(
                                text = "${lead.installmentPartner} • مقدم ${FormattingUtils.formatEgp(lead.downPayment)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF6A1B9A),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Notes / Follow up date snippet
                if (lead.notes.isNotBlank() || lead.followUpDate.isNotBlank()) {
                    val displayText = if (lead.followUpDate.isNotBlank()) "⏰ ${lead.followUpDate} • ${lead.notes}" else lead.notes
                    Text(
                        text = displayText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Quick Triggers Row (Call, WhatsApp, Details)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Call Button
                        OutlinedButton(
                            onClick = onCallClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colorScheme.primary
                            ),
                            modifier = Modifier.testTag("lead_call_${lead.id}")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = "اتصال", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اتصال", fontSize = 12.sp)
                        }

                        // WhatsApp Button
                        Button(
                            onClick = onWhatsAppClick,
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF25D366),
                                contentColor = Color.White
                            ),
                            modifier = Modifier.testTag("lead_whatsapp_${lead.id}")
                        ) {
                            Icon(Icons.Default.Message, contentDescription = "واتساب", modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("واتساب", fontSize = 12.sp)
                        }
                    }

                    // Details Button
                    IconButton(
                        onClick = onCardClick,
                        modifier = Modifier.testTag("lead_details_${lead.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowForward,
                            contentDescription = "عرض التفاصيل",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

private data class LeadCardStatusStyle(
    val bg: Color,
    val border: Color,
    val fg: Color,
    val icon: String,
    val label: String
)
