package com.techguidebro.foodmart.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.techguidebro.foodmart.R
class OrderConfirmActivity : AppCompatActivity() {
    lateinit var buttonOkay: Button
    lateinit var orderSuccessfullyPlaced: RelativeLayout
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirm_activty)
        orderSuccessfullyPlaced=findViewById(R.id.orderSuccessfullyPlaced)
        buttonOkay=findViewById(R.id.buttonOkay)
        buttonOkay.setOnClickListener(View.OnClickListener {
            val intent= Intent(this,MainActivity::class.java)
            startActivity(intent)
            finishAffinity()//finish all the activities
        })
    }
    override fun onBackPressed() {
        //force user to press okay button to take him to dashboard screen
        //user can't go back
    }
}