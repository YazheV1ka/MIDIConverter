package com.app.midiconverter.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.midiconverter.R
import com.app.midiconverter.databinding.FragmentSettingBinding

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
        binding.themeSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if(b){
                binding.themeSwitch.isChecked = true
                if (sharedPref != null) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    val edit = sharedPref.edit()
                    edit.putBoolean("theme_mode", true)
                    edit.apply()
                }
            }else{
                binding.themeSwitch.isChecked = false
                if (sharedPref != null) {
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    val edit = sharedPref.edit()
                    edit.putBoolean("theme_mode", false)
                    edit.apply()
                }
            }
        }

        binding.statisticSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if(b){
                binding.statisticSwitch.isChecked = true
                if (sharedPref != null) {
                    val edit = sharedPref.edit()
                    edit.putBoolean("statistic_mode", true)
                    edit.apply()
                }
            }else{
                binding.statisticSwitch.isChecked = false
                if (sharedPref != null) {
                    val edit = sharedPref.edit()
                    edit.putBoolean("statistic_mode", false)
                    edit.apply()
                }
            }
        }

    }
}