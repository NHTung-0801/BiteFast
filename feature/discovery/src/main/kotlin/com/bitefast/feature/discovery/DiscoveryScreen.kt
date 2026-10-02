package com.bitefast.feature.discovery

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.LocalOffer
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Whatshot
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitefast.core.designsystem.component.EmptyState
import com.bitefast.core.designsystem.component.ErrorState
import com.bitefast.core.designsystem.component.HorizontalFoodDishCard
import com.bitefast.core.designsystem.component.RestaurantCard
import com.bitefast.core.designsystem.component.VerticalFoodDishCard
import com.bitefast.core.designsystem.component.shimmerBrush
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.model.Restaurant

// ─── Route Entry Point ────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryRoute(
    onNavigateToDetail: (String) -> Unit,
    onNavigateToSearch: () -> Unit = {},
    viewModel: DiscoveryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DiscoveryUiEffect.NavigateToDetail -> onNavigateToDetail(effect.restaurantId)
                is DiscoveryUiEffect.ShowSnackbar -> { /* Snackbar host */ }
            }
        }
    }

    DiscoveryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        onNavigateToSearch = onNavigateToSearch,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoveryScreen(
    uiState: DiscoveryUiState,
    onEvent: (DiscoveryUiEvent) -> Unit,
    onNavigateToSearch: () -> Unit = {},
) {
    val topRatedListState = rememberLazyListState()
    val nearbyListState = rememberLazyListState()

    Scaffold(
        topBar = {
            DiscoveryTopBar(onSearchClick = onNavigateToSearch)
        },
    ) { paddingValues ->
        PullToRefreshBox(
            isRefreshing = uiState.isRefreshing,
            onRefresh = { onEvent(DiscoveryUiEvent.Refresh) },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Crossfade(
                targetState = uiState.isLoading,
                animationSpec = tween(300),
                label = "DiscoveryLoadingCrossfade"
            ) { loading ->
                if (loading) {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        item(key = "skeleton_search_bar") {
                            DiscoverySearchBar(
                                query = uiState.searchQuery,
                                onQueryChanged = { onEvent(DiscoveryUiEvent.SearchQueryChanged(it)) },
                                onSearchBarClick = onNavigateToSearch,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }
                        item(key = "skeleton_category_chips") {
                            CategoryChips(
                                categories = FOOD_CATEGORIES,
                                selected = uiState.selectedCategory,
                                onSelect = { onEvent(DiscoveryUiEvent.SelectCategory(it)) },
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }
                        item(key = "shimmer_row_1") {
                            ShimmerHorizontalSection(title = "Đánh giá cao ⭐")
                        }
                        item(key = "shimmer_row_2") {
                            ShimmerHorizontalSection(title = "Gần bạn 📍")
                        }
                        items(count = 3, key = { "shimmer_list_$it" }) {
                            ShimmerRestaurantCard(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp),
                    ) {
                        // ── Search Bar ────────────────────────────────────────────────
                        item(key = "search_bar") {
                            DiscoverySearchBar(
                                query = uiState.searchQuery,
                                onQueryChanged = { onEvent(DiscoveryUiEvent.SearchQueryChanged(it)) },
                                onSearchBarClick = onNavigateToSearch,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            )
                        }

                        // ── Category Chips ────────────────────────────────────────────
                        item(key = "category_chips") {
                            CategoryChips(
                                categories = FOOD_CATEGORIES,
                                selected = uiState.selectedCategory,
                                onSelect = { onEvent(DiscoveryUiEvent.SelectCategory(it)) },
                                modifier = Modifier.padding(bottom = 4.dp),
                            )
                        }

                        // ── Sort Chips ────────────────────────────────────────────────
                        item(key = "sort_chips") {
                            SortChipsRow(
                                selectedSort = uiState.selectedSortOption,
                                onSelectSort = { onEvent(DiscoveryUiEvent.SelectSortOption(it)) },
                                modifier = Modifier.padding(bottom = 6.dp),
                            )
                        }

                        // ── Top Promo Banner Carousel (Tự động trượt & Chỉ số chấm) ───
                        if (uiState.searchQuery.isBlank() && uiState.promoBanners.isNotEmpty()) {
                            item(key = "promo_banner_carousel") {
                                TopPromoBannerCarousel(
                                    banners = uiState.promoBanners,
                                    onBannerClick = { onEvent(DiscoveryUiEvent.ClickPromoBanner(it)) },
                                    modifier = Modifier.padding(vertical = 6.dp)
                                )
                            }
                        }

                // ── Error State ───────────────────────────────────────────────
                if (uiState.errorMessage != null) {
                    item(key = "error_state") {
                        ErrorState(
                            message = uiState.errorMessage,
                            onRetry = { onEvent(DiscoveryUiEvent.Refresh) },
                            modifier = Modifier.padding(top = 32.dp),
                        )
                    }
                    return@LazyColumn
                }

                // ── Empty State ───────────────────────────────────────────────
                if (uiState.restaurants.isEmpty()) {
                    item(key = "empty_state") {
                        EmptyState(
                            title = "Không có nhà hàng nào",
                            subtitle = "Không tìm thấy món ăn hoặc nhà hàng phù hợp. Hãy thử chọn danh mục khác nhé!",
                            icon = Icons.Default.Search,
                            actionText = "Đặt lại bộ lọc",
                            onActionClick = {
                                onEvent(DiscoveryUiEvent.SelectCategory(CATEGORY_ALL))
                                onEvent(DiscoveryUiEvent.SearchQueryChanged(""))
                            },
                            modifier = Modifier.padding(top = 48.dp),
                        )
                    }
                    return@LazyColumn
                }

                // Only show sections if not searching
                if (uiState.searchQuery.isBlank()) {
                    // ── Section 1: Món ăn đánh giá cao (Horizontal Carousel với Snap Fling) ──
                    if (uiState.topRatedDishes.isNotEmpty()) {
                        item(key = "top_rated_header") {
                            SectionHeader(
                                title = "Món ăn đánh giá cao",
                                subtitle = "Khách hàng yêu thích nhất ⭐",
                                icon = Icons.Default.Star,
                                iconTint = Color(0xFFFFC107),
                            )
                        }
                        item(key = "top_rated_dishes_row") {
                            LazyRow(
                                state = topRatedListState,
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = topRatedListState),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                items(uiState.topRatedDishes, key = { "top_dish_${it.id}" }) { dish ->
                                    HorizontalFoodDishCard(
                                        name = dish.name,
                                        restaurantName = dish.restaurantName,
                                        price = dish.price,
                                        imageUrl = dish.imageUrl,
                                        rating = dish.rating,
                                        badgeText = dish.badge ?: "⭐ ${"%.1f".format(dish.rating)}",
                                        onClick = { onEvent(DiscoveryUiEvent.ClickDish(dish)) }
                                    )
                                }
                            }
                        }

                        item(key = "divider_1") {
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // ── Section 2: Món ngon gần bạn & Khuyến mãi (Horizontal Carousel với Snap Fling) ──
                    if (uiState.nearbyPromoDishes.isNotEmpty()) {
                        item(key = "nearby_header") {
                            SectionHeader(
                                title = "Món ngon gần bạn & Ưu đãi",
                                subtitle = "Giao hàng siêu tốc 🛵",
                                icon = Icons.Default.LocationOn,
                                iconTint = OrangePrimary,
                            )
                        }
                        item(key = "nearby_dishes_row") {
                            LazyRow(
                                state = nearbyListState,
                                flingBehavior = rememberSnapFlingBehavior(lazyListState = nearbyListState),
                                contentPadding = PaddingValues(horizontal = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                items(uiState.nearbyPromoDishes, key = { "nearby_dish_${it.id}" }) { dish ->
                                    HorizontalFoodDishCard(
                                        name = dish.name,
                                        restaurantName = dish.restaurantName,
                                        price = dish.price,
                                        imageUrl = dish.imageUrl,
                                        rating = dish.rating,
                                        badgeText = dish.badge ?: "${dish.distanceKm}km",
                                        onClick = { onEvent(DiscoveryUiEvent.ClickDish(dish)) }
                                    )
                                }
                            }
                        }

                        item(key = "divider_2") {
                            Spacer(Modifier.height(8.dp))
                        }
                    }

                    // ── Section 3: Toàn bộ món ngon & Nhà hàng tuyển chọn (Vertical Feed) ──
                    item(key = "all_dishes_header") {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Icon(
                                imageVector = Icons.Default.Whatshot,
                                contentDescription = null,
                                tint = OrangePrimary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = if (uiState.selectedCategory == CATEGORY_ALL) "Toàn bộ món ngon tuyển chọn" else "Món ngon: ${uiState.selectedCategory}",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            )
                            Spacer(Modifier.width(8.dp))
                            val displayCount = if (uiState.allDishes.isNotEmpty()) uiState.allDishes.size else uiState.restaurants.size
                            Text(
                                text = "($displayCount)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }

                    if (uiState.allDishes.isNotEmpty()) {
                        val filteredDishes = if (uiState.selectedCategory == CATEGORY_ALL) {
                            uiState.allDishes
                        } else {
                            uiState.allDishes.filter {
                                it.category.contains(uiState.selectedCategory, ignoreCase = true) ||
                                it.name.contains(uiState.selectedCategory, ignoreCase = true)
                            }.ifEmpty { uiState.allDishes }
                        }

                        items(
                            items = filteredDishes,
                            key = { "vertical_dish_${it.id}" },
                        ) { dish ->
                            VerticalFoodDishCard(
                                name = dish.name,
                                restaurantName = dish.restaurantName,
                                description = dish.description,
                                price = dish.price,
                                imageUrl = dish.imageUrl,
                                rating = dish.rating,
                                distanceKm = dish.distanceKm,
                                onClick = { onEvent(DiscoveryUiEvent.ClickDish(dish)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                            )
                        }
                    } else {
                        items(
                            items = uiState.restaurants,
                            key = { it.id },
                        ) { restaurant ->
                            RestaurantCard(
                                restaurant = restaurant,
                                onClick = { onEvent(DiscoveryUiEvent.ClickRestaurant(restaurant.id)) },
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                            )
                        }
                    }
                } else {
                    // ── Search Results ────────────────────────────────────────
                    item(key = "search_header") {
                        AnimatedVisibility(visible = true, enter = fadeIn(), exit = fadeOut()) {
                            Row(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    text = "Kết quả tìm kiếm",
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

                    items(
                        items = uiState.restaurants,
                        key = { it.id },
                    ) { restaurant ->
                        RestaurantCard(
                            restaurant = restaurant,
                            onClick = { onEvent(DiscoveryUiEvent.ClickRestaurant(restaurant.id)) },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        )
                    }
                }
            }
        }
    }
}
}
}

// ─── Section Header ───────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(iconTint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

// ─── Horizontal Restaurant Card (compact, for LazyRow) ───────────────────────

@Composable
private fun HorizontalRestaurantCard(
    restaurant: Restaurant,
    onClick: () -> Unit,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        modifier = modifier
            .width(170.dp)
            .semantics { contentDescription = "Nhà hàng ${restaurant.name}" },
    ) {
        Column {
            Box {
                AsyncImage(
                    model = restaurant.imageUrl.ifEmpty { restaurant.coverImageUrl },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                )
                // Gradient overlay
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(110.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.35f)),
                                startY = 40f
                            )
                        )
                )
                // Badge
                if (badge != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF4CAF50))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
                // Rating chip at bottom-right
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFFFC107),
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(Modifier.width(2.dp))
                        Text(
                            text = "%.1f".format(restaurant.rating),
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                            color = Color.White,
                        )
                    }
                }
            }
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                Text(
                    text = restaurant.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = "${restaurant.estimatedTime} phút • ${restaurant.distance} km",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

// ─── TopBar ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DiscoveryTopBar(
    onSearchClick: () -> Unit = {}
) {
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
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Tìm kiếm",
                    tint = OrangePrimary,
                )
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
    onSearchBarClick: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChanged,
        placeholder = { Text("Tìm món ăn, nhà hàng...") },
        leadingIcon = {
            IconButton(onClick = onSearchBarClick) {
                Icon(Icons.Default.Search, contentDescription = "Biểu tượng tìm kiếm", tint = OrangePrimary)
            }
        },
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

// ─── Sort Chips ───────────────────────────────────────────────────────────────

@Composable
private fun SortChipsRow(
    selectedSort: DiscoverySortOption,
    onSelectSort: (DiscoverySortOption) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier,
    ) {
        items(DiscoverySortOption.entries.toTypedArray(), key = { it.name }) { option ->
            val isSelected = selectedSort == option
            FilterChip(
                selected = isSelected,
                onClick = { onSelectSort(option) },
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                leadingIcon = {
                    val icon = when (option) {
                        DiscoverySortOption.DEFAULT -> Icons.Default.Whatshot
                        DiscoverySortOption.NEARBY -> Icons.Default.LocationOn
                        DiscoverySortOption.RATING -> Icons.Default.Star
                        DiscoverySortOption.FAST_DELIVERY -> Icons.Default.AccessTime
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isSelected) Color.White else OrangePrimary
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                )
            )
        }
    }
}

// ─── Shimmer Skeleton Horizontal Section ─────────────────────────────────────

@Composable
private fun ShimmerHorizontalSection(title: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            items(count = 4) {
                Column(modifier = Modifier.width(170.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmerBrush()
                    )
                    Spacer(Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.75f)
                            .height(14.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerBrush()
                    )
                    Spacer(Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .height(12.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .shimmerBrush()
                    )
                }
            }
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

// ─── Top Promo Banner Carousel ────────────────────────────────────────────────

@Composable
private fun TopPromoBannerCarousel(
    banners: List<PromoBanner>,
    onBannerClick: (PromoBanner) -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    LaunchedEffect(pagerState) {
        while (true) {
            delay(3500)
            if (banners.isNotEmpty()) {
                val next = (pagerState.currentPage + 1) % banners.size
                pagerState.animateScrollToPage(next)
            }
        }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        HorizontalPager(
            state = pagerState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            pageSpacing = 12.dp,
            modifier = Modifier.fillMaxWidth()
        ) { page ->
            val banner = banners[page]
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clickable { onBannerClick(banner) }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.horizontalGradient(
                                colors = banner.gradientColors.map { Color(it) }
                            )
                        )
                        .padding(horizontal = 20.dp, vertical = 14.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 40.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.25f),
                            modifier = Modifier.padding(bottom = 6.dp)
                        ) {
                            Text(
                                text = banner.tag,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = banner.title,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = banner.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Forward arrow indicator in white circle
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.3f),
                        modifier = Modifier
                            .size(36.dp)
                            .align(Alignment.CenterEnd)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Xem ưu đãi",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Pager indicator dots
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(banners.size) { index ->
                val isSelected = pagerState.currentPage == index
                Box(
                    modifier = Modifier
                        .padding(horizontal = 3.dp)
                        .height(5.dp)
                        .width(if (isSelected) 18.dp else 5.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) OrangePrimary else MaterialTheme.colorScheme.outlineVariant
                        )
                )
            }
        }
    }
}

