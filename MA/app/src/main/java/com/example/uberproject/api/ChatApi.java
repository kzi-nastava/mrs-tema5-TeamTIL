package com.example.uberproject.api;

import com.example.uberproject.dto.request.SendMessageRequest;
import com.example.uberproject.dto.response.ChatDTO;
import com.example.uberproject.dto.response.MessageDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ChatApi {

    // User/Driver: check if they already have a chat
    @GET("chat/my/exists")
    Call<ChatDTO> getExistingChat(@Query("email") String email);

    // Admin: get all chats
    @GET("chat/all")
    Call<List<ChatDTO>> getAllChats();

    // Get specific chat by id (with messages)
    @GET("chat/{chatId}")
    Call<ChatDTO> getChatById(@Path("chatId") int chatId);

    // User/Driver: start a chat with their first message
    @POST("chat/my/start")
    Call<ChatDTO> startChatWithMessage(
            @Query("email") String email,
            @Body SendMessageRequest request
    );

    // HTTP fallback: send message when WebSocket is not available
    @POST("chat/{chatId}/messages")
    Call<MessageDTO> sendMessageHttp(
            @Path("chatId") int chatId,
            @Body SendMessageRequest request
    );
}