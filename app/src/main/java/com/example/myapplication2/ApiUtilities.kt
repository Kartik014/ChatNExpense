package com.example.myapplication2

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtilities {

    private val retrofit by lazy{
        Retrofit.Builder()
            .baseUrl("https://fcm.googleapis.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api by lazy{
        retrofit.create(ApiInterface::class.java)
    }
//    fun getInstance(): ApiInterface{
//        return Retrofit.Builder()
//            .baseUrl("https://fcm.googleapis.com")
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//            .create(ApiInterface::class.java)
//    }
}