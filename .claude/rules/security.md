## Security

- No secrets/API keys in source code or version control.
- Use encrypted storage for sensitive data (EncryptedSharedPreferences, Keystore).
- HTTPS only; validate all input from external sources.
- Obfuscate release builds (R8/ProGuard).