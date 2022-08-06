package com.techguidebro.foodmart.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.fragment.LoginFragment
class LoginRegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_register)
        val sharedPreferencess = getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        if (sharedPreferencess.getBoolean("user_logged_in",false)) {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        } else {
            openLoginFragment()
        }
    }
    fun openLoginFragment() {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(
            R.id.frameLayout,
            LoginFragment(this)
        )//replace the old layout with the new frag  layout
        transaction.commit()//apply changes
        supportActionBar?.title = "Register Yourself"//change the title when each new fragment is opened
    }

    override fun onBackPressed() {
        val currentFragment =
            supportFragmentManager.findFragmentById(R.id.frameLayout)//gets the id og=f the current fragment
        when (currentFragment) {
            !is LoginFragment -> openLoginFragment()
            else -> super.onBackPressed()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> { openLoginFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}