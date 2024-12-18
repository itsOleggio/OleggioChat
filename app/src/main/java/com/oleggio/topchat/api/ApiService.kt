package com.oleggio.topchat.api

import retrofit2.http.*

interface ApiService {
    @POST("/login")
    suspend fun login(@Body loginRequest : String) : String

    @POST("/logout")
    suspend fun logout(@Header("X-Auth-Token") token : String)

    @POST("/messages")
    suspend fun sendMessage(@Header("X-Auth-Token") token: String, @Body message: Message) : String

    @GET("/channels")
    suspend fun getChannels(): List<String>

    @GET("/users")
    suspend fun getUsers(): List<String>

    @GET("/channel/{channel}")
    suspend fun getMessages(
        @Path("channel") chatId: String?,
        @Query("lastKnownId") lastKnownId : Int = 0,
        @Query("limit") limit : Int = 20,
        @Query("reverse") reverse : Boolean = false
    ): List<Message>

    companion object NetConfig {
        const val API_URL = "https://faerytea.name:8008/"
    }
}