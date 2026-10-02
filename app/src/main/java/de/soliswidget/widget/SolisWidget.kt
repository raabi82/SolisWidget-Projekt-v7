package de.soliswidget.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.updateAll
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import de.soliswidget.data.Cache
import de.soliswidget.data.SolisRepository
import de.soliswidget.model.BatteryState
import de.soliswidget.model.GridState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SolisWidget : GlanceAppWidget() {
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            WidgetContent(context)
        }
    }

    @Composable
    private fun WidgetContent(context: Context) {
        val data = Cache(context).load()
        val configured = data != null

        Column(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(androidx.glance.unit.ColorProvider(android.graphics.Color.WHITE))
                .padding(12.dp),
            verticalAlignment = Alignment.Vertical.CenterVertically
        ) {
            Text(
                text = if (data != null) "Solis · ${data.stationName}" else "Solis PV",
                style = TextStyle(fontSize = 16.sp)
            )

            Spacer(GlanceModifier.height(6.dp))

            if (!configured || data == null) {
                Text(
                    text = if (!configured) "App öffnen und Anlage auswählen" else "Noch keine Daten",
                    style = TextStyle(fontSize = 14.sp)
                )
            } else {
                Text(
                    text = "PV ${fmt(data.powerKw)} kW  Solar",
                    style = TextStyle(fontSize = 18.sp)
                )
                Text(
                    text = "Haus ${fmt(data.homeLoadKw)} kW  Hauslast",
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(
                    text = "Batterie ${fmt(data.batteryPercent, 0)} % · ${SolisRepository.batteryLabel(data.batteryPowerKw)}",
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(
                    text = "Netz ${fmt(abs(data.gridPowerKw))} kW · ${SolisRepository.gridLabel(data.gridPowerKw)}",
                    style = TextStyle(fontSize = 14.sp)
                )
                Text(
                    text = "${SolisRepository.supplyLabel(data)}",
                    style = TextStyle(fontSize = 14.sp)
                )
                Spacer(GlanceModifier.height(4.dp))
                Text(
                    text = "Heute ${fmt(data.dayEnergyKwh)} kWh · ${time(data.timestampMillis)}",
                    style = TextStyle(fontSize = 12.sp)
                )
            }
        }
    }

    private fun fmt(value: Double, decimals: Int = 2): String =
        String.format(Locale.GERMANY, "%.${decimals}f", value)

    private fun time(ms: Long): String =
        SimpleDateFormat("HH:mm", Locale.GERMANY).format(Date(ms))

    private fun abs(v: Double) = kotlin.math.abs(v)
}

class SolisWidgetReceiver : androidx.glance.appwidget.GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SolisWidget()

    companion object {
        suspend fun updateAll(context: Context) {
            SolisWidget().updateAll(context)
        }
    }
}
