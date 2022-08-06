package com.techguidebro.foodmart.activity
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.WindowManager
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.techguidebro.foodmart.R
import com.techguidebro.foodmart.fragment.*
import com.techguidebro.foodmart.fragment.DashboardFragment
class MainActivity : AppCompatActivity() {
    lateinit var drawerLayout: DrawerLayout
    lateinit var coordinatorLayout: CoordinatorLayout
    lateinit var toolbar: Toolbar
    lateinit var frameLayout: FrameLayout
    lateinit var navigationView: NavigationView
    lateinit var textViewcurrentUser: TextView
    lateinit var textViewMobileNumber: TextView
    var previousMenuItem: MenuItem? = null
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navigationView = findViewById(R.id.navigationView)
        val sharedPreferencess = getSharedPreferences(
            getString(R.string.shared_preferences),
            Context.MODE_PRIVATE
        )
        drawerLayout = findViewById(R.id.drawerLayout)
        toolbar = findViewById(R.id.toolBar)
        frameLayout = findViewById(R.id.frameLayout)
        val headerView = navigationView.getHeaderView(0)
        navigationView.menu.getItem(0).setCheckable(true)
        navigationView.menu.getItem(0).setChecked(true)
        textViewcurrentUser = headerView.findViewById(R.id.textViewcurrentUser)
        textViewMobileNumber = headerView.findViewById(R.id.textViewMobileNumber)
        coordinatorLayout = findViewById(R.id.coordinatorLayout)
        navigationView = findViewById(R.id.navigationView)
        setupToolbar()
        textViewcurrentUser.text=sharedPreferencess.getString("name","sandeep")
        ("+91-"+sharedPreferencess.getString("mobile_number","9516123456")).also { textViewMobileNumber.text = it }
        val actionBarDrawerToggle = ActionBarDrawerToggle(
            this@MainActivity,
            drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
        navigationView.setNavigationItemSelectedListener {
            if (previousMenuItem != null) {
                previousMenuItem?.isChecked = false
            }
            it.isCheckable = true
            it.isChecked = true
            previousMenuItem = it
            when (it.itemId) {
                R.id.dashboard -> {
                    openDashboard()
                    drawerLayout.closeDrawers()
                }
                R.id.favourites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FavouritesFragment(this))
                        .commit()
                    supportActionBar?.title = "Favourites Restaurants"
                    drawerLayout.closeDrawers()
                }
                R.id.profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, ProfileFragment(this))
                        .commit()
                    supportActionBar?.title = "Your Profile"
                    drawerLayout.closeDrawers()
                }
                R.id.order -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, OrderHistoryFragment(this))
                        .commit()
                    supportActionBar?.title = "Your Previous Orders"
                    drawerLayout.closeDrawers()
                }
                R.id.faqs -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.frameLayout, FaqFragment())
                        .commit()
                    supportActionBar?.title = "Frequently asked questions"
                    drawerLayout.closeDrawers()
                }
                R.id.logout -> {
                    drawerLayout.closeDrawers()

                    val alterDialog = androidx.appcompat.app.AlertDialog.Builder(this)

                    alterDialog.setTitle("Confirmation")
                    alterDialog.setMessage("Are you sure you want to log out?")
                    alterDialog.setPositiveButton("Yes") { text, listener ->
                        sharedPreferencess.edit().putBoolean("user_logged_in", false).apply()
                        ActivityCompat.finishAffinity(this)
                    }
                    alterDialog.setNegativeButton("No") { text, listener ->
                    }
                    alterDialog.create()
                    alterDialog.show()
                }
            }
            return@setNavigationItemSelectedListener true
        }
        openDashboard()
    }
    fun setupToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Toolbar Title"
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        if (id == android.R.id.home) {
            drawerLayout.openDrawer(GravityCompat.START)
        }
        return super.onOptionsItemSelected(item)
    }
    fun openDashboard() {
        val fragment = DashboardFragment(this)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.frameLayout, fragment)
        transaction.commit()
        supportActionBar?.title = "All Restuarants"
        navigationView.setCheckedItem(R.id.dashboard)
    }
    override fun onBackPressed() {
        val frag = supportFragmentManager.findFragmentById(R.id.frameLayout)
        when (frag) {
            !is DashboardFragment -> {
                navigationView.menu.getItem(0).setChecked(true)
                openDashboard()
            }
            else -> super.onBackPressed()
        }
    }
    override fun onResume() {
        getWindow().setSoftInputMode(
            WindowManager.
        LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        super.onResume()
    }

}