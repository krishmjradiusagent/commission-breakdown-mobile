# Host integration contract

This is a developer-preview commission screen module, not a port of the entire HTML application.

Supply OfficeSnapshot from your existing backend. Identical field names are used by both platforms; Android models require your JSON serializer of choice. Money is formatted server-side; no client-side allocation or fee calculation is authoritative. Stable row/section IDs are required. Never accept permission flags from untrusted clients on the server.

- `viewerID`: authenticated agent ID. `personal`: true for Agent and Group Member.
- `editable`, `deletable`: server-authorized capabilities. Personal views additionally require `creatorID == viewerID`.
- `finalized`: disables mutation; `canConfirm`: server approval-workflow capability.
- `sideGross`: include Listing and Buying for dual transactions; backend gross total must reconcile with both.
- `sections`: already filtered for viewer visibility. Do not send hidden financial rows to unauthorized clients.

Actions are intents. The host opens its fee/plan/allocation editors, calls its authenticated APIs, handles validation/conflicts/loading/errors, then supplies a fresh snapshot. No network addresses or credentials are embedded. Delete requires confirmation before the callback. Persist successful mutations before replacing data. A failed mutation must leave the old snapshot visible and show a host error.

Required host routes: Edit(row), Delete(row), AddFee, Confirm, Comment, Back. Supply row IDs to your existing services; use optimistic-concurrency versions and idempotency keys where supported. No endpoint names are assumed.

Mock fixtures are demonstrative and do not establish production permissions. Ally owns TC Fee only. Group and Team fees are intentionally locked. The sample mirrors the single-side fixture payout but is not an independent commission calculator.

October 2, 2026 update: optional row `style` is `income` (green amount, neutral calculator), `payout` (neutral-800 card, green wallet and amount), or omitted (ordinary row). Negative ordinary amounts and receipt icons are red. Supply separate sections for Buying side and Listing side; totals are host-owned. This field is backward compatible with existing snapshots.


October 5, 2026 fee-editor reference update: bundled HTML now includes the full-page fee editor, searchable checkbox payers, manual shares, co-agent modes, inline sliding-scale configuration, and dark-mode refinements. See FEE-EDITOR-HANDOFF.md for frontend/backend implementation. The native SwiftUI/Compose fee form is NOT implemented; existing AddFee/Edit callbacks still require a host editor. No native build or visual parity is claimed.
