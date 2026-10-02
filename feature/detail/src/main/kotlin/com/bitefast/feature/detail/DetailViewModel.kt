package com.bitefast.feature.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.cart.AddToCartResult
import com.bitefast.core.domain.cart.AddToCartUseCase
import com.bitefast.core.domain.cart.ClearCartUseCase
import com.bitefast.core.domain.cart.GetCartUseCase
import com.bitefast.core.domain.favorite.GetFavoritesUseCase
import com.bitefast.core.domain.favorite.ToggleFavoriteUseCase
import com.bitefast.core.domain.restaurant.GetRestaurantDetailUseCase
import com.bitefast.core.model.CartItem
import com.bitefast.core.model.FavoriteType
import com.bitefast.core.model.MenuItem
import com.bitefast.core.model.Restaurant
import com.bitefast.core.common.extension.unaccent
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import javax.inject.Inject

// ── Tabs & Sort Options ──────────────────────────────────────────────────────

enum class DetailTab(val label: String) {
    MENU("Thực đơn"),
    REVIEWS("Đánh giá & Nhận xét")
}

enum class DishSortOption(val label: String) {
    DEFAULT("Tất cả"),
    POPULAR("Bán chạy"),
    PRICE_LOW_TO_HIGH("Giá: Thấp -> Cao"),
    PRICE_HIGH_TO_LOW("Giá: Cao -> Thấp"),
    RATING_HIGH("Đánh giá cao")
}

// ── UiState ─────────────────────────────────────────────────────────────────

data class DishReview(
    val id: String,
    val authorName: String,
    val ratingStars: Int,
    val comment: String,
    val tags: List<String> = emptyList(),
    val timeAgo: String = "Vừa xong"
)

val DEFAULT_DISH_REVIEWS = listOf(
    DishReview(
        id = "rev_1",
        authorName = "Nguyễn Minh T.",
        ratingStars = 5,
        comment = "Món ăn nóng hổi, vị nêm nếm rất vừa miệng chuẩn vị. Đóng gói cẩn thận, giao nhanh!",
        tags = listOf("Đậm đà chuẩn vị", "Nóng hổi thơm phức"),
        timeAgo = "1 ngày trước"
    ),
    DishReview(
        id = "rev_2",
        authorName = "Trần Hoàng L.",
        ratingStars = 5,
        comment = "Khẩu phần nhiều ăn no căng bụng. Thịt mềm tươi ngon, nước chấm ăn kèm rất bắt vị.",
        tags = listOf("Khẩu phần nhiều", "Thịt mềm tươi"),
        timeAgo = "3 ngày trước"
    )
)

