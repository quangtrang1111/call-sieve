<div align="center">

<img src="docs/icon.svg" width="96" alt="CallSieve icon" />

# CallSieve

**Silence spam calls with the precision of a regular expression.**

A tiny, private, open-source call blocker for Android. Write a pattern, and every call that matches it never rings.

[![Build](https://github.com/quangtrang1111/call-sieve/actions/workflows/build.yml/badge.svg)](https://github.com/quangtrang1111/call-sieve/actions/workflows/build.yml)
[![License: MIT](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)
![Android 10+](https://img.shields.io/badge/Android-10%2B-3DDC84?logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.1-7F52FF?logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4)

[![Support me on Ko-fi](https://img.shields.io/badge/Ko--fi-Buy%20me%20a%20coffee-FF5E5B?logo=ko-fi&logoColor=white)](https://ko-fi.com/quangtrang1111/?hidefeed=true&widget=true&embed=true)

</div>

---

## Why CallSieve?

Most call blockers make you choose between a blocklist you maintain by hand, one number at a time, and a cloud service that uploads your call history in exchange for a "spam database".

CallSieve does neither. Spam usually comes in **families**: a premium-rate prefix, a telemarketer's landline range, a run of numbers that only differ in the last few digits. One regex catches the whole family:

```
^1900          → every 1900 premium-rate number
^02871\d{6}    → an entire telemarketing range
^\+            → every international call
^$             → every hidden / private number
```

## Features

- **Regex rules.** Block by prefix, suffix, range or any pattern you can express. Toggle each rule on and off without deleting it.
- **Truly silent.** Blocked calls don't ring, don't wake the screen and don't post a missed-call notification.
- **Still auditable.** Every blocked call is kept in your normal call log, so nothing important disappears without a trace.
- **Built-in tester.** Type any number and see instantly whether it would be blocked, and by which rule.
- **Safety rails.** Invalid patterns are rejected as you type, and patterns broad enough to block almost everything are flagged before you save them.
- **Fail-open by design.** If anything goes wrong while screening, the call is let through. CallSieve will never block a call because of a crash.
- **Contacts always ring.** Android only routes calls from numbers *not* in your contacts through CallSieve.
- **Material You.** Follows your wallpaper colors and dark mode on Android 12+.

## Privacy

CallSieve requests **zero Android permissions**: no internet, no contacts, no phone state, no call log access.

- It cannot send data anywhere because it has no network access at all.
- Rules are stored locally on your device in app-private storage.
- Numbers are matched in memory during the call and are never saved by the app.

It relies on Android's official [`CallScreeningService`](https://developer.android.com/reference/android/telecom/CallScreeningService) API: you grant the *Call screening* role once, and the system asks CallSieve about each incoming call from an unknown number before it rings.

<!-- TODO: add screenshots to docs/screenshots/ and uncomment this section.
## Screenshots

<p align="center">
  <img src="docs/screenshots/main.png" width="260" alt="Rule list and status" />
  <img src="docs/screenshots/tester.png" width="260" alt="Number tester" />
  <img src="docs/screenshots/editor.png" width="260" alt="Rule editor" />
</p>
-->

## Getting started

### Requirements

- Android 10 (API 29) or later
- To build: JDK 17+, Android SDK 35, Gradle 8.9+

### Build and install

**Android Studio:** open the project folder, connect your phone with USB debugging enabled, and press **Run**.

**Command line:**

```bash
git clone https://github.com/quangtrang1111/call-sieve.git
cd call-sieve
gradle installDebug
```

Every push to `main` is also built by [GitHub Actions](https://github.com/quangtrang1111/call-sieve/actions/workflows/build.yml), and the debug APK is attached to each run as an artifact.

### First launch

1. Open CallSieve and tap **Allow call screening**.
2. Choose CallSieve as your call screening app.
3. Tap **Add rule** and enter a pattern, e.g. `^1900`.
4. Use **Test a phone number** to check it does what you expect.

## Writing rules

Before matching, every incoming number is normalized:

| Incoming | Matched as |
|---|---|
| `+84 912 345 678` | `0912345678` |
| `84912345678` | `0912345678` |
| `(028) 7100-0000` | `02871000000` |
| `+1 202 555 0123` | `+12025550123` |
| Hidden / private | *(empty string)* |

Rules use [Kotlin regex syntax](https://kotlinlang.org/api/core/kotlin-stdlib/kotlin.text/-regex/) and match **anywhere** in the number, so anchor with `^` (start) and `$` (end) when you mean it. Rules are checked top to bottom and the first enabled match blocks the call.

| Goal | Pattern |
|---|---|
| Premium-rate 1900 numbers | `^1900` |
| Hanoi and Ho Chi Minh City landlines | `^02[48]` |
| A specific range | `^02871\d{6}$` |
| All international calls | `^\+` |
| Hidden / private numbers | `^$` |
| One exact number | `^0912345678$` |
| Numbers ending in 8888 | `8888$` |

> Normalization currently converts the Vietnam country code (`+84`) to the local `0…` format. Numbers from other countries keep their `+<country code>` form, so write international patterns starting with `^\+`.

## Troubleshooting

| Problem | Fix |
|---|---|
| Calls from a number still ring | Check it isn't saved in your contacts. Contacts bypass call screening. |
| Blocking stops after a while (Xiaomi, Oppo, Vivo, Realme) | Enable **Autostart** for CallSieve and set its battery usage to **No restrictions**. |
| Blocking stopped after I force-stopped the app | Android disables a force-stopped app's services. Open CallSieve once to restore screening. |
| The status card says screening isn't allowed | Another app may have taken the call screening role. Tap **Allow call screening** again. |

## Project structure

```
app/src/main/java/dev/quangtrang/callsieve/
├── CallSieveService.kt   # CallScreeningService: decides block / allow
├── PhoneNumber.kt        # number normalization
├── RuleMatcher.kt        # regex compilation, matching, "too broad" check
├── RuleStore.kt          # JSON persistence in SharedPreferences
├── Rule.kt               # rule model
├── MainActivity.kt
└── ui/                   # Jetpack Compose UI and theme
```

## Contributing

Bug reports and pull requests are welcome. Please [open an issue](https://github.com/quangtrang1111/call-sieve/issues) to discuss larger changes before starting on them.

## License

CallSieve is released under the [MIT License](LICENSE).
