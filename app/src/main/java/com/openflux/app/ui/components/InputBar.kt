package com.openflux.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openflux.app.agent.AgentState
import com.openflux.app.ui.theme.DarkGray
import com.openflux.app.ui.theme.MediumGray
import com.openflux.app.ui.theme.NearBlack
import com.openflux.app.ui.theme.PureBlack
import com.openflux.app.ui.theme.PureWhite

@Composable
fun InputBar(
    input: String,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
    onCancel: () -> Unit,
    state: AgentState,
    modifier: Modifier = Modifier
) {
    val isProcessing = state == AgentState.THINKING || state == AgentState.EXECUTING_TOOL || state == AgentState.COMPACTING

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NearBlack)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = input,
            onValueChange = onInputChange,
            modifier = Modifier.weight(1f),
            textStyle = TextStyle(color = PureWhite, fontSize = 15.sp),
            placeholder = {
                Text(
                    text = if (isProcessing) "AI is thinking..." else "Message OpenFlux...",
                    color = MediumGray,
                    fontSize = 15.sp
                )
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = DarkGray,
                unfocusedContainerColor = DarkGray,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = PureWhite
            ),
            shape = RoundedCornerShape(24.dp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { if (!isProcessing && input.isNotBlank()) onSend() }),
            enabled = !isProcessing,
            singleLine = true,
            maxLines = 1
        )

        Spacer(modifier = Modifier.width(8.dp))

        if (isProcessing) {
            IconButton(
                onClick = onCancel,
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = PureWhite,
                    contentColor = PureBlack
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop",
                    modifier = Modifier.size(20.dp)
                )
            }
        } else {
            IconButton(
                onClick = onSend,
                enabled = input.isNotBlank(),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = if (input.isNotBlank()) PureWhite else DarkGray,
                    contentColor = if (input.isNotBlank()) PureBlack else MediumGray
                ),
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Send,
                    contentDescription = "Send",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
