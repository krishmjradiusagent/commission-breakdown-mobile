# Radius Office — SwiftUI developer preview

Requires iOS 17+, Swift 5.9+, and Xcode with an iOS SDK. Genuine SwiftUI; no WebView.

1. Unzip. In your Xcode app, Add Package Dependencies > Add Local; select this folder.
2. Link the `RadiusOffice` product to your app target.
3. Present `RadiusOfficeView(snapshot: yourSnapshot, fontName: yourRegisteredMonaSansName, onAction: handler)` in an existing NavigationStack or hosting controller.
4. Route actions to existing host editors/services; replace the snapshot after server success.
5. For a standalone demo, create an iOS App project, add this local package, replace the generated App entry point with `Examples/DemoApp.swift`, and run on a simulator. Do not retain two @main entry points.

The demo reports action callbacks; it does not save fees or connect to a backend. Read STATUS.md before integration. HTML reference and tokens are included under reference/.

Updated October 2, 2026: `mock-data.json` and the personal demo include the neutral-gray Net Payout card. `dual-group.json` and `dual-team.json` show side grouping, muted calculator icons, semantic amounts, and final payout cards; pass these snapshots through your host adapter. Team fixture preserves the HTML reference total of $19,000 although displayed rows sum to $23,500; do not use this fixture for authoritative accounting. All HTML routes and runtime assets in `reference/` match current local dist. The source is still a partial native preview; see STATUS.md.

Reopen finalized breakdown preview: Present ReopenFinalizedBreakdownSheet from your host using a sheet and supply onDismiss; keep the existing screen underneath. Both buttons dismiss this independent design preview. It does not restart approvals or void a CDA.


October 5, 2026 fee-editor reference update: bundled HTML now includes the full-page fee editor, searchable checkbox payers, manual shares, co-agent modes, inline sliding-scale configuration, and dark-mode refinements. See FEE-EDITOR-HANDOFF.md for frontend/backend implementation. The native SwiftUI/Compose fee form is NOT implemented; existing AddFee/Edit callbacks still require a host editor. No native build or visual parity is claimed.
