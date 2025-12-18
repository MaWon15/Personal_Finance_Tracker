package com.example.personal_finance_tracker.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["ownerUid"]),
        Index(value = ["categoryId"]),
        Index(value = ["ownerUid", "dateEpochDay"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val transactionId: Long = 0L,
    val ownerUid: String,
    val amountCents: Long,
    val type: String,
    val note: String? = null,
    val dateEpochDay: Long,
    val categoryId: Long? = null
)
