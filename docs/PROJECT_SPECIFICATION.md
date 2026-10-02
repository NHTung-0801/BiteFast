# 🍔 BITEFAST — MASTER SYSTEM SPECIFICATION & ARCHITECTURAL BLUEPRINT

> **Project Name:** BiteFast — Enterprise Android Native Food Delivery & Live Tracking Platform  
> **Author & Lead Architect:** Nguyễn Hoàng Tùng  
> **Contact:** [tungnh0801@gmail.com](mailto:tungnh0801@gmail.com) | GitHub: [@NHTung-0801](https://github.com/NHTung-0801)  
> **Version:** 3.5 — Enterprise Release Grade  
> **Classification:** Public Technical Specification & Comprehensive Architecture Document  

---

## 📋 TABLE OF CONTENTS

1. [Executive Summary & Product Vision](#1-executive-summary--product-vision)
2. [High-Level Architecture (Enterprise Clean Arch + MVI/UDF)](#2-high-level-architecture)
3. [Multi-Module Organization & Boundaries](#3-multi-module-organization--boundaries)
4. [Technology Stack & Architectural Decisions](#4-technology-stack--architectural-decisions)
5. [End-to-End Functional Specifications](#5-end-to-end-functional-specifications)
   - [5.1 Authentication & Guest Mode](#51-authentication--guest-mode)
   - [5.2 Discovery & Intelligent Search](#52-discovery--intelligent-search)
   - [5.3 Restaurant & Dish Customization](#53-restaurant--dish-customization)
   - [5.4 Shopping Cart & Race-Condition Conflict Guard](#54-shopping-cart--conflict-guard)
   - [5.5 Secure Checkout & Payment Integration](#55-secure-checkout--payment-integration)
   - [5.6 Real-Time Order Tracking & Driver Simulation](#56-real-time-order-tracking)
   - [5.7 Order History & One-Click Smart Re-Order](#57-order-history--smart-re-order)
   - [5.8 Order & Dish Rating System](#58-order--dish-rating-system)
   - [5.9 Voucher Wallet & Auto-Best Recommendation](#59-voucher-wallet--recommendation)
   - [5.10 In-App Notification Center](#510-in-app-notification-center)
   - [5.11 Profile Management, Address Book & App Info](#511-profile-management--address-book)
6. [Security & Cryptographic Hardening (OWASP Top 10)](#6-security--cryptographic-hardening)
7. [Accessibility & Inclusive Engineering (WCAG 2.1 AA)](#7-accessibility--inclusive-engineering)
8. [Testing Pyramid & Quality Gates](#8-testing-pyramid--quality-gates)
9. [Execution Roadmap & Milestone Synthesis (Phases 1-6)](#9-execution-roadmap--milestones)
10. [Build & Deployment Guide](#10-build--deployment-guide)

---

## 1. EXECUTIVE SUMMARY & PRODUCT VISION

**BiteFast** is an enterprise-grade Android native food delivery application designed to deliver an uncompromising user experience, offline-first data synchronization, bank-grade cryptographic security, and full accessibility compliance.

### Core Value Propositions:
* **Frictionless Onboarding:** First-class Guest Mode enabling users to browse food, customize dishes, and manage carts before requiring authentication.
* **Instant Discovery:** Sub-10ms accent-insensitive search optimized for Vietnamese food terminology, smooth promotional carousels, and category filter chips.
* **Deterministic Shopping Flow:** Zero-race-condition cart management with mutex serialization, multi-restaurant conflict detection, and greedy voucher optimization.
* **Live Operational Visibility:** Real-time order progress tracking with simulated driver coordinate interpolation.
* **Production Hardiness:** Encrypted offline database (SQLCipher AES-256), certificate pinning (SPKI SHA-256), Android Keystore hardware protection, and 85%+ test coverage.

---

## 2. HIGH-LEVEL ARCHITECTURE

BiteFast enforces a strict **Clean Architecture** layered model combined with **Unidirectional Data Flow (MVI / UDF)**:

```
┌────────────────────────────────────────────────────────────────────────┐
│                   PRESENTATION LAYER (Jetpack Compose M3)              │
│   • Stateless Composables • Shimmer Skeleton • TalkBack Semantics      │
│   • BaseViewModel MVI: StateFlow<UiState> + Channel<UiEffect>          │
│   • Event Dispatch: onEvent(UiEvent)                                   │
└───────────────────────────────────┬────────────────────────────────────┘
                                    │ Observes State & Dispatches Events
                                    ▼
┌────────────────────────────────────────────────────────────────────────┐
│               DOMAIN LAYER (Pure Kotlin JVM - Zero Android SDK)        │
│   • 35+ UseCases (Single Responsibility Principle)                     │
│   • Pure Immutable Business Models (Data classes & Enums)              │
│   • Repository Interfaces (Inversion of Control)                       │
└───────────────────────────────────▲────────────────────────────────────┘
                                    │ Implements Contracts
                                    │
┌───────────────────────────────────┴────────────────────────────────────┐
│                    DATA LAYER (Single Source of Truth)                 │
│   • Mutex-protected Repositories against concurrent mutation           │
│   • Bidirectional Mappers: NetworkDTO ↔ DBEntity ↔ DomainModel         │
│   ┌─────────────────────────────────┬────────────────────────────────┐ │
│   │     Local Encrypted Storage     │       Remote API Services      │ │
│   │  • SQLCipher AES-256 Room DB    │  • Retrofit 2.11 + OkHttp 4    │ │
│   │  • Encrypted DataStore AES-GCM  │  • Certificate Pinning (SPKI)  │ │
│   │  • Android Keystore MasterKey   │  • Live WebSocket Tracking     │ │
│   └─────────────────────────────────┴────────────────────────────────┘ │
└────────────────────────────────────────────────────────────────────────┘
```

### Unidirectional Data Flow (MVI Contract)
Every feature ViewModel implements the UDF contract:
1. **`UiState` (Immutable State):** Represents the entire visual state of the screen at any instant.
2. **`UiEvent` (User Intents):** Actions dispatched from the UI to the ViewModel (e.g., `ClickSearch`, `AddToCart`, `ToggleFavorite`).
3. **`UiEffect` (One-Time Side Effects):** Buffered via a Kotlin Coroutine `Channel` (e.g., navigation triggers, Snackbar alerts, biometric prompts) to survive configuration changes and process death without duplication.

---

## 3. MULTI-MODULE ORGANIZATION & BOUNDARIES

The codebase is partitioned into **18 Gradle modules** organized by responsibility.

### 3.1 Architectural Boundary Rules
* **No Cross-Feature Dependencies:** `:feature:cart` CANNOT depend on `:feature:discovery` or any other feature module. Cross-screen navigation is coordinated at the top-level `:app` module.
* **Domain Purity:** `:core:domain` is a pure Kotlin JVM library without any Android SDK imports (`android.*`). This guarantees fast unit tests running directly on the JVM without emulators or Robolectric.
* **Encapsulation:** Database DAOs and entities are internal to `:core:database` and only consumed by `:core:data`.

### 3.2 Module Catalog

| Module | Type | Description |
|---|---|---|
| `:app` | Android App | Application entry point, Single Activity, centralized `NavHost`, DI composition root. |
| `:core:model` | Pure JVM | Pure domain entities (`Restaurant`, `FoodItem`, `Order`, `Voucher`, `User`). |
| `:core:domain` | Pure JVM | 35+ UseCases and Repository Interfaces. Zero Android dependencies. |
| `:core:data` | Android Lib | Repository implementations, offline-first sync, bidirectional mappers. |
| `:core:database` | Android Lib | Room SQLite Database encrypted with SQLCipher AES-256 and DAOs. |
| `:core:network` | Android Lib | Retrofit 2.11, OkHttp with Certificate Pinning, Mock engine & WebSocket client. |
| `:core:datastore` | Android Lib | Encrypted DataStore using Jetpack Security MasterKey AES-256 GCM. |
| `:core:designsystem` | Android Lib | Material 3 Theme tokens, Custom Shimmer Brush, Accessible UI components. |
| `:core:common` | Pure JVM/Lib | Coroutine Dispatchers, String extensions (`removeAccents`), BaseViewModel. |
| `:core:testing` | Android Lib | Shared Test Doubles, Fake Repositories, Turbine & JUnit rules. |
| `:feature:auth` | Android Lib | Login, Register, Forgot Password with OTP, and Guest Mode gate. |
| `:feature:discovery` | Android Lib | Home Screen, real-time debounced search, category filter chips, banner carousel. |
| `:feature:detail` | Android Lib | Restaurant menu, Dish Customization BottomSheet, review filtering, favorites. |
| `:feature:cart` | Android Lib | Cart management, special cooking instructions, price breakdown, conflict dialog. |
| `:feature:checkout` | Android Lib | Payment options (COD, MoMo, ZaloPay, Cards), card masking, biometric auth. |
| `:feature:order` | Android Lib | Order history with status filters, one-click smart re-order, order detail screen. |
| `:feature:tracking` | Android Lib | Real-time order progress tracking with simulated driver coordinate updates. |
| `:feature:rating` | Android Lib | 1-5 Star order & dish rating with selectable experience tags and reviews. |
| `:feature:notification` | Android Lib | In-app notification center, category tabs, and unread badge synchronization. |
| `:feature:voucher` | Android Lib | Voucher wallet, eligibility evaluation, and greedy auto-recommendation algorithm. |
| `:feature:profile` | Android Lib | User profile, food avatar picker, address book manager, About BiteFast popup. |

---

## 4. TECHNOLOGY STACK & ARCHITECTURAL DECISIONS

| Technology | Version | Architectural Decision & Rationale |
|---|---|---|
| **Kotlin** | `2.1.0` | Modern language features, K2 compiler speed, strict nullability, coroutine flows. |
| **Android Gradle Plugin** | `8.7.0` | Latest build tooling with R8 optimizations and Bytecode manipulation. |
| **Jetpack Compose BOM** | `2024.12.01` | Declarative, reactive UI toolkit delivering smooth 60fps animations. |
| **Material 3** | `1.3.1` | Modern design system, dynamic color, responsive shape scales. |
| **Dagger Hilt** | `2.52` | Compile-time Dependency Injection with scoping, ViewModel lifecycle integration. |
| **Room + SQLCipher** | `2.6.1` / `4.5.4` | Relational offline storage with transparent full-database AES-256 encryption. |
| **Retrofit + OkHttp** | `2.11.0` / `4.12.0` | Robust HTTP networking, HPKP/SPKI Certificate Pinning, Token Authenticator. |
| **Kotlinx Serialization** | `1.7.3` | Reflection-free, high-performance JSON parsing for API responses. |
| **Jetpack Security Crypto** | `1.1.0-alpha06` | Android Keystore backed MasterKey AES-256 GCM encrypted preferences. |
| **Coil Compose** | `2.7.0` | Asynchronous image loading with memory-efficient LRU bitmap caching. |
| **Kotlinx Coroutines** | `1.9.0` | Structured concurrency, reactive data streams via Flow & StateFlow. |
| **Navigation Compose** | `2.8.5` | Type-safe single-activity navigation graph. |
| **JUnit 5 + MockK + Turbine** | `5.11.3` / `1.13.13` | Modern unit testing and coroutine flow emission assertion framework. |

---

## 5. END-TO-END FUNCTIONAL SPECIFICATIONS

### 5.1 Authentication & Guest Mode
* **Guest-First Philosophy:** Allows guests to freely explore restaurants, search dishes, and populate cart items without initial login friction.
* **Login Gate:** Context-aware `LoginGateBottomSheet` intercepting actions requiring user identity (checkout, rating, order history, profile editing).
* **Deep-Link Restoration:** Automatically returns the user to their intended action immediately after successful login.
* **Forgot Password Flow:** Email-based OTP verification with step-by-step password reset.

### 5.2 Discovery & Intelligent Search
* **Vietnamese Accent-Insensitive Search:** Custom `removeAccents` normalization strips diacritics in real-time, allowing users to find "Phở bò tái lăn" by typing "pho".
* **Debounced Query Execution:** 300ms debounce window eliminating redundant search queries.
* **Banner Carousel:** Auto-advancing promotional banners with tactile dot indicators.
* **Category Filter Chips:** Quick filter chips for *Tất cả*, *Cơm*, *Phở & Bún*, *Trà sữa*, *Bánh mì*, *Gợi ý*, *Gần nhất*, and *Đánh giá cao*.

### 5.3 Restaurant & Dish Customization
* **Restaurant Profile:** Banner hero, delivery radius, rating badge, average preparation time, and minimum order threshold.
* **Interactive Dish BottomSheet:** Modal allowing users to customize size (S, M, L), select multiple toppings, adjust quantities, and view live subtotal updates.
* **Customer Reviews Breakdown:** Star rating aggregate with tab filtering by score (All, 5★, 4★, 3★, 2★, 1★).
* **Favorite Toggle:** Instant optimistic bookmarking with Room DB persistence.

### 5.4 Shopping Cart & Conflict Guard
* **Restaurant Conflict Resolution:** Automatic modal prompt when attempting to add a dish from a new restaurant, giving the user the choice to *"Create New Cart"* or *"Keep Current Cart"*.
* **Concurrency Safety:** Mutex synchronization in `CartRepositoryImpl` prevents race-condition data corruption when users tap increment/decrement rapidly.
* **Order Preparation Notes:** Special culinary and delivery instructions field persisted per dish.

### 5.5 Secure Checkout & Payment Integration
* **Multiple Payment Channels:** Cash on Delivery (COD), MoMo E-Wallet, ZaloPay, and Credit/Debit Cards.
* **Sensitive Card Number Masking:** Real-time formatting enforcing display masking (`•••• •••• •••• 1234`).
* **Biometric Hardware Guard:** Keystore-backed `BiometricPrompt` required before submitting high-value or card-funded transactions.

### 5.6 Real-Time Order Tracking & Driver Simulation
* **Step Progression:** Linear step indicator reflecting live status (*Confirmed ➔ Preparing ➔ Delivering ➔ Completed*).
* **Live Driver Simulation:** Simulated WebSocket stream delivering driver coordinates and estimated arrival countdowns.
* **Emergency Actions:** One-tap call and message actions to contact the delivery driver directly.

### 5.7 Order History & One-Click Smart Re-Order
* **Order Status Filter:** Tabs partitioning orders into *Active*, *Completed*, and *Cancelled*.
* **Smart Re-Order:** Verifies restaurant operating status and dish availability before repopulating the user's cart in a single tap.
* **Invoice Breakdown:** Comprehensive receipt view showing subtotal, shipping fee, voucher discount, and final amount.

### 5.8 Order & Dish Rating System
* **Two-Tier Rating:** Rate overall delivery experience and individual dishes with 1-5 stars.
* **Quick Experience Tags:** Pre-defined sentiment tags (*"Giao nhanh"*, *"Món ăn nóng hổi"*, *"Đóng gói cẩn thận"*, *"Hương vị tuyệt vời"*).
* **Detailed Text Review:** Optional written commentary for restaurant feedback.

### 5.9 Voucher Wallet & Auto-Best Recommendation
* **Voucher Management:** Visual voucher cards showing discount percentage, cap value, expiration timestamp, and minimum spend.
* **Greedy Best-Voucher Algorithm:** Evaluates all eligible vouchers against the current subtotal and automatically applies the option maximizing the user's savings.

### 5.10 In-App Notification Center
* **Category Tabs:** Segregated into *Tất cả (All)*, *Đơn hàng (Orders)*, *Khuyến mãi (Promotions)*, and *Hệ thống (System)*.
* **Unread Badge Synchronization:** Real-time counter reflecting unread notifications on the bottom navigation bar.
* **Batch Operations:** Support for *"Mark All as Read"* and clearing notification history.

### 5.11 Profile Management, Address Book & App Info
* **Edit Profile:** Personal info editing (Name, Phone number) with a curated set of 6 food avatars (🍔, 🍕, ☕, 🍜, 🍣, 🌮).
* **Security-Fixed Email:** Displays authenticated email in a read-only secure field.
* **Address Book:** Multi-address management with default address assignment and label tagging (*Home*, *Office*).
* **About BiteFast:** Built-in modal dialog displaying app version (1.0.0), platform specs, and developer information.

---

## 6. SECURITY & CRYPTOGRAPHIC HARDENING (OWASP TOP 10)

| Threat / Vulnerability | Defense Mechanism | Technical Implementation |
|---|---|---|
| **Data at Rest Theft** | Full-database transparent encryption | **Room SQLite + SQLCipher AES-256**. Database key generated via hardware CSPRNG. |
| **Key Extraction** | Hardware-isolated key storage | **Android Keystore (Hardware TEE / StrongBox)**. Master keys never enter application memory space. |
| **Credential & Token Leak** | Encrypted preferences | **Jetpack Security MasterKey AES-256 GCM** for session tokens and sensitive preferences. |
| **Man-in-the-Middle (MITM)** | Certificate Pinning | **OkHttp CertificatePinner** with SHA-256 SPKI public key hashes. |
| **Cleartext Transmission** | Network policy enforcement | `android:usesCleartextTraffic="false"` strictly blocking unencrypted HTTP connections. |
| **Token Refresh Storm** | Serialized silent renewal | **OkHttp TokenAuthenticator + Mutex** resolving concurrent 401 Unauthorized errors safely. |
| **Reverse Engineering** | Symbol stripping & obfuscation | **R8 Full Mode + ProGuard** dictionary obfuscation stripping debug artifacts. |

---

## 7. ACCESSIBILITY & INCLUSIVE ENGINEERING (WCAG 2.1 AA)

* **48dp × 48dp Minimum Touch Targets:** Strictly applied to all interactive controls (buttons, chips, quantity steppers, navigation icons).
* **Semantic Merging (`clearAndSetSemantics`):** Composite card elements merge their child texts into a single, cohesive TalkBack announcement rather than disjointed words.
* **Custom Accessibility Actions:** Steppers support swipe-based increment/decrement actions without requiring precise targeting of small buttons.
* **Dynamic 200% Font Scaling:** UI layouts leverage fluid Compose constraints that accommodate extreme font scaling without clipping or overflow.
* **Reduce Motion Compliance:** Disables infinite looping animations when system accessibility settings request reduced motion.

---

## 8. TESTING PYRAMID & QUALITY GATES

```
         /\
        /  \        10% UI Tests (Compose UI Semantics & Robot Pattern)
       /----\
      /      \      20% Integration Tests (MockWebServer, Room Encrypted In-Memory)
     /--------\
    /          \    70% Unit Tests (Pure Kotlin JVM: UseCases, ViewModels, Mappers)
   /------------\
```

### Quality Commitments:
* **Domain Layer Coverage:** ≥ 95% target (Pure Kotlin JVM, sub-second execution).
* **Data & ViewModel Layer Coverage:** ≥ 85% target (Turbine state/channel verification).
* **CI/CD Quality Gate:** Kover build verification breaks the build if overall coverage drops below 85%.

---

## 9. EXECUTION ROADMAP & MILESTONE SYNTHESIS (PHASES 1-6)

### Phase 1: Architectural Foundation & Domain Modeling
* Setup multi-module Gradle convention plugins (`build-logic`).
* Define pure domain entities in `:core:model`.
* Implement 35+ UseCases and Repository contracts in `:core:domain`.

### Phase 2: Secure Data Layer & Network Infrastructure
* Implement Room SQLite database with SQLCipher AES-256 encryption in `:core:database`.
* Configure OkHttp Certificate Pinning and Retrofit API client in `:core:network`.
* Build AES-256 GCM encrypted preferences in `:core:datastore`.
* Implement Repositories and bidirectional mappers in `:core:data`.

### Phase 3: Enterprise Design System & Shared Components
* Establish Material 3 color tokens, typography scales, and custom Shimmer brush.
* Build accessible `FoodDishCard`, `VoucherCard`, `QuantitySelector`, and `StateViews`.

### Phase 4: Core Shopping Flow Implementation
* Build `:feature:discovery` with real-time accent-insensitive search and category chips.
* Build `:feature:detail` with dish modifier bottom sheet and review filters.
* Build `:feature:cart` with multi-restaurant conflict detection and Mutex safety.
* Build `:feature:checkout` with payment methods and biometric authorization.

### Phase 5: Engagement & Post-Order Operations
* Build `:feature:tracking` with live order progress and driver simulation.
* Build `:feature:order` with history filters and one-click smart re-order.
* Build `:feature:rating` with 1-5 star selection and experience tags.
* Build `:feature:voucher` with greedy auto-recommendation algorithm.
* Build `:feature:notification` with category tabs and unread badge synchronization.
* Build `:feature:profile` with food avatar picker, address book, and About dialog.

### Phase 6: Production Hardening, Quality Gates & Release
* Verify 85%+ code coverage across all modules via JUnit 5, MockK, and Turbine.
* Enforce R8 full-mode obfuscation and resource shrinking.
* Validate TalkBack screen reader accessibility and WCAG 2.1 AA compliance.
* Finalize single-activity navigation wiring in `:app`.

---

## 10. BUILD & DEPLOYMENT GUIDE

### Prerequisites
* **JDK:** OpenJDK 17 or OpenJDK 21 LTS
* **Android Studio:** Ladybug (2024.2.1+) or Koala
* **Android SDK:** Compile SDK 35, Min SDK 24, Target SDK 35

### Command Quick-Reference

```bash
# 1. Clean workspace and check syntax
./gradlew clean

# 2. Run all unit tests across all 18 modules
./gradlew test

# 3. Generate Kover code coverage report
./gradlew koverHtmlReport

# 4. Compile Debug APK
./gradlew assembleDebug

# 5. Compile Release APK (with R8 obfuscation)
./gradlew assembleRelease

# 6. Install directly on connected device/emulator
./gradlew :app:installDebug
```

---

<p align="center">
  <strong>BiteFast System Architecture & Specification</strong><br />
  Designed & Engineered by <strong>Nguyễn Hoàng Tùng</strong> (<a href="mailto:tungnh0801@gmail.com">tungnh0801@gmail.com</a>)<br />
  Lead Mobile Software Engineer & System Architect
</p>
