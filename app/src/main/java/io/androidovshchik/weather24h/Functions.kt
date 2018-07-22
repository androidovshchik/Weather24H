@file:Suppress("unused")

package io.androidovshchik.weather24h

import android.graphics.Point
import android.util.DisplayMetrics
import com.github.androidovshchik.utils.ViewUtil

const val TEST_HEIGHT = 1536

fun Point.bigIconSize(metrics: DisplayMetrics): Int {
    return ViewUtil.dp2px(Math.min(x, y) * 220f / TEST_HEIGHT * 2 / metrics.density)
}

fun Point.dataTextSize(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 56f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.tempTextSize(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 120f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.stripHeight(metrics: DisplayMetrics): Int {
    return ViewUtil.dp2px(Math.min(x, y) * 40f / TEST_HEIGHT * 2 / metrics.density)
}

fun Point.hourTextSize(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 24f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.valueTextSize(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 32f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.lineWidth(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 6f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.circleHoleRadius(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 7f / TEST_HEIGHT * 2 / metrics.density
}

fun Point.circleRadius(metrics: DisplayMetrics): Float {
    return Math.min(x, y) * 12f / TEST_HEIGHT * 2 / metrics.density
}

fun formatBearing(_bearing: Double): String {
    var bearing = _bearing
    if (bearing < 0 && bearing > -180) {
        // Normalize to [0,360]
        bearing += 360.0
    }
    if (bearing > 360 || bearing < -180) {
        return "-"
    }
    return DIRECTIONS[Math.floor((bearing + 11.25) % 360 / 22.5).toInt()]
}