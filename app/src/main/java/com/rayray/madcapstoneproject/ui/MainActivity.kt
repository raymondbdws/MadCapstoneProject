package com.rayray.madcapstoneproject.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.rayray.madcapstoneproject.R
import com.rayray.madcapstoneproject.adapter.ViewPagerAdapter
import com.rayray.madcapstoneproject.ui.ui.AfschrijfFragment
import com.rayray.madcapstoneproject.ui.ui.ArtikelOverzichtFragment
import com.rayray.madcapstoneproject.ui.ui.ControleFragment
import com.rayray.madcapstoneproject.ui.ui.InboekFragment
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Raymond Chang
 */
class MainActivity : AppCompatActivity() {
    var position = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar_main))

        //Firebase
        FirebaseFirestore.setLoggingEnabled(true)
        FirebaseApp.initializeApp(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        val tabLayout: TabLayout = findViewById(R.id.tabLayout)
        val viewPager: ViewPager = findViewById(R.id.viewPager)
        val viewPagerAdapter = ViewPagerAdapter(supportFragmentManager)

        //Tabladen
        viewPagerAdapter.addFragment(ArtikelOverzichtFragment(), "Artikelen")
        viewPagerAdapter.addFragment(InboekFragment(), "Inboeken")
        viewPagerAdapter.addFragment(AfschrijfFragment(), "Afboeken")
        viewPagerAdapter.addFragment(ControleFragment(), "Controle")

        viewPager.adapter = viewPagerAdapter
        tabLayout.setupWithViewPager(viewPager)
        getSelectedTab()
    }

    fun getSelectedTab(): Int {
        tabLayout!!.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                position = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
            }
        })
        return position
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

}