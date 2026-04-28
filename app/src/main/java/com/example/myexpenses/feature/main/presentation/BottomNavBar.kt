package com.example.myexpenses.feature.main.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.myexpenses.core.navigation.ext.isTabSelected
import com.example.myexpenses.core.navigation.route.BottomNavItem
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev1
import com.example.myexpenses.core.ui.theme.BgElev2
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.TextMuted

/**
 * The bar's pill height (68dp) plus outer vertical padding (10dp top + 10dp
 * bottom). Excludes the system navigation bar inset — screens should add
 * that on top of this via WindowInsets.navigationBars padding so the value
 * stays correct on both gesture and hardware-button devices.
 *
 * MainScreen pads InnerNavHost by this amount so each tab's content stops
 * above the floating bar without any per-screen wiring.
 */
val BottomNavBarReservedHeight = 88.dp

/**
 * Floating glass-style bottom nav bar.
 *
 * Design:
 *  • Pill with 28dp rounded corners, floats above the system nav with margin
 *  • Background uses a vertical gradient of semi-transparent surface tints
 *    so content scrolling behind shows through subtly (fake-frosted look)
 *  • Hairline white border on top edge gives the "edge of glass" highlight
 *  • Subtle shadow underneath for depth
 *  • Selected tab: amber pill behind icon, label expands in horizontally,
 *    icon tint animates with a spring
 *  • Unselected tabs: just the icon, no label, dim tint — keeps the bar
 *    compact and readable
 */
@Composable
fun BottomNavBar(
    navController: NavController,
    onTabSelected: (BottomNavItem) -> Unit,
    modifier: Modifier = Modifier){

    // Trigger recomposition when the back stack changes (so isTabSelected reads fresh)
    navController.currentBackStackEntryAsState().value

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 10.dp)){
        Row(
            modifier = Modifier
                .fillMaxWidth()
                //Fixed bar height — guarantees no vertical jitter regardless of
                //which tab is selected. 68dp comfortably fits the 26dp icon
                //pill + ~14dp label + spacing without clipping.
                .height(68.dp)
                .shadow(
                    elevation = 18.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = Color.Black.copy(alpha = 0.6f),
                    spotColor = Color.Black.copy(alpha = 0.7f),
                )
                .clip(RoundedCornerShape(28.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            //Top: lighter elevation tint with translucency
                            BgElev2.copy(alpha = 0.88f),
                            //Bottom: slightly deeper to ground the bar
                            BgElev1.copy(alpha = 0.96f),
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    //Vertical gradient border — bright at top (light hitting glass),
                    //fading near bottom (shadow side). Hallmark of glassy UIs.
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.10f),
                            Color.White.copy(alpha = 0.02f),
                        )
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(horizontal = 6.dp),
            verticalAlignment = Alignment.CenterVertically){
            BottomNavItem.entries.forEach { item ->
                val isSelected = navController.isTabSelected(item.graph)
                GlassNavItem(
                    item = item,
                    selected = isSelected,
                    onClick = { onTabSelected(item) },
                    //Equal weight always — selected vs unselected does NOT
                    //change cell width, so adjacent cells never shift when
                    //the user switches tabs.
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun GlassNavItem(
    item: BottomNavItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier){

    val pillBg by animateColorAsState(
        targetValue = if (selected) Accents.Amber.copy(alpha = 0.18f) else Color.Transparent,
        animationSpec = spring(stiffness = Spring.StiffnessMediumLow),
        label = "nav_pill"
    )
    val tint by animateColorAsState(
        targetValue = if (selected) Accents.Amber else TextMuted,
        animationSpec = tween(220),
        label = "nav_tint"
    )

    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    // Each cell is a fixed-size column: a pill containing the icon on top,
    // a constant-height label slot below. The pill's *background colour*
    // animates on selection; nothing in the layout itself changes size.
    // Result: equal-width cells, constant bar height, smooth color-only
    // transition with no jitter from differing label lengths.
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                // Disable the Material ripple — the bar's selection cue is
                // already the springy amber pill behind the icon, plus the
                // color tween. A ripple on top adds visual noise.
                indication = null,
                onClick = {
                    //Light haptic "tick" on tab tap — feels deliberate, no sound.
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onClick()
                },
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)){
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(width = 44.dp, height = 26.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(pillBg)){
            Icon(
                imageVector = item.icon,
                contentDescription = item.label,
                tint = tint,
                modifier = Modifier.size(20.dp)
            )
        }
        //Label — wraps to its natural intrinsic height (no fixed-height
        //clipping). All labels are the same font/size with maxLines=1, so
        //every cell renders identical height regardless of which tab is
        //selected → no jitter.
        Text(
            text = item.label,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontFamily = InterFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
            color = tint,
            maxLines = 1,
        )
    }
}
