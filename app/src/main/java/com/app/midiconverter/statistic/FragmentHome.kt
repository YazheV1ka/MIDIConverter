package com.app.midiconverter.statistic

import android.app.Activity
import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.app.midiconverter.R
import com.app.midiconverter.ReadDbHelper
import com.app.midiconverter.databinding.FragmentStatisticBinding
import com.app.midiconverter.uploaded.FragmentUploaded
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.time.LocalDate
import java.time.ZoneId


class FragmentHome : Fragment() {

    private fun handleSelectedFile(uri: Uri) {
        println("Selected File URI: $uri")
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex > -1) {
                    val displayName =
                        cursor.getString(columnIndex).replace("'", "").replace("\"", "")
                    println("display name : $displayName")

                    val inputStream: InputStream? = contentResolver.openInputStream(uri)

                    val newFile = createNewFile(displayName)

                    copyFile(inputStream, FileOutputStream(newFile))

                    val bundle = Bundle()
                    bundle.putString("absolute_pass", newFile.absolutePath)
                    val fragment = FragmentUploaded()
                    fragment.arguments = bundle

                    val fragmentTransaction = parentFragmentManager.beginTransaction()
                    fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
                    fragmentTransaction.addToBackStack("")
                    fragmentTransaction.commit()
                }
            }
        }
    }

    private fun createNewFile(fileName: String): File {
        val storageDir = requireContext().cacheDir
        return File.createTempFile(fileName, null, storageDir)
    }

    private fun copyFile(inputStream: InputStream?, outputStream: OutputStream) {
        if (inputStream == null) return

        try {
            val buffer = ByteArray(1024)
            var length: Int
            while (inputStream.read(buffer).also { length = it } > 0) {
                outputStream.write(buffer, 0, length)
            }
        } catch (e: IOException) {
            Log.e("FileCopy", "Error copying file", e)
        } finally {
            try {
                inputStream.close()
                outputStream.close()
            } catch (e: IOException) {
                Log.e("FileCopy", "Error closing streams", e)
            }
        }
    }

    private val binding by lazy { FragmentStatisticBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val pickFile =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedFileUri: Uri? = result.data?.data
                    selectedFileUri?.let { uri ->
                        handleSelectedFile(uri)
                    }
                }
            }

        binding.uploadBtn.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            pickFile.launch(intent)
        }


        val sharedPref =
            requireContext().getSharedPreferences("settings_property", Context.MODE_PRIVATE)

        val theme_mode = sharedPref?.getBoolean("theme_mode", false)

        if (theme_mode == true) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else if (theme_mode == false) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }

        val last_date = sharedPref.getString("last_date", "")
        val statistic_on = sharedPref.getBoolean("statistic_mode", true)
        val current_date = LocalDate.now(ZoneId.of("GMT+3"))
        val lastMonth = sharedPref.getInt("last_month", -1)
        val currentMonth = current_date.monthValue

        var lastCheckedDate = last_date
        val dbHelper = ReadDbHelper(requireContext())
        val db = dbHelper.readableDatabase

        if (statistic_on) {
            binding.statisticTxt.visibility = View.VISIBLE
            binding.cloud.visibility = View.VISIBLE
            binding.goodJobTxt.visibility = View.INVISIBLE

            val notesCursor = db.rawQuery(
                "SELECT COUNT(*) FROM NOTES_BD WHERE update_date = ?",
                arrayOf(current_date.toString())
            )
            notesCursor.moveToFirst()
            val notesCount = notesCursor.count
            notesCursor.close()

            val editor = sharedPref.edit()

            if (lastMonth != currentMonth) {
                editor.putInt("strike", 0)
                editor.putInt("last_month", currentMonth)
                editor.apply()
            }

            val updatedStrikeDay = sharedPref.getInt("strike", 0)

            if (
                notesCount > 0 &&
                !last_date.isNullOrEmpty() &&
                last_date == current_date.minusDays(1).toString() &&
                lastCheckedDate != current_date.toString()
            ) {
                editor.putInt("strike", updatedStrikeDay + 1)
                editor.putString("last_date", current_date.toString())
                editor.apply()

                binding.statisticTxt.text = "Your strike days\nthis month - ${updatedStrikeDay + 1}"
            } else {
                binding.statisticTxt.text = "Your strike days\nthis month - $updatedStrikeDay"
            }
        } else {
            binding.statisticTxt.visibility = View.INVISIBLE
            binding.cloud.visibility = View.VISIBLE
            binding.goodJobTxt.visibility = View.VISIBLE
        }
    }
}