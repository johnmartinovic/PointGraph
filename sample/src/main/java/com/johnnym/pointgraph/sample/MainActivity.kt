package com.johnnym.pointgraph.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.johnnym.pointgraph.LaGrange
import com.johnnym.pointgraph.Range
import com.johnnym.pointgraph.RangeData
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private val laGrange: LaGrange by bindView(R.id.la_grange)
    private val minValueEditText: EditText by bindView(R.id.et_min_value)
    private val maxValueEditText: EditText by bindView(R.id.et_max_value)
    private val approxResultsNumTextView: TextView by bindView(R.id.tv_approx_results_num)
    private val setDataButton: Button by bindView(R.id.btn_set_data)
    private val resetDataButton: Button by bindView(R.id.btn_reset_data)

    private var changingDoneByLaGrange: Boolean = false
    private var changingDoneByEditTexts: Boolean = false

    private var rangeData: RangeData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RxTextView.textChanges(minValueEditText)
                .filter { !changingDoneByLaGrange }
                .subscribe {
                    changingDoneByEditTexts = true
                    updatePriceGraph()
                    changingDoneByEditTexts = false
                }

        RxTextView.textChanges(maxValueEditText)
                .filter { !changingDoneByLaGrange }
                .subscribe {
                    changingDoneByEditTexts = true
                    updatePriceGraph()
                    changingDoneByEditTexts = false
                }

        initLaGrangeSelectorListeners()

        setDataButton.setOnClickListener {
            setLaGrangeData()
        }
        resetDataButton.setOnClickListener {
            resetLaGrangeData()
        }
    }

    private fun setLaGrangeData() {
        val rangeDataList = ArrayList<Range>()
        rangeDataList.add(Range(0f, 20f, 0f))
        rangeDataList.add(Range(21f, 40f, 30f))
        rangeDataList.add(Range(41f, 60f, 50f))
        rangeDataList.add(Range(61f, 80f, 30f))
        rangeDataList.add(Range(81f, 100f, 60f))
        rangeDataList.add(Range(101f, 120f, 60f))
        rangeDataList.add(Range(121f, 140f, 55f))
        rangeDataList.add(Range(141f, 160f, 70f))
        rangeDataList.add(Range(161f, 180f, 75f))
        rangeDataList.add(Range(181f, 200f, 75f))
        rangeDataList.add(Range(200f, 220f, 80f))
        rangeDataList.add(Range(221f, 240f, 100f))
        rangeDataList.add(Range(241f, 260f, 95f))
        rangeDataList.add(Range(261f, 280f, 98f))
        rangeDataList.add(Range(281f, 300f, 95f))
        rangeDataList.add(Range(300f, 320f, 90f))
        rangeDataList.add(Range(321f, 340f, 90f))
        rangeDataList.add(Range(341f, 360f, 85f))
        rangeDataList.add(Range(361f, 380f, 80f))
        rangeDataList.add(Range(381f, 400f, 20f))
        rangeDataList.add(Range(400f, 420f, 10f))
        rangeDataList.add(Range(421f, 440f, 1f))
        rangeDataList.add(Range(441f, 460f, 2f))
        rangeDataList.add(Range(461f, 480f, 0f))
        rangeDataList.add(Range(481f, 500f, 0f))

        rangeData = RangeData(rangeDataList)
        rangeData?.let { rangeData ->
            laGrange.setPointsData(rangeData.pointsData)
        }
    }

    private fun initLaGrangeSelectorListeners() {
        laGrange.addMinSelectorChangeListener(minSelectorPositionChangeListener)
        laGrange.addMaxSelectorChangeListener(maxSelectorPositionChangeListener)
    }

    private fun removeLaGrangeSelectorListeners() {
        laGrange.removeMinSelectorChangeListener(minSelectorPositionChangeListener)
        laGrange.removeMaxSelectorChangeListener(maxSelectorPositionChangeListener)
    }

    private fun resetLaGrangeData() {
        laGrange.setPointsData(null)
        updateApproxResultsNumTextView()
    }

    private fun updatePriceGraph() {
        val minValue: Float? = minValueEditText.text.toString().toFloatOrNull()
        val maxValue: Float? = maxValueEditText.text.toString().toFloatOrNull()

        laGrange.setSelectorsValues(minValue, maxValue)
    }

    private fun updateApproxResultsNumTextView() {
        approxResultsNumTextView.text = String.format(
                "%.0f",
                rangeData?.getApproxCountInRange(
                        laGrange.minSelectorValue,
                        laGrange.maxSelectorValue))
    }

    private val minSelectorPositionChangeListener = object : LaGrange.MinSelectorPositionChangeListener {
        override fun onMinValueChanged(newMinValue: Float) {
            changingDoneByLaGrange = true
            if (!changingDoneByEditTexts) {
                // This is done instead of "setText" method to prevent the block of the keyboard
                // when EditText is focused. This bug has to be researched.
                minValueEditText.text.clear()
                minValueEditText.text.append(String.format("%.0f", newMinValue))
            }
            updateApproxResultsNumTextView()
            changingDoneByLaGrange = false
        }
    }

    private val maxSelectorPositionChangeListener = object : LaGrange.MaxSelectorPositionChangeListener {
        override fun onMaxValueChanged(newMaxValue: Float) {
            changingDoneByLaGrange = true
            if (!changingDoneByEditTexts) {
                // This is done instead of "setText" method to prevent the block of the keyboard
                // when EditText is focused. This bug has to be researched.
                maxValueEditText.text.clear()
                maxValueEditText.text.append(String.format("%.0f", newMaxValue))
            }
            updateApproxResultsNumTextView()
            changingDoneByLaGrange = false
        }
    }
}
