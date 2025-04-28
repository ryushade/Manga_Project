package com.example.ebook_proyect.Api_cliente;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface DniRucApiService {
    @GET("dni/{dni}")
    Call<DniResponse> getUserByDni(
            @Path("dni") String dni,
            @Query("token") String token
    );
}
