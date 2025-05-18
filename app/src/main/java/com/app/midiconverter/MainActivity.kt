package com.app.midiconverter

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import com.app.midiconverter.databinding.ActivityMainBinding
import com.app.midiconverter.databinding.FragmentHomeBinding
import com.app.midiconverter.settings.FragmentSettings
import com.app.midiconverter.statistic.FragmentStatistic
import com.app.quizz.home.FragmentHome
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val fragmentHome = FragmentHome()
        val fragmentSettings = FragmentSettings()
        val fragmentStatistic = FragmentStatistic()

        val sharedPref = getSharedPreferences("settings_property", Context.MODE_PRIVATE)
        val statistic_mode = sharedPref?.getBoolean("statistic_mode", true)

        /*if(statistic_mode == true){
            FragmentStatistic().show(supportFragmentManager, null)
        }*/

        binding.tablayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                supportFragmentManager.popBackStack()
               if (tab?.position == 0) {
                   val fragmentTransaction = supportFragmentManager.beginTransaction()
                   fragmentTransaction.replace(R.id.fragmentContainerView, fragmentStatistic)
                   fragmentTransaction.commit()
               } else if(tab?.position == 1){
                   val fragmentTransaction = supportFragmentManager.beginTransaction()
                   fragmentTransaction.replace(R.id.fragmentContainerView, fragmentHome)
                   fragmentTransaction.commit()
               }else if(tab?.position == 2){
                   val fragmentTransaction = supportFragmentManager.beginTransaction()
                   fragmentTransaction.replace(R.id.fragmentContainerView, fragmentSettings)
                   fragmentTransaction.commit()
               }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {
                supportFragmentManager.popBackStack()

                if (tab?.position == 0) {
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentContainerView, fragmentStatistic)
                    fragmentTransaction.commit()
                } else if(tab?.position == 1){
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentContainerView, fragmentHome)
                    fragmentTransaction.commit()
                }else if(tab?.position == 2){
                    val fragmentTransaction = supportFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentContainerView, fragmentSettings)
                    fragmentTransaction.commit()
                }
            }

        })

    }
}