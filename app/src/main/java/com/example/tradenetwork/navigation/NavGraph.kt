package com.example.tradenetwork.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.tradenetwork.ui.screens.SearchPlaceholder
import com.example.tradenetwork.ui.screens.chat.ChatScreen
import com.example.tradenetwork.ui.screens.chat.ConversationsScreen
import com.example.tradenetwork.ui.screens.details.ProductDetailsScreen
import com.example.tradenetwork.ui.screens.details.SellerProfileScreen
import com.example.tradenetwork.ui.screens.favorites.FavoritesScreen
import com.example.tradenetwork.ui.screens.forgotpassword.ForgotPasswordScreen
import com.example.tradenetwork.ui.screens.home.HomeScreen
import com.example.tradenetwork.ui.screens.listing.CreateListingScreen
import com.example.tradenetwork.ui.screens.listing.EditListingScreen
import com.example.tradenetwork.ui.screens.listing.MyListingsScreen
import com.example.tradenetwork.ui.screens.login.LoginScreen
import com.example.tradenetwork.ui.screens.profile.AccountSettingsScreen
import com.example.tradenetwork.ui.screens.profile.EditProfileScreen
import com.example.tradenetwork.ui.screens.profile.ProfileScreen
import com.example.tradenetwork.ui.screens.register.RegisterScreen
import com.example.tradenetwork.ui.screens.search.SearchScreen
import com.example.tradenetwork.ui.screens.report.MyReportsScreen
import com.example.tradenetwork.ui.screens.splash.SplashScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Register.route) {
            RegisterScreen(navController = navController)
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
        composable(Screen.Search.route) {
            SearchScreen(navController = navController)
        }
        composable(Screen.Sell.route) {
            CreateListingScreen(navController = navController)
        }
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController = navController)
        }
        composable(Screen.Profile.route) {
            ProfileScreen(navController = navController)
        }
        composable(Screen.Conversations.route) {
            ConversationsScreen(navController = navController)
        }
        composable(Screen.MyReports.route) {
            MyReportsScreen(navController = navController)
        }
        composable(Screen.CreateListing.route) {
            CreateListingScreen(navController = navController)
        }
        composable(Screen.MyListings.route) {
            MyListingsScreen(navController = navController)
        }
        composable(Screen.EditProfile.route) {
            EditProfileScreen(navController = navController)
        }
        composable(Screen.AccountSettings.route) {
            AccountSettingsScreen(navController = navController)
        }
        composable(
            route = Screen.EditListing.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            EditListingScreen(navController = navController, productId = productId)
        }
        composable(
            route = Screen.SellerProfile.route,
            arguments = listOf(navArgument("sellerId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sellerId = backStackEntry.arguments?.getString("sellerId") ?: ""
            SellerProfileScreen(navController = navController, sellerId = sellerId)
        }
        composable(
            route = Screen.Chat.route,
            arguments = listOf(navArgument("conversationId") { type = NavType.StringType })
        ) { backStackEntry ->
            val conversationId = backStackEntry.arguments?.getString("conversationId") ?: ""
            ChatScreen(navController = navController, conversationId = conversationId)
        }
        composable(
            route = Screen.ProductDetails.route,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            ProductDetailsScreen(navController = navController, productId = productId)
        }
    }
}
