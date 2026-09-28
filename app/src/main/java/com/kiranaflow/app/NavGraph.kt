package com.kiranaflow.app

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.kiranaflow.app.ui.screens.billing.BillingScreen
import com.kiranaflow.app.ui.screens.pastbills.PastBillsScreen
import com.kiranaflow.app.ui.screens.stock.StockScreen

object Routes {
    const val BILLING    = "billing"
    const val STOCK      = "stock"
    const val PAST_BILLS = "past_bills"
}

@Composable
fun KiranaFlowNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(
        navController    = navController,
        startDestination = Routes.BILLING
    ) {
        composable(Routes.BILLING) {
            BillingScreen(
                onNavigateToStock      = { navController.navigate(Routes.STOCK) },
                onNavigateToPastBills  = { navController.navigate(Routes.PAST_BILLS) }
            )
        }
        composable(Routes.STOCK) {
            StockScreen(
                onNavigateToBilling   = { navController.navigate(Routes.BILLING) { popUpTo(Routes.BILLING) { inclusive = true } } },
                onNavigateToPastBills = { navController.navigate(Routes.PAST_BILLS) }
            )
        }
        composable(Routes.PAST_BILLS) {
            PastBillsScreen(
                onNavigateToBilling = { navController.navigate(Routes.BILLING) { popUpTo(Routes.BILLING) { inclusive = true } } },
                onNavigateToStock   = { navController.navigate(Routes.STOCK) }
            )
        }
    }
}
