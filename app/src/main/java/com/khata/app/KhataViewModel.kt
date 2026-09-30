package com.khata.app

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.khata.app.data.Customer
import com.khata.app.data.CustomerSummary
import com.khata.app.data.KhataDb
import com.khata.app.data.Profile
import com.khata.app.data.Settings
import com.khata.app.data.Txn
import com.khata.app.data.TxnWithCustomer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class KhataViewModel(app: Application) : AndroidViewModel(app) {
    private val dao = KhataDb.get(app).dao()
    private val prefs = app.getSharedPreferences("khata_prefs", Context.MODE_PRIVATE)

    val summaries: StateFlow<List<CustomerSummary>> =
        dao.summaries().stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val allTxns: StateFlow<List<TxnWithCustomer>> =
        dao.allTxns().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val profile: StateFlow<Profile> =
        dao.profile().map { it ?: Profile() }.stateIn(viewModelScope, SharingStarted.Eagerly, Profile())

    // Room "settings" table is the source of truth; prefs only caches it so the theme is right on first frame.
    val theme: StateFlow<String> =
        dao.settings().map { it?.theme ?: "SYSTEM" }
            .stateIn(viewModelScope, SharingStarted.Eagerly, prefs.getString("theme", "SYSTEM") ?: "SYSTEM")

    fun summary(id: Long): Flow<CustomerSummary?> = dao.summary(id)
    fun txns(id: Long): Flow<List<Txn>> = dao.txnsFor(id)

    suspend fun getCustomer(id: Long): Customer? = dao.getCustomer(id)
    suspend fun getTxn(id: Long): Txn? = dao.getTxn(id)

    fun saveCustomer(id: Long, name: String, phone: String, note: String, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val savedId = if (id > 0) {
                val old = dao.getCustomer(id) ?: Customer(id = id, name = name)
                dao.updateCustomer(old.copy(name = name, phone = phone, note = note))
                id
            } else {
                dao.insertCustomer(Customer(name = name, phone = phone, note = note))
            }
            onSaved(savedId)
        }
    }

    fun deleteCustomer(id: Long) {
        viewModelScope.launch { dao.deleteCustomer(id) }
    }

    fun saveTxn(t: Txn) {
        viewModelScope.launch {
            if (t.id > 0) dao.updateTxn(t) else dao.insertTxn(t.copy(id = 0))
        }
    }

    fun deleteTxn(id: Long) {
        viewModelScope.launch { dao.deleteTxn(id) }
    }

    fun saveProfile(p: Profile) {
        viewModelScope.launch { dao.saveProfile(p.copy(id = 1)) }
    }

    fun setTheme(value: String) {
        prefs.edit().putString("theme", value).apply()
        viewModelScope.launch { dao.saveSettings(Settings(id = 1, theme = value)) }
    }
}
