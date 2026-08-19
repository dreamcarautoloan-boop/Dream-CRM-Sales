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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BusinessCenter
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.data.model.InteractionOutcome
import com.example.data.model.InteractionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogInteractionDialog(
    initialClientName: String = "",
    initialSalesRep: String = "Nada",
    initialClientId: Long? = null,
    availableReps: List<String> = listOf("Nada", "Esraa", "Nahla", "Alaa", "Marwan", "Kholoud"),
    onDismiss: () -> Unit,
    onLog: (
        clientId: Long?,
        clientName: String,
        salesRep: String,
        interactionType: String,
        outcome: String,
        notes: String
    ) -> Unit
) {
    var clientName by remember { mutableStateOf(initialClientName) }
    var selectedRep by remember { mutableStateOf(initialSalesRep) }
    var selectedType by remember { mutableStateOf(InteractionType.PHONE_CALL.name) }
    var selectedOutcome by remember { mutableStateOf(InteractionOutcome.INTERESTED.name) }
    var notes by remember { mutableStateOf("") }

    var repExpanded by remember { mutableStateOf(false) }
    var typeExpanded by remember { mutableStateOf(false) }
    var outcomeExpanded by remember { mutableStateOf(false) }

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
                            Icons.Default.Call,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Log Call / Activity",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = clientName,
                    onValueChange = { clientName = it },
                    label = { Text("Client Name *") },
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("log_client_name")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Sales Rep
                ExposedDropdownMenuBox(
                    expanded = repExpanded,
                    onExpandedChange = { repExpanded = !repExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = selectedRep,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Sales Agent") },
                        leadingIcon = { Icon(Icons.Default.BusinessCenter, contentDescription = null) },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = repExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = repExpanded,
                        onDismissRequest = { repExpanded = false }
                    ) {
                        availableReps.forEach { rep ->
                            DropdownMenuItem(
                                text = { Text(rep) },
                                onClick = {
                                    selectedRep = rep
                                    repExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Interaction Type
                ExposedDropdownMenuBox(
                    expanded = typeExpanded,
                    onExpandedChange = { typeExpanded = !typeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentTypeObj = InteractionType.entries.find { it.name == selectedType }
                    OutlinedTextField(
                        value = currentTypeObj?.label ?: selectedType,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Activity Type") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = typeExpanded,
                        onDismissRequest = { typeExpanded = false }
                    ) {
                        InteractionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    selectedType = type.name
                                    typeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Outcome
                ExposedDropdownMenuBox(
                    expanded = outcomeExpanded,
                    onExpandedChange = { outcomeExpanded = !outcomeExpanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    val currentOutcomeObj = InteractionOutcome.entries.find { it.name == selectedOutcome }
                    OutlinedTextField(
                        value = currentOutcomeObj?.label ?: selectedOutcome,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Outcome / Status") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = outcomeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = outcomeExpanded,
                        onDismissRequest = { outcomeExpanded = false }
                    ) {
                        InteractionOutcome.entries.forEach { outcome ->
                            DropdownMenuItem(
                                text = { Text(outcome.label) },
                                onClick = {
                                    selectedOutcome = outcome.name
                                    outcomeExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Interaction Notes & Discussion") },
                    leadingIcon = { Icon(Icons.Default.Notes, contentDescription = null) },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            if (clientName.isNotBlank()) {
                                onLog(
                                    initialClientId,
                                    clientName,
                                    selectedRep,
                                    selectedType,
                                    selectedOutcome,
                                    notes
                                )
                                onDismiss()
                            }
                        },
                        enabled = clientName.isNotBlank(),
                        modifier = Modifier.testTag("submit_log_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save Activity")
                    }
                }
            }
        }
    }
}
