package com.example.myexpenses.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.myexpenses.core.ui.theme.Accents
import com.example.myexpenses.core.ui.theme.BgBase
import com.example.myexpenses.core.ui.theme.BgElev3
import com.example.myexpenses.core.ui.theme.BorderDefault
import com.example.myexpenses.core.ui.theme.InterFamily
import com.example.myexpenses.core.ui.theme.SerifFamily
import com.example.myexpenses.core.ui.theme.Spacing
import com.example.myexpenses.core.ui.theme.TextDisabled
import com.example.myexpenses.core.ui.theme.TextFaint
import com.example.myexpenses.core.ui.theme.TextMuted
import com.example.myexpenses.core.ui.theme.TextPrimary
import com.example.myexpenses.core.ui.theme.TextTertiary
import kotlinx.coroutines.delay

@Composable
fun RegisterNameScreen(
    onRegistrationComplete: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        delay(400)
        focusRequester.requestFocus()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgBase)
            .statusBarsPadding()
            .navigationBarsPadding()
            .imePadding()
    ) {
        // Step indicator
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "STEP 1 OF 1",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                letterSpacing = 1.0.sp
            )
        }

        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 32.dp)
        ) {
            Spacer(Modifier.height(Spacing.xxxl))

            // WELCOME eyebrow
            Text(
                text = "WELCOME",
                style = MaterialTheme.typography.labelSmall,
                color = Accents.Amber,
            )

            Spacer(Modifier.height(14.dp))

            // Title in Instrument Serif
            Text(
                text = "What should we\ncall you?",
                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 38.sp),
                color = TextPrimary,
            )

            Spacer(Modifier.height(14.dp))

            Text(
                text = "Just your first name. We'll use it to personalize your dashboard.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextTertiary,
            )

            Spacer(Modifier.height(48.dp))

            // Bottom-border text field (Instrument Serif)
            BasicTextField(
                value = viewModel.name,
                onValueChange = viewModel::onNameChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequester),
                textStyle = TextStyle(
                    fontFamily = SerifFamily,
                    fontSize = 32.sp,
                    color = TextPrimary,
                    letterSpacing = (-0.3).sp,
                ),
                cursorBrush = SolidColor(Accents.Amber),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Done,
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (viewModel.canProceed) viewModel.completeOnboarding(onRegistrationComplete)
                    }
                ),
                decorationBox = { innerTextField ->
                    Column {
                        Box(modifier = Modifier.padding(bottom = 12.dp)) {
                            if (viewModel.name.isEmpty()) {
                                Text(
                                    text = "Your name",
                                    fontFamily = SerifFamily,
                                    fontSize = 32.sp,
                                    color = TextFaint,
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                            innerTextField()
                        }
                        // Animated bottom border
                        HorizontalDivider(
                            thickness = 1.5.dp,
                            color = if (viewModel.name.isNotEmpty()) Accents.Amber else BorderDefault,
                        )
                    }
                }
            )

            Spacer(Modifier.height(10.dp))

            // Privacy hint
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Outlined.Lock,
                    contentDescription = null,
                    tint = TextFaint,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = "Stays on your device. Never sent anywhere.",
                    fontSize = 12.sp,
                    fontFamily = InterFamily,
                    color = TextFaint,
                )
            }
        }

        // Continue CTA
        Box(modifier = Modifier.padding(horizontal = 24.dp, vertical = 24.dp)) {
            Button(
                onClick = { viewModel.completeOnboarding(onRegistrationComplete) },
                enabled = viewModel.canProceed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Accents.Amber,
                    contentColor = BgBase,
                    disabledContainerColor = BgElev3,
                    disabledContentColor = TextDisabled,
                )
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.labelLarge,
                )
                Spacer(Modifier.size(8.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}
