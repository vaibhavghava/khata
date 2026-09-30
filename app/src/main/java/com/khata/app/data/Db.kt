package com.khata.app.data

import android.content.Context
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "customers")
data class Customer(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String = "",
    val note: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

/** Amounts are stored in paise (1 rupee = 100 paise) as Long: no floating point anywhere. */
@Entity(
    tableName = "transactions",
    foreignKeys = [ForeignKey(
        entity = Customer::class,
        parentColumns = ["id"],
        childColumns = ["customerId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("customerId")]
)
data class Txn(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long,
    val type: String,          // RECEIVED or GIVEN
    val amount: Long,          // paise, always > 0
    val date: String,          // yyyy-MM-dd
    val time: String,          // HH:mm
    val note: String = ""
)

@Entity(tableName = "profile")
data class Profile(
    @PrimaryKey val id: Int = 1,
    val name: String = "",
    val businessName: String = "",
    val phone: String = "",
    val address: String = ""
)

@Entity(tableName = "settings")
data class Settings(
    @PrimaryKey val id: Int = 1,
    val theme: String = "SYSTEM"   // LIGHT, DARK, SYSTEM
)

data class CustomerSummary(
    val id: Long,
    val name: String,
    val phone: String,
    val note: String,
    val createdAt: Long,
    val totalReceived: Long,
    val totalGiven: Long
)

val CustomerSummary.balance: Long get() = totalReceived - totalGiven

data class TxnWithCustomer(
    val id: Long,
    val customerId: Long,
    val type: String,
    val amount: Long,
    val date: String,
    val time: String,
    val note: String,
    val customerName: String
)

const val SUMMARY_SELECT =
    "SELECT c.id AS id, c.name AS name, c.phone AS phone, c.note AS note, c.createdAt AS createdAt, " +
    "COALESCE(SUM(CASE WHEN t.type = 'RECEIVED' THEN t.amount ELSE 0 END), 0) AS totalReceived, " +
    "COALESCE(SUM(CASE WHEN t.type = 'GIVEN' THEN t.amount ELSE 0 END), 0) AS totalGiven " +
    "FROM customers c LEFT JOIN transactions t ON t.customerId = c.id "

@Dao
interface KhataDao {
    // Customers
    @Query(SUMMARY_SELECT + "GROUP BY c.id ORDER BY c.name COLLATE NOCASE ASC")
    fun summaries(): Flow<List<CustomerSummary>>

    @Query(SUMMARY_SELECT + "WHERE c.id = :id GROUP BY c.id")
    fun summary(id: Long): Flow<CustomerSummary?>

    @Query("SELECT * FROM customers WHERE id = :id")
    suspend fun getCustomer(id: Long): Customer?

    @Insert
    suspend fun insertCustomer(c: Customer): Long

    @Update
    suspend fun updateCustomer(c: Customer)

    @Query("DELETE FROM customers WHERE id = :id")
    suspend fun deleteCustomer(id: Long)

    // Transactions
    @Query("SELECT * FROM transactions WHERE customerId = :customerId ORDER BY date ASC, time ASC, id ASC")
    fun txnsFor(customerId: Long): Flow<List<Txn>>

    @Query(
        "SELECT t.id AS id, t.customerId AS customerId, t.type AS type, t.amount AS amount, " +
        "t.date AS date, t.time AS time, t.note AS note, c.name AS customerName " +
        "FROM transactions t INNER JOIN customers c ON c.id = t.customerId " +
        "ORDER BY t.date DESC, t.time DESC, t.id DESC"
    )
    fun allTxns(): Flow<List<TxnWithCustomer>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTxn(id: Long): Txn?

    @Insert
    suspend fun insertTxn(t: Txn): Long

    @Update
    suspend fun updateTxn(t: Txn)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteTxn(id: Long)

    // Profile & settings
    @Query("SELECT * FROM profile WHERE id = 1")
    fun profile(): Flow<Profile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveProfile(p: Profile)

    @Query("SELECT * FROM settings WHERE id = 1")
    fun settings(): Flow<Settings?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSettings(s: Settings)
}

@Database(
    entities = [Customer::class, Txn::class, Profile::class, Settings::class],
    version = 1,
    exportSchema = false
)
abstract class KhataDb : RoomDatabase() {
    abstract fun dao(): KhataDao

    companion object {
        @Volatile
        private var instance: KhataDb? = null

        fun get(context: Context): KhataDb =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext, KhataDb::class.java, "khata.db"
                ).build().also { instance = it }
            }
    }
}
