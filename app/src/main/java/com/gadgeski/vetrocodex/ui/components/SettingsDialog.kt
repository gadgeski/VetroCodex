package com.gadgeski.vetrocodex.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.gadgeski.vetrocodex.data.ClockMode
import com.gadgeski.vetrocodex.ui.theme.BBHBartleFontFamily

/**
 * モード切り替え用の設定ダイアログ
 */
@Composable
fun SettingsDialog(
    currentMode: ClockMode,
    onModeSelected: (ClockMode) -> Unit,
    onDismiss: () -> Unit
) {
    // Vetroの世界観に合わせ、黒基調のダイアログにします
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF111111), // ほぼ黒のグレー
        title = {
            Text(
                text = "SELECT MODE",
                color = Color.White,
                fontFamily = BBHBartleFontFamily,
                fontSize = 20.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // MINIMAL Mode Option
                ModeOptionRow(
                    text = "MINIMAL (Default)",
                    isSelected = currentMode == ClockMode.MINIMAL,
                    onClick = { onModeSelected(ClockMode.MINIMAL) }
                )

                // CYBERPUNK Mode Option
                ModeOptionRow(
                    text = "CYBERPUNK (Flip)",
                    isSelected = currentMode == ClockMode.CYBERPUNK,
                    onClick = { onModeSelected(ClockMode.CYBERPUNK) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "CLOSE",
                    color = Color.White,
                    fontFamily = BBHBartleFontFamily
                )
            }
        }
    )
}

@Composable
fun ModeOptionRow(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = onClick,
            colors = RadioButtonDefaults.colors(
                selectedColor = Color(0xFF00FFFF), // Cyan
                unselectedColor = Color.Gray
            )
        )
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Gray,
            fontFamily = BBHBartleFontFamily,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 16.sp,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}