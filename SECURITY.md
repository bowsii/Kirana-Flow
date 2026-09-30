# Security Policy — KiranaFlow

## Supported Versions

| Version   | Supported          |
| --------- | ------------------ |
| `main`    | ✅ Yes             |
| All others | ❌ No              |

## Reporting a Vulnerability

**Do NOT open a public GitHub issue for security vulnerabilities.**

Please report security vulnerabilities by emailing the project owner directly.
You will receive a response within **48 hours**.

Provide as much of the following information as possible to help triage quickly:

- Type of vulnerability (e.g., SQL injection, insecure key storage, missing auth check)
- Affected module and file paths
- Steps to reproduce, proof-of-concept code, or screenshots
- Potential impact: can financial data be accessed or corrupted?
- Your suggested fix (optional but appreciated)

## Scope

The following are **in scope**:

- Incorrect or missing SQLCipher encryption for the KiranaFlow local database
- Insecure storage of the database passphrase (e.g., plaintext in shared preferences)
- Bypass of Close-Day idempotency or WAL journal integrity checks
- Authentication bypass in any future multi-user or cloud sync feature
- Injection attacks through voice NLU parsed values written to the database

The following are **out of scope**:

- Vulnerabilities in third-party libraries that have published patches (please report upstream)
- Attacks requiring physical access to an unlocked device already running the app
- Denial-of-service on a single-user offline device

## Disclosure Process

1. Security report received → acknowledge within 48 h
2. Triage and root cause analysis → within 5 business days
3. Fix developed, reviewed, and tested in a private branch
4. Release published with patch notes (no CVE number assigned for private reporter agreement)
5. Public disclosure after 30 days or sooner if a patch is released and reporter agrees

## Security Design Notes

KiranaFlow stores all financial data in an SQLite database encrypted with
**SQLCipher** (AES-256-CBC). The passphrase is derived per-device using
the Android Keystore. No financial data leaves the device unless a future
cloud-sync feature is explicitly enabled by the shopkeeper.
