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
        val sharedPreferences = getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        if (sharedPreferences.getBoolean("user_logged_in", false)) {
            startActivity(Intent(this, MainActivity::class.java))
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
        )
        transaction.commit()
        supportActionBar?.title =
            "Register Yourself"
    }
    override fun onBackPressed() {
        when (supportFragmentManager.findFragmentById(R.id.frameLayout)) {
            !is LoginFragment -> openLoginFragment()
            else -> super.onBackPressed()
        }
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                openLoginFragment()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}