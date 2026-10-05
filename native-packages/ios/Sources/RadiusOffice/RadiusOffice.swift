import SwiftUI

/// API-owned values. Amounts are formatted by the host; no commission math is invented here.
public struct OfficeRow: Identifiable, Codable, Equatable {
    public var id: String
    public var title: String
    public var subtitle: String?
    public var value: String
    public var editable: Bool
    public var deletable: Bool
    public var creatorID: String?
    /// nil/default: ordinary row; income: green value; payout: final payout card.
    public var style: String?
    public init(id: String, title: String, subtitle: String? = nil, value: String, editable: Bool = false, deletable: Bool = false, creatorID: String? = nil, style: String? = nil) {
        self.id=id; self.title=title; self.subtitle=subtitle; self.value=value
        self.editable=editable; self.deletable=deletable; self.creatorID=creatorID; self.style=style
    }
}
public struct OfficeSection: Identifiable, Codable, Equatable {
    public var id: String
    public var title: String
    public var rows: [OfficeRow]
    public init(id: String, title: String, rows: [OfficeRow]) { self.id=id; self.title=title; self.rows=rows }
}
public struct OfficeSnapshot: Codable, Equatable {
    public var title: String
    public var status: String
    public var payout: String
    public var viewerID: String
    public var personal: Bool
    public var finalized: Bool
    public var canConfirm: Bool
    public var sections: [OfficeSection]
    public var sideGross: [OfficeRow]
    public init(title: String, status: String, payout: String, viewerID: String, personal: Bool, finalized: Bool = false, canConfirm: Bool = false, sections: [OfficeSection], sideGross: [OfficeRow] = []) {
        self.title=title; self.status=status; self.payout=payout; self.viewerID=viewerID; self.personal=personal
        self.finalized=finalized; self.canConfirm=canConfirm; self.sections=sections; self.sideGross=sideGross
    }
    public func canEdit(_ row: OfficeRow) -> Bool { !finalized && row.editable && (!personal || row.creatorID == viewerID) }
    public func canDelete(_ row: OfficeRow) -> Bool { canEdit(row) && row.deletable }
}
public enum OfficeAction {
    case edit(OfficeRow), delete(OfficeRow), addFee, confirm, comment, back
}
/// The host handles navigation, authorization, persistence, API errors and refreshed snapshots.
/// No mutation is reported successful until the host supplies updated data.
public struct RadiusOfficeView: View {
    public let snapshot: OfficeSnapshot
    public let onAction: (OfficeAction) -> Void
    public let fontName: String?
    @State private var editing=false
    @State private var grossOpen=false
    @State private var pendingDelete: OfficeRow?
    private let primary=Color(red:90/255,green:95/255,blue:242/255)
    private let surface=Color(red:31/255,green:31/255,blue:34/255)
    public init(snapshot: OfficeSnapshot, fontName: String? = nil, onAction: @escaping (OfficeAction) -> Void) {
        self.snapshot=snapshot; self.fontName=fontName; self.onAction=onAction
    }
    private func font(_ size: CGFloat) -> Font { fontName.map { .custom($0, size:size) } ?? .system(size:size) }
    public var body: some View {
        VStack(spacing:0) {
            HStack {
                Button { onAction(.back) } label: { Image(systemName:"chevron.left").frame(width:44,height:44) }.accessibilityLabel("Back")
                VStack(alignment:.leading,spacing:4) {
                    Text(snapshot.title).font(font(16)).fontWeight(.semibold)
                    Text(snapshot.status).font(font(12)).foregroundStyle(.secondary)
                }
                Spacer()
                Button(editing ? "Done" : "Edit") { editing.toggle() }.disabled(snapshot.finalized)
            }.padding(.horizontal,16)
            List {
                ForEach(snapshot.sections) { section in
                    Section {
                        ForEach(section.rows) { row in
                            VStack(alignment:.leading,spacing:8) {
                                commissionRow(row)
                                if editing && snapshot.canEdit(row) {
                                    HStack { Spacer(); actions(row) }
                                }
                            }
                            .padding(.vertical,8)
                            .listRowBackground(surface)
                            .swipeActions(edge:.trailing,allowsFullSwipe:false) {
                                if !editing && snapshot.canEdit(row) {
                                    if snapshot.canDelete(row) { Button(role:.destructive) { pendingDelete=row } label: { Label("Delete",systemImage:"trash") } }
                                    Button { onAction(.edit(row)) } label: { Label("Edit",systemImage:"pencil") }.tint(primary)
                                }
                            }
                        }
                    } header: { Text(section.title).font(font(12)).textCase(nil) }
                }
            }.listStyle(.plain).scrollContentBackground(.hidden)
            VStack(spacing:8) {
                HStack { Text("Net commission"); Spacer(); Text(snapshot.payout).monospacedDigit().fontWeight(.semibold) }
                HStack(spacing:12) {
                    Button { onAction(.addFee) } label: { Image(systemName:"plus").frame(width:44,height:44) }.accessibilityLabel("Add fee").disabled(snapshot.finalized)
                    Button { onAction(.comment) } label: { Image(systemName:"bubble").frame(width:44,height:44) }.accessibilityLabel("Comment")
                    Button("Confirm") { onAction(.confirm) }.buttonStyle(.borderedProminent).frame(maxWidth:.infinity).disabled(!snapshot.canConfirm || snapshot.finalized)
                }
            }.font(font(14)).padding(16).background(surface)
        }
        .foregroundStyle(.white).background(Color(red:22/255,green:22/255,blue:22/255)).tint(primary).preferredColorScheme(.dark)
        .sheet(isPresented:$grossOpen) {
            VStack(alignment:.leading,spacing:20) {
                Text("Gross commission").font(font(16)).fontWeight(.semibold)
                ForEach(snapshot.sideGross) { side in HStack { Text(side.title); Spacer(); Text(side.value).monospacedDigit() } }
                Button("Done") { grossOpen=false }.buttonStyle(.borderedProminent)
            }.font(font(14)).padding(24).presentationDetents([.medium]).presentationDragIndicator(.visible)
        }
        .confirmationDialog("Delete fee?",isPresented:Binding(get:{pendingDelete != nil},set:{if !$0 {pendingDelete=nil}}),titleVisibility:.visible) {
            if let row=pendingDelete { Button("Delete",role:.destructive) { if snapshot.canDelete(row) { onAction(.delete(row)) }; pendingDelete=nil } }
            Button("Cancel",role:.cancel) { pendingDelete=nil }
        }
    }
    // Radius neutral-800, dark foreground, and current prototype tint tokens.
    private let payoutSurface=Color(red:38/255,green:38/255,blue:38/255)
    private let income=Color(red:169/255,green:207/255,blue:128/255)
    private let deduction=Color(red:240/255,green:160/255,blue:160/255)
    private let muted=Color(red:163/255,green:163/255,blue:163/255)
    @ViewBuilder private func commissionRow(_ row:OfficeRow) -> some View {
        let payout=row.style == "payout"
        let negative=row.value.hasPrefix("−") || row.value.hasPrefix("-")
        HStack(spacing:payout ? 8 : 12) {
            Image(systemName:payout ? "wallet.pass" : negative ? "receipt" : "calculator")
                .font(.system(size:16)).foregroundStyle(payout ? income : negative ? deduction : muted)
                .frame(width:16,height:16)
            VStack(alignment:.leading,spacing:2) {
                Text(row.title).font(font(payout ? 13 : 14)).fontWeight(payout ? .semibold : .regular)
                if let sub=row.subtitle { Text(sub).font(font(12)).foregroundStyle(muted) }
                if row.id == "gross", snapshot.sideGross.count > 1 {
                    Button { grossOpen=true } label: { HStack(spacing:4) { Text("Listing + Buying"); Image(systemName:"chevron.right") }.font(font(12)) }
                }
            }
            Spacer(minLength:8)
            Text(row.value).font(font(14)).monospacedDigit().fixedSize()
                .fontWeight(payout ? .semibold : .regular)
                .foregroundStyle(payout ? income : negative ? deduction : row.style == "income" ? income : .white)
        }
        .padding(payout ? 12 : 0)
        .background(payout ? payoutSurface : .clear,in:RoundedRectangle(cornerRadius:14))
        .overlay { if payout { RoundedRectangle(cornerRadius:14).stroke(payoutSurface,lineWidth:1) } }
    }
    @ViewBuilder private func actions(_ row:OfficeRow) -> some View {
        Button { onAction(.edit(row)) } label: { Image(systemName:"pencil").frame(width:44,height:44).background(primary,in:RoundedRectangle(cornerRadius:10)) }.buttonStyle(.plain).accessibilityLabel("Edit " + row.title)
        if snapshot.canDelete(row) {
            Button { pendingDelete=row } label: { Image(systemName:"trash").frame(width:44,height:44).background(Color.red,in:RoundedRectangle(cornerRadius:10)) }.buttonStyle(.plain).accessibilityLabel("Delete " + row.title)
        }
    }
}

