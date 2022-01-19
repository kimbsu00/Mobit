package com.mobit.mobit.renew.model.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class KRWDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "krw.db"
        val DB_VERSION = 1
        val TABLE_NAME = "krw"
        val KRW = "krw"

        @Volatile
        private var instance: KRWDatabase? = null
        fun getInstance(context: Context): KRWDatabase {
            if (instance == null) {
                synchronized(KRWDatabase::class.java) {
                    if (instance == null) {
                        instance = KRWDatabase(context)
                    }
                }
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "create table if not exists $TABLE_NAME($KRW real)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}