package com.radius.office

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

// API-owned formatted values. UI never computes authoritative commission totals.
data class OfficeRow(val id:String, val title:String, val subtitle:String?=null, val value:String,
    val editable:Boolean=false, val deletable:Boolean=false, val creatorID:String?=null, val style:String?=null)
data class OfficeSection(val id:String, val title:String, val rows:List<OfficeRow>)
data class OfficeSnapshot(val title:String, val status:String, val payout:String, val viewerID:String,
    val personal:Boolean, val finalized:Boolean=false, val canConfirm:Boolean=false,
    val sections:List<OfficeSection>, val sideGross:List<OfficeRow> = emptyList()) {
    fun canEdit(row:OfficeRow) = !finalized && row.editable && (!personal || row.creatorID == viewerID)
    fun canDelete(row:OfficeRow) = canEdit(row) && row.deletable
}
sealed interface OfficeAction {
    data class Edit(val row:OfficeRow):OfficeAction
    data class Delete(val row:OfficeRow):OfficeAction
    data object AddFee:OfficeAction
    data object Confirm:OfficeAction
    data object Comment:OfficeAction
    data object Back:OfficeAction
}
private val Primary=Color(0xFF5A5FF2)
private val Surface=Color(0xFF1F1F22)
private val Muted=Color(0xFFA3A3A3)
private val PayoutSurface=Color(0xFF262626)
private val Income=Color(0xFFA9CF80)
private val Deduction=Color(0xFFF0A0A0)

