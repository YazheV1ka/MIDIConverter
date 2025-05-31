package com.app.midiconverter.player

import android.content.ContentValues
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
import com.app.quizz.home.FragmentNotes
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


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

        binding.back.setOnClickListener{
            val fragmentTransaction = parentFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragmentContainerView, FragmentNotes())
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

        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("GMT+3")

        val formattedDateString = formatter.format(Date())

        val date: Date = formatter.parse(formattedDateString)!!

        val currentDateInMillis: Long = date.time
        val values2 = ContentValues().apply {
            put("update_date", currentDateInMillis)
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

    override fun onDestroyView() {
        binding.playerView.player?.stop()
        binding.playerView.player?.release()
        super.onDestroyView()
    }

}