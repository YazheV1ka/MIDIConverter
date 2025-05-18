package com.app.midiconverter.player

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.FileDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.app.midiconverter.R
import com.app.midiconverter.ReadDbHelper
import com.app.midiconverter.databinding.FragmentPlayBinding
import com.app.quizz.home.FragmentHome
import java.time.LocalDate


class FragmentPlay : Fragment() {
    private val binding by lazy { FragmentPlayBinding.inflate(layoutInflater) }
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(UnstableApi::class)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val file_id = arguments?.getInt("file_id", -1)
        var mp3 = ""
        var mp4 = ""
        val dbHelper = ReadDbHelper(requireContext())
        val db = dbHelper.writableDatabase

        //прибери
        addStatistic()

        /*binding.back.setOnClickListener{
            parentFragmentManager.popBackStack()
        }*/

        //check it
        binding.back.setOnClickListener{
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView, FragmentHome())
            fragmentTransaction.commit()
        }

        if(file_id == -1){
            val bpm = arguments?.getString("bpm")
            mp3 = arguments?.getString("mp3").toString()
            mp4 = arguments?.getString("mp4").toString()
            binding.speedTxt.text = "Metronome speed: ${bpm}c"
        }else {

            val cursorCourses2 = db.rawQuery("SELECT * FROM FILES_BD WHERE id = '$file_id'", null)

            if (cursorCourses2.moveToFirst()) {
                do {
                    val metronome_bpm = cursorCourses2.getColumnIndex("metronome_bpm")
                    val mp3_path = cursorCourses2.getColumnIndex("mp3_path")
                    val mp4_path = cursorCourses2.getColumnIndex("mp4_path")

                    mp3 = cursorCourses2.getString(mp3_path)
                    mp4 = cursorCourses2.getString(mp4_path)

                    binding.speedTxt.text =
                        "Metronome speed: ${cursorCourses2.getInt(metronome_bpm)}c"


                } while (cursorCourses2.moveToNext())
            }
            cursorCourses2.close()
        }

        val currentDateInMillis: Long = System.currentTimeMillis()
        val values2 = ContentValues().apply {
            put("update_date", currentDateInMillis.toString())
        }
        db.update("NOTES_BD",
            values2,
            "file_id = ?",
            arrayOf(file_id.toString()) )

        val player = ExoPlayer.Builder(requireContext()).build()
        binding.playerView.player = player
        println("MP4 : $mp4")
        player.addMediaSource(
            ProgressiveMediaSource.Factory(FileDataSource.Factory())
                .createMediaSource(MediaItem.fromUri(mp4)))
        player.prepare()
        player.play()
        binding.switcher.setOnCheckedChangeListener { compoundButton, b ->
            player.stop()
            when (b) {
                true -> player.replaceMediaItem(0, MediaItem.fromUri(mp3))

                false -> player.replaceMediaItem(0, MediaItem.fromUri(mp4))
            }
            player.prepare()
            player.play()
        }
    }

    //check
    @RequiresApi(Build.VERSION_CODES.O)
    fun addStatistic(){
        val sharedPref = requireContext().getSharedPreferences("settings_property", Context.MODE_PRIVATE)

        val last_date = sharedPref.getString("last_date", "")
        val current_date = LocalDate.now()
        val strike_day = sharedPref.getInt("strike", 1)

        if (!last_date.isNullOrEmpty() && last_date == current_date.minusDays(1).toString()
        ) {
            val edit = sharedPref.edit()
            edit.putInt("strike", strike_day + 1)
            edit.putString("last_date", current_date.toString())
            edit.apply()
        } else {
            if (!last_date.isNullOrEmpty() && !last_date.equals(current_date.toString())) {
                val edit = sharedPref.edit()
                edit.putInt("strike", 1)
                edit.apply()
            } else if (!last_date.isNullOrEmpty() && last_date.equals(current_date.toString())) {
                val edit = sharedPref.edit()
                edit.putInt("strike", strike_day)
                edit.apply()
            } else if(last_date.isNullOrEmpty()) {
                val edit = sharedPref.edit()
                edit.putInt("strike", 1)
                edit.apply()
            }
        }

        val edit = sharedPref.edit()
        edit.putString("last_date", LocalDate.now().toString())
        edit.apply()
    }

    override fun onDestroyView() {
        binding.playerView.player?.stop()
        binding.playerView.player?.release()
        super.onDestroyView()
    }

}