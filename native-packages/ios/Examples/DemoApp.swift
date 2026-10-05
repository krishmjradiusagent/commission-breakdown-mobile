import SwiftUI
import RadiusOffice

// Add to a new iOS App target after adding the local RadiusOffice package.
@main struct RadiusOfficeDemoApp: App {
    @State private var message: String?
    var body: some Scene {
        WindowGroup {
            RadiusOfficeView(snapshot: sample) { action in
                message = String(describing: action)
            }.alert("Host action", isPresented: Binding(get: { message != nil }, set: { if !$0 { message=nil } })) {
                Button("OK") { message=nil }
            } message: { Text(message ?? "") }
        }
    }
    private var sample: OfficeSnapshot {
        OfficeSnapshot(title:"118 Maple St",status:"Agent pending",payout:"$4,980",viewerID:"ally",personal:true,canConfirm:true,sections:[
            OfficeSection(id:"basis",title:"Commission basis",rows:[OfficeRow(id:"gross",title:"Gross commission",value:"$12,500")]),
            OfficeSection(id:"ally",title:"Agent Ally",rows:[
                OfficeRow(id:"tc",title:"TC Fee",subtitle:"100% of $700",value:"−$700",editable:true,deletable:true,creatorID:"ally"),
                OfficeRow(id:"warranty",title:"Home Warranty Fee",subtitle:"100% of $400",value:"−$400"),
                OfficeRow(id:"office",title:"Office Fee",subtitle:"To Group · 100%",value:"−$400"),
                OfficeRow(id:"net",title:"Net Payout",subtitle:"Agent Ally",value:"$4,980",style:"payout")])])
    }
}
