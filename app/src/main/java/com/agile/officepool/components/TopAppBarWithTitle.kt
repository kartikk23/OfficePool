package com.agile.officepool.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

import androidx.compose.material3.*
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import com.agile.officepool.ui.theme.RobotoCondensed

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBarWithTitle(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    showTrailingIcon: Boolean = false,
    onTrailingIconClick: (() -> Unit)? = null,
    trailingIcon: ImageVector = Icons.Default.Person,
    contentColor: Color = MaterialTheme.colorScheme.onSurface,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    scrollBehavior: TopAppBarScrollBehavior? = null // <-- Add this
) {
    TopAppBar(
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = contentColor
                )
            }
        },
        title = {
            Text(
                text = title,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = RobotoCondensed,
                color = contentColor,
                style = MaterialTheme.typography.headlineSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        },
        actions = {
            if (showTrailingIcon && onTrailingIconClick != null) {
                IconButton(onClick = onTrailingIconClick) {
                    Icon(
                        imageVector = trailingIcon,
                        contentDescription = "Action",
                        tint = contentColor
                    )
                }
            } else {
                Spacer(modifier = Modifier.width(48.dp)) // Optional spacer to balance layout
            }
        },
        scrollBehavior = scrollBehavior,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.35f), // 👈 color after scroll
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface,
        )
    )
}

