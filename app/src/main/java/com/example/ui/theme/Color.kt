package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Modern Flat & Soft Card Palette
val PrimaryNavy = Color(0xFF1E2A78)
val PrimaryNavyDark = Color(0xFF151E56)
val PrimaryNavyLight = Color(0xFF2E3F9E)
val PrimaryNavyGradientEnd = Color(0xFF3B52C7)

val AccentAmber = Color(0xFFF59E0B)
val AccentGreen = Color(0xFF10B981)
val AccentRed = Color(0xFFEF4444)
val AccentBlue = Color(0xFF2563EB)
val AccentTeal = Color(0xFF0D9488)

// 10-15% Tinted Backgrounds for Badges & Icons
val TintNavy = Color(0x1A1E2A78)
val TintAmber = Color(0x1AF59E0B)
val TintGreen = Color(0x1A10B981)
val TintRed = Color(0x1AEF4444)
val TintBlue = Color(0x1A2563EB)
val TintTeal = Color(0x1A0D9488)

// Surfaces & Backgrounds
val AppBgLight = Color(0xFFF3F5FA)
val CardBgLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val BorderSubtleLight = Color(0x0F1E2A78)

val AppBgDark = Color(0xFF0F1424)
val CardBgDark = Color(0xFF1A2036)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val BorderSubtleDark = Color(0x1FFFFFFF)

// Legacy compatibility aliases
val Navy900 = PrimaryNavy
val AccentGold = AccentAmber
val AccentEmerald = AccentGreen
val AccentRose = AccentRed

// Status Badge Colors (Menunggu=amber, Dilayani=biru, Selesai=hijau)
val StatusWaiting = AccentAmber
val StatusWaitingBg = TintAmber
val StatusCalled = AccentBlue
val StatusCalledBg = TintBlue
val StatusInService = AccentBlue
val StatusInServiceBg = TintBlue
val StatusCompleted = AccentGreen
val StatusCompletedBg = TintGreen
val StatusCancelled = AccentRed
val StatusCancelledBg = TintRed
