package com.example.manga_project.Api_cliente;

import com.example.manga_project.Modelos.CapituloResponse;
import com.example.manga_project.Modelos.CrearComentarioResponse;
import com.example.manga_project.Modelos.Genero;
import com.example.manga_project.Modelos.AprobarProveedorRequest;
import com.example.manga_project.Modelos.AprobarPublicacionRequest;
import com.example.manga_project.Modelos.LoginRequest;
import com.example.manga_project.Modelos.LoginResponse;
import com.example.manga_project.Modelos.PaginaResponse;
import com.example.manga_project.Modelos.RechazarProveedorRequest;
import com.example.manga_project.Modelos.RegisterRequest;
import com.example.manga_project.Modelos.RegisterResponse;
import com.example.manga_project.Modelos.PerfilResponse;
import com.example.manga_project.Modelos.SoliHistorietaProveedorResponse;
import com.example.manga_project.Modelos.Solicitud;
import com.example.manga_project.Modelos.SolicitudDetalle;
import com.example.manga_project.Modelos.SolicitudPublicacionRequest;
import com.example.manga_project.Modelos.SolicitudPublicacionResponse;
import com.example.manga_project.Modelos.SolicitudResponse;
import com.example.manga_project.Modelos.SolicitudesProveedorResponse;
import com.example.manga_project.Modelos.VolumenResponse;
import com.example.manga_project.Modelos.FichaVolumenResponse;
import com.example.manga_project.Modelos.CarritoRequest;
import com.example.manga_project.Modelos.RespuestaGenerica;
import com.example.manga_project.Modelos.ListarCarritoResponse;
import com.example.manga_project.Modelos.CrearComentarioRequest;
import com.example.manga_project.Modelos.BusquedaHistorietaResponse;
import com.example.manga_project.Modelos.ComentariosResponse;
import com.example.manga_project.Modelos.ItemsUsuarioResponse;
import com.example.manga_project.Modelos.StripePaymentSheetResponse;
import com.example.manga_project.Modelos.GuardarVentaResponse;
import com.example.manga_project.Modelos.MasVendidoResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
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

    @POST("/api_rechazar_proveedor")
    Call<SolicitudResponse> rechazarProveedor(@Body RechazarProveedorRequest request);


    @PUT("/api_solicitar_proveedor")
    Call<Void> solicitarProveedor();


    @GET("/api_obtener_generos")
    Call<List<Genero>> obtenerGeneros(@Query("tipo") String tipo);

    @GET("api_obtener_mis_solicitudes")
    Call<SoliHistorietaProveedorResponse> obtenerMisSolicitudes();


    @GET("/api_obtener_solicitud_historieta")
    Call<List<Solicitud>> obtenerSolicitudesPublicacion();

    @GET("/api_solicitud_publicacion/{id}")
    Call<SolicitudDetalle> getSolicitudById(@Path("id") int idSolicitud);

    @GET("/solicitudes/{id}/chapters")
    Call<CapituloResponse> getChapters(@Path("id") int idSolicitud);

    @GET("/solicitudes/{id}/chapters/{chapter}/pages")
    Call<PaginaResponse> getChapterPages(
            @Path("id") int idSolicitud,
            @Path("chapter") String nombreCapitulo
    );

    @Multipart
    @POST("/upload_portada")
    Call<ResponseBody> subirPortada(@Part MultipartBody.Part file);

    @Multipart
    @POST("/upload_zip")
    Call<ResponseBody> subirZip(@Part MultipartBody.Part file);

    @POST("/api_registrar_solicitud")
    Call<SolicitudPublicacionResponse> registrarSolicitud(@Body SolicitudPublicacionRequest request);





//    @GET("/api_libro")
//    Call<ApiResponse> obtenerLibros();
//
//    @GET("/api_libros_mas_vendidos")
//    Call<ApiResponse> obtenerLibrosMasVendidos();
//
//    @GET("/api_libros_antiguos")
//    Call<ApiResponse> obtenerLibrosAntiguos();
//
//    @GET("/api_libros_actuales")
//    Call<ApiResponse> obtenerLibrosActuales;

