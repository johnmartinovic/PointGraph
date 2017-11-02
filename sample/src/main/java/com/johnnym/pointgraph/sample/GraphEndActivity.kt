package com.johnnym.pointgraph.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.johnnym.pointgraph.*
import com.johnnym.pointgraph.sample.common.bindView

class GraphEndActivity : AppCompatActivity() {

    companion object {

        fun getIntent(context: Context): Intent {
            return Intent(context, GraphEndActivity::class.java)
        }
    }

    private val graphEnd: GraphEnd by bindView(R.id.graph_end)
    private val currentTimerStateTextView: TextView by bindView(R.id.tv_current_time_state)
    private val startTimeButton: Button by bindView(R.id.btn_start_time)
    private val stopTimeButton: Button by bindView(R.id.btn_stop_time)

    private lateinit var pointsData: PointsData
    private lateinit var countDownTimer: CountDownTimer

    private var totalTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph_end)

        initCountDownTimer()

        initGraphEndSelectorListener()

        setGraphEndData()

        totalTime = ((pointsData.maxX - pointsData.minX) * 1000).toLong()

        startTimeButton.setOnClickListener {
            initCountDownTimer()
            countDownTimer.start()
        }
        stopTimeButton.setOnClickListener {
            countDownTimer.cancel()
        }
    }

    private fun initCountDownTimer() {
        countDownTimer = object : CountDownTimer(
                totalTime - graphEnd.selectorValue.toLong(),
                50.toLong()) {
            override fun onFinish() {
                setGraphEndSelectorValue(totalTime.toFloat() / 1000)
            }

            override fun onTick(millisUntilFinished: Long) {
                setGraphEndSelectorValue((totalTime - millisUntilFinished).toFloat() / 1000)
            }
        }
    }

    private fun setGraphEndSelectorValue(value: Float) {
        graphEnd.setSelectorValue(value)
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
        graphEnd.addSelectorChangeListener(selectorPositionChangeListener)
    }

    private fun removeLaGrangeSelectorListeners() {
        graphEnd.removeSelectorChangeListener(selectorPositionChangeListener)
    }

    private fun updateCurrentTimerStateTextView(newValue: Float) {
        currentTimerStateTextView.text = String.format(
                "%02.1f",
                newValue)
    }

    private val selectorPositionChangeListener = object : GraphEnd.SelectorPositionChangeListener {
        override fun onValueChanged(newValue: Float) {
            updateCurrentTimerStateTextView(newValue)
        }
    }
}