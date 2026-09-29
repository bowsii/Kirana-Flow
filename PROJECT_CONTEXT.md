# KiranaFlow — Project Context

> Last updated: 2026-09-29

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

**Color Palette (Warm Sand & Deep Accents, NO violet/indigo/blue/navy):**
| Role | Color | Hex |
|---|---|---|
| Background | Warm Sand / Ivory | #EDE8DC |
| Background Deep | Muted Sand | #E0DAC8 |
| Surface / Cards | Pure White | #FFFFFF |
| Primary Action / Header | Deep Forest Teal | #2D6E5E |
| Secondary CTA / Deep Charcoal | Deep Charcoal Forest | #121714 |
| Amount & Accents | Rich Amber / Ochre | #E09B1A |
| Success / Badges | Green Mint | #4CAF50 |
| Error / Danger | Crimson Red | #E53E3E |

> **Approved Palette Rule:**
> The active approved palette is the Warm Sand & Deep Accents palette exclusively (`#EDE8DC`, `#2D6E5E`, `#121714`, `#E09B1A`). No violet, indigo, navy, or blue AI palette colors are permitted anywhere in the codebase. All historical `#1C2333` blue/navy occurrences have been replaced with `#121714` (Deep Charcoal Forest).

---

## Pre-Phase 3 Review Fixes (Items 1 – 17)

All 17 review fixes have been completed, verified with passing tests, and committed one commit per numbered group.

