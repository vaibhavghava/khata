package com.khata.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.khata.app.ui.AboutScreen
import com.khata.app.ui.AllTransactionsScreen
import com.khata.app.ui.CalcIcon
import com.khata.app.ui.CalculatorScreen
import com.khata.app.ui.CustomerFormScreen
import com.khata.app.ui.CustomersScreen
import com.khata.app.ui.KhataTheme
import com.khata.app.ui.LedgerScreen
import com.khata.app.ui.MoreScreen
import com.khata.app.ui.ProfileScreen
import com.khata.app.ui.SettingsScreen
import com.khata.app.ui.TxnFormScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { KhataApp() }
    }
}

private data class Tab(val route: String, val label: String, val icon: ImageVector)

private val tabs = listOf(
    Tab("customers", "Customers", Icons.Default.Person),
    Tab("calculator", "Calculator", CalcIcon),
    Tab("more", "More", Icons.Default.Menu),
    Tab("profile", "Profile", Icons.Default.AccountCircle)
)

@Composable
fun KhataApp() {
    val vm: KhataViewModel = viewModel()
    val theme by vm.theme.collectAsStateWithLifecycle()
    val dark = when (theme) {
        "DARK" -> true
        "LIGHT" -> false
        else -> isSystemInDarkTheme()
    }
    val activity = LocalContext.current as ComponentActivity
    SideEffect {
        val transparent = android.graphics.Color.TRANSPARENT
        val style = if (dark) SystemBarStyle.dark(transparent) else SystemBarStyle.light(transparent, transparent)
        activity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
    }
    KhataTheme(dark) {
        Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
            AppNav(vm)
        }
    }
}

@Composable
private fun AppNav(vm: KhataViewModel) {
    val nav = rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = tabs.any { it.route == route }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo("customers") { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(nav, startDestination = "customers", modifier = Modifier.padding(pad)) {
            composable("customers") {
                CustomersScreen(
                    vm,
                    onAdd = { nav.navigate("customerForm/-1") },
                    onOpen = { nav.navigate("ledger/$it") }
                )
            }
            composable("calculator") { CalculatorScreen() }
            composable("more") {
                MoreScreen(
                    onAllTransactions = { nav.navigate("allTxns") },
                    onSettings = { nav.navigate("settings") },
                    onAbout = { nav.navigate("about") }
                )
            }
            composable("profile") { ProfileScreen(vm) }

            composable("ledger/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                val id = it.arguments?.getLong("id") ?: -1L
                LedgerScreen(
                    vm = vm,
                    customerId = id,
                    onBack = { nav.popBackStack() },
                    onEditCustomer = { nav.navigate("customerForm/$id") },
                    onAddTxn = { type -> nav.navigate("txnForm/$id/$type/-1") },
                    onEditTxn = { txnId, type -> nav.navigate("txnForm/$id/$type/$txnId") },
                    onCustomerDeleted = { nav.popBackStack("customers", false) }
                )
            }
            composable("customerForm/{id}", arguments = listOf(navArgument("id") { type = NavType.LongType })) {
                val id = it.arguments?.getLong("id") ?: -1L
                CustomerFormScreen(
                    vm = vm,
                    customerId = id,
                    onBack = { nav.popBackStack() },
                    onSavedNew = { newId ->
                        nav.navigate("ledger/$newId") {
                            popUpTo("customerForm/{id}") { inclusive = true }
                        }
                    },
                    onDeleted = { nav.popBackStack("customers", false) }
                )
            }
            composable(
                "txnForm/{customerId}/{type}/{txnId}",
                arguments = listOf(
                    navArgument("customerId") { type = NavType.LongType },
                    navArgument("type") { type = NavType.StringType },
                    navArgument("txnId") { type = NavType.LongType }
                )
            ) {
                TxnFormScreen(
                    vm = vm,
                    customerId = it.arguments?.getLong("customerId") ?: -1L,
                    initialType = it.arguments?.getString("type") ?: TYPE_RECEIVED,
                    txnId = it.arguments?.getLong("txnId") ?: -1L,
                    onBack = { nav.popBackStack() }
                )
            }
            composable("allTxns") {
                AllTransactionsScreen(vm, onBack = { nav.popBackStack() }, onOpen = { nav.navigate("ledger/$it") })
            }
            composable("settings") { SettingsScreen(vm, onBack = { nav.popBackStack() }) }
            composable("about") { AboutScreen(onBack = { nav.popBackStack() }) }
        }
    }
}
