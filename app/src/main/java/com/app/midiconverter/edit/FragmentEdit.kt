package com.app.midiconverter.edit

import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.app.midiconverter.R
import com.app.midiconverter.ReadDbHelper
import com.app.midiconverter.databinding.FragmentEditBinding
import com.app.quizz.home.FragmentNotes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FragmentEdit : Fragment() {
    private val binding by lazy { FragmentEditBinding.inflate(layoutInflater) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val file_id = arguments?.getInt("file_id")
        println("file_id: $file_id")

        val dbHelper = ReadDbHelper(requireContext())
        val db = dbHelper.writableDatabase

        val cursorCourses = db.rawQuery("SELECT * FROM NOTES_BD WHERE file_Id = '$file_id'", null)

        if (cursorCourses.moveToFirst()) {
            do {
                val name = cursorCourses.getColumnIndex("name")
                println("name: $name")
                val descr = cursorCourses.getColumnIndex("description")
                val dateIndex = cursorCourses.getColumnIndex("update_date")
                val dateMillisString = cursorCourses.getString(dateIndex)

                if (dateMillisString != null) {
                    try {
                        val dateMillis = dateMillisString.toLong()
                        val date = Date(dateMillis)

                        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                        formatter.timeZone = TimeZone.getTimeZone("GMT+3")
                        val formattedDate = formatter.format(date)

                        binding.updateDate.text = "Update Date: $formattedDate"
                    } catch (e: Exception) {
                        binding.updateDate.text = "Update Date: "
                        e.printStackTrace()
                    }
                }
                binding.name.setText(cursorCourses.getString(name))
                binding.description.setText(cursorCourses.getString(descr))
            } while (cursorCourses.moveToNext())
        }
        cursorCourses.close()

        val cursorCourses2 = db.rawQuery("SELECT * FROM FILES_BD WHERE id = '$file_id'", null)
        if (cursorCourses2.moveToFirst()) {
            do {
                val bpm = cursorCourses2.getColumnIndex("metronome_bpm")

                binding.speed.setText(cursorCourses2.getInt(bpm).toString())
            } while (cursorCourses.moveToNext())
        }
        cursorCourses.close()

        binding.save.setOnClickListener {
            val newName = binding.name.text.toString()
            if (newName.isNotEmpty()) {
                val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                formatter.timeZone = TimeZone.getTimeZone("GMT+3")
                val date = Date().time

                val values = ContentValues().apply {
                    put("name", newName)
                    put("description", binding.description.text.toString())
                    put("update_date", date.toString())
                }
                val values2 = ContentValues().apply {
                    if(binding.speed.text.toString().isNullOrEmpty()) {
                        put("metronome_bpm", 100)
                    }
                    else {
                        put("metronome_bpm", binding.speed.text.toString())
                    }
                }
                db.update(
                    "NOTES_BD",
                    values,
                    "file_id = ?",
                    arrayOf(file_id.toString())
                )
                db.update(
                    "FILES_BD",
                    values2,
                    "id = ?",
                    arrayOf(file_id.toString())
                )
                println("Data with name $newName successfully updated.")
                val fragmentTransaction = parentFragmentManager.beginTransaction()
                fragmentTransaction.replace(R.id.fragmentContainerView, FragmentNotes())
                fragmentTransaction.commit()

            }else{
                Toast.makeText(requireContext(), "Please fill all required fields", Toast.LENGTH_LONG).show()
            }
        }
        binding.delete.setOnClickListener {
            db.delete("NOTES_BD",
                "file_id = ?",
                arrayOf(file_id.toString())
            )
            Toast.makeText(requireContext(), "Deleted", Toast.LENGTH_LONG).show()
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView, FragmentNotes())
            fragmentTransaction.commit()
        }
    }
}