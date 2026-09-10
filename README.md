# Coin Rush 🪙

A hyper-casual lane-dodging arcade game for Android, built with Kotlin + Jetpack Compose,
monetized with Google AdMob.

**Gameplay:** you sit in one of 3 lanes. Obstacles and coins fall from the top.
Tap anywhere to cycle lanes. Dodge the red blocks, grab the gold coins.
Speed ramps up the longer you survive. One hit ends the run — unless you watch a
rewarded ad to revive.

**Monetization (all three AdMob formats):**

| Format | Placement | Revenue style |
|---|---|---|
| Banner | Persistently docked at the bottom | Passive impressions every session |
| Interstitial | Between runs (max every 2nd game-over) | High-CPM full-screen, capped so it never smothers play |
| Rewarded | "Watch ad to revive" on game over | Highest eCPM; opt-in, players *want* to watch |

---

## Get the APK — no Android toolchain needed

The repo ships with a GitHub Actions workflow (`.github/workflows/build-apk.yml`) that
runs the unit tests and builds a debug APK in the cloud on every push:

1. Create a repo on GitHub and push this folder to it.
2. Open the **Actions** tab, wait for the **Build APK** workflow to finish (~5–10 min).
3. Download the `coin-rush-debug-apk` artifact, unzip it, and sideload
   `app-debug.apk` onto any Android 8.0+ phone (allow "install unknown apps").

## Build locally (optional)

1. Install [Android Studio](https://developer.android.com/studio) (includes the JDK and SDK).
2. Open this folder in Android Studio, wait for Gradle sync.
3. Run ▶ on a device or emulator, or `./gradlew assembleDebug` from the terminal.

## Before you publish to Google Play

Monetization only pays out through **your own accounts** — these steps are yours to do:

1. **AdMob** — create an account at [admob.google.com](https://admob.google.com), register
   an app, and create one banner, one interstitial, and one rewarded ad unit.
2. Replace the test IDs with your real ones:
   - App ID in `app/src/main/AndroidManifest.xml`
     (currently Google's sample: `ca-app-pub-3940256099942544~3347511713`)
   - Ad unit IDs in `app/src/main/java/com/coinrush/game/ads/AdController.kt` and `BannerAd.kt`
3. **Google Play Console** — register ($25 one-time), complete identity verification,
   then create the app listing.
4. **Keystore** — generate a real signing key
   (`Build → Generate Signed Bundle → Create new…` in Android Studio, or `keytool`) and
   switch the release build off `signingConfig = debug` in `app/build.gradle.kts`.
5. **Release bundle** — build an AAB (`./gradlew bundleRelease`), add a Privacy Policy
   URL (required — the app serves ads), fill in the content rating questionnaire and
   Data Safety form (declare ad data collection), then submit for review.
6. Ads on a brand-new AdMob account take a few days to start serving — test with the
   test IDs until then so your account never gets flagged for invalid traffic.

## Project structure

```
coin-rush/
├── .github/workflows/build-apk.yml     # Cloud builds: tests + APK artifact
├── app/
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml     # AdMob app ID, launcher activity
│       │   ├── java/com/coinrush/game/
│       │   │   ├── GameEngine.kt       # Pure, unit-tested game logic (no Android deps)
│       │   │   ├── GameViewModel.kt    # 30fps tick loop, best score, ad events
│       │   │   ├── GameScreen.kt       # Compose UI: Canvas rendering + overlays
│       │   │   ├── MainActivity.kt     # Wires ViewModel ⇄ UI ⇄ ads
│       │   │   ├── GameApp.kt          # AdMob SDK init
│       │   │   └── ads/                # AdController (interstitial + rewarded), BannerAd
│       │   └── res/                    # Icons, theme, strings
│       └── test/                       # GameEngine unit tests
├── build.gradle.kts
└── settings.gradle.kts
```

## Ideas for v2

- Swipe controls + left/right lane movement instead of fixed cycling
- Power-ups (magnet, shield, x2 score) — extra rewarded-ad hooks
- Daily streaks and missions for retention
- Leaderboard (Google Play Games Services)
- Sound effects + haptics on coin pickup
