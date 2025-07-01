package com.heard.mobile.ui.screens.personal.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.yml.charts.axis.AxisData
import co.yml.charts.common.model.Point
import co.yml.charts.ui.linechart.LineChart
import co.yml.charts.ui.linechart.model.*

@Composable
fun MyLineChart(modifier: Modifier = Modifier) {
    val pointsData: List<Point> = listOf(
        Point(0f, 40f),
        Point(1f, 90f),
        Point(2f, 15f),
        Point(3f, 60f),
        Point(4f, 85f),
        Point(5f, 30f),
        Point(6f, 70f)
    )

    val xAxisData = AxisData.Builder()
        .axisStepSize(40.dp)
        .backgroundColor(Color.Transparent)
        .steps(pointsData.size - 1)
        .labelData { i ->
            val days = listOf("Lun", "Mar", "Mer", "Gio", "Ven", "Sab", "Dom")
            days.getOrElse(i) { i.toString() }
        }
        .labelAndAxisLinePadding(15.dp)
        .axisLineColor(MaterialTheme.colorScheme.outline)
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .build()

    val yAxisData = AxisData.Builder()
        .steps(5)
        .backgroundColor(Color.Transparent)
        .labelAndAxisLinePadding(20.dp)
        .labelData { i -> "${i * 20} km" }
        .axisLineColor(MaterialTheme.colorScheme.outline)
        .axisLabelColor(MaterialTheme.colorScheme.onSurface)
        .build()

    val lineChartData = LineChartData(
        linePlotData = LinePlotData(
            lines = listOf(
                Line(
                    dataPoints = pointsData,
                    LineStyle(
                        color = MaterialTheme.colorScheme.primary,
                    ),
                    IntersectionPoint(
                        color = MaterialTheme.colorScheme.primary,
                        radius = 4.dp
                    ),
                    SelectionHighlightPoint(
                        color = MaterialTheme.colorScheme.primary
                    ),
                    ShadowUnderLine(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                Color.Transparent
                            )
                        )
                    ),
                    SelectionHighlightPopUp()
                )
            ),
        ),
        xAxisData = xAxisData,
        yAxisData = yAxisData,
        gridLines = GridLines(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        ),
        backgroundColor = Color.Transparent
    )

    LineChart(
        modifier = modifier,
        lineChartData = lineChartData
    )
}