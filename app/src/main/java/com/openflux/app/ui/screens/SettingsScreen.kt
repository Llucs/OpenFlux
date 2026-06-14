package com.openflux.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openflux.app.ui.theme.DarkGray
import com.openflux.app.ui.theme.LightGray
import com.openflux.app.ui.theme.MediumGray
import com.openflux.app.ui.theme.NearBlack
import com.openflux.app.ui.theme.PureBlack
import com.openflux.app.ui.theme.PureWhite
import com.openflux.app.ui.viewmodel.AppSettings
import com.openflux.app.ui.viewmodel.SettingsViewModel
import androidx.compose.foundation.layout.Arrangement

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        TopAppBar(
            title = {
                Text("Settings", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            SettingsSection("API Configuration") {
                SettingsRow("API Key") {
                    TextField(
                        value = settings.apiKey,
                        onValueChange = { viewModel.updateSettings(settings.copy(apiKey = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = PureWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkGray,
                            unfocusedContainerColor = DarkGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PureWhite
                        ),
                        singleLine = true
                    )
                }
                SettingsRow("Model") {
                    TextField(
                        value = settings.model,
                        onValueChange = { viewModel.updateSettings(settings.copy(model = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = PureWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkGray,
                            unfocusedContainerColor = DarkGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PureWhite
                        ),
                        singleLine = true
                    )
                }
                SettingsRow("Base URL") {
                    TextField(
                        value = settings.baseUrl,
                        onValueChange = { viewModel.updateSettings(settings.copy(baseUrl = it)) },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = TextStyle(
                            color = PureWhite,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        ),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = DarkGray,
                            unfocusedContainerColor = DarkGray,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            cursorColor = PureWhite
                        ),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection("Token Management") {
                SettingsRow("Auto Compact Session") {
                    Switch(
                        checked = settings.autoCompact,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(autoCompact = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = MediumGray,
                            uncheckedThumbColor = LightGray,
                            uncheckedTrackColor = DarkGray
                        )
                    )
                }
                SettingsCompactThreshold("Compact Threshold", settings.compactThreshold) { newVal ->
                    viewModel.updateSettings(settings.copy(compactThreshold = newVal))
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            SettingsSection("Behavior") {
                SettingsRow("Dark Mode") {
                    Switch(
                        checked = settings.darkMode,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(darkMode = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = MediumGray,
                            uncheckedThumbColor = LightGray,
                            uncheckedTrackColor = DarkGray
                        )
                    )
                }
                SettingsRow("Stream Responses") {
                    Switch(
                        checked = settings.streamResponse,
                        onCheckedChange = { viewModel.updateSettings(settings.copy(streamResponse = it)) },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = PureWhite,
                            checkedTrackColor = MediumGray,
                            uncheckedThumbColor = LightGray,
                            uncheckedTrackColor = DarkGray
                        )
                    )
                }
                SettingsCompactThreshold("Max Iterations", settings.maxIterations) { newVal ->
                    viewModel.updateSettings(settings.copy(maxIterations = newVal))
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                )
            ) {
                Text("Save & Close", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun SettingsSection(title: String, content: @Composable () -> Unit) {
    Text(
        text = title,
        color = PureWhite,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = NearBlack),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            content()
        }
    }
}

@Composable
private fun SettingsRow(label: String, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = LightGray,
            fontSize = 14.sp
        )
        trailing()
    }
}

@Composable
private fun SettingsCompactThreshold(label: String, value: Int, onValueChange: (Int) -> Unit) {
    var textVal by remember(value) { mutableStateOf(value.toString()) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = LightGray,
            fontSize = 14.sp
        )
        TextField(
            value = textVal,
            onValueChange = { newVal ->
                textVal = newVal
                newVal.toIntOrNull()?.let { onValueChange(it) }
            },
            modifier = Modifier.width(100.dp),
            textStyle = TextStyle(
                color = PureWhite,
                fontFamily = FontFamily.Monospace,
                fontSize = 13.sp
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkGray,
                unfocusedContainerColor = DarkGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PureWhite
            ),
            singleLine = true
        )
    }
}
