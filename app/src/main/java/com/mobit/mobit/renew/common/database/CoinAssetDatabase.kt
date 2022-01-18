package com.mobit.mobit.renew.common.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CoinAssetDatabase(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_NAME = "coin_asset.db"
        val DB_VERSION = 1
        val TABLE_NAME = "coin_asset"
        val CODE = "code"

        @Volatile
        private var instance: CoinAssetDatabase? = null
        fun getInstance(context: Context): CoinAssetDatabase {
            if (instance == null) {
                synchronized(CoinAssetDatabase::class.java) {
                    if (instance == null) {
                        instance = CoinAssetDatabase(context)
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