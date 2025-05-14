package com.example.manga_project.Api_cliente;

import com.example.manga_project.Modelos.LoginRequest;
import com.example.manga_project.Modelos.LoginResponse;
import com.example.manga_project.Modelos.RegisterRequest;
import com.example.manga_project.Modelos.RegisterResponse;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface AuthService {

    @POST("/auth")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api_registrarusuario")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @GET("/api_libro")
    Call<ApiResponse> obtenerLibros();

    @GET("/api_libros_mas_vendidos")
    Call<ApiResponse> obtenerLibrosMasVendidos();

    @GET("/api_libros_antiguos")
    Call<ApiResponse> obtenerLibrosAntiguos();

    @GET("/api_libros_actuales")
    Call<ApiResponse> obtenerLibrosActuales();

    @GET("/api_mejores_autores_libros")
    Call<AutoresApiResponse> obtenerMejoresAutoresLibros();

    @POST("/payment-sheet")
    Call<PaymentIntentResponse> createPaymentIntent(@Body PaymentIntentRequest request);

    @GET("api_libro/{isbn_lib}")
    Call<ApiResponse_L> getLibro(@Path("isbn_lib") String isbnLib);

    @GET("/api_libros_comprados/{email_user}")
    Call<ApiResponse> obtenerLibrosComprados(@Path("email_user") String emailUser);


}
