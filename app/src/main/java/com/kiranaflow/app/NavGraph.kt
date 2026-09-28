package com.kiranaflow.app

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiranaflow.app.ui.screens.billing.BillingScreen
import com.kiranaflow.app.ui.screens.billing.BillingViewModel
import com.kiranaflow.app.ui.screens.pastbills.PastBillsScreen
import com.kiranaflow.app.ui.screens.speak.SpeakScreen
import com.kiranaflow.app.ui.screens.stock.StockScreen

object Routes {
    const val SPEAK      = "speak"       // Landing: mic button
    const val BILLING    = "billing"     // Active bill list
    const val STOCK      = "stock"       // Dashboard + catalog
    const val PAST_BILLS = "past_bills"
}

@Composable
fun KiranaFlowNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Routes.SPEAK
    ) {
        // ── SPEAK (Home / Landing)
        composable(Routes.SPEAK) {
            val billingVm: BillingViewModel = hiltViewModel()
            val state by billingVm.uiState.collectAsState()

            SpeakScreen(
                statusText       = if (state.isListening) "LISTENING" else "MIC READY",
                lastBillNumber   = "#${(System.currentTimeMillis() % 10000).toInt()}",
                lastBillAmount   = "74",
                onMicTap         = {
                    billingVm.toggleListening()
                    navController.navigate(Routes.BILLING)
                },
                onScan           = { navController.navigate(Routes.BILLING) },
                onNavigateToStock     = { navController.navigate(Routes.STOCK) },
                onNavigateToPastBills = { navController.navigate(Routes.PAST_BILLS) },
                onNavigateToBilling   = { navController.navigate(Routes.BILLING) }
            )
        }

        // ── BILLING (Active bill)
        composable(Routes.BILLING) {
            BillingScreen(
                onNavigateToStock     = { navController.navigate(Routes.STOCK) },
                onNavigateToPastBills = { navController.navigate(Routes.PAST_BILLS) },
                onNavigateToSpeak     = {
                    navController.navigate(Routes.SPEAK) {
                        popUpTo(Routes.SPEAK) { inclusive = true }
                    }
                }
            )
        }

        // ── STOCK Dashboard
        composable(Routes.STOCK) {
            StockScreen(
                onNavigateToBilling   = {
                    navController.navigate(Routes.SPEAK) {
                        popUpTo(Routes.SPEAK) { inclusive = true }
                    }
                },
                onNavigateToPastBills = { navController.navigate(Routes.PAST_BILLS) }
            )
        }

        // ── PAST BILLS
        composable(Routes.PAST_BILLS) {
            PastBillsScreen(
                onNavigateToBilling = {
                    navController.navigate(Routes.SPEAK) {
                        popUpTo(Routes.SPEAK) { inclusive = true }
                    }
                },
                onNavigateToStock = { navController.navigate(Routes.STOCK) }
            )
        }
    }
}