data class DetailUiState(
    val isLoading: Boolean = true,
    val restaurantId: String = "",
    val restaurant: Restaurant? = null,
    val menuItems: List<MenuItem> = emptyList(),
    val categories: List<String> = emptyList(),
    val selectedCategory: String = "Tất cả",
    val selectedTab: DetailTab = DetailTab.MENU,
    val selectedSortOption: DishSortOption = DishSortOption.DEFAULT,
    val isFavorite: Boolean = false,
    val favoriteDishIds: Set<String> = emptySet(),
    val selectedReviewFilter: String = "Tất cả",
    val cartItemCount: Int = 0,
    val cartTotalPrice: Double = 0.0,
    val cartItemsFromThisRestaurant: List<CartItem> = emptyList(),
    val showCustomizationSheet: Boolean = false,
    val selectedMenuItem: MenuItem? = null,
    val customizationQuantity: Int = 1,
    val customizationNote: String = "",
    val showConflictDialog: Boolean = false,
    val pendingConflictItem: CartItem? = null,
    val showDishRatingSheet: Boolean = false,
    val ratingMenuItem: MenuItem? = null,
    val selectedRatingStars: Int = 5,
    val selectedRatingTags: Set<String> = emptySet(),
    val ratingComment: String = "",
    val isSubmittingRating: Boolean = false,
    val menuItemRatings: Map<String, Pair<Double, Int>> = emptyMap(),
    val recentDishReviews: Map<String, List<DishReview>> = emptyMap(),
    val errorMessage: String? = null
) : UiState {
    val filteredMenuItems: List<MenuItem>
        get() {
            val categoryFiltered = if (selectedCategory == "Tất cả" || selectedCategory == "Tat ca") {
                menuItems
            } else {
                menuItems.filter { it.category.unaccent() == selectedCategory.unaccent() }
            }
            return when (selectedSortOption) {
                DishSortOption.DEFAULT -> categoryFiltered
                DishSortOption.POPULAR -> categoryFiltered.sortedByDescending { it.isPopular }
                DishSortOption.PRICE_LOW_TO_HIGH -> categoryFiltered.sortedBy { it.price }
                DishSortOption.PRICE_HIGH_TO_LOW -> categoryFiltered.sortedByDescending { it.price }
                DishSortOption.RATING_HIGH -> categoryFiltered.sortedByDescending { getRatingFor(it).first }
            }
        }

    val filteredReviews: List<DishReview>
        get() {
            val all = recentDishReviews.values.flatten().ifEmpty { DEFAULT_DISH_REVIEWS }
            return when (selectedReviewFilter) {
                "Món ăn ngon", "Món ăn ngon (340)" -> all.filter { it.tags.any { t -> t.contains("ngon", ignoreCase = true) || t.contains("vị", ignoreCase = true) } }
                "Giao nhanh", "Giao nhanh (280)" -> all.filter { it.tags.any { t -> t.contains("nhanh", ignoreCase = true) } || it.comment.contains("nhanh", ignoreCase = true) }
                "Đóng gói kỹ", "Đóng gói kỹ (195)" -> all.filter { it.tags.any { t -> t.contains("gói", ignoreCase = true) } || it.comment.contains("gói", ignoreCase = true) }
                "5★", "5 sao" -> all.filter { it.ratingStars >= 5 }
                "4★", "4 sao" -> all.filter { it.ratingStars == 4 }
                else -> all
            }
        }

    val hasCartItems: Boolean get() = cartItemCount > 0

    fun getRatingFor(item: MenuItem): Pair<Double, Int> {
        return menuItemRatings[item.id] ?: Pair(item.rating, item.reviewCount)
    }

    fun getReviewsFor(item: MenuItem): List<DishReview> {
        return recentDishReviews[item.id] ?: DEFAULT_DISH_REVIEWS
    }
}

// ── UiEvent ─────────────────────────────────────────────────────────────────

sealed interface DetailUiEvent : UiEvent {
    data class SelectCategory(val category: String) : DetailUiEvent
    data class SelectTab(val tab: DetailTab) : DetailUiEvent
    data class SelectSortOption(val sort: DishSortOption) : DetailUiEvent
    data class SelectReviewFilter(val filter: String) : DetailUiEvent
    data object ToggleFavorite : DetailUiEvent
    data class ToggleDishFavorite(val item: MenuItem) : DetailUiEvent
    data class ClickMenuItem(val item: MenuItem) : DetailUiEvent
    data class QuickAddToCart(val item: MenuItem) : DetailUiEvent
    data class UpdateQuantity(val delta: Int) : DetailUiEvent
    data class UpdateNote(val note: String) : DetailUiEvent
    data object ConfirmAddToCart : DetailUiEvent
    data object DismissCustomization : DetailUiEvent
    data object ConfirmConflictAndReplace : DetailUiEvent
    data object DismissConflictDialog : DetailUiEvent
    data class OpenDishRating(val item: MenuItem) : DetailUiEvent
    data object DismissDishRating : DetailUiEvent
    data class SelectRatingStars(val stars: Int) : DetailUiEvent
    data class ToggleRatingTag(val tag: String) : DetailUiEvent
    data class UpdateRatingComment(val comment: String) : DetailUiEvent
    data object SubmitDishRating : DetailUiEvent
    data object ClickViewCart : DetailUiEvent
    data object ClickBack : DetailUiEvent
}

