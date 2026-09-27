package com.bitefast.feature.rating

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.OrangePrimaryDark
import com.bitefast.core.model.Order

@Composable
fun RatingRoute(
    orderId: String,
    onNavigateBack: () -> Unit = {},
    viewModel: RatingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is RatingUiEffect.NavigateBack -> onNavigateBack()
                is RatingUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    RatingScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RatingScreen(
    uiState: RatingUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (RatingUiEvent) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.semantics { contentDescription = "Man hinh danh gia don hang" },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Danh gia don hang",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Quay lai"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    BiteFastButton(
                        text = if (uiState.isSubmitting) "Dang gui..." else "Gui danh gia",
                        onClick = { onEvent(RatingUiEvent.SubmitRating) },
                        enabled = !uiState.isSubmitting,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = OrangePrimary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    OrderSummaryBanner(order = uiState.order, orderId = uiState.orderId)
                }

                // Restaurant Rating Section
                item {
                    RatingCard(
                        title = "Chat luong mon an & Nha hang",
                        subtitle = uiState.order?.restaurantName ?: "Nha hang doi tac",
                        icon = Icons.Default.Restaurant,
                        rating = uiState.restaurantRating,
                        onRatingSelected = { onEvent(RatingUiEvent.SetRestaurantRating(it)) }
                    )
                }

                // Driver Rating Section
                item {
                    RatingCard(
                        title = "Dich vu giao hang cua Tai xe",
                        subtitle = uiState.order?.driverName?.ifBlank { "Nguyen Van Hung" } ?: "Nguyen Van Hung",
                        icon = Icons.Default.TwoWheeler,
                        rating = uiState.driverRating,
                        onRatingSelected = { onEvent(RatingUiEvent.SetDriverRating(it)) }
                    )
                }

                // Tags Section
                item {
                    TagsSelectionCard(
                        availableTags = uiState.availableTags,
                        selectedTags = uiState.selectedTags,
                        onToggleTag = { onEvent(RatingUiEvent.ToggleTag(it)) }
                    )
                }

                // Detailed Comment Section
                item {
                    CommentCard(
                        comment = uiState.comment,
                        onCommentChange = { onEvent(RatingUiEvent.UpdateComment(it)) },
                        isAnonymous = uiState.isAnonymous,
                        onAnonymousChange = { onEvent(RatingUiEvent.ToggleAnonymous(it)) }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

// ── 1. Order Summary Banner ─────────────────────────────────────────────────

@Composable
private fun OrderSummaryBanner(
    order: Order?,
    orderId: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(OrangePrimary.copy(alpha = 0.15f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Restaurant,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = order?.restaurantName ?: "Don hang #${orderId}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Ma don: #${order?.id ?: orderId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ── 2. Interactive Star Rating Card ─────────────────────────────────────────

@Composable
private fun RatingCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    rating: Int,
    onRatingSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = OrangePrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 5 Interactive Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { starIndex ->
                    val isSelected = starIndex <= rating
                    val scale by animateFloatAsState(
                        targetValue = if (isSelected) 1.15f else 1.0f,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                        label = "starScale"
                    )

                    Icon(
                        imageVector = if (isSelected) Icons.Default.Star else Icons.Outlined.Star,
                        contentDescription = "Danh gia $starIndex sao",
                        tint = if (isSelected) Color(0xFFFFB300) else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .size(38.dp)
                            .scale(scale)
                            .clickable { onRatingSelected(starIndex) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Mood description
            Text(
                text = when (rating) {
                    1 -> "Rat te"
                    2 -> "Te"
                    3 -> "Binh thuong"
                    4 -> "Hai long"
                    5 -> "Tuyet voi!"
                    else -> ""
                },
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (rating >= 4) OrangePrimaryDark else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ── 3. Quick Tag Compliments ────────────────────────────────────────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagsSelectionCard(
    availableTags: List<String>,
    selectedTags: Set<String>,
    onToggleTag: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Dieu ban hai long nhat?",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                availableTags.forEach { tag ->
                    val isSelected = selectedTags.contains(tag)
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleTag(tag) },
                        label = { Text(text = tag) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangePrimary.copy(alpha = 0.15f),
                            selectedLabelColor = OrangePrimaryDark
                        )
                    )
                }
            }
        }
    }
}

// ── 4. Detailed Comment & Anonymous Card ────────────────────────────────────

@Composable
private fun CommentCard(
    comment: String,
    onCommentChange: (String) -> Unit,
    isAnonymous: Boolean,
    onAnonymousChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Chia se chi tiet (khong bat buoc)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = comment,
                onValueChange = onCommentChange,
                placeholder = { Text("Mon an ngon mieng, nong hoi. Shipper giao dung hen va rat le phep...") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                shape = RoundedCornerShape(14.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Danh gia an danh",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Ten cua ban se khong hien thi tren danh gia",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isAnonymous,
                    onCheckedChange = onAnonymousChange,
                    colors = SwitchDefaults.colors(checkedThumbColor = OrangePrimary)
                )
            }
        }
    }
}
