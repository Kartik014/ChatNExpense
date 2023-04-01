package com.example.myapplication2

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiInterface {

    @Headers("Authorization:key=AAAAJA0Mg_s:APA91bH2ukrfGj4lN2fGxtYxEIg_U0BI7zUl8FLroMceOs57EYo8bb9hhCbp8DzKxWL04sLy9K6hFoCMDWIn2xqN7iymZLglpi_74_-nOCYKiR_W_jl57o7xwcFRyG37UO2v7McGmBfL","Content-Type:application/json")
// TODO content-ype
        @POST("fcm/send")
    suspend fun sendNotification(@Body notification: PushNotification):Response<ResponseBody>
}