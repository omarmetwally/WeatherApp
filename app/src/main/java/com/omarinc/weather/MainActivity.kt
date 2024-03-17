package com.omarinc.weather

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.omarinc.weather.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
//    lateinit var contentLayout: LinearLayout


    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        drawerLayout=binding.drawerLayout
//        contentLayout-binding.contentContainer


        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            setHomeAsUpIndicator(R.drawable.icons8_menu)
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)

            title = ""
        }

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string._0, R.string._0_0)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.navigatorLayout, navController)


        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                // Calculate the translation amount based on the slide offset
                val slideAmount = drawerView.width * slideOffset
                // Assuming your content layout is directly after the Toolbar in the DrawerLayout
                binding.contentContainer.translationX = slideAmount
            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })


//
//        supportActionBar?.apply {
//            setHomeAsUpIndicator(R.drawable.icons8_menu)
//            setDisplayHomeAsUpEnabled(true)
//            setDisplayShowHomeEnabled(true)
//            title=""
//            setBackgroundDrawable(ColorDrawable(ContextCompat.getColor(this@MainActivity, R.color.blue_sky)))
//
//        }
//        val navController = findNavController(R.id.nav_host_fragment)
//        NavigationUI.setupWithNavController(binding.navigatorLayout, navController)
//
//
//
//        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string._0, R.string._0_0)
//        drawerLayout.addDrawerListener(actionBarDrawerToggle)
//        actionBarDrawerToggle.syncState()

        // to make the Navigation drawer icon always appear on the action bar
//        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onOptionsItemSelected(item: android.view.MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (binding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    binding.drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    binding.drawerLayout.openDrawer(GravityCompat.START)
                }
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}