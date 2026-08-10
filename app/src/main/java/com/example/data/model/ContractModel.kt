package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ContractType(
    val title: String,
    val subtitle: String,
    val description: String,
    val iconName: String,
    val popularTag: String
) {
    SOFTWARE_DEV(
        title = "Software Development Agreement",
        subtitle = "For Web, Mobile App & SaaS Developers",
        description = "Freelance Software Engineers, Full-Stack Devs, & Technical Agencies",
        iconName = "Code",
        popularTag = "Most Popular"
    ),
    GRAPHIC_DESIGN(
        title = "Graphic & Logo Design Contract",
        subtitle = "For Designers, Illustrators & UI/UX Creators",
        description = "Brand Designers, UI/UX Specialists, Illustrators & Visual Artists",
        iconName = "Palette",
        popularTag = "Essential"
    ),
    SOCIAL_MEDIA(
        title = "Social Media Management Agreement",
        subtitle = "For SMMs, Content Creators & Growth Agencies",
        description = "Social Media Managers, Copywriters, Content Strategists & Agencies",
        iconName = "Share",
        popularTag = "Recurring Income"
    )
}

enum class FieldType {
    TEXT, NUMBER, CURRENCY, DATE, MULTILINE, DROPDOWN
}

data class FormField(
    val id: String,
    val label: String,
    val placeholder: String,
    val fieldType: FieldType = FieldType.TEXT,
    val value: String = "",
    val isRequired: Boolean = true,
    val helperText: String = "",
    val options: List<String> = emptyList()
)

@Entity(tableName = "contracts")
data class ContractEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val type: String, // ContractType.name
    val partyA: String,
    val partyB: String,
    val partyAEmail: String = "",
    val partyBEmail: String = "",
    val jurisdictionRegion: String = "US - Federal & State Law",
    val formValuesJson: String,
    val generatedDraftText: String,
    val signatureBase64: String? = null,
    val partyBSignatureBase64: String? = null,
    val signatureTimestamp: Long? = null,
    val partyBSignatureTimestamp: Long? = null,
    val signatureHash: String? = null,
    val remoteSigningToken: String? = null,
    val pdfPath: String? = null,
    val status: String = "DRAFT", // DRAFT, SIGNED, EXPORTED, PENDING_REMOTE_SIGN
    val isCloudSynced: Boolean = true,
    val isEncrypted: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

