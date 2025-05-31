package com.app.quizz.home

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.midiconverter.R
import com.app.midiconverter.ReadDbHelper
import com.app.midiconverter.databinding.FragmentHomeBinding
import com.app.midiconverter.home.DataList
import com.app.midiconverter.home.ListAdapter
import com.app.midiconverter.uploaded.FragmentUploaded
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream


class FragmentNotes : Fragment() {

    private val binding by lazy { FragmentHomeBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dbHelper = ReadDbHelper(requireContext())
        val db = dbHelper.readableDatabase

        val cursorCourses = db.rawQuery("SELECT * FROM NOTES_BD", null)

        var listMain : ArrayList<DataList> = arrayListOf()

        if (cursorCourses.moveToFirst()) {
            do {
                val column_id = cursorCourses.getColumnIndex("id")
                val name = cursorCourses.getColumnIndex("name")
                val description = cursorCourses.getColumnIndex("description")
                val file_id = cursorCourses.getColumnIndex("file_Id")
                val update_date = cursorCourses.getColumnIndex("update_date")
                listMain.add(
                    DataList(
                        cursorCourses.getInt(column_id),
                        cursorCourses.getString(name),
                        cursorCourses.getString(description),
                        cursorCourses.getInt(file_id),
                        cursorCourses.getString(update_date)
                    )
                )
            } while (cursorCourses.moveToNext())
        }
        cursorCourses.close()

        var list: ArrayList<DataList> = arrayListOf()
        list.addAll(listMain)

        val adapter = ListAdapter(list, parentFragmentManager)

        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.searchBtn.setOnClickListener{
            val search_key = binding.search.text.toString()
            val filteredList : ArrayList<DataList> = ArrayList(
                listMain.filter{
                    it.name.lowercase().contains(search_key.lowercase()) || it.description?.lowercase()?.contains(search_key.lowercase()) == true
                }
            )
            val size = list.size
            list.clear()
            binding.recyclerView.adapter?.notifyItemRangeRemoved(0, size)
            list.addAll(filteredList)
            binding.recyclerView.adapter?.notifyItemRangeInserted(0, list.size)
        }


        binding.sort.setOnClickListener{
            val popupMenu = PopupMenu(requireContext(), binding.sort)
            popupMenu.getMenuInflater().inflate(R.menu.pop_up_items, popupMenu.getMenu())

            popupMenu.setOnMenuItemClickListener { menuItem ->
                val sortedList = if(menuItem.itemId == R.id.menu_name) {
                    ArrayList(list.sortedBy { it.name })
                } else if(menuItem.itemId == R.id.menu_description) {
                    ArrayList(list.sortedBy { it.description })
                } else {
                    ArrayList(list.sortedByDescending{it.update_date})
                }
                val size = list.size
                list.clear()
                binding.recyclerView.adapter?.notifyItemRangeRemoved(0, size)
                list.addAll(sortedList)
                binding.recyclerView.adapter?.notifyItemRangeInserted(0, list.size)
                true
            }
            popupMenu.show()
        }

        val pickFile =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val selectedFileUri: Uri? = result.data?.data
                    selectedFileUri?.let { uri ->
                        handleSelectedFile(uri)
                    }
                }
            }

        binding.addNew.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf"
            }
            pickFile.launch(intent)
        }
    }
    private fun handleSelectedFile(uri: Uri) {
        println("Selected File URI: $uri")
        val contentResolver: ContentResolver = requireContext().contentResolver
        val cursor = contentResolver.query(uri, null, null, null, null)

        cursor?.use { cursor ->
            if (cursor.moveToFirst()) {
                val columnIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (columnIndex > -1) {
                    val displayName = cursor.getString(columnIndex).replace("'","").replace("\"", "")
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
}