// ── UiEffect ────────────────────────────────────────────────────────────────

sealed interface DetailUiEffect : UiEffect {
    data object NavigateBack : DetailUiEffect
    data object NavigateToCart : DetailUiEffect
    data class ShowSnackbar(val message: String) : DetailUiEffect
}

// ── ViewModel ───────────────────────────────────────────────────────────────

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val getRestaurantDetailUseCase: GetRestaurantDetailUseCase,
    private val addToCartUseCase: AddToCartUseCase,
    private val clearCartUseCase: ClearCartUseCase,
    private val getCartUseCase: GetCartUseCase,
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    savedStateHandle: SavedStateHandle
) : BaseViewModel<DetailUiState, DetailUiEvent, DetailUiEffect>(
    initialState = DetailUiState(restaurantId = savedStateHandle.get<String>("restaurantId") ?: "res_1"),
    savedStateHandle = savedStateHandle
) {

    init {
        loadRestaurantDetail()
        observeCart()
        observeFavorites()
    }

    private fun observeFavorites() {
        val id = uiState.value.restaurantId.ifBlank { "res_1" }
        viewModelScope.launch {
            getFavoritesUseCase.isFavorite(id).collect { isFav ->
                updateState { it.copy(isFavorite = isFav) }
            }
        }
        viewModelScope.launch {
            getFavoritesUseCase(FavoriteType.DISH).collect { favs ->
                val favIds = favs.map { it.targetId }.toSet()
                updateState { it.copy(favoriteDishIds = favIds) }
            }
        }
    }

    private fun loadRestaurantDetail() {
        val id = uiState.value.restaurantId.ifBlank { "res_1" }
        viewModelScope.launch {
            try {
                val detailResult = getRestaurantDetailUseCase(id)
                val rawCategories = detailResult.menuItems.map { it.category }.filter { it.isNotBlank() }.distinct()
                val categories = listOf("Tất cả") + rawCategories

                val savedRatings = mutableMapOf<String, Pair<Double, Int>>()
                detailResult.menuItems.forEach { menuItem ->
                    val savedScore = savedStateHandle.get<Double>("ratings_${menuItem.id}_score")
                    val savedCount = savedStateHandle.get<Int>("ratings_${menuItem.id}_count")
                    if (savedScore != null && savedCount != null) {
                        savedRatings[menuItem.id] = Pair(savedScore, savedCount)
                    }
                }

                updateState {
                    it.copy(
                        isLoading = false,
                        restaurant = detailResult.restaurant,
                        menuItems = detailResult.menuItems,
                        categories = categories,
                        isFavorite = detailResult.restaurant.isFavorite,
                        menuItemRatings = it.menuItemRatings + savedRatings,
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                updateState {
                    it.copy(isLoading = false, errorMessage = "Không thể tải thông tin nhà hàng")
                }
            }
        }
    }

    private fun observeCart() {
        viewModelScope.launch {
            getCartUseCase()
                .catch { emit(emptyList()) }
                .collect { items ->
                    val id = uiState.value.restaurantId
                    val relevant = items.filter { it.restaurantId == id }
                    updateState {
                        it.copy(
                            cartItemCount = items.sumOf { item -> item.quantity },
                            cartTotalPrice = items.sumOf { item -> item.totalPrice },
                            cartItemsFromThisRestaurant = relevant
                        )
                    }
                }
        }
    }

    override fun onEvent(event: DetailUiEvent) {
        when (event) {
            is DetailUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
            }

            is DetailUiEvent.SelectTab -> {
                updateState { it.copy(selectedTab = event.tab) }
            }

            is DetailUiEvent.SelectSortOption -> {
                updateState { it.copy(selectedSortOption = event.sort) }
            }

            is DetailUiEvent.SelectReviewFilter -> {
                updateState { it.copy(selectedReviewFilter = event.filter) }
            }

            is DetailUiEvent.ToggleFavorite -> {
                val current = uiState.value.restaurant
                if (current != null) {
                    viewModelScope.launch {
                        val isNowFav = toggleFavoriteUseCase.toggleRestaurant(current)
                        updateState { it.copy(isFavorite = isNowFav) }
                        sendEffect(
                            DetailUiEffect.ShowSnackbar(
                                if (isNowFav) "Đã lưu ${current.name} vào danh sách yêu thích" else "Đã gỡ ${current.name} khỏi danh sách yêu thích"
                            )
                        )
                    }
                } else {
                    val newFav = !uiState.value.isFavorite
                    updateState { it.copy(isFavorite = newFav) }
                }
            }

            is DetailUiEvent.ToggleDishFavorite -> {
                val restName = uiState.value.restaurant?.name ?: ""
                viewModelScope.launch {
                    val isNowFav = toggleFavoriteUseCase.toggleDish(event.item, restName)
                    val updated = if (isNowFav) {
                        uiState.value.favoriteDishIds + event.item.id
                    } else {
                        uiState.value.favoriteDishIds - event.item.id
                    }
                    updateState { it.copy(favoriteDishIds = updated) }
                    sendEffect(
                        DetailUiEffect.ShowSnackbar(
                            if (isNowFav) "Đã lưu ${event.item.name} vào danh sách yêu thích" else "Đã gỡ ${event.item.name} khỏi danh sách yêu thích"
                        )
                    )
                }
            }

            is DetailUiEvent.ClickMenuItem -> {
                updateState {
                    it.copy(
                        showCustomizationSheet = true,
                        selectedMenuItem = event.item,
                        customizationQuantity = 1,
                        customizationNote = ""
                    )
                }
            }

            is DetailUiEvent.QuickAddToCart -> {
                addItemToCart(
                    CartItem(
                        id = "ci_${System.currentTimeMillis()}",
                        menuItemId = event.item.id,
                        restaurantId = uiState.value.restaurantId,
                        name = event.item.name,
                        price = event.item.price,
                        quantity = 1,
                        imageUrl = event.item.imageUrl
                    )
                )
            }

            is DetailUiEvent.UpdateQuantity -> {
                val newQty = (uiState.value.customizationQuantity + event.delta).coerceIn(1, 99)
                updateState { it.copy(customizationQuantity = newQty) }
            }

            is DetailUiEvent.UpdateNote -> {
                updateState { it.copy(customizationNote = event.note) }
            }

            is DetailUiEvent.ConfirmAddToCart -> {
                val item = uiState.value.selectedMenuItem ?: return
                val cartItem = CartItem(
                    id = "ci_${System.currentTimeMillis()}",
                    menuItemId = item.id,
                    restaurantId = uiState.value.restaurantId,
                    name = item.name,
                    price = item.price,
                    quantity = uiState.value.customizationQuantity,
                    notes = uiState.value.customizationNote,
                    imageUrl = item.imageUrl
                )
                updateState { it.copy(showCustomizationSheet = false) }
                addItemToCart(cartItem)
            }

            is DetailUiEvent.DismissCustomization -> {
                updateState { it.copy(showCustomizationSheet = false, selectedMenuItem = null) }
            }

            is DetailUiEvent.ConfirmConflictAndReplace -> {
                val pending = uiState.value.pendingConflictItem ?: return
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null) }
                viewModelScope.launch {
                    clearCartUseCase()
                    when (addToCartUseCase(pending)) {
                        is AddToCartResult.Success -> {
                            sendEffect(DetailUiEffect.ShowSnackbar("Đã xóa giỏ hàng cũ và thêm ${pending.name}"))
                        }
                        else -> {}
                    }
                }
            }

            is DetailUiEvent.DismissConflictDialog -> {
                updateState { it.copy(showConflictDialog = false, pendingConflictItem = null) }
            }

            is DetailUiEvent.OpenDishRating -> {
                updateState {
                    it.copy(
                        showCustomizationSheet = false,
                        showDishRatingSheet = true,
                        ratingMenuItem = event.item,
                        selectedRatingStars = 5,
                        selectedRatingTags = emptySet(),
                        ratingComment = ""
                    )
                }
            }

            is DetailUiEvent.DismissDishRating -> {
                updateState { it.copy(showDishRatingSheet = false, ratingMenuItem = null) }
            }

            is DetailUiEvent.SelectRatingStars -> {
                updateState { it.copy(selectedRatingStars = event.stars) }
            }

            is DetailUiEvent.ToggleRatingTag -> {
                val current = uiState.value.selectedRatingTags
                val updated = if (current.contains(event.tag)) current - event.tag else current + event.tag
                updateState { it.copy(selectedRatingTags = updated) }
            }

            is DetailUiEvent.UpdateRatingComment -> {
                updateState { it.copy(ratingComment = event.comment) }
            }

            is DetailUiEvent.SubmitDishRating -> {
                val item = uiState.value.ratingMenuItem ?: return
                val currentRatingInfo = uiState.value.menuItemRatings[item.id] ?: Pair(item.rating, item.reviewCount)
                val currentScore = currentRatingInfo.first
                val currentCount = currentRatingInfo.second
                val newCount = currentCount + 1
                val newScore = ((currentScore * currentCount) + uiState.value.selectedRatingStars) / newCount
                val roundedScore = (newScore * 10).toInt() / 10.0

                val newReview = DishReview(
                    id = "rev_${System.currentTimeMillis()}",
                    authorName = "Bạn (Đã xác minh)",
                    ratingStars = uiState.value.selectedRatingStars,
                    comment = uiState.value.ratingComment.ifBlank { "Món ăn ngon và phục vụ rất tốt!" },
                    tags = uiState.value.selectedRatingTags.toList(),
                    timeAgo = "Vừa xong"
                )

                val existingReviews = uiState.value.recentDishReviews[item.id] ?: DEFAULT_DISH_REVIEWS
                val updatedReviews = listOf(newReview) + existingReviews

                val newRatingsMap = uiState.value.menuItemRatings + (item.id to Pair(roundedScore, newCount))
                val newReviewsMap = uiState.value.recentDishReviews + (item.id to updatedReviews)

                // Persist into SavedStateHandle
                savedStateHandle["ratings_${item.id}_score"] = roundedScore
                savedStateHandle["ratings_${item.id}_count"] = newCount

                updateState {
                    it.copy(
                        showDishRatingSheet = false,
                        ratingMenuItem = null,
                        menuItemRatings = newRatingsMap,
                        recentDishReviews = newReviewsMap
                    )
                }
                sendEffect(DetailUiEffect.ShowSnackbar("Cảm ơn bạn đã đánh giá ${uiState.value.selectedRatingStars}⭐ cho món ${item.name}!"))
            }

            is DetailUiEvent.ClickViewCart -> {
                sendEffect(DetailUiEffect.NavigateToCart)
            }

            is DetailUiEvent.ClickBack -> {
                sendEffect(DetailUiEffect.NavigateBack)
            }
        }
    }

    private fun addItemToCart(cartItem: CartItem) {
        viewModelScope.launch {
            when (val result = addToCartUseCase(cartItem)) {
                is AddToCartResult.Success -> {
                    sendEffect(DetailUiEffect.ShowSnackbar("Đã thêm ${cartItem.quantity}x ${cartItem.name} vào giỏ"))
                }
                is AddToCartResult.Conflict -> {
                    updateState {
                        it.copy(
                            showConflictDialog = true,
                            pendingConflictItem = cartItem
                        )
                    }
                }
            }
        }
    }
}
