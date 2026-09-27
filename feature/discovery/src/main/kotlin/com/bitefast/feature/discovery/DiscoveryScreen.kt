package com.bitefast.feature.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.EmptyState
import com.bitefast.core.designsystem.component.ErrorState
import com.bitefast.core.designsystem.component.RestaurantCard
import com.bitefast.core.designsystem.component.shimmerBrush
import com.bitefast.core.designsystem.theme.OrangePrimary

// ─── Route Entry Point ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryRoute(
    onNavigateToDetail: (String) -> Unit,
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiscoveryUiEffect.NavigateToDetail -> onNavigateToDetail(effect.restaurantId)
                is DiscoveryUiEffect.ShowSnackbar -> { /* TODO: Snackbar host */ }
            }
        }
    }

    DiscoveryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    uiState: DiscoveryUiState,
    onEvent: (DiscoveryUiEvent) -> Unit,
) {
    Scaffold(
        topBar = { DiscoveryTopBar() },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEvent(DiscoveryUiEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                // ── Search Bar ────────────────────────────────────────────────
                item(key = "search_bar") {
                    DiscoverySearchBar(
                        query = uiState.searchQuery,
                        onQueryChanged = { onEvent(DiscoveryUiEvent.SearchQueryChanged(it)) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    )
                }

                // ── Category Chips ────────────────────────────────────────────
                item(key = "category_chips") {
                    CategoryChips(
                        categories = FOOD_CATEGORIES,
                        selected = uiState.selectedCategory,
                        onSelect = { onEvent(DiscoveryUiEvent.SelectCategory(it)) },
                        modifier = Modifier.padding(bottom = 8.dp),
                    )
                }

                // ── Section Header ────────────────────────────────────────────
                item(key = "section_header") {
                    AnimatedVisibility(
                        visible = !uiState.isLoading,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = if (uiState.searchQuery.isBlank()) "Gợi ý cho bạn 🍽️"
                                else "Kết quả tìm kiếm",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(Modifier.width(8.dp))
                            if (uiState.restaurants.isNotEmpty()) {
                                Text(
                                    text = "(${uiState.restaurants.size})",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }

                // ── Content: Loading / Error / Empty / List ───────────────────
                when {
                    uiState.isLoading -> {
                        items(count = 5, key = { "shimmer_$it" }) {
                            ShimmerRestaurantCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }

                    uiState.errorMessage != null -> {
                        item(key = "error_state") {
                            ErrorState(
                                message = uiState.errorMessage,
                                onRetry = { onEvent(DiscoveryUiEvent.Refresh) },
                                modifier = Modifier.padding(top = 32.dp),
                            )
                        }
                    }

                    uiState.restaurants.isEmpty() -> {
                        item(key = "empty_state") {
                            EmptyState(
                                title = if (uiState.searchQuery.isNotBlank())
                                    "Khong tim thay: ${uiState.searchQuery}"
                                else
                                    "Chua co nha hang nao trong khu vuc",
                                subtitle = if (uiState.searchQuery.isNotBlank())
                                    "Thu tu khoa khac hoac chon danh muc khac"
                                else
                                    "Vui long thu lai sau",
                                modifier = Modifier.padding(top = 32.dp),
                            )
                        }

                    }
                    else -> {
                        items(
                            items = uiState.restaurants,
                            key = { it.id },
                        ) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                onClick = { onEvent(DiscoveryUiEvent.ClickRestaurant(restaurant.id)) },
                                modifier = Modifier
                                    .padding(horizontal = 16.dp, vertical = 6.dp)
                                    .animateItem(),
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoveryTopBar() {
    TopAppBar(
        title = {
            Column {
                Text(
                    text = "BiteFast 🍔",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(2.dp))
                    Text(
                        text = "Hồ Chí Minh",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
        ),
    )
}

// ─── Search Bar ───────────────────────────────────────────────────────────────

@Composable
private fun DiscoverySearchBar(
    query: String,
    onQueryChanged: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Tìm món ăn, nhà hàng...") },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Biểu tượng tìm kiếm") },
        shape = RoundedCornerShape(24.dp),
        singleLine = true,
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = OrangePrimary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
        ),
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = "Thanh tìm kiếm nhà hàng và món ăn" },
    )
}

// ─── Category Chips ───────────────────────────────────────────────────────────

@Composable
private fun CategoryChips(
    categories: List<String>,
    selected: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(categories, key = { it }) { category ->
            FilterChip(
                selected = selected == category,
                onClick = { onSelect(category) },
                label = { Text(category, style = MaterialTheme.typography.labelLarge) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = androidx.compose.ui.graphics.Color.White,
                ),
                modifier = Modifier.semantics {
                    contentDescription = if (selected == category)
                        "Danh mục $category đang được chọn"
                    else
                        "Chọn danh mục $category"
                },
            )
        }
    }
}

// ─── Shimmer Skeleton Card ────────────────────────────────────────────────────

@Composable
private fun ShimmerRestaurantCard(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp)
                .clip(RoundedCornerShape(16.dp))
                .shimmerBrush()
        )
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.65f)
                .height(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerBrush()
        )
        Spacer(Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth(0.45f)
                .height(12.dp)
                .clip(RoundedCornerShape(4.dp))
                .shimmerBrush()
        )
        Spacer(Modifier.height(8.dp))
    }
}

