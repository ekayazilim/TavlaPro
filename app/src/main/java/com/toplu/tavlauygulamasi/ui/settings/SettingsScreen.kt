package com.toplu.tavlauygulamasi.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.toplu.tavlauygulamasi.ai.AIDifficulty
import com.toplu.tavlauygulamasi.ui.theme.GameTheme
import com.toplu.tavlauygulamasi.ui.theme.GameThemes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentDifficulty: AIDifficulty,
    onDifficultyChange: (AIDifficulty) -> Unit,
    currentTheme: GameTheme,
    onThemeChange: (GameTheme) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ayarlar") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF1B120E),
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF2D1E16)
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("AI Zorluğu", color = Color.White, style = MaterialTheme.typography.titleLarge)
            AIDifficulty.values().forEach { difficulty ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentDifficulty == difficulty,
                        onClick = { onDifficultyChange(difficulty) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD4AF37))
                    )
                    Text(text = difficulty.name, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
            
            Text("Oyun Teması", color = Color.White, style = MaterialTheme.typography.titleLarge)
            GameThemes.all.forEach { theme ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    RadioButton(
                        selected = currentTheme.name == theme.name,
                        onClick = { onThemeChange(theme) },
                        colors = RadioButtonDefaults.colors(selectedColor = Color(0xFFD4AF37))
                    )
                    Text(text = theme.name, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text("Ses Ayarları", color = Color.White, style = MaterialTheme.typography.titleLarge)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Text("Ses Efektleri", color = Color.White)
                Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFFD4AF37)))
            }
        }
    }
}