//    @GET("/api_mejores_autores_libros")
//    Call<AutoresApiResponse> obtenerMejoresAutoresLibros();

    @POST("/payment-sheet")
    Call<PaymentIntentResponse> createPaymentIntent(@Body PaymentIntentRequest request);

//    @GET("api_libro/{isbn_lib}")
//    Call<ApiResponse_L> getLibro(@Path("isbn_lib") String isbnLib);

//    @GET("/api_libros_comprados/{email_user}")
//    Call<ApiResponse> obtenerLibrosComprados(@Path("email_user") String emailUser);

    @POST("/api_aprobar_publicacion")
    Call<SolicitudResponse> aprobarPublicacion(@Body AprobarPublicacionRequest request);

    @GET("/historietas/novedades")
    Call<List<VolumenResponse>> getNovedades();



    @GET("/historietas/genero/{id_genero}")
    Call<List<VolumenResponse>> getPorGenero(@Path("id_genero") int idGenero);

    @GET("/volumenes/{id_vol}")
    Call<FichaVolumenResponse> getFichaVolumen(@Path("id_vol") int idVol);

    @GET("/volumenes/{id_vol}/chapters")
    Call<CapituloResponse> getCapitulosVolumen(@Path("id_vol") int idVol);

    @GET("/volumenes/{id_vol}/chapters/{chapter}/pages")
    Call<PaginaResponse> getPaginasCapitulo(@Path("id_vol") int idVol, @Path("chapter") String chapter);

    @POST("/carrito/agregar")
    Call<RespuestaGenerica> agregarAlCarrito(@Body CarritoRequest request);

    @GET("/carrito")
    Call<ListarCarritoResponse> listarCarrito();

    @POST("/carrito/vaciar")
    Call<RespuestaGenerica> vaciarCarrito();

    @DELETE("/carrito/item")
    Call<RespuestaGenerica> eliminarItemCarrito(@Query("id_volumen") int idVolumen);

    @POST("/api_crear_comentario")
    Call<CrearComentarioResponse> crearComentario(@Body CrearComentarioRequest request);

    @PUT("/api_cancelar_solicitud_proveedor")
    Call<RespuestaGenerica> cancelarSolicitudProveedor();

    @GET("/api_lista_busqueda")
    Call<BusquedaHistorietaResponse> buscarHistorietas(@Query("q") String query);

    @GET("api_obtener_comentarios/{id_historieta}")
    Call<ComentariosResponse> getComentarios(@Path("id_historieta") int idHistorieta);

    @GET("/api/users/items")
    Call<ItemsUsuarioResponse> getItemsUsuario(
        @Query("type") String type // 'purchases' o 'wishlist'
    );

    @POST("/api_guardar_venta")
    Call<GuardarVentaResponse> guardarVenta(@Body Map<String, Object> body, @Header("Authorization") String token);

    @POST("api_agregar_wishlist")
    Call<RespuestaGenerica> agregarWishlist(@Body Map<String, Integer> body);

    @DELETE("/api_eliminar_wishlist/{id_volumen}")
    Call<RespuestaGenerica> eliminarWishlist(@Path("id_volumen") int idVolumen);

    @POST("/api_rechazar_solicitud_publicacion")
    Call<SolicitudResponse> rechazarSolicitudPublicacion(@Body Map<String, Integer> body);

    @DELETE("/api_borrar_solicitud_publicacion/{id_solicitud}")
    Call<RespuestaGenerica> borrarSolicitudPublicacion(@Path("id_solicitud") int idSolicitud);

    // Stripe PaymentSheet endpoint
    @POST("/payment-sheet")
    Call<StripePaymentSheetResponse> getStripePaymentSheet(@Body Map<String, Object> body);

    @GET("/volumenes_mas_vendidos")
    Call<com.example.manga_project.Modelos.MasVendidosApiResponse> getMasVendidos();
}
