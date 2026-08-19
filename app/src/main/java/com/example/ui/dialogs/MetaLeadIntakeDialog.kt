package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.DealEntity
import com.example.data.model.DealStage
import com.example.data.model.InstallmentPartner
import com.example.data.model.LeadSource
import com.example.ui.theme.CrmAmber
import com.example.ui.theme.CrmAmberContainer
import com.example.ui.theme.CrmEmerald
import com.example.ui.theme.CrmEmeraldContainer
import com.example.ui.theme.CrmRose
import com.example.ui.theme.CrmRoseContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetaLeadIntakeDialog(
    availableReps: List<String>,
    existingDuplicate: DealEntity?,
    onCheckDuplicate: (String) -> Unit,
    onClearDuplicateCheck: () -> Unit,
    onDismiss: () -> Unit,
    onSubmitLead: (
        clientName: String,
        phone: String,
        salesRep: String,
        carModel: String,
        budgetOrPrice: Double,
        notes: String
    ) -> Unit
) {
    var clientName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var salesRep by remember { mutableStateOf(availableReps.firstOrNull() ?: "Nada") }
    var carModel by remember { mutableStateOf("") }
    var budgetText by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("Meta Ads Lead: Interested in car loan installment") }
    var repDropdownExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(phone) {
        if (phone.length >= 7) {
            onCheckDuplicate(phone)
        } else {
            onClearDuplicateCheck()
        }
    }

    Dialog(onDismissRequest = {
        onClearDuplicateCheck()
        onDismiss()
    }) {
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Icon(
                                Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Meta Ads Lead Intake (الموديريتور)",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Facebook Ads Direct Ingestion & Deduplication",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    IconButton(onClick = {
                        onClearDuplicateCheck()
                        onDismiss()
                    }) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Deduplication status banner
                if (existingDuplicate != null) {
                    Surface(
                        color = CrmRoseContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Warning, contentDescription = null, tint = CrmRose)
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "تنبيه: العميل مسجل بالفعل في النظام! (Duplicate)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CrmRose
                                )
                                Text(
                                    text = "اسم العميل: ${existingDuplicate.clientName} • مسند إلى: ${existingDuplicate.salesRep} (${existingDuplicate.stage})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFF881337)
                                )
                            }
                        }
                    }
                } else if (phone.length >= 8) {
                    Surface(
                        color = CrmEmeraldContainer,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = CrmEmerald, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "رقم هاتف جديد وغير مكرر (Unique Lead)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = CrmEmerald
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number (رقم الهاتف) *") },
                    placeholder = { Text("010xxxxxxxx") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meta_lead_phone_input")
                )

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client Name (اسم العميل من فيسبوك)") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meta_lead_name_input")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = carModel,
                        onValueChange = { carModel = it },
                        label = { Text("Car Model / Ad Campaign") },
                        placeholder = { Text("e.g. Kia Sportage 2024") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1.2f)
                    )

                    OutlinedTextField(
                        value = budgetText,
                        onValueChange = { budgetText = it },
                        label = { Text("Estimated Budget") },
                        placeholder = { Text("e.g. 1000000") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Assign to Sales Rep
                ExposedDropdownMenuBox(
                    expanded = repDropdownExpanded,
                    onExpandedChange = { repDropdownExpanded = !repDropdownExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = salesRep,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Assign Lead to Sales Rep (توزيع الليد على السيلز)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repDropdownExpanded) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
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

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Campaign Notes / Lead Details") },
                    maxLines = 2,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = {
                        onClearDuplicateCheck()
                        onDismiss()
                    }) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (clientName.isBlank() && phone.isNotBlank()) {
                                clientName = "Meta Lead ($phone)"
                            }
                            val budgetVal = budgetText.toDoubleOrNull() ?: 850_000.0
                            onSubmitLead(clientName, phone, salesRep, carModel, budgetVal, notes)
                            onClearDuplicateCheck()
                            onDismiss()
                        },
                        enabled = phone.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("meta_lead_submit_button")
                    ) {
                        Text(if (existingDuplicate != null) "Save Anyway (متابعة الإدخال)" else "Add Meta Lead (إضافة الليد)")
                    }
                }
            }
        }
    }
}
