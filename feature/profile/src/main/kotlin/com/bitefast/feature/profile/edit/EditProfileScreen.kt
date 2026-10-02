package com.bitefast.feature.profile.edit

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.bitefast.core.designsystem.component.BiteFastButton
import com.bitefast.core.designsystem.component.shimmerBrush
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.OrangePrimaryDark

// ─── Route ────────────────────────────────────────────────────────────────────

@Composable
fun EditProfileRoute(
    onNavigateBack: () -> Unit = {},
    viewModel: EditProfileViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is EditProfileUiEffect.NavigateBack -> onNavigateBack()
                is EditProfileUiEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    EditProfileScreen(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onEvent = viewModel::onEvent,
    )
}

// ─── Screen ───────────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    uiState: EditProfileUiState,
    snackbarHostState: SnackbarHostState,
    onEvent: (EditProfileUiEvent) -> Unit,
) {
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Chỉnh sửa hồ sơ",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { onEvent(EditProfileUiEvent.BackClicked) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                ),
            )
        },
    ) { paddingValues ->
        if (uiState.isLoading) {
            EditProfileSkeleton(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .imePadding(),
        ) {
            // ── Avatar Header Section ─────────────────────────────────────────
            AvatarSection(
                displayAvatar = uiState.displayAvatar,
                displayName = uiState.name,
                onEditClick = { /* Scroll xuống preset row */ },
            )

            Spacer(Modifier.height(8.dp))

            // ── Preset Avatar Picker ──────────────────────────────────────────
            PresetAvatarPicker(
                presets = PRESET_AVATARS,
                selectedAvatar = uiState.selectedAvatar,
                onSelect = { onEvent(EditProfileUiEvent.AvatarSelected(it)) },
            )

            Spacer(Modifier.height(28.dp))

            // ── Form Fields ───────────────────────────────────────────────────
            Column(modifier = Modifier.padding(horizontal = 20.dp)) {

                // Họ và tên
                ProfileTextField(
                    value = uiState.name,
                    onValueChange = { onEvent(EditProfileUiEvent.NameChanged(it)) },
                    label = "Họ và tên",
                    placeholder = "Nhập họ và tên của bạn",
                    leadingIcon = Icons.Default.Person,
                    errorMessage = uiState.nameError,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next,
                    ),
                )

                Spacer(Modifier.height(16.dp))

                // Số điện thoại
                ProfileTextField(
                    value = uiState.phone,
                    onValueChange = { onEvent(EditProfileUiEvent.PhoneChanged(it)) },
                    label = "Số điện thoại",
                    placeholder = "VD: 0987654321",
                    leadingIcon = Icons.Default.Phone,
                    errorMessage = uiState.phoneError,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Done,
                    ),
                )

                Spacer(Modifier.height(16.dp))

                // Email – Read-only
                ProfileReadOnlyField(
                    value = uiState.email,
                    label = "Email đăng nhập",
                    hint = if (uiState.email.isEmpty()) 
                        "Đăng nhập để liên kết email với tài khoản."
                    else 
                        "Email liên kết tài khoản, không thể thay đổi.",
                )

                Spacer(Modifier.height(32.dp))

                // Nút Lưu thay đổi
                SaveButton(
                    isSaving = uiState.isSaving,
                    onClick = { onEvent(EditProfileUiEvent.Submit) },
                    modifier = Modifier.fillMaxWidth(),
                )

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// ─── Avatar Section ───────────────────────────────────────────────────────────

@Composable
private fun AvatarSection(
    displayAvatar: String?,
    displayName: String,
    onEditClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(Brush.verticalGradient(listOf(OrangePrimary, OrangePrimaryDark)))
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(contentAlignment = Alignment.BottomEnd) {
                // Avatar Circle
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.25f))
                        .clickable(onClick = onEditClick),
                    contentAlignment = Alignment.Center,
                ) {
                    if (displayAvatar != null) {
                        Text(
                            text = displayAvatar,
                            fontSize = 48.sp,
                        )
                    } else {
                        Text(
                            text = displayName.take(1).uppercase().ifEmpty { "B" },
                            style = MaterialTheme.typography.headlineLarge.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 40.sp,
                            ),
                        )
                    }
                }

                // Edit badge
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(OrangePrimaryDark)
                        .border(2.dp, Color.White, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Chỉnh sửa avatar",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp),
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Text(
                text = displayName.ifEmpty { "Bitefast User" },
                style = MaterialTheme.typography.titleMedium.copy(
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                ),
            )
            Text(
                text = "Chọn avatar bên dưới để thay đổi",
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White.copy(alpha = 0.75f),
                ),
            )
        }
    }
}

