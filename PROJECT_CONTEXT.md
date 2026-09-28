# KiranaFlow — Project Context

> Last updated: 2026-09-28

---

## Overview

KiranaFlow is an **offline-first, voice-powered Android POS** for kirana shop owners in Tamil Nadu. The shopkeeper speaks in Tamil or Tanglish; the phone understands, bills, and updates inventory — all on-device, no internet required.

---

## Implementation Stages

### ✅ Stage 1 — Foundation (2026-09-28)
**Branch / commit:** `main`

**What was built:**
- Full Android project scaffolding (Kotlin + Jetpack Compose, AGP 8.7.3, Kotlin 2.1.0)
- Room database (SQLite WAL mode) with entities: `CatalogItem`, `Bill`, `BillItem`
- Dual inventory system: **STOCK** (shelf count) and **FLOW** (daily sold qty)
- Seed catalog: 10 STOCK + 6 FLOW items with Tamil/Tanglish aliases
- Rule-based NLU engine handling Tamil numbers (oru=1, rendu=2...), English numbers, units, intents
- Fuzzy catalog validator (7-step: exact > prefix > contains > token overlap > Levenshtein)
- Android SpeechRecognizer ASR (Tamil ta-IN locale, free, on-device)
- Transaction Queue + WAL service (PENDING > inventory > COMMITTED, crash recovery on startup)
- Hilt DI with AppModule providing DB, DAOs, Vibrator
- Navigation graph: Billing > Stock > Past Bills

**Screens implemented:**
| Screen | File | Features |
|---|---|---|
| Speak (Home) | SpeakScreen.kt | Big centered SPEAK card, live waveform bars, "TAP & SPEAK", last bill status, Barcode scan CTA |
| Billing | BillingScreen.kt | Header with Live status & sound button, cart list cards with dark qty pills & ✓OK badges, listening banner, total amount bar, navy DONE CTA |
| Payment Modal | PaymentBottomSheet.kt | Cream background, synced header, large gold amount with speaker trigger, deep teal CASH / navy UPI cards, quick tender & change helper cards |
| Stock & Counter | StockScreen.kt | Live counter card, today's gross revenue with progress bar, cash/UPI tender breakdown, lock drawer & WhatsApp share actions |
| Past Bills | PastBillsScreen.kt | Summary metrics, bill list with receipt icons, detailed item breakdown |

**Color Palette (Reference Design — Warm Sand & Deep Accents, NO violet/indigo/blue):**
| Role | Color | Hex |
|---|---|---|
| Background | Warm Sand / Ivory | #EDE8DC |
| Background Deep | Muted Sand | #E0DAC8 |
| Surface / Cards | Pure White | #FFFFFF |
| Primary Action / Header | Deep Forest Teal | #2D6E5E |
| Secondary CTA / Navy | Dark Navy Charcoal | #1C2333 |
| Amount & Accents | Rich Amber / Ochre | #E09B1A |
| Success / Badges | Green Mint | #4CAF50 |
| Error / Danger | Crimson Red | #E53E3E |

> **Approved Palette Rule:**
> The active approved palette is the Warm Sand & Deep Accents mockup palette exclusively (`#EDE8DC`, `#2D6E5E`, `#1C2333`, `#E09B1A`). No violet, indigo, or blue AI palette colors are permitted anywhere in the codebase.

**Tech Stack Used (all free):**
| Component | Technology |
|---|---|
| Language | Kotlin 2.1.0 |
| UI | Jetpack Compose (Material3) |
| DI | Hilt 2.54 |
| Database | Room 2.6.1 (SQLite WAL mode) |
| ASR | Android SpeechRecognizer (Tamil ta-IN) |
| NLU | Rule-based engine (free, on-device) |
| Navigation | Jetpack Navigation Compose 2.8.5 |
| State | StateFlow + ViewModel |

---

## Architecture

```
app/
├── ui/
│   ├── theme/           # Color.kt, Type.kt, Theme.kt
│   └── screens/
│       ├── speak/       # SpeakScreen (Landing / Tap & Speak)
│       ├── billing/     # BillingScreen, BillingViewModel, PaymentBottomSheet
│       ├── stock/       # StockScreen, StockViewModel
│       └── pastbills/   # PastBillsScreen, PastBillsViewModel
├── data/
│   ├── db/              # KiranaFlowDatabase, CatalogDao, BillDao
│   ├── model/           # CatalogItem, Bill, BillItem, Cart, VoiceCommand
│   └── repository/      # KiranaRepository
└── service/
    ├── NluEngine.kt
    ├── CatalogValidator.kt
    ├── VoiceRecognitionService.kt
    └── BillingQueue.kt
```

---

## Voice Commands Supported

| Say this | Parsed as |
|---|---|
| Rendu Parle-G | ADD, item=Parle-G, qty=2 |
| Oru liter paal | ADD, item=Milk, qty=1, unit=L |
| Dettol soap | ADD, item=Dettol Soap, qty=1 |
| Bill potru | COMMIT - finalise bill |
| Remove last | REMOVE_LAST |
| Open camera | OPEN_CAMERA |

---

## GitHub Push History

| Date | Stage | Commit Message |
|---|---|---|
| 2026-09-28 | Stage 1 | feat: Stage 1 - Android foundation, billing UI, NLU engine, Room + WAL |
| 2026-09-28 | UI Redesign | feat: Pixel-accurate UI redesign matching design mockups (Speak, Billing, Payment modal, Stock dashboard) |
| 2026-09-28 | Phase 1 | feat(core): Phase 1 Data correctness - Money/Quantity value classes, durable WAL journal, serial transaction queue actor, movement ledger, draft cart recovery, UUIDv7 |

