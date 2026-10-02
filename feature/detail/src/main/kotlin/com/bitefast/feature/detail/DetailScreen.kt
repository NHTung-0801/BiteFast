package com.bitefast.feature.detail

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.bitefast.core.designsystem.component.shimmerBrush
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.theme.ErrorRed
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.OrangePrimaryDark
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import java.text.DecimalFormat

@Composable
fun DetailRoute(
    restaurantId: String,
    onNavigateBack: () -> Unit = {},
    onNavigateToCart: () -> Unit = {},
    viewModel: DetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is DetailUiEffect.NavigateBack -> onNavigateBack()
                is DetailUiEffect.NavigateToCart -> onNavigateToCart()
                is DetailUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    DetailScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
        onShareRestaurant = { restaurant ->
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, "Kham pha quan ngon ${restaurant.name} tren BiteFast: ${restaurant.address}")
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(sendIntent, "Chia se nha hang"))
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    uiState: DetailUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (DetailUiEvent) -> Unit,
    onShareRestaurant: (Restaurant) -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val ratingSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        modifier = modifier.semantics { contentDescription = "Man hinh chi tiet nha hang va thuc don" },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            AnimatedVisibility(
                visible = uiState.hasCartItems,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                FloatingCartBar(
                    cartItemCount = uiState.cartItemCount,
                    cartTotalPrice = uiState.cartTotalPrice,
                    onClickViewCart = { onEvent(DetailUiEvent.ClickViewCart) }
                )
            }
        }
    ) { padding ->
        Crossfade(
            targetState = uiState.isLoading,
            animationSpec = tween(300),
            label = "DetailLoadingCrossfade"
        ) { loading ->
            if (loading) {
                DetailScreenSkeleton(modifier = Modifier.padding(padding))
            } else {
                val restaurant = uiState.restaurant
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(bottom = if (uiState.hasCartItems) 72.dp else padding.calculateBottomPadding())
                ) {
                    // 1. Hero Image Header with Top Bar Overlay
                    item {
                        if (restaurant != null) {
                            RestaurantHeroHeader(
                                restaurant = restaurant,
                                isFavorite = uiState.isFavorite,
                                onBackClick = { onEvent(DetailUiEvent.ClickBack) },
                                onFavoriteClick = { onEvent(DetailUiEvent.ToggleFavorite) },
                                onShareClick = { onShareRestaurant(restaurant) }
                            )
                        }
                    }

                    // 2. Restaurant Info Card
                    item {
                        if (restaurant != null) {
                            RestaurantInfoSection(restaurant = restaurant)
                        }
                    }

                    // 3. Dual Tabs: [Thực đơn] & [Đánh giá (500+ ★)]
                    item {
                        DetailTabRow(
                            selectedTab = uiState.selectedTab,
                            rating = restaurant?.rating ?: 4.8,
                            onTabSelected = { onEvent(DetailUiEvent.SelectTab(it)) }
                        )
                    }

                    if (uiState.selectedTab == DetailTab.MENU) {
                        // 4. Single Dish Sort & Filter Row
                        item {
                            DishSortFilterRow(
                                selectedSort = uiState.selectedSortOption,
                                onSortSelected = { onEvent(DetailUiEvent.SelectSortOption(it)) }
                            )
                        }

                        // 4.2 Menu Items Section Header
                        item {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Thực đơn món ngon",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.filteredMenuItems.size} món",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // 5. Menu Items List
                        items(
                            items = uiState.filteredMenuItems,
                            key = { it.id }
                        ) { item ->
                            val (ratingScore, reviewCount) = uiState.getRatingFor(item)
                            val isItemFavorite = uiState.favoriteDishIds.contains(item.id)
                            MenuItemRow(
                                item = item,
                                ratingScore = ratingScore,
                                reviewCount = reviewCount,
                                isFavorite = isItemFavorite,
                                onClick = { onEvent(DetailUiEvent.ClickMenuItem(item)) },
                                onQuickAdd = { onEvent(DetailUiEvent.QuickAddToCart(item)) },
                                onRatingClick = { onEvent(DetailUiEvent.ClickMenuItem(item)) },
                                onToggleFavorite = { onEvent(DetailUiEvent.ToggleDishFavorite(item)) }
                            )
                            HorizontalDivider(
                                modifier = Modifier.padding(horizontal = 16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    } else {
                        // ── Tab REVIEWS ──
                        item {
                            RestaurantReviewsSection(
                                restaurant = restaurant,
                                reviews = uiState.filteredReviews,
                                selectedFilter = uiState.selectedReviewFilter,
                                onFilterSelected = { onEvent(DetailUiEvent.SelectReviewFilter(it)) },
                                onWriteReviewClick = {
                                    val firstItem = uiState.menuItems.firstOrNull()
                                    if (firstItem != null) {
                                        onEvent(DetailUiEvent.OpenDishRating(firstItem))
                                    }
                                }
                            )
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }

        // Customization BottomSheet (Chi tiet mon an & Dat mon)
        if (uiState.showCustomizationSheet && uiState.selectedMenuItem != null) {
            val item = uiState.selectedMenuItem
            val (ratingScore, reviewCount) = uiState.getRatingFor(item)
            ModalBottomSheet(
                onDismissRequest = { onEvent(DetailUiEvent.DismissCustomization) },
                sheetState = sheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                CustomizationSheetContent(
                    item = item,
                    ratingScore = ratingScore,
                    reviewCount = reviewCount,
                    reviews = uiState.getReviewsFor(item),
                    quantity = uiState.customizationQuantity,
                    note = uiState.customizationNote,
                    onOpenRating = { onEvent(DetailUiEvent.OpenDishRating(item)) },
                    onQuantityChange = { onEvent(DetailUiEvent.UpdateQuantity(it)) },
                    onNoteChange = { onEvent(DetailUiEvent.UpdateNote(it)) },
                    onConfirmAdd = { onEvent(DetailUiEvent.ConfirmAddToCart) }
                )
            }
        }

        // Dish Rating BottomSheet (Danh gia rieng cho tung mon an - Option 1 + Option 2)
        if (uiState.showDishRatingSheet && uiState.ratingMenuItem != null) {
            ModalBottomSheet(
                onDismissRequest = { onEvent(DetailUiEvent.DismissDishRating) },
                sheetState = ratingSheetState,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                DishRatingSheetContent(
                    item = uiState.ratingMenuItem,
                    selectedStars = uiState.selectedRatingStars,
                    selectedTags = uiState.selectedRatingTags,
                    comment = uiState.ratingComment,
                    onSelectStars = { onEvent(DetailUiEvent.SelectRatingStars(it)) },
                    onToggleTag = { onEvent(DetailUiEvent.ToggleRatingTag(it)) },
                    onCommentChange = { onEvent(DetailUiEvent.UpdateRatingComment(it)) },
                    onSubmitRating = { onEvent(DetailUiEvent.SubmitDishRating) },
                    onDismiss = { onEvent(DetailUiEvent.DismissDishRating) }
                )
            }
        }

        // Conflict Dialog
        if (uiState.showConflictDialog) {
            AlertDialog(
                onDismissRequest = { onEvent(DetailUiEvent.DismissConflictDialog) },
                title = { Text("Tạo giỏ hàng mới?") },
                text = {
                    Text(
                        "Bạn đang có món từ một nhà hàng khác trong giỏ hàng. Thêm món từ nhà hàng này sẽ làm mới toàn bộ giỏ hàng cũ."
                    )
                },
                confirmButton = {
                    Button(
                        onClick = { onEvent(DetailUiEvent.ConfirmConflictAndReplace) },
                        colors = ButtonDefaults.buttonColors(containerColor = OrangePrimary)
                    ) {
                        Text("Xóa giỏ cũ & Thêm mới")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { onEvent(DetailUiEvent.DismissConflictDialog) }) {
                        Text("Hủy bỏ")
                    }
                }
            )
        }
    }
}

// ── 1. Hero Image Header ────────────────────────────────────────────────────

@Composable
private fun RestaurantHeroHeader(
    restaurant: Restaurant,
    isFavorite: Boolean,
    onBackClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(260.dp)
    ) {
        AsyncImage(
            model = restaurant.coverImageUrl.ifBlank { restaurant.imageUrl },
            contentDescription = restaurant.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.65f)
                        )
                    )
                )
        )

        // Top Navigation Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(48.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Quay lại",
                        tint = Color.White
                    )
                }
            }

            Row {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Chia sẻ nhà hàng",
                            tint = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color.Black.copy(alpha = 0.45f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Bỏ yêu thích nhà hàng" else "Yêu thích nhà hàng",
                            tint = if (isFavorite) ErrorRed else Color.White
                        )
                    }
                }
            }
        }
    }
}

