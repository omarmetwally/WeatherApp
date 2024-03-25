package com.omarinc.weather

import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavOptions
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.google.android.material.navigation.NavigationView
import com.omarinc.weather.databinding.ActivityMainBinding
import com.omarinc.weather.sharedpreferences.SharedPreferencesImpl
import com.omarinc.weather.utilities.Constants
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var drawerLayout: DrawerLayout
    lateinit var navigationView: NavigationView
    lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
//    lateinit var contentLayout: LinearLayout


    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        var lang =setAppLanguage()
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

        actionBarDrawerToggle = ActionBarDrawerToggle(this, drawerLayout, R.string.openDrawer, R.string.closeDrawer)
        actionBarDrawerToggle.drawerArrowDrawable.color=getColor(R.color.white)
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()

        val navController = findNavController(R.id.nav_host_fragment)
        NavigationUI.setupWithNavController(binding.navigatorLayout, navController)


        binding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
               if(lang=="ar"){
                val slideAmount = drawerView.width * slideOffset
                binding.contentContainer.translationX = -slideAmount
               }
                else
               {
                   val slideAmount = drawerView.width * slideOffset
                   binding.contentContainer.translationX = slideAmount
               }
            }

            override fun onDrawerOpened(drawerView: View) {}

            override fun onDrawerClosed(drawerView: View) {}

            override fun onDrawerStateChanged(newState: Int) {}
        })

        binding.navigatorLayout.setNavigationItemSelectedListener { menuItem ->
            val navController = findNavController(R.id.nav_host_fragment)
            when (menuItem.itemId) {
                R.id.homeFragment -> {
                    val options = getNavOptions()
                    navController.navigate(R.id.homeFragment, null, options)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                } R.id.favoriteFragment -> {
                    val options = getNavOptions()
                    navController.navigate(R.id.favoriteFragment, null, options)
                drawerLayout.closeDrawer(GravityCompat.START)
                    true
                } R.id.alertFragment -> {
                    val options = getNavOptions()
                    navController.navigate(R.id.alertFragment, null, options)
                drawerLayout.closeDrawer(GravityCompat.START)
                    false
                }
                R.id.settingFragment -> {
                    val options = getNavOptions()
                    navController.navigate(R.id.settingFragment, null, options)
                drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                else -> false
            }

        }

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

    private fun setAppLanguage():String {
        val sharedPreferences = SharedPreferencesImpl.getInstance(this)
        val language = sharedPreferences.readStringFromSharedPreferences(Constants.KEY_LANGUAGE)
        val locale = Locale(language)
        Locale.setDefault(locale)
        val resources: Resources = this.resources
        val config = Configuration(resources.configuration)
        config.setLocale(locale)

        val layoutDirection = if (language == "ar") {
            View.LAYOUT_DIRECTION_RTL
        } else {
            View.LAYOUT_DIRECTION_LTR
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            config.setLayoutDirection(locale)
            this.window.decorView.layoutDirection = layoutDirection
        }

        resources.updateConfiguration(config, resources.displayMetrics)
        return language
    }


    private fun getNavOptions(): NavOptions {
        return NavOptions.Builder()
            .setLaunchSingleTop(true)  // Ensure the current destination is not added to the back stack
            .setPopUpTo(findNavController(R.id.nav_host_fragment).graph.startDestinationId, false)
            // Pop up to the start destination of the graph before navigating. This ensures that navigating to the current destination won't add it again to the back stack
            .build()
    }
}