/** Host owns API calls, editors, error reporting and replacement snapshots. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadiusOfficeScreen(snapshot:OfficeSnapshot, modifier:Modifier=Modifier, fontFamily:FontFamily=FontFamily.Default, onAction:(OfficeAction)->Unit) {
    var editing by remember { mutableStateOf(false) }
    var grossOpen by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<OfficeRow?>(null) }
    MaterialTheme(colorScheme=darkColorScheme(primary=Primary, background=Color(0xFF161616),surface=Surface)) {
        CompositionLocalProvider(LocalContentColor provides Color.White, LocalTextStyle provides LocalTextStyle.current.copy(fontFamily=fontFamily,fontSize=14.sp)) {
            Column(modifier.fillMaxSize().background(Color(0xFF161616)).safeDrawingPadding()) {
                Row(Modifier.fillMaxWidth().padding(horizontal=16.dp),verticalAlignment=Alignment.CenterVertically) {
                    IconButton(onClick={onAction(OfficeAction.Back)}) { Icon(Icons.Default.ArrowBack,"Back") }
                    Column(Modifier.weight(1f)) { Text(snapshot.title,fontSize=16.sp); Text(snapshot.status,fontSize=12.sp,color=Muted) }
                    TextButton(onClick={editing=!editing},enabled=!snapshot.finalized) { Text(if(editing) "Done" else "Edit") }
                }
                LazyColumn(Modifier.weight(1f).padding(horizontal=16.dp)) {
                    snapshot.sections.forEach { section ->
                        item(key="section-${section.id}") { Text(section.title,Modifier.padding(vertical=16.dp),fontSize=12.sp,color=Muted) }
                        items(section.rows,key={"${section.id}-${it.id}"}) { row ->
                            FeeRow(row,snapshot.canEdit(row),snapshot.canDelete(row),editing,
                                onEdit={onAction(OfficeAction.Edit(row))},onDelete={pendingDelete=row},
                                onGross=if(row.id=="gross" && snapshot.sideGross.size>1) {{grossOpen=true}} else null)
                        }
                    }
                }
                Column(Modifier.background(Surface).padding(16.dp)) {
                    Row { Text("Net commission",Modifier.weight(1f)); Text(snapshot.payout) }
                    Row(verticalAlignment=Alignment.CenterVertically) {
                        IconButton(onClick={onAction(OfficeAction.AddFee)},enabled=!snapshot.finalized) { Icon(Icons.Default.Add,"Add fee") }
                        IconButton(onClick={onAction(OfficeAction.Comment)}) { Icon(Icons.Default.ChatBubbleOutline,"Comment") }
                        Button(onClick={onAction(OfficeAction.Confirm)},enabled=snapshot.canConfirm && !snapshot.finalized,modifier=Modifier.weight(1f)) { Text("Confirm") }
                    }
                }
            }
            if(grossOpen) ModalBottomSheet(onDismissRequest={grossOpen=false}) {
                Column(Modifier.padding(24.dp),verticalArrangement=Arrangement.spacedBy(20.dp)) {
                    Text("Gross commission",fontSize=16.sp)
                    snapshot.sideGross.forEach { Row { Text(it.title,Modifier.weight(1f)); Text(it.value) } }
                    Button(onClick={grossOpen=false}) { Text("Done") }
                }
            }
            pendingDelete?.let { row ->
                AlertDialog(onDismissRequest={pendingDelete=null},title={Text("Delete fee?")},text={Text(row.title)},
                    confirmButton={TextButton(onClick={if(snapshot.canDelete(row))onAction(OfficeAction.Delete(row));pendingDelete=null}) { Text("Delete",color=Color.Red) }},
                    dismissButton={TextButton(onClick={pendingDelete=null}) { Text("Cancel") }})
            }
        }
    }
}
@Composable
private fun FeeRow(row:OfficeRow,editable:Boolean,deletable:Boolean,editing:Boolean,onEdit:()->Unit,onDelete:()->Unit,onGross:(()->Unit)?) {
    val payout=row.style=="payout"
    val negative=row.value.startsWith("−") || row.value.startsWith("-")
    val width=with(LocalDensity.current) {(if(deletable)104.dp else 52.dp).toPx()}
    var offset by remember(row.id,editing,editable) { mutableFloatStateOf(0f) }
    val actionButtons: @Composable ()->Unit = {
        Row(horizontalArrangement=Arrangement.spacedBy(8.dp)) {
            FilledIconButton(onClick=onEdit,shape=RoundedCornerShape(10.dp),colors=IconButtonDefaults.filledIconButtonColors(containerColor=Primary,contentColor=Color.White),modifier=Modifier.size(44.dp)) { Icon(Icons.Default.Edit,"Edit ${row.title}",Modifier.size(16.dp)) }
            if(deletable) FilledIconButton(onClick=onDelete,shape=RoundedCornerShape(10.dp),colors=IconButtonDefaults.filledIconButtonColors(containerColor=Color(0xFFDC2626),contentColor=Color.White),modifier=Modifier.size(44.dp)) { Icon(Icons.Default.Delete,"Delete ${row.title}",Modifier.size(16.dp)) }
        }
    }
    Column {
        Box(Modifier.fillMaxWidth().clip(RoundedCornerShape(0.dp))) {
            if(editable && !editing && offset<0) Box(Modifier.align(Alignment.CenterEnd)) { actionButtons() }
            Row(Modifier.offset { IntOffset(offset.roundToInt(),0) }.fillMaxWidth().clip(RoundedCornerShape(if(payout)14.dp else 0.dp)).background(if(payout)PayoutSurface else Surface)
                .pointerInput(editable,editing,width) {
                    if(editable && !editing) detectHorizontalDragGestures(
                        onDragEnd={offset=if(offset < -width/3) -width else 0f},onDragCancel={offset=0f}
                    ) { change,amount -> change.consume();offset=(offset+amount).coerceIn(-width,0f) }
                }.padding(vertical=12.dp,horizontal=if(payout)12.dp else 0.dp),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(if(payout)8.dp else 12.dp)) {
                Icon(if(payout)Icons.Default.AccountBalanceWallet else if(negative)Icons.Default.ReceiptLong else Icons.Default.Calculate,null,Modifier.size(16.dp),tint=if(payout)Income else if(negative)Deduction else Muted)
                Column(Modifier.weight(1f)) {
                    Text(row.title,fontSize=if(payout)13.sp else 14.sp,fontWeight=if(payout)androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal)
                    row.subtitle?.let { Text(it,fontSize=12.sp,color=Muted) }
                    if(onGross!=null) TextButton(onClick=onGross,contentPadding=PaddingValues(0.dp)) { Text("Listing + Buying",fontSize=12.sp);Icon(Icons.Default.ChevronRight,null,Modifier.size(12.dp)) }
                }
                Text(row.value,color=if(payout || row.style=="income")Income else if(negative)Deduction else Color.White,fontSize=14.sp,fontWeight=if(payout)androidx.compose.ui.text.font.FontWeight.SemiBold else androidx.compose.ui.text.font.FontWeight.Normal)
            }
        }
        if(editing && editable) Box(Modifier.fillMaxWidth().padding(bottom=8.dp),contentAlignment=Alignment.CenterEnd) { actionButtons() }
        if(!payout) HorizontalDivider(color=Color.White.copy(alpha=.08f))
    }
}

/** Independent preview over the existing viewer. Both actions dismiss; no workflow mutation. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReopenFinalizedBreakdownSheet(fontFamily:FontFamily=FontFamily.Default,onDismiss:()->Unit) {
    MaterialTheme(colorScheme=darkColorScheme(primary=Primary,surface=Surface)) {
        ModalBottomSheet(onDismissRequest=onDismiss,containerColor=Surface) {
            CompositionLocalProvider(LocalTextStyle provides LocalTextStyle.current.copy(fontFamily=fontFamily)) {
                Column(Modifier.padding(horizontal=24.dp).padding(bottom=24.dp),verticalArrangement=Arrangement.spacedBy(16.dp)) {
                    Row(verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.Refresh,null,Modifier.size(16.dp),tint=Color(0xFFFDE68A))
                        Text("Reopen finalized breakdown?",Modifier.weight(1f),fontSize=18.sp,fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                        IconButton(onClick=onDismiss) { Icon(Icons.Default.Close,"Close reopen finalized breakdown") }
                    }
                    Text("This commission breakdown has already been finalized. Saving your changes will restart the approval workflow — all parties (you, the team lead, and the auditor) will need to review and approve again.",fontSize=13.sp,color=Muted)
                    Text("This will void the existing CDA.",Modifier.fillMaxWidth().background(Color(0xFF3A2D0C),RoundedCornerShape(14.dp)).padding(horizontal=16.dp,vertical=12.dp),fontSize=13.sp,color=Color(0xFFFDE68A),fontWeight=androidx.compose.ui.text.font.FontWeight.SemiBold)
                    Button(onClick=onDismiss,modifier=Modifier.fillMaxWidth()) { Text("Edit & restart approval") }
                    OutlinedButton(onClick=onDismiss,modifier=Modifier.fillMaxWidth()) { Text("Cancel") }
                }
            }
        }
    }
}
