package com.techguidebro.foodmart

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.AnimationUtils.*
import android.widget.RelativeLayout
import android.widget.TextView
import com.techguidebro.foodmart.activity.LoginRegisterActivity

class WelcomeActivity : AppCompatActivity() {

    lateinit var txtDeliver: TextView
    lateinit var txtFood: TextView
    lateinit var topAnim: Animation
    lateinit var bottomAnim: Animation
    lateinit var relativeLayout: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)
        //hooks
        txtDeliver = findViewById(R.id.txtDeliver)
        txtFood = findViewById(R.id.txtFood)
        relativeLayout = findViewById(R.id.relSplash)
        //animations
        topAnim = loadAnimation(this, R.anim.top_animation)
        bottomAnim = loadAnimation(this, R.anim.bottom_animation)
        relativeLayout.setAnimation(bottomAnim)
        txtDeliver.setAnimation(topAnim)
        txtFood.setAnimation(topAnim)
        relativeLayout.visibility = View.VISIBLE
        Handler().postDelayed({
            kotlin.run {
                startActivity(
                    Intent(
                        this@WelcomeActivity,
                        LoginRegisterActivity::class.java
                    ).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                overridePendingTransition(R.anim.righttoleft, R.anim.top_animation)
                finish()
            }
        }, 3500)
    }
}
