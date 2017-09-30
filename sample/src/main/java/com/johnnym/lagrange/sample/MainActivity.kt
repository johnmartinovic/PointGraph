package com.johnnym.lagrange.sample

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import com.johnnym.lagrange.LaGrange
import com.johnnym.lagrange.Range
import com.johnnym.lagrange.RangeData
import java.util.ArrayList

class MainActivity : AppCompatActivity() {

    private lateinit var laGrange: LaGrange
    private lateinit var minValueTextView: TextView
    private lateinit var maxValueTextView: TextView
    private lateinit var approxResultsNumTextView: TextView
    private lateinit var setDataButton: Button
    private lateinit var resetDataButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        laGrange = findViewById(R.id.la_grange)
        minValueTextView = findViewById(R.id.tv_min_value)
        maxValueTextView = findViewById(R.id.tv_max_value)
        approxResultsNumTextView = findViewById(R.id.tv_approx_results_num)
        setDataButton = findViewById(R.id.btn_set_data)
        resetDataButton = findViewById(R.id.btn_reset_data)

        setDataButton.setOnClickListener {
            setLaGrange()
        }
        resetDataButton.setOnClickListener {
            resetLaGrange()
        }
    }

    private fun setLaGrange() {
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

        laGrange.addMinSelectorChangeListener(object: LaGrange.MinSelectorPositionChangeListener {
            override fun onMinValueChanged(newMinValue: Long) {
                minValueTextView.text = newMinValue.toString()
            }
        })

        laGrange.addMaxSelectorChangeListener(object: LaGrange.MaxSelectorPositionChangeListener {
            override fun onMaxValueChanged(newMaxValue: Long) {
                maxValueTextView.text = newMaxValue.toString()
            }
        })

        laGrange.setRangeData(RangeData(rangeDataList))
    }

    private fun resetLaGrange() {
        laGrange.setRangeData(null)
    }
}
