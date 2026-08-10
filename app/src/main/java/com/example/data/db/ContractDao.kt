package com.example.data.db

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ContractEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractDao {
    @Query("SELECT * FROM contracts ORDER BY updatedAt DESC")
    fun getAllContracts(): Flow<List<ContractEntity>>

    @Query("SELECT * FROM contracts WHERE id = :id LIMIT 1")
    suspend fun getContractById(id: Long): ContractEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContract(contract: ContractEntity): Long

    @Update
    suspend fun updateContract(contract: ContractEntity)

    @Delete
    suspend fun deleteContract(contract: ContractEntity)

    @Query("DELETE FROM contracts WHERE id = :id")
    suspend fun deleteContractById(id: Long)
}
