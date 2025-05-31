package com.app.midiconverter

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.app.midiconverter.databinding.ActivityMainBinding
import com.app.midiconverter.settings.FragmentSettings
import com.app.midiconverter.statistic.FragmentHome
import com.app.quizz.home.FragmentNotes
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener

class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val fragmentHome = FragmentHome()
        val fragmentNotes = FragmentNotes()
        val fragmentSettings = FragmentSettings()

        binding.tablayout.addOnTabSelectedListener(object : OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab?) {
                supportFragmentManager.popBackStack()
               if (tab?.position == 0) {
                   val fragmentTransaction = supportFragmentManager.beginTransaction()
                   fragmentTransaction.replace(R.id.fragmentContainerView, fragmentHome)
                   fragmentTransaction.commit()
               } else if(tab?.position == 1){
                   val fragmentTransaction = supportFragmentManager.beginTransaction()
                   fragmentTransaction.replace(R.id.fragmentContainerView, fragmentNotes)
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
                onTabSelected(tab)
            }

        })

    }
}