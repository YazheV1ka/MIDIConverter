package com.app.midiconverter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class ReadDbHelper (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val SQL_CREATE_ENTRIES =
        "CREATE TABLE NOTES_BD (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "description TEXT, " +
                "file_Id INTEGER, " +
                "update_date TEXT)"

    private val SQL_CREATE_FILES =
        "CREATE TABLE FILES_BD (" +
                "id INTEGER PRIMARY KEY," +
                "name TEXT," +
                "path TEXT, " +
                "metronome_bpm INTEGER, " +
                "mp3_path TEXT, " +
                "mp4_path TEXT)"

    private val SQL_STATISTIC =
        "CREATE TABLE STATISTIC_DB (" +
                "id INTEGER PRIMARY KEY," +
                "strike_days INTEGER," +
                "update_date TEXT)"

    private val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS NOTES_BD"

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_ENTRIES)
        db.execSQL(SQL_CREATE_FILES)
        db.execSQL(SQL_STATISTIC)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(SQL_DELETE_ENTRIES)
        onCreate(db)
    }
    override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        onUpgrade(db, oldVersion, newVersion)
    }
    companion object {
        const val DATABASE_VERSION = 1
        const val DATABASE_NAME = "Notes.db"
    }
}