// ─── Preset Avatar Picker ─────────────────────────────────────────────────────

@Composable
private fun PresetAvatarPicker(
    presets: List<String>,
    selectedAvatar: String?,
    onSelect: (String) -> Unit,
) {
    Column(modifier = Modifier.padding(horizontal = 20.dp)) {
        Text(
            text = "Chọn hình đại diện",
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            ),
        )
        Spacer(Modifier.height(10.dp))
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 0.dp),
        ) {
            items(presets) { avatar ->
                val isSelected = selectedAvatar == avatar
                val scale by animateFloatAsState(
                    targetValue = if (isSelected) 1.15f else 1f,
                    animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                    label = "avatar_scale_$avatar",
                )

                Box(
                    modifier = Modifier
                        .scale(scale)
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) OrangePrimary.copy(alpha = 0.15f)
                            else MaterialTheme.colorScheme.surfaceVariant,
                        )
                        .then(
                            if (isSelected) Modifier.border(2.dp, OrangePrimary, CircleShape)
                            else Modifier
                        )
                        .clickable { onSelect(avatar) },
                    contentAlignment = Alignment.Center,
                ) {
                    Text(text = avatar, fontSize = 26.sp)
                }

                // Check mark overlay khi được chọn
                AnimatedVisibility(
                    visible = isSelected,
                    enter = scaleIn() + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(OrangePrimary),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp),
                        )
                    }
                }
            }
        }
    }
}

// ─── ProfileTextField (Editable) ─────────────────────────────────────────────

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: androidx.compose.ui.graphics.vector.ImageVector,
    errorMessage: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Spacer(Modifier.height(6.dp))
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.outline) },
            leadingIcon = {
                Icon(
                    imageVector = leadingIcon,
                    contentDescription = null,
                    tint = if (errorMessage != null) MaterialTheme.colorScheme.error
                    else OrangePrimary,
                )
            },
            isError = errorMessage != null,
            supportingText = if (errorMessage != null) {
                { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
            } else null,
            keyboardOptions = keyboardOptions,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = OrangePrimary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
            ),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
        )
    }
}

// ─── ProfileReadOnlyField (Email) ─────────────────────────────────────────────

@Composable
private fun ProfileReadOnlyField(
    value: String,
    label: String,
    hint: String,
) {
    val hasValue = value.isNotEmpty()
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            ),
        )
        Spacer(Modifier.height(6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            tonalElevation = 0.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = if (hasValue) 
                        MaterialTheme.colorScheme.primary
                    else 
                        MaterialTheme.colorScheme.outline,
                    modifier = Modifier.size(20.dp),
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (hasValue) value else "Chưa có email",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = if (hasValue)
                                MaterialTheme.colorScheme.onSurface
                            else
                                MaterialTheme.colorScheme.onSurfaceVariant,
                        ),
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        text = hint,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = MaterialTheme.colorScheme.outline,
                        ),
                    )
                }
            }
        }
    }
}

// ─── Save Button ─────────────────────────────────────────────────────────────

@Composable
private fun SaveButton(
    isSaving: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isSaving) {
        Box(modifier = modifier.height(52.dp), contentAlignment = Alignment.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(
                    modifier = Modifier.size(22.dp),
                    color = OrangePrimary,
                    strokeWidth = 2.5.dp,
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "Đang lưu...",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = OrangePrimary,
                        fontWeight = FontWeight.SemiBold,
                    ),
                )
            }
        }
    } else {
        BiteFastButton(
            text = "Lưu thay đổi",
            onClick = onClick,
            modifier = modifier,
        )
    }
}

// ─── Skeleton Loading ─────────────────────────────────────────────────────────

@Composable
private fun EditProfileSkeleton(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxSize()) {
        // Header skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .shimmerBrush()
        )
        Spacer(Modifier.height(20.dp))
        Column(modifier = Modifier.padding(horizontal = 20.dp)) {
            repeat(3) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .shimmerBrush()
                )
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}
