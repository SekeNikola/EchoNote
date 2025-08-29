package com.example.app.network;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import android.content.Context;
import com.example.app.util.ApiKeyProvider;

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000,\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\b\u0010\u000b\u001a\u00020\fH\u0002J\u000e\u0010\r\u001a\u00020\u000e2\u0006\u0010\u000f\u001a\u00020\u0010R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u0011\u0010\u0005\u001a\u00020\u00068F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\bR\u0010\u0010\t\u001a\u0004\u0018\u00010\u0004X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\n\u001a\u0004\u0018\u00010\u0006X\u0082\u000e\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0011"}, d2 = {"Lcom/example/app/network/RetrofitInstance;", "", "()V", "BASE_URL", "", "api", "Lcom/example/app/network/OpenAIService;", "getApi", "()Lcom/example/app/network/OpenAIService;", "apiKey", "apiService", "getClient", "Lokhttp3/OkHttpClient;", "init", "", "context", "Landroid/content/Context;", "app_debug"})
public final class RetrofitInstance {
    @org.jetbrains.annotations.NotNull()
    private static final java.lang.String BASE_URL = "https://api.openai.com";
    @org.jetbrains.annotations.Nullable()
    private static java.lang.String apiKey;
    @org.jetbrains.annotations.Nullable()
    private static com.example.app.network.OpenAIService apiService;
    @org.jetbrains.annotations.NotNull()
    public static final com.example.app.network.RetrofitInstance INSTANCE = null;
    
    private RetrofitInstance() {
        super();
    }
    
    public final void init(@org.jetbrains.annotations.NotNull()
    android.content.Context context) {
    }
    
    private final okhttp3.OkHttpClient getClient() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull()
    public final com.example.app.network.OpenAIService getApi() {
        return null;
    }
}