package com.app.midiconverter.home

data class DataList(
    val id : Int,
    val name : String = "Note$id",
    val description : String? = null,
    val file_id : Int,
    val update_date : String? = null
) {
}