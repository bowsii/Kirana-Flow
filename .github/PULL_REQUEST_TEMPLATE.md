## Description

<!-- A clear and concise description of what this PR does. -->

## Type of change

- [ ] Bug fix (non-breaking change that fixes an issue)
- [ ] New feature (non-breaking change that adds functionality)
- [ ] Breaking change (fix or feature that would cause existing functionality to not work as expected)
- [ ] Refactor (no functional change)
- [ ] Documentation update
- [ ] CI/CD update

## Checklist

- [ ] I have read `CONTRIBUTING.md`
- [ ] My commits follow the **Conventional Commits** format (`feat:`, `fix:`, `refactor:`, `test:`, `docs:`, `ci:`, `build:`)
- [ ] I have added tests that prove my fix is effective or that my feature works
- [ ] All existing tests pass locally (`./gradlew testDebugUnitTest`)
- [ ] Detekt passes (`./gradlew detekt`) with zero new violations, no new `@Suppress` added
- [ ] Ktlint passes (`./gradlew ktlintCheck`) with zero new violations
- [ ] No hardcoded `Color(0x...)` literals outside `Color.kt` (enforced by CI)
- [ ] No hardcoded user-visible strings in composables (use string resources)
- [ ] No new secrets, keystores, or `google-services.json` files committed
- [ ] I have updated `PROJECT_CONTEXT.md` if the architecture or feature set changed
- [ ] Screenshots or screen recordings attached (for UI changes)

## Financial / Billing impact

<!-- If this change touches BillingQueue, BillJournal, KiranaRepository, or any DAO, answer: -->
- [ ] No financial logic changed
- [ ] Financial logic changed — WAL/recovery behavior re-verified with tests
- [ ] Close-Day idempotency unaffected or re-tested

## Linked issues

Closes #<!-- issue number -->

## Testing

Describe how you tested this change:

```
# example
./gradlew :core:data:testDebugUnitTest
```

Test results: <!-- paste relevant output -->

## Screenshots (if applicable)

| Before | After |
|--------|-------|
|        |       |
