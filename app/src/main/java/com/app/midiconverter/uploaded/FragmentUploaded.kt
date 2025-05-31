package com.app.midiconverter.uploaded

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.app.midiconverter.ApiService
import com.app.midiconverter.R
import com.app.midiconverter.ReadDbHelper
import com.app.midiconverter.databinding.FragmentUploadedBinding
import com.app.midiconverter.player.FragmentPlay
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.zip.ZipInputStream


class FragmentUploaded : Fragment() {
    private val binding by lazy { FragmentUploadedBinding.inflate(layoutInflater) }
    private val BASE_URL = "https://detail-urge-foster-ripe.trycloudflare.com"

    val apiService: ApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        retrofit.create(ApiService::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    private var file_id = -1
    private lateinit var file: File

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val receivedData = arguments?.getString("absolute_pass")
        file = File(receivedData)
        val dbHelper = ReadDbHelper(requireContext())
        val db = dbHelper.writableDatabase
        val cursorCourses = db.rawQuery("SELECT * FROM FILES_BD WHERE name = ?", arrayOf(file.name))
        cursorCourses.close()
        val name = getFileExtension(file.name.toString())

        val multiPart = MultipartBody.Part.createFormData(
            "file",
            name,
            RequestBody.create(MediaType.parse("audio/*"), file)
        )
        println("file name: ${name}")

        binding.name.setText(name)
        binding.linearProgress.setOnClickListener { }

        binding.yes.setOnClickListener {
            if (binding.name.text.toString() != "") {
                if (binding.metronomeSpeed.text.toString().isNullOrEmpty()) {
                    addRequest(multiPart, 100)
                } else {
                    addRequest(multiPart, Integer.parseInt(binding.metronomeSpeed.text.toString()))
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill all required fields",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.no.setOnClickListener {
            if (binding.name.text.toString() != "") {
                if (binding.metronomeSpeed.text.toString().isNullOrEmpty()) {
                    addRequest(multiPart, 100, false)
                } else {
                    addRequest(
                        multiPart,
                        Integer.parseInt(binding.metronomeSpeed.text.toString()),
                        false
                    )

                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill all required fields",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addStatistic() {
        val sharedPref =
            requireContext().getSharedPreferences("settings_property", Context.MODE_PRIVATE)
        val edit = sharedPref.edit()
        edit.putString("update_date", LocalDate.now(ZoneId.of("GMT+3")).toString())
        edit.apply()
        println("last date: ${LocalDate.now(ZoneId.of("GMT+3")).toString()}")
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun addRequest(file: MultipartBody.Part, metronome_speed: Int, save: Boolean = true) {

        binding.linearProgress.isVisible = true
        binding.loadingGif.isVisible = true
        binding.loadingText.isVisible = true
        binding.loadingText.text = "Rendering..."

        Glide.with(requireContext())
            .asGif()
            .load(R.drawable.rendering_gif)
            .into(binding.loadingGif)

        val call: Call<ResponseBody> = apiService.createFile(metronome_speed, file)

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseData = response.body()?.string()
                    println("response $responseData")

                    val tabLayout = requireActivity().findViewById<TabLayout>(R.id.tablayout)
                    for (i in 0 until tabLayout.tabCount) {
                        tabLayout.getTabAt(i)?.view?.isEnabled = false
                    }

                    lifecycleScope.launch(Dispatchers.Main) {
                        binding.loadingText.text = "Rendering audio file..."

                        withContext(Dispatchers.IO) {
                            Thread.sleep(40000)
                        }

                        binding.loadingText.text = "Rendering video file..."

                        withContext(Dispatchers.IO) {
                            Thread.sleep(40000)
                        }

                        if (responseData != null) {
                            processWithSuccessRequest(save, responseData)
                        }

                        binding.loadingGif.isVisible = false
                        binding.loadingText.isVisible = false
                        binding.linearProgress.isVisible = false

                        for (i in 0 until tabLayout.tabCount) {
                            tabLayout.getTabAt(i)?.view?.isEnabled = true
                        }
                    }
                } else {
                    binding.linearProgress.isVisible = false
                    Toast.makeText(requireContext(), "Cannot process file. Choose a smaller file", Toast.LENGTH_LONG)
                        .show()
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(
                    requireContext(),
                    "Something went wrong. Please check your file" + t.message,
                    Toast.LENGTH_LONG
                ).show()
                t.printStackTrace()
            }
        })
    }

    fun getFileExtension(fileName: String): String? {
        val indexOfMid = fileName.indexOf(".pdf")
        return if (indexOfMid != -1) {
            fileName.substring(0, indexOfMid)
        } else {
            fileName
        }
    }

    fun downloadFile(url: String, destinationDir: String): File {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.connect()

        val input = connection.inputStream
        val outputFile = File(destinationDir, "downloadedFile.zip")

        val output = FileOutputStream(outputFile)

        val buffer = ByteArray(1024)
        var bytesRead: Int

        while (input.read(buffer).also { bytesRead = it } != -1) {
            output.write(buffer, 0, bytesRead)
        }

        output.close()
        input.close()

        return outputFile
    }

    fun unzip(zipFile: File, destinationDir: String): List<File> {
        val extractedFiles = mutableListOf<File>()
        val buffer = ByteArray(1024)
        val zipInputStream = ZipInputStream(zipFile.inputStream())

        var zipEntry = zipInputStream.nextEntry
        while (zipEntry != null) {
            val newFile = File(destinationDir, zipEntry.name)
            extractedFiles.add(newFile)

            FileOutputStream(newFile).use { fos ->
                var len: Int
                while (zipInputStream.read(buffer).also { len = it } > 0) {
                    fos.write(buffer, 0, len)
                }
            }

            zipEntry = zipInputStream.nextEntry
        }

        zipInputStream.closeEntry()
        zipInputStream.close()

        return extractedFiles
    }

    private suspend fun processWithSuccessRequest(save: Boolean, responseData: String) {
        val file2 = requireContext().cacheDir

        val zipFile = try {
            withContext(Dispatchers.IO) {
                downloadFile(
                    "https://detail-urge-foster-ripe.trycloudflare.com/file/" + responseData,
                    file2.absolutePath
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(
                    requireContext(),
                    "Cannot process file. Server shutdown or Choose smaller file",
                    Toast.LENGTH_LONG
                ).show()
            }
            return
        }

        val extractedFiles = unzip(zipFile, file2.absolutePath)
        if (save) {
            val dbHelper = ReadDbHelper(requireContext())

            val db = dbHelper.writableDatabase
            val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            formatter.timeZone = TimeZone.getTimeZone("GMT+3")

            val formattedDateString = formatter.format(Date())

            val date: Date = formatter.parse(formattedDateString)!!
            val currentDateInMilliseconds = date.time

            val values2 = ContentValues().apply {
                put("name", binding.name.text.toString())
                put("path", file.absolutePath)
                if (binding.metronomeSpeed.text.toString().isNullOrEmpty()) {
                    put("metronome_bpm", 100)
                } else {
                    put("metronome_bpm", binding.metronomeSpeed.text.toString())
                }
                put("mp3_path", extractedFiles[0].absolutePath)
                put("mp4_path", extractedFiles[1].absolutePath)
            }

            val newRowId2 = db?.insert("FILES_BD", null, values2)

            val cursorCourses =
                db.rawQuery("SELECT * FROM FILES_BD WHERE path = '${file.absolutePath}'", null)

            if (cursorCourses.moveToFirst()) {
                do {
                    val column_id = cursorCourses.getColumnIndex("id")
                    file_id = cursorCourses.getInt(column_id)
                } while (cursorCourses.moveToNext())
            }
            cursorCourses.close()

            val values = ContentValues().apply {
                put("name", binding.name.text.toString())
                put("description", binding.description.text.toString())
                put("file_Id", file_id)
                put("update_date", currentDateInMilliseconds)
            }

            val newRowId = db?.insert("NOTES_BD", null, values)

            val fragment = FragmentPlay()
            fragment.arguments = bundleOf("file_id" to file_id)

            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
            fragmentTransaction.commit()
        } else {
            val bundle = Bundle()
            bundle.putString("mp3", extractedFiles[0].absolutePath)
            bundle.putString("mp4", extractedFiles[1].absolutePath)
            if (binding.metronomeSpeed.text.toString().isNullOrEmpty()) {
                bundle.putString("bpm", "100")
            } else {
                bundle.putString("bpm", binding.metronomeSpeed.text.toString())
            }
            parentFragmentManager.popBackStack()

            val fragment = FragmentPlay()
            fragment.arguments = bundle
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView, fragment)
            fragmentTransaction.commit()
        }
    }
}
