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

    private static final String BASE_URL = "https://grupo1damb.pythonanywhere.com/";

    private static Retrofit retrofitSinToken = null;
    private static Retrofit retrofitConToken = null;
    private static Context context;  // Agregar variable estática para el contexto

    // Establecer el contexto de la aplicación
    public static void setContext(Context appContext) {
        context = appContext.getApplicationContext();  // Se asegura de obtener el contexto de la aplicación
    }

    // Interceptor para agregar el token al encabezado Authorization
    private static Interceptor getAuthInterceptor() {
        return new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                if (context != null) {
                    SharedPreferences sharedPreferences = context.getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    String token = sharedPreferences.getString("token", null); // Obtener el token guardado

                    if (token != null) {
                        Request newRequest = chain.request().newBuilder()
                                .addHeader("Authorization", "Bearer " + token) // Agregar el token al encabezado
                                .build();
                        return chain.proceed(newRequest);
                    }
                }
                return chain.proceed(chain.request()); // No agregar token si no hay token
            }
        };
    }

    // Método para obtener el cliente Retrofit sin token
    public static Retrofit getClientSinToken() {
        if (retrofitSinToken == null) {
            retrofitSinToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofitSinToken;
    }

    // Método para obtener el cliente Retrofit con token
    public static Retrofit getClientConToken() {
        if (retrofitConToken == null && context != null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(getAuthInterceptor()) // Aplicar el Interceptor para agregar el token
                    .build();

            retrofitConToken = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client) // Usar el cliente con el interceptor
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        } else if (context == null) {
            // Aquí podrías lanzar una excepción o manejar el caso de alguna otra manera
            throw new IllegalStateException("El contexto no ha sido inicializado.");
        }
        return retrofitConToken;
    }
}