---

## Production Refinement Status

### ✅ Phase 1: Data Correctness (Completed)
1. **Money and Quantities**:
   - `Money` value class: All monetary amounts stored as `Long` in paise (1 INR = 100 paise). Unit-safe arithmetic, Indian number formatting (e.g., `₹16,420`).
   - `Quantity` value class: All quantities stored as `Long` in base units (grams, milliliters, pieces). `DisplayUnit` defines multipliers and labels (`kg`, `g`, `L`, `ml`, `dozen`, `pack`, `pcs`).
2. **Durable Write-Ahead Log (WAL)**:
   - `bill_journal` table: `journalId` (UUIDv7), `billId`, `payloadJson`, `status` (`PENDING`, `APPLIED`), `createdAt`, `appliedAt`.
   - Atomic `@Transaction` commits Bill + BillItems + StockMovements + FlowDaily, and updates journal status to `APPLIED`.
   - Idempotent crash recovery on application startup (`replayPendingJournals()`).
3. **Serial Transaction Queue (Actor Pattern)**:
   - Single-consumer Kotlin `Channel` running in application-scoped coroutine.
   - Strict FIFO ordering for bill commits. UI observes `Flow<CommitResult>`.
4. **Inventory Movement Ledger**:
   - `stock_movements` table: `id`, `itemId`, `deltaBaseUnits`, `reason` (`SALE`, `PURCHASE`, `ADJUSTMENT`, `RETURN`, `VOID`), `refId`, `businessDate`, `createdAt`.
   - Materialized `stockBaseUnits` column updated in same transaction, with `computeStockFromLedger` rebuild routine.
   - `flow_daily` table: Aggregates daily sold units per item per business day for fresh goods.
5. **Configurable Business Day Boundary**:
   - `BusinessDayManager` with configurable cutoff hour (default 2 AM) for late-night kirana operations.
6. **Active Cart Crash Resilience**:
   - `draft_cart` table auto-persists in-progress cart on every mutation and restores on app relaunch.
7. **Sync-Ready Identifiers & Audit Columns**:
   - Primary keys use time-ordered RFC 9562 `UuidV7`.
   - All business entities include `deviceId`, `createdAt`, `updatedAt`, and `deletedAt` (soft delete).
8. **Room Hygiene & Schema Export**:
   - `exportSchema = true` enabled with schema JSON committed to `app/schemas/`.
9. **Automated Testing**:
   - Unit tests covering `Money`, `Quantity`, `UuidV7`, and `BusinessDayManager` pass cleanly in `./gradlew test`.
10. **UI Truthfulness & Operations (Section C)**:
   - Removed fake "ONLINE" and "SYNCED" status pills, replaced with truthful indicators (`OFFLINE READY`, `MIC READY`, `LISTENING`).
   - Wired "Lock Drawer & Close Day" to `closeBusinessDay()`: snapshots `flow_daily` and calculates tomorrow's FLOW purchase plan and low-stock reorder alerts.
   - Wired "Share Day Summary via WhatsApp" to Android system share sheet (`Intent.ACTION_SEND`, `type = "text/plain"`).

---

### 🏗️ Phase 2 — Multi-Module Refactoring (In Progress)
- **Step 1 — Gradle convention plugins**: Implemented in `build-logic/convention` with precompiled convention plugins (`android.library`, `android.room`, `android.hilt`, `android.compose`, `jvm.library`) and clean version catalog.
- **Step 2 — Core module extraction**: Extracted `:core:common` (UUIDv7, utils), `:core:model` (entities, value classes Money/Quantity), `:core:database` (Room database, migrations, DAOs, SQLCipher Keystore security, Hilt DatabaseModule), `:core:data` (Repositories, WAL BillingQueue, CatalogValidator). All tests from Section A passing.
- **Step 3 — Domain use cases**: Extracted `:core:domain` with use cases `AddItemFromVoiceUseCase`, `RemoveLastItemUseCase`, `CommitBillUseCase`, `RecoverPendingBillsUseCase`, `GetReorderListUseCase`, `GetTomorrowFlowPlanUseCase`, `VoidBillUseCase`, and `CloseBusinessDayUseCase`. Refactored ViewModels to invoke domain use cases only. All tests passing.

---

## Planned Production Refinement (Phases 2-6)

| Phase | Description | Status |
|---|---|---|
| Phase 1 | Data correctness & transactional integrity | ✅ Completed |
| Phase 2 | Clean multi-module architecture, domain use cases, engine interfaces | 🟡 In Progress (Steps 1, 2, 3 done) |
| Phase 3 | Voice pipeline hardening: AudioRecord 16kHz PCM, Silero VAD, keyword spotter, offline Tamil pack, Tamil fractions parser | Pending |
| Phase 4 | On-device models: Whisper-Small INT8, Gemma-3n-E2B INT4, Play Asset Delivery, QNN/Hexagon NPU binding | Pending |
| Phase 5 | CameraX barcode scanner, daily FLOW report WhatsApp share, local UPI QR generator, Tamil/English string localization | Pending |
| Phase 6 | Golden utterance evaluation, unit & instrumentation tests, offline telemetry, CI/CD & release build | Pending |
