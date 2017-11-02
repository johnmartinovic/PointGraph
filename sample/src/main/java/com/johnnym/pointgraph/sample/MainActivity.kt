package com.johnnym.pointgraph.sample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import com.johnnym.pointgraph.sample.common.bindView

class MainActivity : AppCompatActivity() {

    private val demoLaGrangeButton: Button by bindView(R.id.btn_demo_la_grange)
    private val demoGraphEndButton: Button by bindView(R.id.btn_demo_graph_end)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        demoLaGrangeButton.setOnClickListener {
            startActivity(LaGrangeActivity.getIntent(this))
        }
        demoGraphEndButton.setOnClickListener {
            startActivity(GraphEndActivity.getIntent(this))
        }
    }
}