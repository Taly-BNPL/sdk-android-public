## v1.3 - November 2026

SDK file: `aar/v1.3/taly-sdk-release.aar`. To upgrade from v1.2, see [Migrating from v1.2 to v1.3](readme.md#migrating-from-v12-to-v13).

### Added
- **Per-merchant API client credentials**: New `TalySdk.initialize(context, clientId, clientSecret, userName, password, environment)`. The client ID and client secret are generated in the Taly merchant portal (**API credentials** page).
- **Android 16 support**: Built and tested against API level 36 (edge-to-edge layout, predictive back).
- **Screenshot protection**: Screenshots and screen recording of the checkout screen are blocked in `Environment.Production`.

### Changed
- **Payment result verification**: `onPaymentSuccess` is now called only when the Taly server confirms the order (`CONFIRMED`). Rejected, cancelled, incomplete or unconfirmed orders call `onPaymentFailure`. The checkout page URL alone can no longer mark an order as paid.
- **Logging**: The SDK writes no logs in `Environment.Production`, whatever the log level.
- **Deprecated**: `TalySdk.initialize(context, userName, password, environment)` still compiles, but no longer signs in because the shared API client was removed. Use the new `initialize` with client credentials.

### Removed
- **Mixpanel analytics**: The `com.mixpanel.android:mixpanel-android` dependency is no longer required.
- **Shared API client**: The built-in shared client credentials were removed from the SDK.

### Fixed
- **Login crash**: Fixed a crash when the SDK sign-in failed for a reason other than a network error.
- **Token handling**: An access token is only sent to the environment that issued it, and a late sign-in response can no longer replace the token of a newer session.

## v1.2 - March 2026

### Fixed
- **Payment cancel redirection**: Fix issue where users were redirected incorrectly after cancelling payment.

## v1.1 - January 2026

### Fixed
- **Payment WebView Issue**: Resolved issue where payment webview was displaying empty content, preventing users from completing transactions.
- **Android 35 Navigation Bar**: Fixed navigation bar overlapping with webview content on devices running Android API level 35 or above.