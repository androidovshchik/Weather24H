package io.androidovshchik.weather24h

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Point
import android.graphics.Typeface
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.github.androidovshchik.utils.WindowUtil
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import io.androidovshchik.weather24h.model.Data
import io.androidovshchik.weather24h.parser.MyList
import io.androidovshchik.weather24h.parser.SiteResponse
import kotlinx.android.synthetic.main.overlay.view.*
import java.util.*

class OverlayLayout : RelativeLayout {

    private var robotoBold: Typeface

    private var window: Point

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        robotoBold = Typeface.createFromAsset(context.assets, "Roboto-Bold.ttf")
        window = WindowUtil.getWindowSize(context)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        clock.textSize = window.tempTextSize(resources.displayMetrics)
        big_icon.layoutParams.height = window.bigIconSize(resources.displayMetrics)
        data1.textSize = window.dataTextSize(resources.displayMetrics)
        data2.textSize = window.dataTextSize(resources.displayMetrics)
        data3.textSize = window.dataTextSize(resources.displayMetrics)
        temperature.textSize = window.tempTextSize(resources.displayMetrics)
        strip.layoutParams.height = window.stripHeight(resources.displayMetrics)
        chart.description = null
        chart.legend.isEnabled = false
        chart.xAxis.setDrawLabels(false)
        chart.axisLeft.setDrawLabels(false)
        chart.axisRight.setDrawLabels(false)
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = 7.5f
        chart.setPinchZoom(false)
        chart.isDragEnabled = false
        chart.setScaleEnabled(false)
        chart.isScaleXEnabled = false
        chart.isScaleYEnabled = false
        chart.isDoubleTapToZoomEnabled = false
        chart.isDragDecelerationEnabled = false
        chart.setViewPortOffsets(0f, 0f, 0f, 0f)
    }

    fun bindOverlay(siteResponse: SiteResponse) {
        val now = System.currentTimeMillis()
        val entries = ArrayList<Entry>()
        var minY = Integer.MAX_VALUE
        var maxY = Integer.MIN_VALUE
        val maxColumns = 8
        strip.removeAllViews()
        for (i in siteResponse.list.indices) {
            val list = siteResponse.list[i]
            if (now > list.dtTxt.time) {
                if (now - list.dtTxt.time < 3 * HOUR) {
                    bindTopPart(list)
                } else {
                    continue
                }
            }
            if (entries.size < maxColumns) {
                val temp = Math.round(list.main.temp - 273.15).toInt()
                if (temp > maxY) {
                    maxY = temp
                }
                if (temp < minY) {
                    minY = temp
                }
                entries.add(Entry(entries.size.toFloat(), temp.toFloat()))
                addStripView(list.dtTxt.time)
            }
        }
        if (entries.size > 0) {
            bindBottomPart(entries, minY, maxY)
        }
    }

    @SuppressLint("SetTextI18n")
    private fun bindTopPart(myList: MyList) {
        val icon = Data.getIcon(context, myList.weather[0].icon)
        big_icon.setImageResource(if (icon == 0) R.drawable.ic_02d_big else icon)
        data1.text = "${myList.main.humidity}%"
        data2.text = Math.round(myList.main.pressure).toString() + " мм"
        data3.text = String.format("%.1f", myList.wind.speed) + " м/с " + formatBearing(myList.wind.deg.toDouble())
        val temp = Math.round(myList.main.temp - 273.15)
        temperature.text = (if (temp > 0) "+" else "") + temp.toString() + " \u2103"
    }

    private fun addStripView(time: Long) {
        val textView = TextView(context.applicationContext)
        textView.layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1f)
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = time
        textView.setTextColor(Color.WHITE)
        textView.textSize = window.hourTextSize(resources.displayMetrics)
        textView.gravity = Gravity.CENTER
        textView.setTypeface(textView.typeface, Typeface.BOLD)
        textView.text = calendar.get(Calendar.HOUR_OF_DAY).toString()
        strip.addView(textView, strip.childCount)
    }

    private fun bindBottomPart(entries: ArrayList<Entry>, minY: Int, maxY: Int) {
        val dataSet = LineDataSet(entries, null)
        dataSet.valueTextColor = Color.WHITE
        dataSet.valueTextSize = window.valueTextSize(resources.displayMetrics)
        dataSet.valueTypeface = robotoBold
        dataSet.lineWidth = window.lineWidth(resources.displayMetrics)
        dataSet.setCircleColorHole(Color.BLACK)
        dataSet.circleHoleRadius = window.circleHoleRadius(resources.displayMetrics)
        dataSet.circleRadius = window.circleRadius(resources.displayMetrics)
        dataSet.setValueFormatter { value, _, _, _ -> (if (value > 0) "+" else "") + Math.round(value) }
        chart.data = LineData(dataSet)
        chart.axisLeft.axisMinimum = minY - 0.5f
        chart.axisLeft.axisMaximum = maxY + 1.5f
        chart.axisRight.axisMinimum = minY - 0.5f
        chart.axisRight.axisMaximum = maxY + 1.5f
        chart.invalidate()
    }
}