package com.openflux.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.openflux.app.ui.theme.LightGray
import com.openflux.app.ui.theme.MediumGray
import com.openflux.app.ui.theme.NearBlack
import com.openflux.app.ui.theme.PureBlack
import com.openflux.app.ui.theme.PureWhite
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExplorerScreen(
    onBack: () -> Unit
) {
    var currentPath by remember { mutableStateOf("/") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PureBlack)
    ) {
        TopAppBar(
            title = {
                Column {
                    Text("File Explorer", color = PureWhite, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    Text(
                        text = currentPath,
                        color = MediumGray,
                        fontSize = 11.sp
                    )
                }
            },
            navigationIcon = {
                IconButton(onClick = {
                    val parent = File(currentPath).parent
                    if (parent != null) currentPath = parent
                    else onBack()
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PureWhite)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = PureBlack)
        )

        val currentDir = File(currentPath)
        val items = remember(currentPath) {
            currentDir.listFiles()?.filter { !it.name.startsWith(".") }?.sortedBy {
                if (it.isDirectory) 0 else 1
            }?.map { it.name } ?: emptyList()
        }

        if (items.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Empty directory", color = MediumGray, fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(items) { name ->
                    val file = File(currentPath, name)
                    val isDir = file.isDirectory()

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (isDir) {
                                    currentPath = file.absolutePath
                                }
                            }
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isDir) Icons.Default.Folder else Icons.Default.Description,
                            contentDescription = null,
                            tint = if (isDir) PureWhite else LightGray,
                            modifier = Modifier.padding(end = 12.dp)
                        )
                        Column {
                            Text(
                                text = name,
                                color = PureWhite,
                                fontSize = 14.sp,
                                fontWeight = if (isDir) FontWeight.Medium else FontWeight.Normal
                            )
                            if (!isDir) {
                                Text(
                                    text = formatSize(file.length()),
                                    color = MediumGray,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun formatSize(bytes: Long): String {
    return when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "${bytes / 1024} KB"
        else -> "${"%.1f".format(bytes.toDouble() / (1024 * 1024))} MB"
    }
}