// ── 2. Restaurant Info Section ──────────────────────────────────────────────

@Composable
private fun RestaurantInfoSection(
    restaurant: Restaurant,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = restaurant.name,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = restaurant.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Stats row (Rating, Distance, Time)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB300),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurant.rating} (500+)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurant.distance} km",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${restaurant.estimatedTime} phút",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            // Address & status row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = restaurant.address,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (restaurant.isOpen) SuccessGreen.copy(alpha = 0.12f) else ErrorRed.copy(alpha = 0.12f)
                ) {
                    Text(
                        text = if (restaurant.isOpen) "Đang mở cửa" else "Tạm đóng",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (restaurant.isOpen) SuccessGreen else ErrorRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── 3. Category Filter Row ──────────────────────────────────────────────────

@Composable
private fun CategoryFilterRow(
    categories: List<String>,
    selectedCategory: String,
    onCategorySelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(categories) { category ->
            val isSelected = category == selectedCategory
            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(category) },
                label = {
                    Text(
                        text = category,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ── 3.1. Detail Tab Row (Thực đơn / Đánh giá) ───────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailTabRow(
    selectedTab: DetailTab,
    rating: Double,
    onTabSelected: (DetailTab) -> Unit,
    modifier: Modifier = Modifier
) {
    PrimaryTabRow(
        selectedTabIndex = if (selectedTab == DetailTab.MENU) 0 else 1,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = OrangePrimary,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
    ) {
        Tab(
            selected = selectedTab == DetailTab.MENU,
            onClick = { onTabSelected(DetailTab.MENU) },
            text = {
                Text(
                    text = "Thực đơn",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selectedTab == DetailTab.MENU) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
        Tab(
            selected = selectedTab == DetailTab.REVIEWS,
            onClick = { onTabSelected(DetailTab.REVIEWS) },
            text = {
                Text(
                    text = "Đánh giá ($rating ★)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = if (selectedTab == DetailTab.REVIEWS) FontWeight.Bold else FontWeight.Medium
                )
            }
        )
    }
}

// ── 3.2. Dish Sort Filter Row ───────────────────────────────────────────────

@Composable
private fun DishSortFilterRow(
    selectedSort: DishSortOption,
    onSortSelected: (DishSortOption) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.fillMaxWidth().padding(vertical = 4.dp)
    ) {
        items(DishSortOption.entries.toTypedArray(), key = { it.name }) { option ->
            val isSelected = selectedSort == option
            FilterChip(
                selected = isSelected,
                onClick = { onSortSelected(option) },
                label = {
                    Text(
                        text = option.label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = OrangePrimary,
                    selectedLabelColor = Color.White
                )
            )
        }
    }
}

// ── 3.3. Restaurant Reviews Section ─────────────────────────────────────────

@Composable
private fun RestaurantReviewsSection(
    restaurant: Restaurant?,
    reviews: List<DishReview>,
    selectedFilter: String = "Tất cả",
    onFilterSelected: (String) -> Unit = {},
    onWriteReviewClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Overall score & rating breakdown
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: big score
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(end = 16.dp)
                ) {
                    Text(
                        text = "%.1f".format(restaurant?.rating ?: 4.8),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimaryDark
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        repeat(5) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = Color(0xFFFFB800),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "500+ đánh giá",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Vertical Divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(70.dp)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )

                Spacer(modifier = Modifier.width(16.dp))

                // Right: progress bars for 5, 4, 3, 2, 1 stars
                Column(modifier = Modifier.weight(1f)) {
                    RatingBarRow(stars = 5, progress = 0.85f, count = "85%")
                    RatingBarRow(stars = 4, progress = 0.12f, count = "12%")
                    RatingBarRow(stars = 3, progress = 0.02f, count = "2%")
                    RatingBarRow(stars = 2, progress = 0.01f, count = "1%")
                    RatingBarRow(stars = 1, progress = 0.00f, count = "0%")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Quick feedback chips (Interactive FilterChips)
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filterOptions = listOf(
                "Tất cả",
                "Món ăn ngon (340)",
                "Giao nhanh (280)",
                "Đóng gói kỹ (195)",
                "5★",
                "4★"
            )
            items(filterOptions) { filter ->
                val isSelected = filter == selectedFilter || (selectedFilter.isBlank() && filter == "Tất cả")
                FilterChip(
                    selected = isSelected,
                    onClick = { onFilterSelected(filter) },
                    label = {
                        Text(
                            text = filter,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary,
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Write review button
        BiteFastButton(
            text = "Viết đánh giá của bạn",
            onClick = onWriteReviewClick,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Nhận xét từ khách hàng thực tế",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Reviews list
        reviews.forEach { review ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(OrangePrimary.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = review.authorName.take(1),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = OrangePrimary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = review.authorName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = review.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Row {
                            repeat(review.ratingStars) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB800),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    if (review.comment.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = review.comment,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (review.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            review.tags.forEach { tag ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = OrangePrimary.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = tag,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = OrangePrimary,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RatingBarRow(
    stars: Int,
    progress: Float,
    count: String
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 1.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "$stars★",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(22.dp)
        )
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = Color(0xFFFFB800),
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = count,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(28.dp)
        )
    }
}

// ── 4. Menu Item Row ────────────────────────────────────────────────────────

@Composable
private fun MenuItemRow(
    item: MenuItem,
    ratingScore: Double,
    reviewCount: Int,
    isFavorite: Boolean = false,
    onClick: () -> Unit,
    onQuickAdd: () -> Unit,
    onRatingClick: () -> Unit,
    onToggleFavorite: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Image thumbnail
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(12.dp))
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            if (item.isPopular) {
                Surface(
                    color = OrangePrimary,
                    shape = RoundedCornerShape(bottomEnd = 8.dp),
                    modifier = Modifier.align(Alignment.TopStart)
                ) {
                    Text(
                        text = "Bán chạy",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.width(14.dp))

        // Info & Price
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            // Dish Rating and Category Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Clickable rating badge directly for this dish
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFFFF8E1))
                        .clickable(onClick = onRatingClick)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Đánh giá món ${item.name}",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "$ratingScore ($reviewCount đánh giá)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                }

                // Category Tag
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = item.category,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = item.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = item.price.formatVnd(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = OrangePrimaryDark
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // Action Buttons: Favorite + Quick Add
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(38.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = if (isFavorite) "Bỏ thích ${item.name}" else "Yêu thích ${item.name}",
                    tint = if (isFavorite) ErrorRed else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(22.dp)
                )
            }

            IconButton(
                onClick = onQuickAdd,
                modifier = Modifier.size(44.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(OrangePrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Thêm món ${item.name} vào giỏ hàng",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ── 5. Customization BottomSheet Content ────────────────────────────────────

@Composable
private fun CustomizationSheetContent(
    item: MenuItem,
    ratingScore: Double,
    reviewCount: Int,
    reviews: List<DishReview> = emptyList(),
    quantity: Int,
    note: String,
    onOpenRating: () -> Unit,
    onQuantityChange: (Int) -> Unit,
    onNoteChange: (String) -> Unit,
    onConfirmAdd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = item.imageUrl,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = item.price.formatVnd(),
                    style = MaterialTheme.typography.titleMedium,
                    color = OrangePrimaryDark,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Dedicated Dish Rating Card (Click to open rating sheet for this specific dish)
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFFFF8E1),
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onOpenRating)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Số sao của món",
                        tint = Color(0xFFFFB800),
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "$ratingScore",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFB45309)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "($reviewCount lượt đánh giá)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF92400E)
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Đánh giá món này",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = OrangePrimary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = OrangePrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        // ── Recent Reviews Section ──
        if (reviews.isNotEmpty()) {
            Spacer(modifier = Modifier.height(14.dp))
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Đánh giá từ thực khách",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                    )
                    Text(
                        text = "${reviews.size} nhận xét",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                reviews.take(2).forEachIndexed { index, review ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    }
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                        .background(OrangePrimary.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = review.authorName.take(1),
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = OrangePrimary
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = review.authorName,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                                )
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                repeat(review.ratingStars) {
                                    Icon(
                                        imageVector = Icons.Default.Star,
                                        contentDescription = null,
                                        tint = Color(0xFFFFB800),
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = review.timeAgo,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (review.comment.isNotBlank()) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = review.comment,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (review.tags.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                review.tags.take(2).forEach { tag ->
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = OrangePrimary.copy(alpha = 0.08f)
                                    ) {
                                        Text(
                                            text = tag,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = OrangePrimary,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Note TextField
        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text("Ghi chú cho nhà hàng (ví dụ: ít cay, không hành)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            maxLines = 2
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Quantity Selector and Confirm Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // [- Qty +]
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(24.dp))
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                IconButton(
                    onClick = { onQuantityChange(-1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Remove, contentDescription = "Giảm số lượng")
                }
                Text(
                    text = "$quantity",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
                IconButton(
                    onClick = { onQuantityChange(1) },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Tăng số lượng")
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Add to Cart Button
            BiteFastButton(
                text = "Thêm • ${(item.price * quantity).formatVnd()}",
                onClick = onConfirmAdd,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── 5.1. Dish Rating BottomSheet Content (Option 1 + Option 2) ───────────────

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun DishRatingSheetContent(
    item: MenuItem,
    selectedStars: Int,
    selectedTags: Set<String>,
    comment: String,
    onSelectStars: (Int) -> Unit,
    onToggleTag: (String) -> Unit,
    onCommentChange: (String) -> Unit,
    onSubmitRating: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val quickTags = listOf(
        "Đậm đà chuẩn vị",
        "Thịt mềm tươi",
        "Nước sốt ngon",
        "Khẩu phần nhiều",
        "Trình bày đẹp",
        "Nóng hổi thơm phức"
    )

    val starDescriptions = mapOf(
        1 to "Rất tệ 😞",
        2 to "Chưa ngon 🙁",
        3 to "Bình thường 😐",
        4 to "Ngon miệng 😊",
        5 to "Tuyệt hảo! 🤩"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Đánh giá món ăn",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Đóng"
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Dish info preview card
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AsyncImage(
                    model = item.imageUrl,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(8.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${item.price.formatVnd()} d",
                        style = MaterialTheme.typography.bodyMedium,
                        color = OrangePrimaryDark,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Interactive Star Rating
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Bạn thấy món ăn này như thế nào?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(10.dp))

            // 5 Interactive Stars
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                (1..5).forEach { star ->
                    IconButton(
                        onClick = { onSelectStars(star) },
                        modifier = Modifier.size(44.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "$star sao",
                            tint = if (star <= selectedStars) Color(0xFFFFB800) else Color(0xFFE0E0E0),
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = starDescriptions[selectedStars] ?: "",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = OrangePrimary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Quick tags
        Text(
            text = "Điểm bạn yêu thích ở món này:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickTags.forEach { tag ->
                val isSelected = selectedTags.contains(tag)
                FilterChip(
                    selected = isSelected,
                    onClick = { onToggleTag(tag) },
                    label = {
                        Text(
                            text = tag,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = OrangePrimary.copy(alpha = 0.15f),
                        selectedLabelColor = OrangePrimaryDark
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Comment Input
        OutlinedTextField(
            value = comment,
            onValueChange = onCommentChange,
            label = { Text("Nhận xét thêm về món ăn (tùy chọn)") },
            placeholder = { Text("Món có vừa miệng không, nguyên liệu có tươi ngon...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            minLines = 3,
            maxLines = 4
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Submit Button
        BiteFastButton(
            text = "Gửi đánh giá món ăn",
            onClick = onSubmitRating,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

// ── 6. Floating Cart Bar ────────────────────────────────────────────────────

@Composable
private fun FloatingCartBar(
    cartItemCount: Int,
    cartTotalPrice: Double,
    onClickViewCart: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(18.dp),
        color = OrangePrimary,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClickViewCart)
                .padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BadgedBox(
                    badge = {
                        Badge(containerColor = Color.White, contentColor = OrangePrimary) {
                            Text(text = "$cartItemCount", fontWeight = FontWeight.Bold)
                        }
                    }
                ) {
                    Icon(
                        imageVector = Icons.Default.ShoppingCart,
                        contentDescription = "Giỏ hàng",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "$cartItemCount món trong giỏ",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                    Text(
                        text = cartTotalPrice.formatVnd(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Xem giỏ hàng",
                    style = MaterialTheme.typography.titleSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun Double.formatVnd(): String {
    val symbols = java.text.DecimalFormatSymbols(java.util.Locale("vi", "VN")).apply {
        groupingSeparator = '.'
    }
    return DecimalFormat("#,###", symbols).format(this) + " đ"
}

@Composable
private fun DetailScreenSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
                .shimmerBrush()
        )
        Column(modifier = Modifier.padding(16.dp)) {
            // Restaurant Title Skeleton
            Box(
                modifier = Modifier
                    .width(220.dp)
                    .height(26.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .shimmerBrush()
            )
            Spacer(modifier = Modifier.height(10.dp))
            // Rating and metadata row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .width(70.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
                Box(
                    modifier = Modifier
                        .width(60.dp)
                        .height(18.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerBrush()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            // Category Pills row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .width(80.dp)
                            .height(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .shimmerBrush()
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            // Menu Items Skeleton
            repeat(4) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .width(160.dp)
                                .height(20.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(0.85f)
                                .height(14.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .width(80.dp)
                                .height(16.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .shimmerBrush()
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Box(
                        modifier = Modifier
                            .size(88.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .shimmerBrush()
                    )
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            }
        }
    }
}

