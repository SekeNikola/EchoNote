package com.example.app.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00000\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J(\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\n\u001a\u00020\u000b2\b\b\u0001\u0010\f\u001a\u00020\rH\u00a7@\u00a2\u0006\u0002\u0010\u000e\u00a8\u0006\u000f"}, d2 = {"Lcom/example/app/network/OpenAIService;", "", "summarizeText", "Lretrofit2/Response;", "Lcom/example/app/network/GPTResponse;", "request", "Lcom/example/app/network/GPTRequest;", "(Lcom/example/app/network/GPTRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "transcribeAudio", "Lcom/example/app/network/TranscriptionResponse;", "file", "Lokhttp3/MultipartBody$Part;", "model", "Lokhttp3/RequestBody;", "(Lokhttp3/MultipartBody$Part;Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface OpenAIService {
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "/v1/audio/transcriptions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object transcribeAudio(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @retrofit2.http.Part(value = "model")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody model, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.app.network.TranscriptionResponse>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/v1/chat/completions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object summarizeText(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.app.network.GPTRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.app.network.GPTResponse>> $completion);
}