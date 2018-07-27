package io.androidovshchik.weather24h

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.androidovshchik.data.Preferences
import com.github.androidovshchik.utils.WindowUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.androidovshchik.weather24h.current.Current
import io.androidovshchik.weather24h.model.Data
import io.androidovshchik.weather24h.forecast.Forecast
import kotlinx.android.synthetic.main.overlay.view.*
import timber.log.Timber
import java.util.*

class OverlayLayout : RelativeLayout {

    private var robotoBold: Typeface

    private var window: Point

    private var preferences: Preferences

    val items = ArrayList<Data>()

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        Timber.d("> constructor")
        robotoBold = Typeface.createFromAsset(context.assets, "Roboto-Bold.ttf")
        window = WindowUtil.getWindowSize(context)
        preferences = Preferences(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        Timber.d("> onFinishInflate")
        setBackgroundColor(Color.parseColor(preferences.getString(PREFERENCE_TOP_BACK_COLOR)))
        val clockTempColor = Color.parseColor(preferences.getString(PREFERENCE_CLOCK_TEMP_COLOR))
        clock.textSize = window.tempTextSize(resources.displayMetrics)
        clock.setTextColor(clockTempColor)
        val iconDataColor = Color.parseColor(preferences.getString(PREFERENCE_ICON_DATA_COLOR))
        big_icon.layoutParams.height = window.bigIconSize(resources.displayMetrics)
        data1.textSize = window.dataTextSize(resources.displayMetrics)
        data1.setTextColor(iconDataColor)
        data2.textSize = window.dataTextSize(resources.displayMetrics)
        data2.setTextColor(iconDataColor)
        data3.textSize = window.dataTextSize(resources.displayMetrics)
        data3.setTextColor(iconDataColor)
        temperature.textSize = window.tempTextSize(resources.displayMetrics)
        temperature.setTextColor(clockTempColor)
        strip.layoutParams.height = window.stripHeight(resources.displayMetrics)
        strip.setBackgroundColor(clockTempColor)
        grid.toggleGrid(false)
        grid.numberOfColumns = 8
        grid.setLineColor(Color.parseColor(preferences.getString(PREFERENCE_GRID_COLOR)))
        grid.setBackgroundColor(Color.parseColor(preferences.getString(PREFERENCE_GRAPH_BACK_COLOR)))
        chart.description = null
        chart.legend.isEnabled = false
        chart.xAxis.setDrawLabels(false)
        chart.xAxis.setDrawGridLines(false)
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = 7.5f
        chart.axisLeft.setDrawLabels(false)
        chart.axisLeft.setDrawGridLines(false)
        chart.axisRight.setDrawLabels(false)
        chart.axisRight.setDrawGridLines(false)
        chart.isDragEnabled = false
        chart.isScaleXEnabled = false
        chart.isScaleYEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.isDragDecelerationEnabled = false
        chart.setPinchZoom(false)
        chart.setScaleEnabled(false)
        chart.setViewPortOffsets(0f, 0f, 0f, 0f)
    }

    @SuppressLint("SetTextI18n")
    fun bindTopPart(current: Current) {
        Timber.d("> bindTopPart")
        for (i in 0 until items.size) {
            if (items[i].apiId == current.weather[0].id || i == items.size - 1) {
                val data = if (i == items.size - 1) {
                    items[49] // very hardcoded :)
                } else {
                    items[i]
                }
                val icon = data.getIcon(context.applicationContext)
                big_icon.setImageResource(if (icon == 0) R.drawable.ic_02d_big else icon)
                big_icon.drawable.setColorFilter(Color.parseColor(preferences.getString(PREFERENCE_ICON_DATA_COLOR)),
                    PorterDuff.Mode.MULTIPLY)
                val background = data.getBackground(context.applicationContext)
                backgroundImage.setImageResource(if (background == 0) R.drawable.ic_02d1 else background)
                break
            }
        }
        data1.text = "${Math.round(current.main.humidity)}%"
        data2.text = Math.round(current.main.pressure / 1.333224f).toString() + " мм"
        data3.text = String.format("%.1f", current.wind.speed) + " м/с " + formatBearing(current.wind.deg.toDouble())
        val temp = Math.round(current.main.temp - 273.15)
        temperature.text = (if (temp > 0) "+" else "") + temp + " \u2103"
    }

    fun bindStripBottomPart(forecast: Forecast) {
        Timber.d("> bindStripBottomPart")
        val now = System.currentTimeMillis()
        val entries = ArrayList<Entry>()
        var minY = Integer.MAX_VALUE
        var maxY = Integer.MIN_VALUE
        val maxColumns = 8
        strip.removeAllViews()
        for (i in forecast.list.indices) {
            val myList = forecast.list[i]
            if (now - myList.dtTxt.time > 3 * HOUR) {
                continue
            }
            if (entries.size < maxColumns) {
                val temp = Math.round(myList.main.temp - 273.15).toInt()
                if (temp > maxY) {
                    maxY = temp
                }
                if (temp < minY) {
                    minY = temp
                }
                entries.add(Entry(entries.size.toFloat(), temp.toFloat()))
                addStripView(myList.dtTxt.time)
            }
        }
        if (entries.size > 0) {
            bindBottomPart(entries, minY, maxY)
        }
    }

    private fun addStripView(time: Long) {
        val textView = TextView(context.applicationContext)
        textView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        textView.setTextColor(Color.parseColor(preferences.getString(PREFERENCE_ICON_DATA_COLOR)))
        textView.textSize = window.hourTextSize(resources.displayMetrics)
        textView.gravity = Gravity.CENTER
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        textView.text = calendar.get(Calendar.HOUR_OF_DAY).toString()
        strip.addView(textView, strip.childCount)
    }

    private fun bindBottomPart(entries: ArrayList<Entry>, minY: Int, maxY: Int) {
        Timber.d("> bindBottomPart")
        grid.numberOfRows = maxY - minY + 3
        grid.toggleGrid(true)
        val dataSet = LineDataSet(entries, null)
        val lineColor = Color.parseColor(preferences.getString(PREFERENCE_LINE_COLOR))
        dataSet.color = lineColor
        dataSet.valueTextColor = Color.parseColor(preferences.getString(PREFERENCE_ICON_DATA_COLOR))
        dataSet.valueTextSize = window.valueTextSize(resources.displayMetrics)
        dataSet.valueTypeface = robotoBold
        dataSet.lineWidth = window.lineWidth(resources.displayMetrics)
        dataSet.circleHoleRadius = window.circleHoleRadius(resources.displayMetrics)
        dataSet.circleRadius = window.circleRadius(resources.displayMetrics)
        dataSet.setCircleColor(lineColor)
        dataSet.setCircleColorHole(Color.parseColor(preferences.getString(PREFERENCE_TOP_BACK_COLOR)))
        dataSet.setValueFormatter { value, _, _, _ -> (if (value > 0) "+" else "") + Math.round(value) }
        chart.data = LineData(dataSet)
        chart.axisLeft.axisMinimum = minY - 0.5f
        chart.axisLeft.axisMaximum = maxY + 2f
        chart.axisRight.axisMinimum = minY - 0.5f
        chart.axisRight.axisMaximum = maxY + 2f
        chart.invalidate()
    }
}