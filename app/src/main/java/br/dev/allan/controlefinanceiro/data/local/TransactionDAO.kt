package br.dev.allan.controlefinanceiro.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import br.dev.allan.controlefinanceiro.domain.model.CategorySum
import br.dev.allan.controlefinanceiro.domain.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<TransactionEntity>>

    // Busca transações do mês OU fixas (desde que criadas antes ou no mês selecionado)
    @Query("""
        SELECT * FROM transactions 
        WHERE (date BETWEEN :start AND :end) 
        OR (isFixed = 1 AND date <= :end)
        ORDER BY date DESC
    """)
    fun getTransactionsByMonth(start: Long, end: Long): Flow<List<Transaction>>

    // Soma Despesas: do mês OU fixas
    @Query("""
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE type = 'EXPENSE' 
    AND (
        -- Caso 1: Transação normal no mês selecionado
        (date BETWEEN :start AND :end) 
        
        -- Caso 2: Transação Fixa (qualquer mês após a criação)
        OR (isFixed = 1 AND date <= :end)
        
        -- Caso 3: Transação Parcelada (dentro do limite de meses)
        OR (
            isInstallment = 1 
            AND date <= :end 
            AND (
                -- Calcula quantos meses se passaram desde a compra
                -- 1 mês = 2629746000 milissegundos (aproximadamente)
                -- Mas no SQL é mais seguro contar a diferença de meses:
                ((:end - date) / 2629746000) < installmentCount
            )
        )
    )
""")
    fun getTotalExpensesByMonth(start: Long, end: Long): Flow<Double?>

    // Soma Receitas: do mês OU fixas
    @Query("""
    SELECT SUM(
        CASE 
            WHEN isInstallment = 1 AND installmentCount > 0 
            THEN amount / installmentCount 
            ELSE amount 
        END
    ) 
    FROM transactions 
    WHERE type = 'INCOME' 
    AND ((date BETWEEN :start AND :end) OR (isFixed = 1 AND date <= :end))
""")
    fun getTotalIncomesByMonth(start: Long, end: Long): Flow<Double?>

    // Soma por categoria
    @Query("""
    SELECT 
        category, 
        SUM(
            CASE 
                WHEN isInstallment = 1 AND installmentCount > 0 
                THEN amount / installmentCount 
                ELSE amount 
            END
        ) as total 
    FROM transactions 
    WHERE type = 'EXPENSE' 
    AND (
        -- 1. Transações comuns do mês
        (date BETWEEN :start AND :end) 
        
        -- 2. Transações fixas (sempre aparecem após a data de criação)
        OR (isFixed = 1 AND date <= :end)
        
        -- 3. Parcelas ativas: 
        -- Aparecem se a data da compra for anterior ao fim do mês atual
        -- E se a diferença de meses for menor que o total de parcelas
        OR (
            isInstallment = 1 
            AND date <= :end 
            AND ((:end - date) / 2629746000) < installmentCount
        )
    )
    GROUP BY category
""")
    fun getExpensesByCategory(start: Long, end: Long): Flow<List<CategorySum>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity)

    @Update
    suspend fun updateTransaction(transaction: TransactionEntity)

    @Delete
    suspend fun deleteTransaction(transaction: TransactionEntity)
}