/// Independent bottom-sheet preview. The host presents it over the existing viewer.
/// Both actions dismiss the preview; this component never changes approval or CDA data.
public struct ReopenFinalizedBreakdownSheet: View {
    public let onDismiss: () -> Void
    public let fontName: String?
    public init(fontName: String? = nil, onDismiss: @escaping () -> Void) {
        self.fontName=fontName; self.onDismiss=onDismiss
    }
    private func font(_ size: CGFloat) -> Font { fontName.map { .custom($0,size:size) } ?? .system(size:size) }
    public var body: some View {
        VStack(alignment:.leading,spacing:16) {
            HStack(spacing:12) {
                Image(systemName:"arrow.counterclockwise").foregroundStyle(Color(red:253/255,green:230/255,blue:138/255))
                Text("Reopen finalized breakdown?").font(font(18)).fontWeight(.semibold)
                Spacer()
                Button(action:onDismiss) { Image(systemName:"xmark") }.accessibilityLabel("Close reopen finalized breakdown")
            }
            Text("This commission breakdown has already been finalized. Saving your changes will restart the approval workflow — all parties (you, the team lead, and the auditor) will need to review and approve again.")
                .font(font(13)).foregroundStyle(Color(red:163/255,green:163/255,blue:163/255))
            Text("This will void the existing CDA.").font(font(13)).fontWeight(.semibold)
                .foregroundStyle(Color(red:253/255,green:230/255,blue:138/255))
                .frame(maxWidth:.infinity,alignment:.leading).padding(.horizontal,16).padding(.vertical,12)
                .background(Color(red:58/255,green:45/255,blue:12/255),in:RoundedRectangle(cornerRadius:14))
            Button(action:onDismiss) { Text("Edit & restart approval").frame(maxWidth:.infinity) }.buttonStyle(.borderedProminent)
            Button(action:onDismiss) { Text("Cancel").frame(maxWidth:.infinity) }.buttonStyle(.bordered)
        }
        .font(font(14)).padding(24).foregroundStyle(.white)
        .tint(Color(red:90/255,green:95/255,blue:242/255))
        .presentationDetents([.medium,.large]).presentationDragIndicator(.visible)
        .preferredColorScheme(.dark)
    }
}
