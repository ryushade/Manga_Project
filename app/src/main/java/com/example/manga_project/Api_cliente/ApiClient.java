package com.example.manga_project.Api_cliente;

import android.content.Context;
import android.content.SharedPreferences;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import java.io.IOException;

public class ApiClient {

    private static Retrofit retrofitSinToken = null;
    private static Retrofit retrofitConToken = null;
    private static Context context;

    // URLs para local y remoto
    private static final String BASE_URL_LOCAL = "http://10.0.2.2:5000/";
    private static final String BASE_URL_REMOTA = "https://grupo1damb.pythonanywhere.com/";

    private static String baseUrlActual = BASE_URL_REMOTA; // Se puede cambiar dinámicamente

    public static void setContext(Context appContext) {
        context = appContext.getApplicationContext();
    }

    // Llamar esto en el inicio de la app o desde preferencias
    public static void usarBackendLocal(boolean usarLocal) {
        baseUrlActual = usarLocal ? BASE_URL_LOCAL : BASE_URL_REMOTA;
        retrofitSinToken = null;  // Reinicia los clientes para que usen la nueva URL
        retrofitConToken = null;
    }

    private static Interceptor getAuthInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (context != null) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null);
                    if (token != null) {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token)
                                .build();
                        return chain.proceed(newRequest);
                    }
                }
                return chain.proceed(chain.request());
            }
        };
    }

    public static Retrofit getClientSinToken() {
        if (retrofitSinToken == null) {
            retrofitSinToken = new Retrofit.Builder()
                    .baseUrl(baseUrlActual)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitSinToken;
    }

    public static Retrofit getClientConToken() {
        if (retrofitConToken == null && context != null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(getAuthInterceptor())
                    .build();

            retrofitConToken = new Retrofit.Builder()
                    .baseUrl(baseUrlActual)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else if (context == null) {
            throw new IllegalStateException("El contexto no ha sido inicializado.");
        }
        return retrofitConToken;
    }
}
