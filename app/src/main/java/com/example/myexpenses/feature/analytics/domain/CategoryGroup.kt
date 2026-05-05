package com.example.myexpenses.feature.analytics.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsBus
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MoreHoriz
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.myexpenses.core.common.ExpenseCategory
import com.example.myexpenses.core.ui.theme.CategoryTone
import com.example.myexpenses.core.ui.theme.CategoryTones

/**
 * Six high-level category groups for the Insights screen donut + legend.
 *
 * Multiple [ExpenseCategory] entries roll up into a single visual group so the
 * breakdown reads as decisions ("where is most of my money going?") rather
 * than the long flat list of every individual category.
 */
enum class CategoryGroup(
    val id: String,
    val displayName: String,
    val icon: ImageVector,
    val tone: CategoryTone,
    val children: List<ExpenseCategory>,
) {
    FOOD("food", "Food", Icons.Outlined.Restaurant, CategoryTones.Food,
        listOf(
            ExpenseCategory.BREAKFAST,
            ExpenseCategory.LUNCH,
            ExpenseCategory.DINNER,
            ExpenseCategory.DINEIN,
            ExpenseCategory.ONLINE_FOOD,
            ExpenseCategory.GROCERY,
        )),
    TRANSPORT("transport", "Transport", Icons.Outlined.DirectionsBus, CategoryTones.Commute,
        listOf(ExpenseCategory.COMMUTE, ExpenseCategory.SMOG)),
    LIVING("living", "Living", Icons.Outlined.Home, CategoryTones.Rent,
        listOf(ExpenseCategory.RENT)),
    HEALTH("health", "Health", Icons.Outlined.Favorite, CategoryTones.Healthcare,
        listOf(ExpenseCategory.HEALTHCARE, ExpenseCategory.INSURANCE)),
    LIFESTYLE("lifestyle", "Lifestyle", Icons.Outlined.ShoppingBag, CategoryTones.Personal,
        listOf(
            ExpenseCategory.PERSONALCARE,
            ExpenseCategory.ENTERTAINMENT,
            ExpenseCategory.SHOPPING,
            ExpenseCategory.UTILITIES,
        )),
    OTHER("other", "Other", Icons.Outlined.MoreHoriz, CategoryTones.Misc,
        listOf(ExpenseCategory.INVESTMENT, ExpenseCategory.MISCELLANEOUS));

    companion object {
        fun forCategory(c: ExpenseCategory): CategoryGroup =
            entries.firstOrNull { c in it.children } ?: OTHER

        fun fromId(id: String): CategoryGroup? = entries.firstOrNull { it.id == id }
    }
}
