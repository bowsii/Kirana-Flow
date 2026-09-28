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
| Billing | BillingScreen.kt | Mic button, voice waveform, cart list, quantity stepper, cart footer, payment trigger |
| Payment Modal | PaymentBottomSheet.kt | Cash/UPI selector, quick tender chips, change calculator, amount-in-words |
| Stock | StockScreen.kt | STOCK/FLOW catalog, low-stock alerts, search, edit dialog |
| Past Bills | PastBillsScreen.kt | Stats cards, bill list, detail dialog |

**Color Palette (NO violet/indigo/blue):**
| Role | Color | Hex |
|---|---|---|
| Background | Deep Dark Green | #0D1008 |
| Primary CTA | Emerald Green | #40C074 |
| Secondary / Warning | Amber Gold | #FFAA00 |
| STOCK tag | Emerald | #40C074 |
| FLOW tag | Amber | #FFAA00 |
| Error | Red | #FF5252 |

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

---

## Planned Next Stages

| Stage | Description |
|---|---|
| 2 | Add barcode scanner via CameraX, connect Open Camera intent |
| 3 | Whisper-Small INT8 integration via ONNX Runtime (replace Android ASR) |
| 4 | Gemma-3n-E2B INT4 NLU (replace rule-based engine) |
| 5 | Daily FLOW report, reorder list WhatsApp share |
| 6 | QNN / Hexagon NPU binding for under 400ms inference |
