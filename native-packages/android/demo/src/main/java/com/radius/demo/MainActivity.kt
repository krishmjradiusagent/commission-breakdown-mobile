package com.radius.demo
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.radius.office.*
class MainActivity: ComponentActivity() {
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  setContent {
   RadiusOfficeScreen(OfficeSnapshot("118 Maple St","Agent pending","$4,980","ally",true,canConfirm=true,sections=listOf(
    OfficeSection("basis","Commission basis",listOf(OfficeRow("gross","Gross commission",value="$12,500"))),
    OfficeSection("ally","Agent Ally",listOf(
     OfficeRow("tc","TC Fee","100% of $700","−$700",true,true,"ally"),
     OfficeRow("warranty","Home Warranty Fee","100% of $400","−$400"),
     OfficeRow("office","Office Fee","To Group · 100%","−$400"),
     OfficeRow("net","Net Payout","Agent Ally","$4,980",style="payout")
    ))
   ))) { action -> Toast.makeText(this,"Host action: $action",Toast.LENGTH_SHORT).show() }
  }
 }
}
