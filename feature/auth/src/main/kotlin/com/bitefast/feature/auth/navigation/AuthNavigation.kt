package com.bitefast.feature.auth.navigation

import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import com.bitefast.feature.auth.LoginRoute
import com.bitefast.feature.auth.RegisterRoute

const val LOGIN_ROUTE = "auth/login"
const val REGISTER_ROUTE = "auth/register"

fun NavController.navigateToLogin(navOptions: NavOptions? = null) {
    navigate(LOGIN_ROUTE, navOptions)
}

fun NavController.navigateToRegister(navOptions: NavOptions? = null) {
    navigate(REGISTER_ROUTE, navOptions)
}

fun NavGraphBuilder.loginScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    composable(route = LOGIN_ROUTE) {
        LoginRoute(
            onNavigateToHome = onNavigateToHome,
            onNavigateToRegister = onNavigateToRegister
        )
    }
}

fun NavGraphBuilder.registerScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    composable(route = REGISTER_ROUTE) {
        RegisterRoute(
            onNavigateToHome = onNavigateToHome,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}
