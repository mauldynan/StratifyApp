package com.example.stratify

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
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

    private lateinit var lineChart: LineChart

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_full_analysis, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🔹 Dapatkan referensi ke layout yang di-include
        val headerView = view.findViewById<View>(R.id.header)

        // 🔹 Cari tombol "btnBack" dari layout yang di-include
        val btnBack = headerView.findViewById<ImageView>(R.id.btnBack)
        btnBack.setOnClickListener {
            // Gunakan Navigation Component untuk kembali ke fragment sebelumnya
            findNavController().popBackStack()
        }

        // 🔹 Inisialisasi LineChart
        lineChart = view.findViewById(R.id.lineChartSentiment)
        setupLineChart()
        loadChartData()
    }

    private fun setupLineChart() {
        lineChart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setTouchEnabled(true)
            isDragEnabled = true
            setScaleEnabled(true)
            setPinchZoom(true)
            animateX(1000)
            axisRight.isEnabled = false

            // 🔹 Menambahkan jarak di bagian bawah chart
            extraBottomOffset = 20f
        }

        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            textColor = Color.DKGRAY
            setDrawGridLines(false)
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return "Minggu ${value.toInt()}"
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
            horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER // Memusatkan legenda
            verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM // Menempatkan legenda di bawah chart
            orientation = Legend.LegendOrientation.HORIZONTAL
            setDrawInside(false) // Memastikan legenda berada di luar chart
        }
    }

    private fun loadChartData() {
        // 🔹 Contoh data dummy
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

        if (positiveEntries.isEmpty() && negativeEntries.isEmpty()) {
            lineChart.clear()
            lineChart.setNoDataText("Belum ada data sentimen.")
            lineChart.invalidate()
            return
        }

        val positiveSet = LineDataSet(positiveEntries, "Positif").apply {
            color = Color.parseColor("#4CAF50")
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.parseColor("#4CAF50"))
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val negativeSet = LineDataSet(negativeEntries, "Negatif").apply {
            color = Color.parseColor("#F44336")
            lineWidth = 2f
            circleRadius = 4f
            setCircleColor(Color.parseColor("#F44336"))
            valueTextColor = Color.BLACK
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }

        val lineData = LineData(positiveSet, negativeSet)
        lineChart.data = lineData
        lineChart.invalidate() // 🔹 Refresh chart
    }
}