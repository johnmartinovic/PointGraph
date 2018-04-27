package com.johnnym.pointgraph.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.johnnym.pointgraph.*
import com.johnnym.pointgraph.graphend.GraphEnd
import com.johnnym.pointgraph.sample.common.bindView
import com.johnnym.pointgraph.utils.S_TO_MS_FACTOR

class GraphEndActivity : AppCompatActivity() {

    companion object {

        private const val TOTAL_TIME: Long = 20
        private const val REFRESH_TIME_MS: Long = 1000 / 60

        private const val POINTS_DATA = "pointsData"

        fun getIntent(context: Context): Intent {
            return Intent(context, GraphEndActivity::class.java)
        }
    }

    private val graphEnd: GraphEnd by bindView(R.id.graph_end)
    private val currentTimerStateTextView: TextView by bindView(R.id.tv_current_time_state)
    private val startTimeButton: Button by bindView(R.id.btn_start_time)
    private val stopTimeButton: Button by bindView(R.id.btn_stop_time)

    private var pointsData: PointsData? = null

    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_end)

        initGraphEndSelectorListener()

        if (savedInstanceState == null) setGraphEndData()

        currentTimerStateTextView.setOnClickListener {
            graphEnd.toggleGraphVisibility()
        }

        startTimeButton.setOnClickListener {
            countDownTimer?.cancel()
            initCountDownTimer()
            countDownTimer?.start()
        }
        stopTimeButton.setOnClickListener {
            countDownTimer?.cancel()
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle?) {
        super.onRestoreInstanceState(savedInstanceState)

        savedInstanceState?.let {
            pointsData = it.getParcelable(POINTS_DATA)
            updateCurrentTimerStateTextView(graphEnd.selectorValue)
        }
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        outState?.putParcelable(POINTS_DATA, pointsData)

        super.onSaveInstanceState(outState)
    }

    private fun initCountDownTimer() {
        countDownTimer = object : CountDownTimer(
                ((TOTAL_TIME.toFloat() - graphEnd.selectorValue) * S_TO_MS_FACTOR).toLong(),
                REFRESH_TIME_MS) {

            override fun onFinish() {
                setGraphEndSelectorValue(TOTAL_TIME.toFloat())
            }

            override fun onTick(millisUntilFinished: Long) {
                setGraphEndSelectorValue(
                        TOTAL_TIME.toFloat() - millisUntilFinished.toFloat() / S_TO_MS_FACTOR)
            }
        }
    }

    private fun setGraphEndSelectorValue(value: Float) {
        graphEnd.setSelectorValue(value, false)
    }

    private fun setGraphEndData() {
        val points = listOf(
                Point(0f, 200f),
                Point(1f, 300f),
                Point(2f, 420f),
                Point(3f, 400f),
                Point(4f, 390f),
                Point(5f, 385f),
                Point(6f, 390f),
                Point(7f, 380f),
                Point(8f, 375f),
                Point(9f, 320f),
                Point(10f, 300f),
                Point(11f, 240f),
                Point(12f, 190f),
                Point(13f, 120f),
                Point(14f, 100f),
                Point(15f, 120f),
                Point(16f, 120f),
                Point(17f, 110f),
                Point(18f, 115f),
                Point(19f, 100f),
                Point(20f, 120f))

        pointsData = PointsData(points)
        graphEnd.setPointsData(pointsData)
    }

    private fun initGraphEndSelectorListener() {
        graphEnd.addSelectorListener(selectorListener)
    }

    private fun removeLaGrangeSelectorListeners() {
        graphEnd.removeSelectorListener(selectorListener)
    }

    private fun updateCurrentTimerStateTextView(newValue: Float) {
        currentTimerStateTextView.text = String.format(
                "%02.1f",
                newValue)
    }

    private val selectorListener = object : GraphEnd.SelectorListener {

        override fun onSelectorPressed() {
            countDownTimer?.cancel()
        }

        override fun onValueChanged(newValue: Float) {
            updateCurrentTimerStateTextView(newValue)
        }

        override fun onSelectorReleased() {
            // do nothing
        }
    }
}