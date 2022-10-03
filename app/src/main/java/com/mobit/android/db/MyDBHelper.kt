package com.mobit.android.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import com.mobit.android.data.CoinAsset
import com.mobit.android.data.MainIndicator
import com.mobit.android.data.Transaction
import org.json.JSONObject

class MyDBHelper(val context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {
    companion object {
        val DB_NAME = "mobit.db"
        val DB_VERSION = 2
        val TABLE_NAME =
            arrayOf(
                "favorite",
                "krw",
                "coinAsset",
                "trade",
                "firstFlag",
                "mainIndicatorType",
                "mainIndicator"
            )

        // about favorite
        val CODE = "code"

        // about krw
        val KRW = "krw"

        // about coinAsset
        val NAME = "name"
        val NUMBER = "number"
        val AMOUNT = "amount"
        val AVERAGE_PRICE = "averagePrice"

        // about transaction
        val TRANSACTION = "trade"

        // about flag
        val FIRST_SETTING = "firstSetting"

        // about mainIndicatorType
        val MAIN_INDICATOR_TYPE = "mainIndicatorType"

        // about mainIndicator
        val MAIN_INDICATOR = "mainIndicator"
    }

    // 관심 코인을 DB에 저장
    fun insertFavoirte(code: String): Boolean {
        val values = ContentValues()
        values.put(CODE, code)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[0], null, values) > 0
        db.close()
        return ret
    }

    // 관심 코인을 DB로부터 제거
    fun deleteFavorite(code: String): Boolean {
        val strsql = "select * from ${TABLE_NAME[0]} where $CODE='$code';"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        val flag = cursor.count != 0
        if (flag) {
            cursor.moveToFirst()
            db.delete(TABLE_NAME[0], "$CODE=?", arrayOf(code))
        }
        cursor.close()
        db.close()
        return flag
    }

    // DB에 저장되어 있는 관심 코인들의 code를 반환
    fun getFavorites(): ArrayList<String> {
        val ret = ArrayList<String>()

        val strsql = "select * from ${TABLE_NAME[0]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            do {
                val code = cursor.getString(0)
                ret.add(code)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return ret
    }

    // KRW 테이블에 저장되어 있는 데이터를 krw로 변경
    fun setKRW(krw: Double): Boolean {
        clearKRW()
        return insertKRW(krw)
    }

    // KRW 테이블에 krw 데이터 추가
    fun insertKRW(krw: Double): Boolean {
        val values = ContentValues()
        values.put(KRW, krw)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[1], null, values) > 0
        db.close()
        return ret
    }

    // KRW 테이블의 데이터를 모두 제거
    fun clearKRW() {
        val strsql = "delete from ${TABLE_NAME[1]}"
        val db = writableDatabase
        db.execSQL(strsql)
    }

    // DB에 저장되어 있는 KRW 값을 반환
    fun getKRW(): Double? {
        var ret: Double? = null

        val strsql = "select * from ${TABLE_NAME[1]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            ret = cursor.getDouble(0)
        }
        cursor.close()
        db.close()
        return ret
    }

    // DB에 coinAsset을 저장
    fun insertCoinAsset(coinAsset: CoinAsset): Boolean {
        val values = ContentValues()
        values.put(CODE, coinAsset.code)
        values.put(NAME, coinAsset.name)
        values.put(NUMBER, coinAsset.number)
        values.put(AMOUNT, coinAsset.amount)
        values.put(AVERAGE_PRICE, coinAsset.averagePrice)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[2], null, values) > 0
        db.close()
        return ret
    }

    // DB에 있는 코인 자산 데이터 중에서 coinAsset에 해당하는 데이터를 삭제
    fun deleteCoinAsset(coinAsset: CoinAsset): Boolean {
        val strsql = "select * from ${TABLE_NAME[2]} where $CODE='${coinAsset.code}';"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        val flag = cursor.count != 0
        if (flag) {
            cursor.moveToFirst()
            db.delete(TABLE_NAME[2], "$CODE=?", arrayOf(coinAsset.code))
        }
        cursor.close()
        db.close()
        return flag
    }

    // DB에 있는 코인 자산 데이터를 업데이트
    fun updateCoinAsset(coinAsset: CoinAsset): Boolean {
        val strsql = "select * from ${TABLE_NAME[2]} where $CODE='${coinAsset.code}';"
        val db = writableDatabase
        val cursor = db.rawQuery(strsql, null)
        val flag = cursor.count != 0
        if (flag) {
            cursor.moveToFirst()
            val values = ContentValues()
            values.put(NUMBER, coinAsset.number)
            values.put(AMOUNT, coinAsset.amount)
            values.put(AVERAGE_PRICE, coinAsset.averagePrice)
            db.update(TABLE_NAME[2], values, "$CODE=?", arrayOf(coinAsset.code))
        }
        cursor.close()
        db.close()
        return flag
    }

    // DB에 저장되어 있는 코인 자산 정보를 반환
    fun getCoinAssets(): ArrayList<CoinAsset> {
        val ret = ArrayList<CoinAsset>()

        val strsql = "select * from ${TABLE_NAME[2]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            do {
                val code = cursor.getString(0)
                val name = cursor.getString(1)
                val number = cursor.getDouble(2)
                val amount = cursor.getDouble(3)
                val averagePrice = cursor.getDouble(4)
                ret.add(CoinAsset(code, name, number, amount, averagePrice))
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        return ret
    }

    fun findCoinAsset(code: String): Boolean {
        val strsql = "select * from ${TABLE_NAME[2]} where $CODE='$code';"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        val ret = cursor.count != 0
        cursor.close()
        db.close()
        return ret
    }

    // DB에 trasaction을 JSONObject 형태의 string으로 저장
    fun insertTransaction(transaction: Transaction): Boolean {
        val values = ContentValues()
        val json: String = createJSONObject(transaction)
        values.put(TRANSACTION, json)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[3], null, values) > 0
        db.close()
        return ret
    }

    // DB에 저장되어 있는 거래내역 정보를 반환
    fun getTransactions(): ArrayList<Transaction> {
        val ret = ArrayList<Transaction>()

        val strsql = "select * from ${TABLE_NAME[3]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            do {
                val jsonObject = cursor.getString(0)
                val transaction = createTransactionFromJSONObject(jsonObject)
                ret.add(transaction)
            } while (cursor.moveToNext())
        }
        cursor.close()
        db.close()

        ret.reverse()
        return ret
    }

    // Transaction 객체 하나를 JSONObject 형태로 바꾸고, 이를 String 타입으로 리턴한다.
    fun createJSONObject(transaction: Transaction): String {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("code", transaction.code)
        jsonObject.put("name", transaction.name)
        jsonObject.put("time", transaction.time)
        jsonObject.put("type", transaction.type)
        jsonObject.put("quantity", transaction.quantity)
        jsonObject.put("unitPrice", transaction.unitPrice)
        jsonObject.put("tradePrice", transaction.tradePrice)
        jsonObject.put("fee", transaction.fee)
        jsonObject.put("totalPrice", transaction.totalPrice)
        return jsonObject.toString()
    }

    fun createTransactionFromJSONObject(transaction: String): Transaction {
        val jsonObject: JSONObject = JSONObject(transaction)
        val code = jsonObject.getString("code")
        val name = jsonObject.getString("name")
        val time = jsonObject.getString("time")
        val type = jsonObject.getInt("type")
        val quantity = jsonObject.getDouble("quantity")
        val unitPrice = jsonObject.getDouble("unitPrice")
        val tradePrice = jsonObject.getDouble("tradePrice")
        val fee = jsonObject.getDouble("fee")
        val totalPrice = jsonObject.getDouble("totalPrice")
        return Transaction(code, name, time, type, quantity, unitPrice, tradePrice, fee, totalPrice)
    }

    fun setFlag(flag: Boolean): Boolean {
        val values = ContentValues()
        val num = if (flag) 1 else 0
        values.put(FIRST_SETTING, num)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[4], null, values) > 0
        db.close()
        return ret
    }

    fun getFlag(): Boolean {
        var ret = false

        val strsql = "select * from ${TABLE_NAME[4]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            val num = cursor.getInt(0)
            Log.i("getFlag num", num.toString())
            ret = if (num == 1) true else false
        }
        cursor.close()
        db.close()

        return ret
    }

    fun clearDB(): Boolean {
        val strsql1 = "delete from ${TABLE_NAME[0]}"
        val strsql2 = "delete from ${TABLE_NAME[1]}"
        val strsql3 = "delete from ${TABLE_NAME[2]}"
        val strsql4 = "delete from ${TABLE_NAME[3]}"
        val strsql5 = "delete from ${TABLE_NAME[4]}"
        val strsql6 = "delete from ${TABLE_NAME[5]}"

        val db = writableDatabase
        db.execSQL(strsql1)
        db.execSQL(strsql2)
        db.execSQL(strsql3)
        db.execSQL(strsql4)
        db.execSQL(strsql5)
        db.execSQL(strsql6)

        return true
    }

    // mainIndicatorType 테이블에 저장되어 있는 데이터를 mainIndicatorType 변경
    fun setMainIndicatorType(mainIndicatorType: Int): Boolean {
        clearMainIndicatorType()
        return insertMainIndicatorType(mainIndicatorType)
    }

    // KRW 테이블에 krw 데이터 추가
    fun insertMainIndicatorType(mainIndicatorType: Int): Boolean {
        val values = ContentValues()
        values.put(MAIN_INDICATOR_TYPE, mainIndicatorType)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[5], null, values) > 0
        db.close()
        return ret
    }

    // mainIndicatorType 테이블의 데이터를 모두 제거
    fun clearMainIndicatorType() {
        val strsql = "delete from ${TABLE_NAME[5]}"
        val db = writableDatabase
        db.execSQL(strsql)
    }

    fun getMainIndicatorType(): Int {
        var ret: Int = -1

        val strsql = "select * from ${TABLE_NAME[5]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            val num = cursor.getInt(0)
            ret = num
        }
        cursor.close()
        db.close()

        return ret
    }

    fun setMainIndicator(mainIndicator: MainIndicator): Boolean {
        clearMainIndicator()
        return insertMainIndicator(mainIndicator)
    }

    fun insertMainIndicator(mainIndicator: MainIndicator): Boolean {
        val jsonObject: JSONObject = JSONObject()
        jsonObject.put("MA_N1", mainIndicator.MA_N1)
        jsonObject.put("MA_N2", mainIndicator.MA_N2)
        jsonObject.put("MA_N3", mainIndicator.MA_N3)
        jsonObject.put("MA_N4", mainIndicator.MA_N4)
        jsonObject.put("MA_N5", mainIndicator.MA_N5)
        jsonObject.put("BB_N", mainIndicator.BB_N)
        jsonObject.put("BB_K", mainIndicator.BB_K)
        jsonObject.put("DBT_1", mainIndicator.DBT_1)
        jsonObject.put("DBT_2", mainIndicator.DBT_2)
        jsonObject.put("DBT_3", mainIndicator.DBT_3)
        jsonObject.put("DBT_4", mainIndicator.DBT_4)
        jsonObject.put("DBT_5", mainIndicator.DBT_5)
        jsonObject.put("ENV_N", mainIndicator.ENV_N)
        jsonObject.put("ENV_K", mainIndicator.ENV_K)
        jsonObject.put("PC_N", mainIndicator.PC_N)
        val jsonString: String = jsonObject.toString()

        val values = ContentValues()
        values.put(MAIN_INDICATOR, jsonString)

        val db = writableDatabase
        val ret = db.insert(TABLE_NAME[6], null, values) > 0
        db.close()
        return ret
    }

    fun clearMainIndicator() {
        val strsql = "delete from ${TABLE_NAME[6]}"
        val db = writableDatabase
        db.execSQL(strsql)
    }

    fun getMainIndicator(): MainIndicator {
        val ret: MainIndicator = MainIndicator()

        val strsql = "select * from ${TABLE_NAME[6]};"
        val db = readableDatabase
        val cursor = db.rawQuery(strsql, null)
        cursor.moveToFirst()
        if (cursor.count != 0) {
            val jsonString: String = cursor.getString(0)
            val jsonObject: JSONObject = JSONObject(jsonString)
            ret.MA_N1 = jsonObject.getInt("MA_N1")
            ret.MA_N2 = jsonObject.getInt("MA_N2")
            ret.MA_N3 = jsonObject.getInt("MA_N3")
            ret.MA_N4 = jsonObject.getInt("MA_N4")
            ret.MA_N5 = jsonObject.getInt("MA_N5")
            ret.BB_N = jsonObject.getInt("BB_N")
            ret.BB_K = jsonObject.getDouble("BB_K").toFloat()
            ret.DBT_1 = jsonObject.getInt("DBT_1")
            ret.DBT_2 = jsonObject.getInt("DBT_2")
            ret.DBT_3 = jsonObject.getInt("DBT_3")
            ret.DBT_4 = jsonObject.getInt("DBT_4")
            ret.DBT_5 = jsonObject.getInt("DBT_5")
            ret.ENV_N = jsonObject.getInt("ENV_N")
            ret.ENV_K = jsonObject.getInt("ENV_K")
            ret.PC_N = jsonObject.getInt("PC_N")
        }
        cursor.close()
        db.close()

        return ret
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTable1 = "create table if not exists ${TABLE_NAME[0]}($CODE text primary key)"
        val createTable2 =
            "create table if not exists ${TABLE_NAME[1]}($KRW real)"
        val createTable3 =
            "create table if not exists ${TABLE_NAME[2]}($CODE text primary key, $NAME text, $NUMBER real, $AMOUNT real, $AVERAGE_PRICE real)"
        val createTable4 = "create table if not exists ${TABLE_NAME[3]}($TRANSACTION text)"
        val createTable5 = "create table if not exists ${TABLE_NAME[4]}($FIRST_SETTING INTEGER)"
        val createTable6 =
            "create table if not exists ${TABLE_NAME[5]}($MAIN_INDICATOR_TYPE INTEGER)"
        val createTable7 = "create table if not exists ${TABLE_NAME[6]}($MAIN_INDICATOR text)"
        db?.execSQL(createTable1)
        db?.execSQL(createTable2)
        db?.execSQL(createTable3)
        db?.execSQL(createTable4)
        db?.execSQL(createTable5)
        db?.execSQL(createTable6)
        db?.execSQL(createTable7)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val createTable6 =
            "create table if not exists ${TABLE_NAME[5]}($MAIN_INDICATOR_TYPE INTEGER)"
        val createTable7 = "create table if not exists ${TABLE_NAME[6]}($MAIN_INDICATOR text)"
        db?.execSQL(createTable6)
        db?.execSQL(createTable7)
    }

}