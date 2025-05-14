package com.example.manga_project.Api_cliente;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiReniec {
    private static final String BASE_URL = "https://dniruc.apisperu.com/api/v1/";
    private static Retrofit retrofit = null;

    public static DniRucApiService getApiService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(DniRucApiService.class);
    }
}
