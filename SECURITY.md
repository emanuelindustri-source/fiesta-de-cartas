# Security policy

## Supported versions

| Version | Supported |
|---|---|
| 1.1.x | Yes |
| 1.0.x | No |

## Reporting a vulnerability

Please **do not** open a public issue for security problems. Write to
**emanuel.industri@gmail.com** with:

- a short description of the issue;
- the app version and Android version;
- steps to reproduce;
- any relevant logs or screenshots (without personal data).

You will receive an acknowledgement as soon as possible. Please allow time to
investigate and publish a fix before disclosing the issue.

## Secrets

This repository must never contain:

- the release keystore (`*.jks`) or its passwords;
- `keystore.properties`, `local.properties` or `.env` files;
- Google, AdMob, GitHub or payment-provider credentials;
- personal access tokens.

Local builds read signing data from `keystore.properties` (ignored by Git) or
from environment variables (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`,
`RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`). CI release builds use encrypted
GitHub Actions secrets.

If a secret is exposed, revoke it immediately, rotate the credential and check
the repository history. A leaked signing key cannot be "unsaid": publish a new
application identity or coordinate with the stores.

## Scope

The LAN server is designed for trusted local networks. It does not implement
authentication beyond a short room code and must not be exposed to the public
internet. Reports about that design are welcome, but it is not a hosted service.
