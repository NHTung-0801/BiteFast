package com.bitefast.feature.discovery

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.bitefast.core.common.BaseViewModel
import com.bitefast.core.common.UiEffect
import com.bitefast.core.common.UiEvent
import com.bitefast.core.common.UiState
import com.bitefast.core.domain.repository.RestaurantRepository
import com.bitefast.core.domain.restaurant.GetRestaurantsUseCase
import com.bitefast.core.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

// ─── Dish Model for Discovery ─────────────────────────────────────────────────

data class DiscoveryDish(
    val id: String,
    val name: String,
    val restaurantId: String,
    val restaurantName: String,
    val price: Double,
    val imageUrl: String,
    val description: String = "",
    val rating: Double = 4.8,
    val distanceKm: Float = 1.2f,
    val category: String = "Cơm",
    val badge: String? = null,
    val isPopular: Boolean = false,
)

// ─── Promo Banner Model ───────────────────────────────────────────────────────

data class PromoBanner(
    val id: String,
    val title: String,
    val subtitle: String,
    val tag: String,
    val gradientColors: List<Long>,
    val actionCategory: String? = null
)

val DEFAULT_PROMO_BANNERS = listOf(
    PromoBanner(
        id = "banner_1",
        title = "TIỆC DEAL 0 ĐỒNG",
        subtitle = "Freeship toàn bộ đơn từ 0đ hôm nay",
        tag = "HOT DEAL 🔥",
        gradientColors = listOf(0xFFFF5722, 0xFFFF9800),
        actionCategory = null
    ),
    PromoBanner(
        id = "banner_2",
        title = "ĐẠI TIỆC TRÀ SỮA",
        subtitle = "Mua 1 tặng 1 + Free topping trân châu",
        tag = "GIẢM 50% 🧋",
        gradientColors = listOf(0xFF7B1FA2, 0xFFBA68C8),
        actionCategory = "Trà sữa"
    ),
    PromoBanner(
        id = "banner_3",
        title = "CƠM TRƯA ĐẬM VỊ",
        subtitle = "Chuẩn vị nhà làm - Giao nhanh 15 phút",
        tag = "BÁN CHẠY 🍚",
        gradientColors = listOf(0xFF00897B, 0xFF4DB6AC),
        actionCategory = "Cơm"
    ),
    PromoBanner(
        id = "banner_4",
        title = "PIZZA & GÀ RÁN GIÒN",
        subtitle = "Combo gia đình siêu tiết kiệm giảm 35%",
        tag = "TIỆC TÙNG 🍕",
        gradientColors = listOf(0xFFC2185B, 0xFFE91E63),
        actionCategory = "Pizza"
    )
)

// ─── Sort Option ─────────────────────────────────────────────────────────────

enum class DiscoverySortOption(val label: String) {
    DEFAULT("Gợi ý"),
    NEARBY("Gần nhất"),
    RATING("Đánh giá cao"),
    FAST_DELIVERY("Giao nhanh"),
}

// ─── UiState ──────────────────────────────────────────────────────────────────

data class DiscoveryUiState(
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val promoBanners: List<PromoBanner> = DEFAULT_PROMO_BANNERS,
    val restaurants: List<Restaurant> = emptyList(),
    val topRatedDishes: List<DiscoveryDish> = emptyList(),
    val nearbyPromoDishes: List<DiscoveryDish> = emptyList(),
    val allDishes: List<DiscoveryDish> = emptyList(),
    val selectedCategory: String = CATEGORY_ALL,
    val selectedSortOption: DiscoverySortOption = DiscoverySortOption.DEFAULT,
    val searchQuery: String = "",
    val errorMessage: String? = null,
) : UiState

// ─── UiEvent ──────────────────────────────────────────────────────────────────

sealed interface DiscoveryUiEvent : UiEvent {
    data class SelectCategory(val category: String) : DiscoveryUiEvent
    data class SelectSortOption(val sortOption: DiscoverySortOption) : DiscoveryUiEvent
    data class SearchQueryChanged(val query: String) : DiscoveryUiEvent
    data object Refresh : DiscoveryUiEvent
    data class ClickRestaurant(val restaurantId: String) : DiscoveryUiEvent
    data class ClickDish(val dish: DiscoveryDish) : DiscoveryUiEvent
    data class ClickPromoBanner(val banner: PromoBanner) : DiscoveryUiEvent
    data object DismissError : DiscoveryUiEvent
}

// ─── UiEffect ─────────────────────────────────────────────────────────────────

sealed interface DiscoveryUiEffect : UiEffect {
    data class NavigateToDetail(val restaurantId: String) : DiscoveryUiEffect
    data class ShowSnackbar(val message: String) : DiscoveryUiEffect
}

// ─── Constants ────────────────────────────────────────────────────────────────

const val CATEGORY_ALL = "Tất cả"
val FOOD_CATEGORIES = listOf(
    CATEGORY_ALL, "Cơm", "Phở & Bún", "Trà sữa", "Bánh mì", "Gà rán", "Pizza", "Đồ uống"
)

// ─── ViewModel ────────────────────────────────────────────────────────────────

