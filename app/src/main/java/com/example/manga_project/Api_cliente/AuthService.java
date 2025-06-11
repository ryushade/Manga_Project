package com.example.manga_project.Api_cliente;

import com.example.manga_project.Modelos.Genero;
import com.example.manga_project.Modelos.AprobarProveedorRequest;
import com.example.manga_project.Modelos.LoginRequest;
import com.example.manga_project.Modelos.LoginResponse;
import com.example.manga_project.Modelos.RegisterRequest;
import com.example.manga_project.Modelos.RegisterResponse;
import com.example.manga_project.Modelos.PerfilResponse;
import com.example.manga_project.Modelos.SoliHistorietaProveedorResponse;
import com.example.manga_project.Modelos.SolicitudPublicacionRequest;
import com.example.manga_project.Modelos.SolicitudPublicacionResponse;
import com.example.manga_project.Modelos.SolicitudResponse;
import com.example.manga_project.Modelos.SolicitudesProveedorResponse;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface AuthService {

    @POST("/auth")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    @POST("/api_registrarusuario")
    Call<RegisterResponse> register(@Body RegisterRequest registerRequest);

    @GET("/api_obtener_usuario_data")
    Call<PerfilResponse> getPerfil();

    @GET("api_obtener_proveedor")
    Call<SolicitudesProveedorResponse> obtenerSolicitudesProveedor();


    @POST("/api_aprobar_proveedor")
    Call<SolicitudResponse> aprobarProveedor(@Body AprobarProveedorRequest request);


    @PUT("/api_solicitar_proveedor")
    Call<Void> solicitarProveedor();


    @GET("/api_obtener_generos")
    Call<List<Genero>> obtenerGeneros(@Query("tipo") String tipo);

    @GET("api_obtener_mis_solicitudes")
    Call<SoliHistorietaProveedorResponse> obtenerMisSolicitudes();

    @Multipart
    @POST("/upload_portada")
    Call<ResponseBody> subirPortada(@Part MultipartBody.Part file);

    @Multipart
    @POST("/upload_zip")
    Call<ResponseBody> subirZip(@Part MultipartBody.Part file);

    @POST("/api_registrar_solicitud")
    Call<SolicitudPublicacionResponse> registrarSolicitud(@Body SolicitudPublicacionRequest request);





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