| # | Item | Status | Key Files Modified | Test Name / Verification |
|---|---|---|---|---|
| 1 | Palette Rule (#1C2333 replaced with #121714) | **DONE** | `core/ui/.../Color.kt`, `PROJECT_CONTEXT.md` | Verification across all Compose theme components |
| 2 | Caller-side PENDING journal write & recovery test | **DONE** | `core/data/.../BillingQueue.kt` | `BillingQueueIntegrationTest.testCrashAfterBillPotruBeforeConsumerRuns_billIsRecoveredOnRestart` |
| 3 | SQLCipher net.zetetic migration, Keystore AES-256 GCM key, fail loudly | **DONE** | `core/database/.../DatabaseSecurityManager.kt`, `KiranaFlowDatabase.kt` | `EncryptedDatabaseTest.testOpenEncryptedDb_commitBill_close_reopen_readBillBack` |
| 4 | Bind status pills to real hardware & speech state | **DONE** | `feature/billing/.../VoiceStateMonitor.kt`, `BillingViewModel.kt`, `BillingScreen.kt` | `VoiceStateMonitorTest` (3 unit tests) |
| 5 | `:ai:audio` 16 kHz Mono AudioFrame, ring buffer, VAD hook, reactive `SpeechEngine` | **DONE** | `ai/audio/.../AudioFrame.kt`, `AudioRingBuffer.kt`, `VadHook.kt`, `ai/asr/.../SpeechEngine.kt`, `AndroidSpeechEngine.kt` | `AudioFrameRingBufferTest`, `AndroidSpeechEngineTest` |
| 6 | IntentParser returns `List<ParsedCommand>` with confidence scores | **DONE** | `ai/nlu/.../ParsedCommand.kt`, `IntentParser.kt`, `RuleBasedIntentParser.kt` | `RuleBasedIntentParserTest` (6 unit tests) |
| 7 | InferenceRuntime backend contract (QNN -> NNAPI -> CPU), warmUp(), close() | **DONE** | `ai/runtime/.../InferenceRuntime.kt`, `AndroidOnDeviceRuntime.kt` | `InferenceRuntimeTest` (3 unit tests) |
| 8 | Move CatalogValidator into `:core:domain` | **DONE** | `core/domain/.../validation/CatalogValidator.kt` | `CatalogValidatorTest` (4 unit tests) |
| 9 | Move voice orchestration to usecase + foreground service (microphone type) | **DONE** | `core/domain/.../usecase/VoiceOrchestratorUseCase.kt`, `ai/asr/.../VoiceRecognitionForegroundService.kt`, `AndroidManifest.xml` | `VoiceOrchestratorUseCaseTest` (2 unit tests) |
| 10 | FTS4 with unicode61 tokenizer, `contentEntity = CatalogItem::class`, Tamil-script test | **DONE** | `core/database/.../CatalogItemFts.kt`, `CatalogDao.kt`, `KiranaFlowDatabase.kt` (v3) | `CatalogFtsTest.testFts_unicode61_matchesTamilScriptAlias`, `testFts_contentEntity_syncOnUpdateAndInsert` |
| 11 | Move tests into the modules they cover | **DONE** | `core/model/.../DataCorrectnessTest.kt`, `core/data/.../BillingQueueIntegrationTest.kt` | All module tests running inside target modules with zero test debt |
| 12 | Kover test coverage per module | **DONE** | `build.gradle.kts`, `gradle/libs.versions.toml` | 38 passing tests across modules; reports generated via `koverPrintCoverageDebug` |
| 13 | Verify stock rebuild sums ALL ledger movements across dates | **DONE** | `core/data/.../BillingQueueIntegrationTest.kt`, `StockMovementDao.kt` | `BillingQueueIntegrationTest.testStockRebuild_sumsAllMovementsAcrossMultipleDates_notJustOneDay` |
| 14 | Reorder threshold crossing test | **DONE** | `core/data/.../BillingQueueIntegrationTest.kt` | `BillingQueueIntegrationTest.testReorderThresholdCrossing` |
| 15 | `bill_counter` table & sequential bill number per device in commit transaction | **DONE** | `core/model/.../BillCounter.kt`, `core/database/.../BillCounterDao.kt`, `BillingQueue.kt`, `KiranaFlowDatabase.kt` (v4) | `BillingQueueIntegrationTest.testSequentialBillNumbers_assignedPerDeviceInCommitTransaction` |
| 16 | Idempotent Close Day (`closed_business_days`) & blocked void after close day | **DONE** | `core/model/.../ClosedBusinessDay.kt`, `core/database/.../ClosedBusinessDayDao.kt`, `BillingQueue.kt`, `KiranaRepository.kt`, `KiranaFlowDatabase.kt` (v5) | `BillingQueueIntegrationTest.testCloseBusinessDay_isIdempotent`, `testVoidAfterCloseDay_isBlocked` |
| 17 | GitHub Actions CI workflow (build, unit tests, detekt, ktlint, Room schema diff) | **DONE** | `.github/workflows/ci.yml`, `build.gradle.kts`, `gradle/libs.versions.toml` | CI workflow verified with `detekt`, `ktlintCheck`, `testDebugUnitTest`, schema diff |

---

## Kover Test Metrics & Coverage Summary

Total automated unit tests: **38** (0 failures, 0 skipped).

| Module | Passing Tests | Test Classes | Coverage (Line) |
|---|---|---|---|
| `:ai:nlu` | 6 | `RuleBasedIntentParserTest` | 82.2% |
| `:ai:runtime` | 3 | `InferenceRuntimeTest` | 77.8% |
| `:core:data` | 12 | `BillingQueueIntegrationTest` | 73.0% |
| `:ai:audio` | 3 | `AudioFrameRingBufferTest` | 48.2% |
| `:ai:asr` | 2 | `AndroidSpeechEngineTest` | 37.4% |
| `:core:domain` | 6 | `VoiceOrchestratorUseCaseTest`, `CatalogValidatorTest` | 34.0% |
| `:core:model` | 5 | `DataCorrectnessTest` | 21.0% |
| `:core:database` | 2 | `CatalogFtsTest` (+ `EncryptedDatabaseTest` instrumented) | 11.1% |
| `:feature:billing` | 3 | `VoiceStateMonitorTest` | 2.2% |

---

## GitHub Commit History

| Commit | Group / Phase | Description |
|---|---|---|
| `985de9a` | Item 1 | `fix(ui): replace navy #1C2333 with charcoal forest #121714 in palette` |
| `2c2de0c` | Item 2 | `fix(data): write PENDING journal before queueing and clear cart on success` |
| `78f9b6f` | Item 3 | `feat(security): integrate SQLCipher with Keystore AES-GCM and strict validation` |
| `e4697a4` | Item 4 | `feat(ui): bind voice status pills to real hardware and recognizer state` |
| `d0a4fdf` | Item 5 | `feat(ai): introduce :ai:audio module and reactive SpeechEngine contract` |
| `d37c310` | Item 6 | `feat(ai): update IntentParser to return List<ParsedCommand> with confidence` |
| `c5a00ea` | Item 7 | `feat(ai): define InferenceRuntime contract with backend priority fallback` |
| `5e0136e` | Item 8 | `refactor(domain): move CatalogValidator to :core:domain` |
| `a60e013` | Item 9 | `feat(ai): move voice orchestration to domain usecase and add microphone foreground service` |
| `2364c92` | Item 10 | `feat(database): configure FTS4 unicode61 tokenizer and test Tamil script aliases` |
| `7da7573` | Item 11 | `refactor(test): move unit tests into respective domain, data and model modules` |
| `2784125` | Item 12 | `build(ci): add Kover test coverage plugin and report coverage per module` |
| `040cb13` | Item 13 | `test(stock): verify stock rebuild sums all ledger movements across multiple dates` |
| `2b53f56` | Item 14 | `test(stock): add test for reorder threshold crossing` |
| `612335d` | Item 15 | `feat(billing): assign sequential bill numbers per device via bill_counter table` |
| `d666781` | Item 16 | `feat(billing): enforce Close Day idempotency and block void after close day` |
| `27fe1c2` | Item 17 | `ci: add GitHub Actions workflow for build, unit tests, detekt, ktlint and schema diff` |
