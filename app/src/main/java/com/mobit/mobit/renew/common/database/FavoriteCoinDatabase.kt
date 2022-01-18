package com.mobit.mobit.renew.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FavoriteCoinDatabase(val context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "favorite_coin.db"
        val DB_VERSION = 1
        val TABLE_NAME = "favorite_coin"
        val CODE = "code"

        @Volatile
        private var instance: FavoriteCoinDatabase? = null
        fun getInstance(context: Context): FavoriteCoinDatabase {
            if (instance == null) {
                synchronized(FavoriteCoinDatabase::class.java) {
                    if (instance == null) {
                        instance = FavoriteCoinDatabase(context)
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