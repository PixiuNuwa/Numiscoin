package cl.numiscoin2

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2

class OnboardingActivity : AppCompatActivity() {

    private lateinit var viewPager: ViewPager2
    private lateinit var indicatorLayout: LinearLayout
    private lateinit var btnSkip: Button
    private lateinit var btnNext: Button

    private lateinit var onboardingPagerAdapter: OnboardingPagerAdapter
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        initViews()
        setupViewPager()
        setupIndicators()
        setListeners()
    }

    private fun initViews() {
        viewPager = findViewById(R.id.viewPager)
        indicatorLayout = findViewById(R.id.indicatorLayout)
        btnSkip = findViewById(R.id.btnSkip)
        btnNext = findViewById(R.id.btnNext)
    }

    private fun setupViewPager() {
        onboardingPagerAdapter = OnboardingPagerAdapter(this)
        viewPager.adapter = onboardingPagerAdapter
        viewPager.registerOnPageChangeCallback(pageChangeCallback)
    }

    private fun setupIndicators() {
        val indicators = arrayOfNulls<ImageView>(3)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(8, 0, 8, 0)
        }

        for (i in indicators.indices) {
            indicators[i] = ImageView(this)
            indicators[i]?.setImageResource(
                if (i == 0) R.drawable.indicator_active else R.drawable.indicator_inactive
            )
            indicators[i]?.layoutParams = layoutParams
            indicatorLayout.addView(indicators[i])
        }
    }

    private fun setListeners() {
        btnSkip.setOnClickListener {
            goToLogin()
        }

        btnNext.setOnClickListener {
            if (currentPage < onboardingPagerAdapter.itemCount - 1) {
                viewPager.currentItem = currentPage + 1
            } else {
                goToLogin()
            }
        }
    }

    private val pageChangeCallback = object : ViewPager2.OnPageChangeCallback() {
        override fun onPageSelected(position: Int) {
            currentPage = position
            updateIndicators(position)
            updateButtons(position)
        }
    }

    private fun updateIndicators(position: Int) {
        for (i in 0 until indicatorLayout.childCount) {
            val imageView = indicatorLayout.getChildAt(i) as ImageView
            imageView.setImageResource(
                if (i == position) R.drawable.indicator_active else R.drawable.indicator_inactive
            )
        }
    }

    private fun updateButtons(position: Int) {
        if (position == onboardingPagerAdapter.itemCount - 1) {
            btnNext.text = "Comenzar"
        } else {
            btnNext.text = "Siguiente"
        }
    }

    private fun goToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}