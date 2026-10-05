# Radius Office — Kotlin Compose developer preview

Requires JDK 17, Android SDK 35, Android Studio, and Gradle 8.9. Genuine Compose; no WebView. Dependency versions are pinned. Gradle is not bundled.

Standalone: open this folder in Android Studio, configure Gradle 8.9, sync, select demo, and run on API 26+. Alternatively run `gradle :demo:assembleDebug` after configuring ANDROID_HOME.

Existing app: copy radius-office into your project, include it in settings.gradle.kts, align Kotlin/Compose/AGP versions with your app, and add implementation(project(":radius-office")). Call RadiusOfficeScreen(snapshot, fontFamily = approvedMonaSansFamily, onAction = handler) from your Compose hierarchy. Use ComposeView for a View-based host.

The demo reports callbacks, not backend success. Supply server-filtered snapshots and host editors/services. The module does not require Internet permission; the host networking layer owns it. Read API-CONTRACT.md and STATUS.md. HTML reference and tokens are included under reference/.

Updated October 2, 2026: `mock-data.json` and the personal demo include the neutral-gray Net Payout card. `dual-group.json` and `dual-team.json` show side grouping, muted calculator icons, semantic amounts, and final payout cards; pass these snapshots through your host adapter. Team fixture preserves the HTML reference total of $19,000 although displayed rows sum to $23,500; do not use this fixture for authoritative accounting. All HTML routes and runtime assets in `reference/` match current local dist. The source is still a partial native preview; see STATUS.md.

Reopen finalized breakdown preview: Present ReopenFinalizedBreakdownSheet from your host using a sheet and supply onDismiss; keep the existing screen underneath. Both buttons dismiss this independent design preview. It does not restart approvals or void a CDA.


October 5, 2026 fee-editor reference update: bundled HTML now includes the full-page fee editor, searchable checkbox payers, manual shares, co-agent modes, inline sliding-scale configuration, and dark-mode refinements. See FEE-EDITOR-HANDOFF.md for frontend/backend implementation. The native SwiftUI/Compose fee form is NOT implemented; existing AddFee/Edit callbacks still require a host editor. No native build or visual parity is claimed.
