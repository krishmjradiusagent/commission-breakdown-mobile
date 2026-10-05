# Add / Edit fee — frontend and backend handoff

Updated: October 5, 2026. Audience: Milan, Saranjith, Shaily, and implementation engineers.

## Delivery and source of truth

- Prototype: [Commission breakdown](https://radius-office-commissions.radiusagent-2682.chatgpt.site/Commission%20breakdown.html).
- Repository: [commission-breakdown-mobile](https://github.com/krishmjradiusagent/commission-breakdown-mobile).
- Canonical web reference: `Commission breakdown.html`; main editor is `#dedSheet.fee-page`.
- Native packages: `downloads/radius-office-ios-source.zip` and `downloads/radius-office-android-source.zip`. Read each package's `STATUS.md` for its actual implementation coverage.
- This is a prototype and implementation specification. No production backend was inspected or changed. API shapes below are proposed integration contracts, not existing deployed endpoints.
- Latest HTML changes passed source/logic checks. Latest visual refinements have not been rendered-verified. Native device builds and device/accessibility review remain required.

## What changed

| Before | Current approved direction |
| --- | --- |
| Add fee bottom sheet became crowded | Full-page Add / Edit fee with Back, scrolling fields, and fixed Save fee |
| Fixed payer label | Search people or roles; multi-select with a checkbox for every result |
| Role-level share controls | Selected person's name with a manually entered percentage to the right |
| Dropdown extending down | Payer and co-agent dropdowns open upward within available editor space |
| Repeated “Agent percentage” subtext | Removed; role appears in search results and accessibility labels |
| Loose total label | Bordered Total shares row: label left, percentage right; spacing above |
| Missing co-agent configuration | Each agent pays / Split equally / Proportionate to allocation |
| Missing tier configuration | Inline Sliding scale box, short description, switch right, editable tier rows |
| Inconsistent dark header and switch | Header matches content background; dark off-track; themed checkbox list |
| Indented Apply fee segment | Outer side margins removed; inner control padding retained |

Existing surrounding commission cards, credits/referrals, permission rules, wire-instruction flows, and payout styling stay in place. This is not a redesign of the whole commission workflow.

## Frontend implementation

### Entry and navigation

1. Route the existing Add fee action and permitted Edit fee action to one full-page editor. Do not add an intermediate dialog.
2. Add mode uses “Add fee”; edit mode uses “Edit fee”. Back returns to the prior breakdown and scroll position. Preserve the existing unsaved-change confirmation.
3. Keep header and footer outside the scrolling field region. Account for keyboard and safe-area insets. Header background must equal the form background.
4. Save fee persists once, then returns to refreshed breakdown. Preserve draft on network/validation failure; prevent duplicate submissions while saving.

### Fields in order

| Field | Behavior |
| --- | --- |
| Name | Required, trimmed; example placeholder “E&O, TC, title fee” |
| Amount | Required flat monetary amount above zero, currency prefix |
| Who pays? | Search by name or role; current side's eligible agent/co-agents, team lead, group lead |
| Selected people | One name + manual percentage input per selection; no repetitive role subtext |
| Total shares | Label left/value right in bordered Item; hide for one payer at 100%; show red invalid total |
| Co-agent splits | Visible when two or more agents are selected; upward single-select dropdown |
| Deal side | Only when the existing context permits switching sides; use server side IDs in production |
| Apply fee | Pre-split / Post-split; preserve existing timing semantics |
| Payable to | Existing Radius / team-group / external choices; actual eligible recipients from backend |
| Recipient name | Required for external recipient; existing wire-instruction entry opens directly |
| Sliding scale | Bordered box; title and “Enable tiered fee values.” left, switch right |
| Tier rows | Expand inline when on: From, Up to, Fee value; Add tier and per-row Remove |

The web reference is a flat-fee editor. The screenshots of the web fee-type settings page were design references; a new percentage-fee type selector was not added in this change.

### People and shares

- Search result text: `Name · Role`. Roles: Agent, Team lead, Group lead. Every result has a visible checked/unchecked checkbox. Multi-select remains open while selecting; typed search never drops selections.
- Selection alone determines which rows appear: agent + team, team + group, agent + group, all three, or any valid subset; include co-agents individually.
- Percentages are entered manually and apply to this fee. Do not substitute commission-plan split percentages automatically.
- Require at least one selected payer. Each selected percentage must be finite, greater than 0, at most 100. Total must equal 100 (prototype tolerance 0.001); production should use exact fixed-point basis points.
- No hidden/unselected payer may receive a charge. A deleted or ineligible payer on edit needs an explicit resolution; never silently reassign its share.
- New selections may require manually filling/rebalancing shares. Deselecting does not silently normalize the remaining people.
- Keep stable IDs separate from display names. Prototype keys such as `Agent:Mark Perez`, `Team`, and `Group` are demo identifiers, not production identity keys.

### Co-agent modes: actual prototype behavior

The sum of selected agent percentages is the **agent pool percentage**. Team/group portions are unaffected by changing the co-agent mode.

| Mode | Current behavior |
| --- | --- |
| Split equally (`equal`) | Selecting the mode divides the current agent pool equally across selected agents; remainder goes to the last selected agent. Manual edits afterward remain authoritative. |
| Proportionate to allocation (`allocation`) | Selecting the mode divides the pool using the selected agents' commission allocations; prototype falls back to equal if their total allocation is zero. Manual edits afterward remain authoritative. |
| Each agent pays (`each`) | Each selected agent is charged the entire agent pool, so total charged can exceed the base Amount. Individual displayed shares still partition the pool and sum with other roles to 100. |

Example: Amount $100, two agents at 40% each and team at 20%. Equal mode charges $40/$40/$20 = $100. Each agent pays charges $80/$80/$20 = $180. A Total fee preview appears beside Amount for repeated charges. Do not multiply team/group portions.

This repeated-charge interpretation is observable prototype behavior, not an independently confirmed production accounting rule. Product/backend owners must confirm it before using it for real money. Changing a mode must not silently erase user-entered shares on every render.

### Sliding scale scope

- Keep it on the same page. Toggle on seeds one tier if none exists. Add tier pre-fills From with the previous Up to. From remains editable in the prototype.
- Toggle off hides tiers but retains their draft values; edit roundtrip restores toggle and rows.
- **Configuration only in this delivery. No tier evaluation, threshold basis, or payout recalculation from tiers was requested.** Base Amount still drives the prototype charge.
- Prototype saves raw tier strings and does not fully validate them. Production must validate stored configuration, but must not activate financial calculations based on unspecified rules.
- Before activating scale pricing, product must define threshold metric, currency/unit, inclusive/exclusive bounds, overlap/gap policy, open-ended final tier, whole-value vs marginal tiers, and timing relative to splits. Until then store as inactive configuration and make that state explicit in the API.

### Design and accessibility

- Source system: `Radius UI Design System/styles.css`, `components/components.css`; `forms/{InputGroup,Checkbox,Switch,Button,Combobox}.jsx`, `data-display/Item.jsx`, and `tokens/{colors,spacing,typography,radius}.css`.
- Match the bundled HTML component structures: `.rds-input-group`, `.rds-checkbox`, `.rds-switch`, `.rds-item.rds-item--bordered`, `.rds-combobox`, `.rds-command__item`, `.rds-btn`.
- Form uses `--space-6` vertical section gaps and `--space-4` horizontal padding; selected payer rows use `--space-4` gaps; summary has `--space-4` top separation. Percentage column is 100px in the reference; adapt for accessibility without clipping the name.
- Use paired semantic background/foreground, popover/popover-foreground, border, primary/primary-foreground tokens. Dark off switch uses approved scoped `--neutral-700` correction. No hardcoded light surfaces in dark mode.
- Name and percentage are vertically centered. Total shares is neutral unless invalid; do not use payout green for a percentage total.
- Upward list is scrollable, bounded by editor/keyboard safe area, maximum 240px in reference. Ensure sufficient space instead of allowing a zero-height or clipped list.
- Accessible labels include person's name, role, and percentage; tier inputs include tier number and field label. Toggle exposes expanded fields. Errors must be text, not color alone.
- Keyboard: arrow keys navigate options, Enter/Space toggle, Escape dismisses and restores focus; normal tab order. Native platforms require equivalent VoiceOver/TalkBack labels, checkbox state, and focus restoration.
- Full-page slide uses 240ms existing easing, disabled for reduced-motion preference. Preserve dynamic text, sufficient tap targets, and keyboard-aware scrolling.

## Backend integration proposal

Use the application's existing authenticated fee routes and persistence model; names here are conceptual. Do not create unauthenticated endpoints or trust UI capability flags.

### Required reads

Load transaction/side context, current fee (for edit), revision, currency, permitted payer identities with roles/allocations, eligible payable recipients, existing wire-instruction reference, and server-derived create/edit capabilities. Return only data the viewer is allowed to see. Fetch fresh eligibility when changing sides; require resolving invalid selected people.

### Proposed mutation payload

```json
{
  "schemaVersion": 1,
  "feeId": "fee_123",
  "revision": 7,
  "transactionId": "transaction_123",
  "sideId": "side_buying",
  "name": "Transaction coordinator fee",
  "currency": "USD",
  "amountMinor": 10000,
  "stage": "postsplit",
  "payers": [
    { "partyId": "agent_ally", "role": "agent", "shareBasisPoints": 4000 },
    { "partyId": "agent_benny", "role": "agent", "shareBasisPoints": 4000 },
    { "partyId": "team_123", "role": "team", "shareBasisPoints": 2000 }
  ],
  "coAgentMode": "equal",
  "payableTo": { "kind": "external", "partyId": null, "name": "Harbor Services" },
  "wireInstructionsId": null,
  "slidingScale": { "enabled": false, "calculationEnabled": false, "tiers": [] }
}
```

For create, omit feeId/revision as appropriate for the existing API. Use an idempotency key in the existing transport contract. Tier proposal: `{ "id": "tier_1", "fromMinor": 0, "toMinor": 100000, "feeMinor": 5000 }`; null final upper bound only after its semantics are agreed. The prototype permits incomplete draft strings; an API draft representation must be distinct from valid persisted configuration.

### Server validation and transaction

1. Authenticate user and authorize transaction, side, action, fee ownership, and current status. Recheck inside the write transaction. Finalized fees cannot be edited through this flow. Preserve existing lead permissions and own-fee restrictions for personal views.
2. Validate revision/idempotency; reject stale edits with the current revision. Do not overwrite concurrently changed fees.
3. Validate nonempty bounded name and external recipient name, known currency, positive integer minor amount, known stage/mode, unique eligible party IDs, valid role bindings, and exact 10,000 basis-point total. Backend owns length/range limits; return them to clients rather than inventing separate platform limits.
4. Validate allowed recipient and wire reference access. Never include raw bank details in this fee payload or Slack handoff.
5. Store payer rows, mode, timing, recipient, and inactive scale configuration atomically; derive legacy role totals from payer rows if needed. Never trust client-calculated totals or creator identity.
6. Apply the existing authoritative fee/split accounting rules with exact decimal/minor-unit arithmetic. Allocate rounding remainder deterministically so allocations reconcile; report base amount separately from repeated-charge total.
7. Preserve existing approval/review transitions and audit actor/time/before-after data. The prototype resets non-draft status to draft after mutation; map this through the real workflow service, not a blind status assignment.
8. Return persisted fee, revision, validated capabilities, and refreshed permitted breakdown/payout snapshot. Clients update UI only after success.

### Errors

Return stable field paths (`payers[1].shareBasisPoints`, `slidingScale.tiers[0].toMinor`) and user-readable messages. Distinguish invalid input, forbidden, finalized, stale revision, missing party, and retryable service failure. Preserve draft and focus the first error. Never show “saved” on a failed request.

### Legacy migration

- Map prototype `n`→name, `a`→amount, `p`/agentPct/teamPct/groupPct→roles, `personShares`→payer rows, `agentDistribution`→coAgentMode, `stage` unchanged, side `mine`/`other`→real side IDs.
- Old `Both` means Agent + Team; `All` means Agent + Team + Group. Resolve actual people through current server context and retain historic financial results.
- Do not infer identities from names or redistribute an old fee silently. Missing mapping should block edit until resolved while leaving the historical fee visible.
- Deploy additive nullable fields/child tables first, dual-read old data, migrate deterministically, and validate accounting equivalence before removing old fields.

## Acceptance checklist

- [ ] Add and edit enter the same full-page editor; Back/cancel preserves existing discard protection and return scroll.
- [ ] Search checkbox selection survives filtering; zero results and duplicate names are handled by IDs.
- [ ] Agent-only, team-only, group-only, all role pairs, all three, and multiple agents work.
- [ ] Deselecting removes the charge; 0/blank/negative/nonfinite/over-100 shares and non-100 totals cannot save.
- [ ] Equal and allocation modes preserve non-agent shares; manually changed percentages roundtrip.
- [ ] Repeated-charge mode displays base vs total correctly after product confirms its financial rule.
- [ ] Add/remove/toggle tier rows roundtrip; turning on scale does not activate unspecified accounting logic.
- [ ] External recipient validation and direct wire entry preserve editor draft.
- [ ] Save loading/error/retry/idempotency, stale revisions, removed people, forbidden/finalized cases tested.
- [ ] Backend payouts reconcile, privacy is server-enforced, and audit/workflow updates are atomic.
- [ ] Dark/light, smallest supported screen, keyboard, long names, dynamic type, screen reader, reduced motion reviewed on current rendered builds.
- [ ] Native demo builds and device interaction checks completed before calling native delivery production-ready.

## Code navigation and current proof

Web: `openCommissionFee`, `feeEditorPeople`, `initFeePeople`, `syncFeePeople`, `renderFeePeople`, `positionFeeDropdown`, `renderFeeTiers`, `agentFeeDistribution`, `feeTotalAmount`, and the `dedSave` handler. Demo scenario adapter: `openSheetExampleFee` / `saveSheetExampleFee`.

Local `.task-checks/add-fee/verify.cjs` checks inline-script parsing, seven role combinations, named manual allocations, unselected agents, repeated charges, and legacy allocation. These checks passed; they do not prove layout, production APIs, or native behavior. This document distinguishes approved UI, observed prototype behavior, and proposed production work so implementation does not mistake mock state for a backend contract.
