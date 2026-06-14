package com.openflux.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HorizontalRule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openflux.app.agent.AgentState
import com.openflux.app.model.PlanItemStatus
import com.openflux.app.ui.components.InputBar
import com.openflux.app.ui.components.MessageBubble
import com.openflux.app.ui.components.TokenDisplay
import com.openflux.app.ui.theme.AccentGreen
import com.openflux.app.ui.theme.DarkGray
import com.openflux.app.ui.theme.LightGray
import com.openflux.app.ui.theme.MediumGray
import com.openflux.app.ui.theme.NearBlack
import com.openflux.app.ui.theme.PureBlack
import com.openflux.app.ui.theme.PureWhite
import com.openflux.app.ui.theme.WarningYellow
import com.openflux.app.ui.viewmodel.ChatViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    onOpenTerminal: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenFiles: () -> Unit
) {
    val messages by viewModel.messages.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val agentState by viewModel.agentState.collectAsState()
    val tokenUsage by viewModel.tokenUsage.collectAsState()
    val listState = rememberLazyListState()
    var showMenu by remember { mutableStateOf(false) }
    val plan = viewModel.plan

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("OpenFlux", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (agentState != AgentState.IDLE) {
                        Text(
                            text = when (agentState) {
                                AgentState.THINKING -> "Thinking..."
                                AgentState.EXECUTING_TOOL -> "Executing tool..."
                                AgentState.COMPACTING -> "Compacting session..."
                                AgentState.ERROR -> "Error occurred"
                                else -> ""
                            },
                            color = PureWhite.copy(alpha = 0.6f),
                            fontSize = 12.sp
                        )
                    }
                }
            },
            actions = {
                IconButton(onClick = { viewModel.newSession() }) {
                    Icon(Icons.Default.Add, contentDescription = "New session", tint = PureWhite)
                }
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = PureWhite)
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Terminal") },
                        onClick = { showMenu = false; onOpenTerminal() }
                    )
                    DropdownMenuItem(
                        text = { Text("File Explorer") },
                        onClick = { showMenu = false; onOpenFiles() }
                    )
                    DropdownMenuItem(
                        text = { Text("Settings") },
                        onClick = { showMenu = false; onOpenSettings() }
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = PureBlack
            )
        )

        TokenDisplay(usage = tokenUsage)

        if (plan.items.isNotEmpty()) {
            PlanDisplay(plan = viewModel.plan)
        }

        if (messages.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "OpenFlux",
                        color = PureWhite,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Autonomous AI Agent for Android",
                        color = PureWhite.copy(alpha = 0.5f),
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(messages) { message ->
                    MessageBubble(message = message)
                }
                if (agentState == AgentState.THINKING) {
                    item {
                        ThinkingIndicator()
                    }
                }
            }
        }

        InputBar(
            input = inputText,
            onInputChange = { viewModel.updateInput(it) },
            onSend = { viewModel.sendMessage() },
            onCancel = { viewModel.cancelResponse() },
            state = agentState
        )
    }
}

@Composable
fun PlanDisplay(
    plan: com.openflux.app.model.Plan,
    modifier: Modifier = Modifier
) {
    val completedCount = plan.items.count { it.status == PlanItemStatus.COMPLETED }
    val totalCount = plan.items.size
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkGray)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Plan",
                color = PureWhite,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "$completedCount / $totalCount",
                color = LightGray,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        plan.items.forEach { item ->
            val icon = when (item.status) {
                PlanItemStatus.COMPLETED -> Icons.Default.CheckCircle
                PlanItemStatus.IN_PROGRESS -> Icons.Default.PlayCircle
                PlanItemStatus.CANCELLED -> Icons.Default.HorizontalRule
                PlanItemStatus.PENDING -> Icons.Default.RadioButtonUnchecked
            }
            val iconColor = when (item.status) {
                PlanItemStatus.COMPLETED -> AccentGreen
                PlanItemStatus.IN_PROGRESS -> WarningYellow
                PlanItemStatus.CANCELLED -> MediumGray
                PlanItemStatus.PENDING -> LightGray
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.padding(end = 6.dp)
                )
                Text(
                    text = item.content,
                    color = if (item.status == PlanItemStatus.CANCELLED) MediumGray else PureWhite.copy(alpha = 0.9f),
                    fontSize = 12.sp,
                    maxLines = 1
                )
            }
        }
    }
}

@Composable
fun ThinkingIndicator() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Text(
            text = "OpenFlux is thinking...",
            color = PureWhite.copy(alpha = 0.5f),
            fontSize = 13.sp
        )
    }
}
