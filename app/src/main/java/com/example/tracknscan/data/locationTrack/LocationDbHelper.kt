package com.example.tracknscan.data.locationTrack

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.tracknscan.helpers.Constants
import com.example.tracknscan.model.locationTrack.LocationDomain

class LocationDbHelper(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, Constants.Map.DB_NAME, factory, Constants.Map.DB_VERSION) {

    override fun onCreate(db: SQLiteDatabase?) {
        //sqlite query where column names + data types is given
        val query = ("CREATE TABLE  " + Constants.Map.TABLE_NAME + " (" +
                Constants.Map.ID_COLUMN + " TEXT PRIMARY KEY, " +
                Constants.Map.LAT_COLUMN + " DOUBLE," +
                Constants.Map.LON_COLUMN + " DOUBLE" + ")")

        // we are calling sqlite method for executing our query
        db?.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {
        // check if table already exists
        db?.execSQL("DROP TABLE IF EXISTS " + Constants.Map.TABLE_NAME)
        onCreate(db)
    }

    fun addLocation(location: LocationDomain){
        // creating a writable variable of the database
        // as we want to insert value in database
        val db = this.writableDatabase

        // creating a content values variable
        val values = ContentValues()

        // inserting values in form of key-value pair
        values.put(Constants.Map.ID_COLUMN, location.id)
        values.put(Constants.Map.LAT_COLUMN, location.latitude)
        values.put(Constants.Map.LON_COLUMN, location.longitude)

        // all values are inserted into database
        db.insert(Constants.Map.TABLE_NAME, null, values)

        // at last, cloe database
        db.close()
    }

    //method to delete data
    fun deleteFirstLocation(location: LocationDomain){
        val db = this.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(Constants.Map.ID_COLUMN, location.id)
        // Deleting Row
        db.execSQL("DELETE FROM " + Constants.Map.TABLE_NAME +
                    " WHERE " + Constants.Map.ID_COLUMN + "='" + location.id +"'")
        db.close() // Closing database connection
    }

    // get all data from database
    @SuppressLint("Range")
    fun getLocations(): MutableList<LocationDomain> {
        val allLocations: MutableList<LocationDomain> = mutableListOf()

        // creating a readable variable of database
        // as we want to read value from it
        val db = this.readableDatabase

        val selectALLQuery = "SELECT * FROM " + Constants.Map.TABLE_NAME
        val cursor = db.rawQuery(selectALLQuery, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    val latitude = cursor.getDouble(cursor.getColumnIndex(Constants.Map.LAT_COLUMN))
                    val longitude = cursor.getDouble(cursor.getColumnIndex(Constants.Map.LON_COLUMN))
                    val id = cursor.getString(cursor.getColumnIndex(Constants.Map.ID_COLUMN))
                    val locationInList = LocationDomain(id, latitude, longitude)

                    allLocations += locationInList
                } while (cursor.moveToNext())
            }
        }
        cursor.close()
        db.close()

        // returns a cursor to read data from the database
        return allLocations
    }

}