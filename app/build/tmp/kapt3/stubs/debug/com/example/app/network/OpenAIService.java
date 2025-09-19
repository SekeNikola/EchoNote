package com.example.app.network;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Response;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000D\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J\u001e\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00a2\u0006\u0002\u0010\u0007J\u001e\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\nH\u00a7@\u00a2\u0006\u0002\u0010\u000bJ@\u0010\f\u001a\b\u0012\u0004\u0012\u00020\r0\u00032\b\b\u0001\u0010\u000e\u001a\u00020\u000f2\b\b\u0001\u0010\u0010\u001a\u00020\u00112\n\b\u0003\u0010\u0012\u001a\u0004\u0018\u00010\u00112\n\b\u0003\u0010\u0013\u001a\u0004\u0018\u00010\u0011H\u00a7@\u00a2\u0006\u0002\u0010\u0014J\u0014\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\u00160\u0003H\u00a7@\u00a2\u0006\u0002\u0010\u0017\u00a8\u0006\u0018"}, d2 = {"Lcom/example/app/network/OpenAIService;", "", "generateSpeech", "Lretrofit2/Response;", "Lokhttp3/ResponseBody;", "request", "Lcom/example/app/network/TTSRequest;", "(Lcom/example/app/network/TTSRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "summarizeText", "Lcom/example/app/network/GPTResponse;", "Lcom/example/app/network/GPTRequest;", "(Lcom/example/app/network/GPTRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "transcribeAudio", "Lcom/example/app/network/TranscriptionResponse;", "file", "Lokhttp3/MultipartBody$Part;", "model", "Lokhttp3/RequestBody;", "language", "prompt", "(Lokhttp3/MultipartBody$Part;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lokhttp3/RequestBody;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "validateApiKey", "Lcom/example/app/network/ModelsResponse;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface OpenAIService {
    
    @retrofit2.http.Multipart()
    @retrofit2.http.POST(value = "/v1/audio/transcriptions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object transcribeAudio(@retrofit2.http.Part()
    @org.jetbrains.annotations.NotNull()
    okhttp3.MultipartBody.Part file, @retrofit2.http.Part(value = "model")
    @org.jetbrains.annotations.NotNull()
    okhttp3.RequestBody model, @retrofit2.http.Part(value = "language")
    @org.jetbrains.annotations.Nullable()
    okhttp3.RequestBody language, @retrofit2.http.Part(value = "prompt")
    @org.jetbrains.annotations.Nullable()
    okhttp3.RequestBody prompt, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.app.network.TranscriptionResponse>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/v1/chat/completions")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object summarizeText(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.app.network.GPTRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.app.network.GPTResponse>> $completion);
    
    @retrofit2.http.GET(value = "/v1/models")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object validateApiKey(@org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<com.example.app.network.ModelsResponse>> $completion);
    
    @retrofit2.http.Headers(value = {"Content-Type: application/json"})
    @retrofit2.http.POST(value = "/v1/audio/speech")
    @org.jetbrains.annotations.Nullable()
    public abstract java.lang.Object generateSpeech(@retrofit2.http.Body()
    @org.jetbrains.annotations.NotNull()
    com.example.app.network.TTSRequest request, @org.jetbrains.annotations.NotNull()
    kotlin.coroutines.Continuation<? super retrofit2.Response<okhttp3.ResponseBody>> $completion);
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 3, xi = 48)
    public static final class DefaultImpls {
    }
}