package com.example.ui.dialogs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.SalesRepTargetEntity
import com.example.ui.util.FormattingUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CommissionCalculatorDialog(
    targets: List<SalesRepTargetEntity>,
    onDismiss: () -> Unit
) {
    var selectedRepName by remember { mutableStateOf(targets.firstOrNull()?.name ?: "Custom") }
    var salesVolumeText by remember { mutableStateOf("4300000") }
    var customRateText by remember { mutableStateOf("0.2") }
    var repExpanded by remember { mutableStateOf(false) }

    val salesVolume = salesVolumeText.toDoubleOrNull() ?: 0.0
    val selectedTarget = targets.find { it.name.equals(selectedRepName, ignoreCase = true) }

    val (effectiveRate, tierLabel) = if (selectedTarget != null) {
        when {
            salesVolume >= selectedTarget.bestCaseTarget -> Pair(selectedTarget.bestCaseRate, "Best Case Tier (≥ ${FormattingUtils.formatCompact(selectedTarget.bestCaseTarget)})")
            salesVolume >= selectedTarget.baseTarget -> Pair(selectedTarget.baseRate, "Base Tier (≥ ${FormattingUtils.formatCompact(selectedTarget.baseTarget)})")
            salesVolume >= selectedTarget.worstCaseTarget -> Pair(selectedTarget.worstCaseRate, "Worst Case Tier (≥ ${FormattingUtils.formatCompact(selectedTarget.worstCaseTarget)})")
            else -> Pair(selectedTarget.worstCaseRate, "Under Minimum Target")
        }
    } else {
        val r = (customRateText.toDoubleOrNull() ?: 0.5) / 100.0
        Pair(r, "Custom Commission Rate")
    }

    val repCommission = salesVolume * effectiveRate
    val companyGrossRevenue = salesVolume * 0.025 // 2.5% standard brokerage fee
    val netAfterCommission = companyGrossRevenue - repCommission

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 6.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.Calculate,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Commission Calculator",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Select Sales Rep Scheme
                ExposedDropdownMenuBox(
                    expanded = repExpanded,
                    onExpandedChange = { repExpanded = !repExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRepName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sales Rep Scheme") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = repExpanded,
                        onDismissRequest = { repExpanded = false }
                    ) {
                        targets.forEach { target ->
                            DropdownMenuItem(
                                text = { Text("${target.name} (Worst: ${FormattingUtils.formatPercent(target.worstCaseRate)}, Base: ${FormattingUtils.formatPercent(target.baseRate)}, Best: ${FormattingUtils.formatPercent(target.bestCaseRate)})") },
                                onClick = {
                                    selectedRepName = target.name
                                    repExpanded = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = { Text("Custom Percentage") },
                            onClick = {
                                selectedRepName = "Custom"
                                repExpanded = false
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = salesVolumeText,
                    onValueChange = { salesVolumeText = it },
                    label = { Text("Sales Achievement Volume (£ / EGP)") },
                    leadingIcon = { Icon(Icons.Default.Payments, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                if (selectedRepName == "Custom") {
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = customRateText,
                        onValueChange = { customRateText = it },
                        label = { Text("Custom Rate (%)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Results Card
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Calculation Outcome",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = tierLabel,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Applicable Rate:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FormattingUtils.formatPercent(effectiveRate),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Sales Rep Commission:",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = FormattingUtils.formatCurrency(repCommission),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Gross Brokerage (2.5%):",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = FormattingUtils.formatCurrency(companyGrossRevenue),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Net Company Revenue:",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                            Text(
                                text = FormattingUtils.formatCurrency(netAfterCommission),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Close")
                }
            }
        }
    }
}
