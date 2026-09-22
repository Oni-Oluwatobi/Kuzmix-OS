package com.kreadivegalaxy.kuzmixos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kreadivegalaxy.kuzmixos.ui.theme.KuzmixOrange

@Composable
fun PurpleCelestialSphere(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(300.dp)
            .drawBehind {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            KuzmixOrange.copy(alpha = 0.5f),
                            KuzmixOrange.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
            }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KuzmixTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    isPassword: Boolean = false,
    modifier: Modifier = Modifier
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.White.copy(alpha = 0.5f), fontSize = 14.sp) },
        singleLine = true,
        visualTransformation = if (isPassword) PasswordVisualTransformation() else androidx.compose.ui.text.input.VisualTransformation.None,
        keyboardOptions = if (isPassword) KeyboardOptions(keyboardType = KeyboardType.Password) else KeyboardOptions.Default,
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Black.copy(alpha = 0.3f),
            unfocusedContainerColor = Color.Black.copy(alpha = 0.3f),
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier.fillMaxWidth()
    )
}

@Composable
fun BootSequenceScreen(
    hardwareTier: HardwareTier,
    deviceModel: String,
    onBootComplete: () -> Unit
) {
    KuzmixLoginScreen(
        hardwareTier = hardwareTier,
        onLoginSuccess = onBootComplete
    )
}
