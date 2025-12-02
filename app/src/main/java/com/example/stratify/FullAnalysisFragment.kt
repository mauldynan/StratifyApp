package com.example.stratify

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter

class FullAnalysisFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                MaterialTheme {
                    FullAnalysisScreen(onBackClicked = { findNavController().popBackStack() })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FullAnalysisScreen(onBackClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Full Analysis") },
                navigationIcon = {
                    IconButton(onClick = onBackClicked) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LineChartComposable()
        }
    }
}

@Composable
fun LineChartComposable() {
    val dummyData = generateDummyData()

    AndroidView(
        factory = { context ->
            LineChart(context)
        },
        update = { lineChart ->
            setupLineChart(lineChart)
            loadChartData(lineChart, dummyData.first, dummyData.second)
        },
        modifier = Modifier.fillMaxSize()
    )
}

private fun setupLineChart(lineChart: LineChart) {
    lineChart.apply {
        description.isEnabled = false
        setDrawGridBackground(false)
        setTouchEnabled(true)
        isDragEnabled = true
        setScaleEnabled(true)
        setPinchZoom(true)
        animateX(1000)
        axisRight.isEnabled = false
        extraBottomOffset = 20f
    }

    lineChart.xAxis.apply {
        position = XAxis.XAxisPosition.BOTTOM
        granularity = 1f
        textColor = Color.DKGRAY
        setDrawGridLines(false)
        valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return "Week ${value.toInt()}"
            }
        }
    }

    lineChart.axisLeft.apply {
        textColor = Color.DKGRAY
        setDrawGridLines(true)
    }

    lineChart.legend.apply {
        form = Legend.LegendForm.LINE
        textSize = 12f
        textColor = Color.BLACK
        horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        orientation = Legend.LegendOrientation.HORIZONTAL
        setDrawInside(false)
    }
}

private fun loadChartData(
    lineChart: LineChart,
    positiveEntries: List<Entry>,
    negativeEntries: List<Entry>
) {
    if (positiveEntries.isEmpty() && negativeEntries.isEmpty()) {
        lineChart.clear()
        lineChart.setNoDataText("No sentiment data available.")
        lineChart.invalidate()
        return
    }

    val positiveSet = LineDataSet(positiveEntries, "Positive").apply {
        color = Color.parseColor("#4CAF50")
        lineWidth = 2f
        circleRadius = 4f
        setCircleColor(Color.parseColor("#4CAF50"))
        valueTextColor = Color.BLACK
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }

    val negativeSet = LineDataSet(negativeEntries, "Negative").apply {
        color = Color.parseColor("#F44336")
        lineWidth = 2f
        circleRadius = 4f
        setCircleColor(Color.parseColor("#F44336"))
        valueTextColor = Color.BLACK
        mode = LineDataSet.Mode.CUBIC_BEZIER
    }

    val lineData = LineData(positiveSet, negativeSet)
    lineChart.data = lineData
    lineChart.invalidate()
}

private fun generateDummyData(): Pair<List<Entry>, List<Entry>> {
    val positiveEntries = listOf(
        Entry(1f, 1f),
        Entry(2f, 2f),
        Entry(3f, 1.5f),
        Entry(4f, 2.5f),
        Entry(5f, 3f)
    )

    val negativeEntries = listOf(
        Entry(1f, 0.5f),
        Entry(2f, 1f),
        Entry(3f, 0.8f),
        Entry(4f, 1.5f),
        Entry(5f, 2f)
    )
    return Pair(positiveEntries, negativeEntries)
}

