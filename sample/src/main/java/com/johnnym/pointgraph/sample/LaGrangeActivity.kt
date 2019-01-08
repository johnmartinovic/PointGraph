package com.johnnym.pointgraph.sample

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.johnnym.pointgraph.Range
import com.johnnym.pointgraph.RangeData
import com.johnnym.pointgraph.lagrange.LaGrange
import com.johnnym.pointgraph.sample.common.bindView

class LaGrangeActivity : AppCompatActivity() {

    companion object {

        private const val RANGE_DATA = "rangeData"

        fun getIntent(context: Context): Intent {
            return Intent(context, LaGrangeActivity::class.java)
        }
    }

    private val laGrange: LaGrange by bindView(R.id.la_grange)
    private val minValueEditText: EditText by bindView(R.id.et_min_value)
    private val maxValueEditText: EditText by bindView(R.id.et_max_value)
    private val approxResultsNumTextView: TextView by bindView(R.id.tv_approx_results_num)
    private val setDataButton: Button by bindView(R.id.btn_set_data)
    private val resetDataButton: Button by bindView(R.id.btn_reset_data)

    private var changingDoneByLaGrange: Boolean = true
    private var changingDoneByEditTexts: Boolean = true

    private var rangeData: RangeData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_la_grange)

        changingDoneByLaGrange = true
        changingDoneByEditTexts = true

        RxTextView.textChanges(minValueEditText)
                .filter { !changingDoneByLaGrange }
                .subscribe {
                    changingDoneByEditTexts = true
                    updateLaGrangeSelectorsValues()
                    changingDoneByEditTexts = false
                }

        RxTextView.textChanges(maxValueEditText)
                .filter { !changingDoneByLaGrange }
                .subscribe {
                    changingDoneByEditTexts = true
                    updateLaGrangeSelectorsValues()
                    changingDoneByEditTexts = false
                }

        initLaGrangeSelectorsListeners()

        setDataButton.setOnClickListener {
            setLaGrangeData()
        }

        resetDataButton.setOnClickListener {
            resetLaGrangeData()
            minValueEditText.text.clear()
            maxValueEditText.text.clear()
            approxResultsNumTextView.text = ""
        }
    }

    override fun onResume() {
        super.onResume()

        changingDoneByLaGrange = false
        changingDoneByEditTexts = false
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.let {
            rangeData = it.getParcelable(RANGE_DATA)
            updateApproxResultsNumTextView()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable(RANGE_DATA, rangeData)

        super.onSaveInstanceState(outState)
    }

    private fun setLaGrangeData() {
        val rangeDataList = listOf(
                Range(0f, 20f, 0f),
                Range(21f, 40f, 30f),
                Range(41f, 60f, 50f),
                Range(61f, 80f, 30f),
                Range(81f, 100f, 60f),
                Range(101f, 120f, 60f),
                Range(121f, 140f, 55f),
                Range(141f, 160f, 70f),
                Range(161f, 180f, 75f),
                Range(181f, 200f, 75f),
                Range(200f, 220f, 80f),
                Range(221f, 240f, 100f),
                Range(241f, 260f, 95f),
                Range(261f, 280f, 98f),
                Range(281f, 300f, 95f),
                Range(300f, 320f, 90f),
                Range(321f, 340f, 90f),
                Range(341f, 360f, 85f),
                Range(361f, 380f, 80f),
                Range(381f, 400f, 20f),
                Range(401f, 420f, 10f),
                Range(421f, 440f, 1f),
                Range(441f, 460f, 2f),
                Range(461f, 480f, 0f),
                Range(481f, 500f, 0f))

        rangeData = RangeData(rangeDataList)
        rangeData?.let { rangeData ->
            laGrange.setPointsData(rangeData.pointsData)
        }
    }

    private fun initLaGrangeSelectorsListeners() {
        laGrange.addSelectorsListener(selectorsListener)
    }

    private fun removeLaGrangeSelectorListeners() {
        laGrange.removeSelectorsListener(selectorsListener)
    }

    private fun resetLaGrangeData() {
        laGrange.setPointsData(null)
        updateApproxResultsNumTextView()
    }

    private fun updateLaGrangeSelectorsValues() {
        val minValue: Float? = minValueEditText.text.toString().toFloatOrNull()
        val maxValue: Float? = maxValueEditText.text.toString().toFloatOrNull()

        laGrange.setSelectorsValues(minValue, maxValue, true)
    }

    private fun updateApproxResultsNumTextView() {
        approxResultsNumTextView.text = String.format(
                "%.0f",
                rangeData?.getApproxCountInRange(
                        laGrange.minSelectorValue,
                        laGrange.maxSelectorValue))
    }

    private val selectorsListener = object : LaGrange.SelectorsListener {

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
