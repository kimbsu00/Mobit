package com.mobit.mobit.renew.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class DealRecordDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "deal_record.db"
        val DB_VERSION = 1
        val TABLE_NAME = "deal_record"
        val CODE = "code"

        @Volatile
        private var instance: DealRecordDatabase? = null
        fun getInstance(context: Context): DealRecordDatabase {
            if (instance == null) {
                synchronized(DealRecordDatabase::class.java) {
                    if (instance == null) {
                        instance = DealRecordDatabase(context)
                    }
                }
            }
            return instance!!
        }
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable = "create table if not exists $TABLE_NAME($CODE text primary key)"
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {

    }

}