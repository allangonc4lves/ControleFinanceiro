package br.dev.allan.controlefinanceiro.presentation.ui.main

import androidx.compose.material3.FabPosition
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoBottomAppBar
import br.dev.allan.controlefinanceiro.presentation.ui.components.FabBottomBar
import br.dev.allan.controlefinanceiro.presentation.ui.main.components.ZenoTopBar
import br.dev.allan.controlefinanceiro.presentation.ui.screens.navigation.NavHost

@Composable
fun MainScreen() {
    val navController = rememberNavController()

    val currentBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntry?.destination?.route

    Scaffold(
        topBar = { ZenoTopBar() },
        bottomBar = {
            ZenoBottomAppBar(navController = navController)

        },
        floatingActionButton = { FabBottomBar(currentRoute = currentRoute, navController = navController) },
        floatingActionButtonPosition = FabPosition.Center,
    ) { innerPadding ->
        NavHost(navController, innerPadding)
    }
}