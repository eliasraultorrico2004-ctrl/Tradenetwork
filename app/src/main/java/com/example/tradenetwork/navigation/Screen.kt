package com.example.tradenetwork.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object Search : Screen("search")
    object Sell : Screen("sell")
    object Favorites : Screen("favorites")
    object Profile : Screen("profile")
    object ProductDetails : Screen("product_details/{productId}") {
        fun createRoute(productId: String) = "product_details/$productId"
    }
    object CreateListing : Screen("create_listing")
    object MyListings : Screen("my_listings")
    object EditListing : Screen("edit_listing/{productId}") {
        fun createRoute(productId: String) = "edit_listing/$productId"
    }
    object SellerProfile : Screen("seller_profile/{sellerId}") {
        fun createRoute(sellerId: String) = "seller_profile/$sellerId"
    }
    object Chat : Screen("chat/{conversationId}") {
        fun createRoute(conversationId: String) = "chat/$conversationId"
    }
    object Conversations : Screen("conversations")
    object EditProfile : Screen("edit_profile")
    object AccountSettings : Screen("account_settings")
    object MyReports : Screen("my_reports")
}