@OptIn(FlowPreview::class)
@HiltViewModel
class DiscoveryViewModel @Inject constructor(
    private val getRestaurantsUseCase: GetRestaurantsUseCase,
    private val restaurantRepository: RestaurantRepository,
    savedStateHandle: SavedStateHandle,
) : BaseViewModel<DiscoveryUiState, DiscoveryUiEvent, DiscoveryUiEffect>(
    initialState = DiscoveryUiState(),
    savedStateHandle = savedStateHandle,
) {
    /** Search query Flow riêng để debounce 300ms trước khi trigger search. */
    private val searchTrigger = MutableStateFlow(Pair(CATEGORY_ALL, ""))

    init {
        // Lắng nghe thay đổi query/category với debounce 300ms — tránh gọi API liên tục
        searchTrigger
            .debounce(300L)
            .distinctUntilChanged()
            .flatMapLatest { (category, query) ->
                updateState { it.copy(isLoading = true, errorMessage = null) }
                val cuisineParam = if (category == CATEGORY_ALL) null else category
                val queryParam = query.ifBlank { null }
                getRestaurantsUseCase(queryParam, cuisineParam)
            }
            .onEach { restaurants ->
                val allDishesList = mutableListOf<DiscoveryDish>()
                restaurants.forEach { res ->
                    val menu = runCatching { restaurantRepository.getRestaurantMenu(res.id) }.getOrDefault(emptyList())
                    menu.forEach { item ->
                        val badge = when {
                            res.isFreeDelivery -> "Freeship"
                            item.isPopular -> "Bán chạy"
                            item.price < 50_000 -> "Giá tốt"
                            else -> null
                        }
                        allDishesList.add(
                            DiscoveryDish(
                                id = item.id,
                                name = item.name,
                                restaurantId = res.id,
                                restaurantName = res.name,
                                price = item.price,
                                imageUrl = item.imageUrl,
                                description = item.description,
                                rating = res.rating,
                                distanceKm = res.distance,
                                category = item.category,
                                badge = badge,
                                isPopular = item.isPopular
                            )
                        )
                    }
                }

                val topRated = allDishesList
                    .sortedByDescending { it.rating }
                    .take(10)

                val nearbyPromo = allDishesList
                    .filter { it.distanceKm <= 2.5f || it.badge != null }
                    .sortedBy { it.distanceKm }
                    .take(10)

                val sortedRestaurants = applySort(restaurants, uiState.value.selectedSortOption)
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        restaurants = sortedRestaurants,
                        topRatedDishes = topRated,
                        nearbyPromoDishes = nearbyPromo,
                        allDishes = allDishesList,
                        errorMessage = null,
                    )
                }
            }
            .catch { e ->
                updateState {
                    it.copy(
                        isLoading = false,
                        isRefreshing = false,
                        errorMessage = e.message ?: "Lỗi tải danh sách nhà hàng.",
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onEvent(event: DiscoveryUiEvent) {
        when (event) {
            is DiscoveryUiEvent.SelectCategory -> {
                updateState { it.copy(selectedCategory = event.category) }
                searchTrigger.update { Pair(event.category, uiState.value.searchQuery) }
            }

            is DiscoveryUiEvent.SelectSortOption -> {
                updateState {
                    it.copy(
                        selectedSortOption = event.sortOption,
                        restaurants = applySort(it.restaurants, event.sortOption)
                    )
                }
            }

            is DiscoveryUiEvent.SearchQueryChanged -> {
                updateState { it.copy(searchQuery = event.query) }
                searchTrigger.update { Pair(uiState.value.selectedCategory, event.query) }
            }

            is DiscoveryUiEvent.Refresh -> {
                updateState { it.copy(isRefreshing = true, errorMessage = null) }
                searchTrigger.update { it } // re-emit cùng value để trigger lại collect
                viewModelScope.launch {
                    searchTrigger.emit(
                        Pair(uiState.value.selectedCategory, uiState.value.searchQuery)
                    )
                }
            }

            is DiscoveryUiEvent.ClickRestaurant -> {
                sendEffect(DiscoveryUiEffect.NavigateToDetail(event.restaurantId))
            }

            is DiscoveryUiEvent.ClickDish -> {
                sendEffect(DiscoveryUiEffect.NavigateToDetail(event.dish.restaurantId))
            }

            is DiscoveryUiEvent.ClickPromoBanner -> {
                val category = event.banner.actionCategory
                if (!category.isNullOrBlank()) {
                    onEvent(DiscoveryUiEvent.SelectCategory(category))
                } else {
                    sendEffect(DiscoveryUiEffect.ShowSnackbar("Áp dụng ưu đãi: ${event.banner.title}!"))
                }
            }

            is DiscoveryUiEvent.DismissError -> {
                updateState { it.copy(errorMessage = null) }
            }
        }
    }

    private fun applySort(list: List<Restaurant>, option: DiscoverySortOption): List<Restaurant> {
        return when (option) {
            DiscoverySortOption.DEFAULT -> list
            DiscoverySortOption.NEARBY -> list.sortedBy { it.distance }
            DiscoverySortOption.RATING -> list.sortedByDescending { it.rating }
            DiscoverySortOption.FAST_DELIVERY -> list.sortedBy { it.estimatedTime }
        }
    }
}
