package com.bitefast.core.designsystem.component

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Percent
import androidx.compose.material.icons.filled.Sell
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bitefast.core.designsystem.theme.ErrorRed
import com.bitefast.core.designsystem.theme.OrangePrimary
import com.bitefast.core.designsystem.theme.SuccessGreen
import com.bitefast.core.designsystem.theme.WarningAmber
import com.bitefast.core.model.Voucher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun VoucherCard(
    voucher: Voucher,
    isEligible: Boolean = true,
    calculatedDiscount: Double = 0.0,
    missingAmount: Double = 0.0,
    statusMessage: String? = null,
    isSelected: Boolean = false,
    onApply: () -> Unit = {},
    onCopyCode: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val alpha = if (isEligible) 1f else 0.72f

    // Theme color based on voucher type
    val (typeColor, typeIcon, typeLabel) = when (voucher.type) {
        "free_shipping" -> Triple(Color(0xFF00897B), Icons.Default.LocalShipping, "FREESHIP")
        "percentage" -> Triple(OrangePrimary, Icons.Default.Percent, "${voucher.value.toInt()}% OFF")
        else -> Triple(Color(0xFF7E57C2), Icons.Default.Sell, "GIẢM TIỀN")
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .alpha(alpha),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isSelected) 4.dp else 1.5.dp),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, OrangePrimary) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Banner (Type & Icon)
            Box(
                modifier = Modifier
                    .width(84.dp)
                    .fillMaxHeight()
                    .background(typeColor.copy(alpha = 0.15f))
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(typeColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = typeIcon,
                            contentDescription = typeLabel,
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = typeLabel,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = typeColor,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Dotted vertical line divider
            Canvas(
                modifier = Modifier
                    .width(1.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = Color.LightGray.copy(alpha = 0.6f),
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f),
                    strokeWidth = 2f
                )
            }

            // Right Content Area
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(12.dp)
            ) {
                // Header: Code & Copy button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = voucher.code,
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }

                    IconButton(
                        onClick = { onCopyCode(voucher.code) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Sao chép mã",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Title
                Text(
                    text = voucher.name,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                // Description
                if (voucher.description.isNotBlank()) {
                    Text(
                        text = voucher.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status message / Condition / Upsell banner
                if (isEligible) {
                    val discountText = if (calculatedDiscount > 0) {
                        "Tiết kiệm %,.0fđ".format(calculatedDiscount)
                    } else {
                        "Đủ điều kiện áp dụng"
                    }
                    Text(
                        text = discountText,
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = SuccessGreen
                    )
                } else {
                    if (missingAmount > 0) {
                        Surface(
                            color = WarningAmber.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "💡 Mua thêm %,.0fđ để dùng mã này".format(missingAmount),
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                                color = WarningAmber,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                    } else if (!statusMessage.isNullOrBlank()) {
                        Text(
                            text = statusMessage,
                            style = MaterialTheme.typography.labelSmall,
                            color = ErrorRed
                        )
                    }
                }

                // Expiry Date
                if (voucher.endDate > 0) {
                    val expiryString = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(voucher.endDate))
                    Text(
                        text = "HSD: $expiryString",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isEligible) {
                        if (isSelected) {
                            OutlinedButton(
                                onClick = onApply,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = OrangePrimary
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Đã chọn", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            ElevatedButton(
                                onClick = onApply,
                                shape = RoundedCornerShape(20.dp),
                                colors = ButtonDefaults.elevatedButtonColors(
                                    containerColor = OrangePrimary,
                                    contentColor = Color.White
                                ),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text("Áp dụng", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold))
                            }
                        }
                    } else {
                        OutlinedButton(
                            onClick = onApply,
                            enabled = false,
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier.height(34.dp)
                        ) {
                            Text("Chưa đủ điều kiện", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }
    }
}
