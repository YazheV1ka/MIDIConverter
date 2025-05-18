package com.app.midiconverter

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface ApiService {
    @POST("/file/create")
    @Multipart
    fun createFile(
        @Part("metronome_speed") metronome_speed: Int,
        @Part file: MultipartBody.Part
    ): Call<ResponseBody>
}