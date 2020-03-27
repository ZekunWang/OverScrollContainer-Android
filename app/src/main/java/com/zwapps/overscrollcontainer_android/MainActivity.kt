package com.zwapps.overscrollcontainer_android

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.zwapps.overscrollcontainer.OverScrollContainer

class MainActivity : AppCompatActivity() {

    private val imageView: ImageView by lazy {
        findViewById<ImageView>(R.id.image).apply {
            setOnClickListener {
                Toast.makeText(context, "Click image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val recyclerView: RecyclerView by lazy {
        findViewById<RecyclerView>(R.id.recyclerview)
    }

    private val recyclerViewContainer: OverScrollContainer by lazy {
        findViewById<OverScrollContainer>(R.id.recyclerview_container)
    }

    private val scrollView: ScrollView by lazy {
        findViewById<ScrollView>(R.id.scrollview)
    }

    private val contentView: LinearLayout by lazy {
        findViewById<LinearLayout>(R.id.content_view)
    }

    private val scrollViewContainer: OverScrollContainer by lazy {
        findViewById<OverScrollContainer>(R.id.scrollview_container)
    }

    private lateinit var adapter: MyAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var originalHeight = -1F

        recyclerViewContainer.overScrollListener = {
            if (originalHeight == -1F) {
                originalHeight = imageView.measuredHeight.toFloat()
                imageView.pivotY = 0F
            }

            imageView.scaleY = (originalHeight + recyclerViewContainer.child.translationY) / originalHeight
            imageView.scaleX = imageView.scaleY
        }

        scrollViewContainer.overScrollListener = {
            if (originalHeight == -1F) {
                originalHeight = imageView.measuredHeight.toFloat()
                imageView.pivotY = 0F
            }

            imageView.scaleY = (originalHeight + scrollView.translationY) / originalHeight
            imageView.scaleX = imageView.scaleY
        }

        val colors = mutableListOf<Int>()

        for (i in 0..10) {
            colors.add(Color.BLUE)
            colors.add(Color.RED)
            colors.add(Color.GREEN)
        }

        adapter = MyAdapter(colors)
        recyclerView.adapter = adapter

        colors.forEachIndexed { index, color ->
            val view = TextView(this).apply {
                layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 250)
                setBackgroundColor(color)
                setTextColor(Color.GRAY)
                gravity = Gravity.CENTER
                textSize = 20F
                text = "ScrollView - $index"
                setOnClickListener {
                    Toast.makeText(context, "ScrollView click - $index", Toast.LENGTH_SHORT).show()
                }
            }

            contentView.addView(view)
        }
    }
}
