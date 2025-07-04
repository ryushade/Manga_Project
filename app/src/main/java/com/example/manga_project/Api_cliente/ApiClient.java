package com.example.manga_project.Api_cliente;

import android.content.Context;
import android.content.SharedPreferences;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofitConToken;
    private static Retrofit retrofitSinToken;
    private static Context ctx;

    // Cambia al puerto correcto
    public static final String BASE_URL_LOCAL  = "http://192.168.1.6:5000/";
    public static final String BASE_URL_REMOTA = "https://grupo1damb.pythonanywhere.com/";
    private static String baseUrlActual         = BASE_URL_REMOTA;

    // Llama en Application o LaunchActivity
    public static void setContext(Context appCtx) {
        ctx = appCtx.getApplicationContext();
    }

    /** Interceptor que añade el JWT */
    private static Interceptor authInterceptor() {
        return chain -> {
            SharedPreferences sp = ctx.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            String token = sp.getString("token", "");
            Request req  = token.isEmpty()
                    ? chain.request()
                    : chain.request().newBuilder()
                    .header("Authorization", "Bearer " + token)
                    .build();
            return chain.proceed(req);
        };
    }

    /** Cliente **con** token */
    public static Retrofit getClientConToken() {
        if (ctx == null) throw new IllegalStateException("ApiClient.setContext() no llamado");

        if (retrofitConToken == null) {
            HttpLoggingInterceptor log = new HttpLoggingInterceptor();
            log.setLevel(HttpLoggingInterceptor.Level.BASIC); // Cambiado de BODY a BASIC para evitar OutOfMemoryError

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(authInterceptor())
                    .addInterceptor(log)         // <-- imprime petición y respuesta
                    .build();

            retrofitConToken = new Retrofit.Builder()
                    .baseUrl(baseUrlActual)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitConToken;
    }

    /** Cliente **sin** token */
    public static Retrofit getClientSinToken() {
        if (retrofitSinToken == null) {
            retrofitSinToken = new Retrofit.Builder()
                    .baseUrl(baseUrlActual)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitSinToken;
    }
}
