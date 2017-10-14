package com.johnnym.lagrange.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.jakewharton.rxbinding2.widget.RxTextView
import com.johnnym.lagrange.LaGrange
import com.johnnym.lagrange.Range
import com.johnnym.lagrange.RangeData
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var laGrange: LaGrange
    private lateinit var minValueEditText: EditText
    private lateinit var maxValueEditText: EditText
    private lateinit var approxResultsNumTextView: TextView
    private lateinit var setDataButton: Button
    private lateinit var resetDataButton: Button

    private var changingDoneByLaGrange: Boolean = false
    private var changingDoneByEditTexts: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        laGrange = findViewById(R.id.la_grange)
        minValueEditText = findViewById(R.id.et_min_value)
        maxValueEditText = findViewById(R.id.et_max_value)
        approxResultsNumTextView = findViewById(R.id.tv_approx_results_num)
        setDataButton = findViewById(R.id.btn_set_data)
        resetDataButton = findViewById(R.id.btn_reset_data)

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
        rangeDataList.add(Range(0, 20, 0))
        rangeDataList.add(Range(21, 40, 30))
        rangeDataList.add(Range(41, 60, 50))
        rangeDataList.add(Range(61, 80, 30))
        rangeDataList.add(Range(81, 100, 60))
        rangeDataList.add(Range(101, 120, 60))
        rangeDataList.add(Range(121, 140, 55))
        rangeDataList.add(Range(141, 160, 70))
        rangeDataList.add(Range(161, 180, 75))
        rangeDataList.add(Range(181, 200, 75))
        rangeDataList.add(Range(200, 220, 80))
        rangeDataList.add(Range(221, 240, 100))
        rangeDataList.add(Range(241, 260, 95))
        rangeDataList.add(Range(261, 280, 98))
        rangeDataList.add(Range(281, 300, 95))
        rangeDataList.add(Range(300, 320, 90))
        rangeDataList.add(Range(321, 340, 90))
        rangeDataList.add(Range(341, 360, 85))
        rangeDataList.add(Range(361, 380, 80))
        rangeDataList.add(Range(381, 400, 20))
        rangeDataList.add(Range(400, 420, 10))
        rangeDataList.add(Range(421, 440, 1))
        rangeDataList.add(Range(441, 460, 2))
        rangeDataList.add(Range(461, 480, 0))
        rangeDataList.add(Range(481, 500, 0))

        laGrange.setRangeData(RangeData(rangeDataList))
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
        laGrange.setRangeData(null)
        updateApproxResultsNumTextView()
    }

    private fun updatePriceGraph() {
        val minValue: Long? = minValueEditText.text.toString().toLongOrNull()
        val maxValue: Long? = maxValueEditText.text.toString().toLongOrNull()

        laGrange.setSelectorsValues(minValue, maxValue)
    }

    private fun updateApproxResultsNumTextView() {
        approxResultsNumTextView.text = String.format("%d", laGrange.getApproxCountInSelectedRange())
    }

    private val minSelectorPositionChangeListener = object : LaGrange.MinSelectorPositionChangeListener {
        override fun onMinValueChanged(newMinValue: Long) {
            changingDoneByLaGrange = true
            if (!changingDoneByEditTexts) {
                // This is done instead of "setText" method to prevent the block of the keyboard
                // when EditText is focused. This bug has to be researched.
                minValueEditText.text.clear()
                minValueEditText.text.append(newMinValue.toString())
            }
            updateApproxResultsNumTextView()
            changingDoneByLaGrange = false
        }
    }

    private val maxSelectorPositionChangeListener = object : LaGrange.MaxSelectorPositionChangeListener {
        override fun onMaxValueChanged(newMaxValue: Long) {
            changingDoneByLaGrange = true
            if (!changingDoneByEditTexts) {
                // This is done instead of "setText" method to prevent the block of the keyboard
                // when EditText is focused. This bug has to be researched.
                maxValueEditText.text.clear()
                maxValueEditText.text.append(newMaxValue.toString())
            }
            updateApproxResultsNumTextView()
            changingDoneByLaGrange = false
        }
    }
}
