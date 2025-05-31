package com.app.midiconverter.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.midiconverter.databinding.FragmentSettingBinding
import androidx.core.content.edit

class FragmentSettings : Fragment() {
    private val binding by lazy { FragmentSettingBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPref = activity?.getSharedPreferences("settings_property", Context.MODE_PRIVATE)

        val theme_mode = sharedPref?.getBoolean("theme_mode", false)
        val statistic_on = sharedPref?.getBoolean("statistic_mode", true)

        if(theme_mode == true){
            binding.themeSwitch.isChecked = true
        }else if(theme_mode == false){
            binding.themeSwitch.isChecked = false
        }

        if(statistic_on == true){
            binding.statisticSwitch.isChecked = true
        }else if(statistic_on == false){
            binding.statisticSwitch.isChecked = false
        }
        binding.themeSwitch.setOnCheckedChangeListener { _, isChecked ->
            sharedPref?.edit {
                putBoolean("theme_mode", isChecked)
            }

            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        binding.statisticSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if(b){
                binding.statisticSwitch.isChecked = true
                sharedPref?.edit {
                    putBoolean("statistic_mode", true)
                }
            }else{
                binding.statisticSwitch.isChecked = false
                sharedPref?.edit {
                    putBoolean("statistic_mode", false)
                }
            }
        }

    }
}