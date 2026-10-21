# Phase 2: Marketplace Home and Product Feed Implementation Plan

This plan outlines the steps to build the main marketplace feed, including product browsing, categories, and basic search, while reusing the authentication foundation from Phase 1.

## User Review Required

> [!IMPORTANT]
> This phase requires manual addition of test data in your Firebase Firestore console to see products in the app. I will provide the JSON/fields for these test products.

## Proposed Changes

### Configuration & Dependencies

#### [MODIFY] [libs.versions.toml](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/gradle/libs.versions.toml)
Add Coil for image loading.

#### [MODIFY] [build.gradle.kts (App)](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/build.gradle.kts)
Add Coil dependency.

---

### Data Layer

#### [NEW] [Product.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/model/Product.kt)
Define the `Product` data class and `ProductStatus` enum.

#### [NEW] [ProductRepository.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/repository/ProductRepository.kt)
Interface for fetching and filtering products from Firestore.

#### [NEW] [ProductRepositoryImpl.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/data/repository/ProductRepositoryImpl.kt)
Firestore implementation of `ProductRepository`.

---

### Logic Layer (ViewModels)

#### [NEW] [ProductViewModel.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/viewmodel/ProductViewModel.kt)
Manages the marketplace UI state, including category filtering, search, and pull-to-refresh.

---

### UI & Navigation Layer

#### [MODIFY] [Screen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/navigation/Screen.kt)
Add new routes: Search, Sell, Favorites, Profile, ProductDetails.

#### [MODIFY] [NavGraph.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/navigation/NavGraph.kt)
Update with new screens and placeholders.

#### [NEW] [ProductCard.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/components/ProductCard.kt)
Reusable card for product display.

#### [NEW] [CategorySelector.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/components/CategorySelector.kt)
Horizontal scrolling category list.

#### [MODIFY] [HomeScreen.kt](file:///C:/Users/Elias/AndroidStudioProjects/Tradenetwork/app/src/main/java/com/example/tradenetwork/ui/screens/home/HomeScreen.kt)
Replace placeholder with full marketplace UI, search, and bottom navigation.

#### [NEW] Placeholder Screens
Create basic placeholder screens for Search, Sell, Favorites, Profile, and Product Details.

## Verification Plan

### Automated Tests
*   Verify Firestore queries for category filtering.
*   Verify search filtering logic in `ProductViewModel`.

### Manual Verification
1.  **Test Data**: Manually add 3 products to Firestore as instructed.
2.  **Home Feed**: Verify products appear on the Home screen.
3.  **Categories**: Tap different categories and verify the feed updates.
4.  **Search**: Enter a title in the search bar and verify products are filtered.
5.  **Pull-to-Refresh**: Verify the refresh indicator works and triggers a reload.
6.  **Navigation**: Click a product card to navigate to the placeholder details screen. Test bottom navigation icons.
7.  **Empty States**: Clear all products in Firestore and verify "No products available" message.
