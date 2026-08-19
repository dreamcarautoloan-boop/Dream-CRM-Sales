package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class InteractionType(val label: String) {
    PHONE_CALL("Phone Call"),
    WHATSAPP("WhatsApp"),
    SITE_VIEWING("Site Viewing"),
    OFFICE_MEETING("Office Meeting")
}

enum class InteractionOutcome(val label: String) {
    INTERESTED("Interested"),
    FOLLOW_UP("Follow Up Needed"),
    VIEWING_SCHEDULED("Viewing Scheduled"),
    NEGOTIATING("Negotiating Offer"),
    DEAL_CLOSED("Deal Closed"),
    NOT_ANSWERING("Not Answering"),
    NOT_INTERESTED("Not Interested")
}

@Entity(tableName = "call_logs")
data class CallLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clientId: Long? = null,
    val clientName: String,
    val salesRep: String,
    val interactionType: String = InteractionType.PHONE_CALL.name,
    val outcome: String = InteractionOutcome.INTERESTED.name,
    val notes: String = "",
    val month: String = "May",
    val timestamp: Long = System.currentTimeMillis()
)
