package com.app.midiconverter.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.RecyclerView
import com.app.midiconverter.R
import com.app.midiconverter.databinding.ItemBinding
import com.app.midiconverter.edit.FragmentEdit
import com.app.midiconverter.player.FragmentPlay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class ListAdapter(private val itemList: ArrayList<DataList>, val parentFragmentManager: FragmentManager) : RecyclerView.Adapter<ListAdapter.MyViewHolder>(){

    class MyViewHolder(private val binding : ItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item : DataList, parentFragmentManager: FragmentManager) {

            binding.listen.setOnClickListener {
                val bundle = bundleOf("file_id" to item.file_id)
                val fragment = FragmentPlay()
                fragment.arguments = bundle
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
                fragmentTransaction.addToBackStack("")
                fragmentTransaction.commit()
            }

            binding.custom.setOnClickListener {
                val bundle = bundleOf("file_id" to item.file_id)
                println("file_id: ${item.file_id}")
                val fragment = FragmentEdit()
                fragment.arguments = bundle
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
                fragmentTransaction.addToBackStack("")
                fragmentTransaction.commit()
            }
            binding.title.text = item.name
            binding.description.text = item.description

            val date = Date(item.update_date!!.toLong())
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("GMT+3")
            binding.updateDate.text = formatter.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ItemBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(itemList.get(position),parentFragmentManager)
    }
}