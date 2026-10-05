# Delivery status — developer preview

Included: genuine native commission list renderer; supplied role/visibility data; ownership-gated Edit/Delete; native swipe/drag controls; read/edit states; destructive confirmation; dual-side gross sheet; payout footer; action callbacks; demo entry point; sample JSON; HTML reference.

Not yet ported: complete Home/queue, plan and allocation editors, fee forms, CDA, wire instructions, cap progress, tracker, activity/comments, full role-specific compositions, multi-side agent grouping, light mode, full animation matching. Host callbacks are integration hooks, not implementations of these editors or services.

Not verified: device/simulator rendering, pixel parity, VoiceOver/TalkBack interactions, host integration, Android dependency resolution/build, iOS SDK build. Swift source syntax can be checked locally, but syntax is not an iOS build.

Native system icons and fallback fonts are used. These are known differences from HTML. Supply approved Mona Sans font assets through the exposed font parameter. Do not mark this package as an exact visual port or production-ready. The HTML reference remains the acceptance target.

Acceptance before production: compile both demos; implement host adapters/editors; review every required role/state against HTML; verify vertical scroll versus horizontal swipe, closed-tray visibility, keyboard/accessibility actions, own/foreign/finalized fees, error recovery, and authoritative payout refresh.

October 2, 2026: refreshed all bundled HTML entrypoints and their assets from current local dist. Native renderer now supports payout cards and neutral income icons with green values; personal demo includes Net Payout. Dual-side fixtures include Team and Group totals plus Personal payout. System icon/font differences and full role-specific composition gaps above remain. No UI/device testing performed.

October 2 follow-up: added a separate native ReopenFinalizedBreakdownSheet component matching the new HTML bottom-sheet copy and actions. The reference uses a separate View as option without switching roles. Source parsing/archive checks only; device builds and UI review remain unverified.


October 5, 2026 fee-editor reference update: bundled HTML now includes the full-page fee editor, searchable checkbox payers, manual shares, co-agent modes, inline sliding-scale configuration, and dark-mode refinements. See FEE-EDITOR-HANDOFF.md for frontend/backend implementation. The native SwiftUI/Compose fee form is NOT implemented; existing AddFee/Edit callbacks still require a host editor. No native build or visual parity is claimed.
