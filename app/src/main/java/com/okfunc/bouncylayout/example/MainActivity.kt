package com.okfunc.bouncylayout.example

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.okfunc.core.base.activity.BaseStylesActivity
import com.okfunc.core.base.adapter.SuperAdapter
import com.okfunc.core.base.ext.elazy
import com.okfunc.core.base.ext.layout
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : BaseStylesActivity() {

    val adatper by elazy { TestAdapter(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // recyclerView.layoutManager = GridLayoutManager(this, 3)
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerView.adapter = adatper

//        adatper.addHeaderView(layout(R.layout.test_header, recyclerView, false))
//        adatper.addFooterView(layout(R.layout.test_footer, recyclerView, false))
        adatper.setData(Array(20) { it.toString() })
        adatper.notifyDataSetChanged()
    }
}

class TestAdapter(ctx: Context) : SuperAdapter<String>(ctx) {
    override fun onCreateItemView(parent: ViewGroup, viewType: Int) = R.layout.item_test

    private val random = Random()

    override fun onBindItemView(view: View, viewType: Int, item: String, position: Int) {
        view as TextView
        view.text = "position $position"
        view.setBackgroundColor(Color.rgb(random.nextInt(255), random.nextInt(255), random.nextInt(255)))
    }
}
