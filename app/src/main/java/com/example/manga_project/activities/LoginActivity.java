package com.example.manga_project.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.manga_project.R;
import com.example.manga_project.databinding.ActivityLoginBinding;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.LoginRequest;
import com.example.manga_project.Modelos.LoginResponse;
import com.example.manga_project.Modelos.PerfilResponse;
import com.example.manga_project.Modelos.GoogleLoginRequest;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.OAuthProvider;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.auth.FacebookAuthProvider;

import org.json.JSONObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private ActivityLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    private CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // 🔧 Configurar ApiClient para usar local o remoto
        ApiClient.setContext(getApplicationContext());
        // ApiClient.usarBackendLocal(true); // ✅ true para local (http://10.0.2.2), false para PythonAnywhere
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura el inicio de sesión de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();
        callbackManager = CallbackManager.Factory.create();

        // Botón login con Google
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Botón login normal
        binding.btnCorreo.setOnClickListener(v -> loginUser(
                binding.txtEmail.getText().toString(),
                binding.txtContrasena.getText().toString()
        ));

        // Redirección a Olvidar Password
        binding.txtContrasenaOlvidada.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, OlvidarPasswordActivity.class);
            startActivity(intent);
        });

        // Botón login con Twitter
        binding.btnTwitter.setOnClickListener(v -> signInWithTwitter());

        // Botón login con Facebook
        binding.btnFacebook.setOnClickListener(v -> signInWithFacebook());

        setupRegisterLink();

        LoginManager.getInstance().registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d("FACEBOOK_LOGIN", "onSuccess: token=" + loginResult.getAccessToken());
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d("FACEBOOK_LOGIN", "onCancel");
                Toast.makeText(LoginActivity.this, "Inicio de sesión con Facebook cancelado", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(FacebookException error) {
                Log.e("FACEBOOK_LOGIN", "onError: " + error.getMessage(), error);
                Toast.makeText(LoginActivity.this, "Error con Facebook: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void signInWithGoogle() {
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, 1001);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            if (task.isSuccessful()) {
                GoogleSignInAccount account = task.getResult();
                if (account != null) {
                    String idToken = account.getIdToken();
                    // Autenticar con FirebaseAuth usando el idToken de Google
                    AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
                    FirebaseAuth.getInstance().signInWithCredential(credential)
                        .addOnCompleteListener(this, firebaseTask -> {
                            if (firebaseTask.isSuccessful()) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                if (user != null) {
                                    user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                                        if (tokenTask.isSuccessful()) {
                                            String firebaseIdToken = tokenTask.getResult().getToken();
                                            loginWithGoogleBackend(firebaseIdToken);
                                        } else {
                                            // Error obteniendo el idToken de Firebase
                                        }
                                    });
                                }
                            } else {
                                // Error autenticando con Firebase
                            }
                        });
                }
            } else {
                // Manejar error de Google Sign-In
            }
        }
    }

    private void loginWithGoogleBackend(String firebaseIdToken) {
        AuthService authService = ApiClient.getClientConToken().create(AuthService.class);
        GoogleLoginRequest request = new GoogleLoginRequest(firebaseIdToken);
        authService.loginGoogle(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    // Guardar el token en SharedPreferences correcto (myPrefs)
                    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    prefs.edit().putString("token", token).apply();
                    // Navegar a la pantalla principal
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "No se pudo iniciar sesión. Usuario no registrado o error en el backend.", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red al iniciar sesión.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null && user.getEmail() != null) {
                            saveUserEmail(user.getEmail());
                        }

                        if (user != null) {
                            saveUserToken(user);

                            String email = "admin@gmail.com";
                            String password = "admin";

                            AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
                            LoginRequest loginRequest = new LoginRequest(email, password);

                            authService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                                @Override
                                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                                    if (response.isSuccessful()) {
                                        String token = response.body().getToken();
                                        int idRol = response.body().getIdRol();  // 👈 extrae el rol

                                        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                                        prefs.edit()
                                                .putString("token", token)
                                                .putInt("id_rol", idRol) // 👈 guarda el rol
                                                .putBoolean("is_google_user", true)
                                                .apply();

                                        // Obtener id_user y guardarlo tras login con Google
                                        AuthService apiConToken = ApiClient.getClientConToken().create(AuthService.class);
                                        apiConToken.getPerfil().enqueue(new Callback<PerfilResponse>() {
                                            @Override
                                            public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> res) {
                                                if (res.isSuccessful() && res.body() != null) {
                                                    int idUser = res.body().getId_user();
                                                    prefs.edit().putInt("userId", idUser).apply();
                                                    Log.d("LoginActivity", "Saved userId=" + idUser);
                                                }
                                                navigateToMainScreen();
                                            }
                                            @Override public void onFailure(Call<PerfilResponse> call, Throwable t) {
                                                navigateToMainScreen();
                                            }
                                        });
                                    } else {
                                        Log.e("AuthError", "Error al obtener el token: " + response.message());
                                    }
                                }


                                @Override
                                public void onFailure(Call<LoginResponse> call, Throwable t) {
                                    Log.e("AuthError", "Error en la solicitud: " + t.getMessage());
                                }
                            });
                        }
                    } else {
                        Log.w("TAG", "signInWithCredential:failure", task.getException());
                    }
                });
    }

    private void loginUser(String email, String password) {
        AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
        LoginRequest loginRequest = new LoginRequest(email, password);

        authService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    saveToken(token);
                    saveUserEmail(email);
                    SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("is_google_user", false)
                            .putInt("id_rol", response.body().getIdRol())
                            .apply();
                    // Obtener id_user y guardarlo
                    AuthService apiConToken = ApiClient.getClientConToken().create(AuthService.class);
                    apiConToken.getPerfil().enqueue(new Callback<PerfilResponse>() {
                        @Override
                        public void onResponse(Call<PerfilResponse> call, Response<PerfilResponse> res) {
                            if (res.isSuccessful() && res.body() != null) {
                                int idUser = res.body().getId_user();
                                prefs.edit().putInt("userId", idUser).apply();
                                Log.d("LoginActivity", "Saved userId=" + idUser);
                            }
                            navigateToMainScreen();
                        }
                        @Override public void onFailure(Call<PerfilResponse> call, Throwable t) {
                            navigateToMainScreen();
                        }
                    });
                } else {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            String errorMessage = errorObj.optString("message", "Error en el login");
                            Log.e("LoginError", errorMessage);
                        }
                    } catch (Exception e) {
                        Log.e("LoginError", "Error procesando la respuesta");
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Log.e("LoginError", "Fallo de conexión: " + t.getMessage());
            }
        });
    }

    private void setupRegisterLink() {
        String text = "¿Aún no tienes una cuenta? Regístrate";
        SpannableString spannableString = new SpannableString(text);
        int colorPrimary = ContextCompat.getColor(this, R.color.colorPrimary);

        spannableString.setSpan(new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                startActivity(new Intent(LoginActivity.this, SelectRegisterActivity.class));
            }

            @Override
            public void updateDrawState(@NonNull TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
                ds.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
                ds.setColor(colorPrimary);
            }
        }, text.indexOf("Regístrate"), text.length(), SpannableString.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.lblRedRegister.setText(spannableString);
        binding.lblRedRegister.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void saveUserToken(FirebaseUser user) {
        if (user != null) {
            SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
            prefs.edit().putString("token", user.getUid()).apply();
        }
    }

    private void saveUserEmail(String email) {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit()
                .putString("email_user", email)
                .putBoolean("is_google_user", true)
                .apply();
        Log.d("LoginActivity", "Correo guardado: " + email);
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        prefs.edit().putString("token", token).apply();
    }

    private void navigateToMainScreen() {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        int idRol = prefs.getInt("id_rol", -1);

        Intent intent;
        switch (idRol) {
            case 3:
                intent = new Intent(this, MainAdminActivity.class);
                break;
            case 2:
                intent = new Intent(this, MainProveedorActivity.class);
                break;
            case 1:
            default:
                intent = new Intent(this, MainActivity.class);
                break;
        }

        startActivity(intent);
        finish();
    }

    private void signInWithTwitter() {
        OAuthProvider.Builder provider = OAuthProvider.newBuilder("twitter.com");
        mAuth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener(authResult -> {
                FirebaseUser user = authResult.getUser();
                if (user != null) {
                    user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                        if (tokenTask.isSuccessful()) {
                            String firebaseIdToken = tokenTask.getResult().getToken();
                            loginWithTwitterBackend(firebaseIdToken);
                        } else {
                            Toast.makeText(this, "Error obteniendo idToken de Twitter", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(this, "Error autenticando con Twitter: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
    }

    private void loginWithTwitterBackend(String firebaseIdToken) {
        AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
        GoogleLoginRequest request = new GoogleLoginRequest(firebaseIdToken);
        authService.loginTwitter(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    prefs.edit().putString("token", token).apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "No se pudo iniciar sesión con Twitter.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += "\nError leyendo el error del backend.";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("TwitterLogin", "Código: " + response.code() + ", error: " + errorMsg);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red al iniciar sesión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("TwitterLogin", "onFailure: ", t);
            }
        });
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, java.util.Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d("FACEBOOK_LOGIN", "handleFacebookAccessToken: token=" + token);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
            .addOnCompleteListener(this, task -> {
                if (task.isSuccessful()) {
                    Log.d("FACEBOOK_LOGIN", "signInWithCredential:success");
                    FirebaseUser user = mAuth.getCurrentUser();
                    if (user != null) {
                        user.getIdToken(true).addOnCompleteListener(tokenTask -> {
                            if (tokenTask.isSuccessful()) {
                                String firebaseIdToken = tokenTask.getResult().getToken();
                                Log.d("FACEBOOK_LOGIN", "FirebaseIdToken obtenido correctamente");
                                loginWithFacebookBackend(firebaseIdToken);
                            } else {
                                Log.e("FACEBOOK_LOGIN", "Error obteniendo idToken de Facebook", tokenTask.getException());
                                Toast.makeText(this, "Error obteniendo idToken de Facebook", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Exception e = task.getException();
                    if (e instanceof com.google.firebase.auth.FirebaseAuthUserCollisionException) {
                        Log.e("FACEBOOK_LOGIN", "Usuario ya existe con otro método de autenticación", e);
                        Toast.makeText(this, "Ya existe una cuenta con este correo usando otro método de inicio de sesión. Inicia sesión con ese método y vincula Facebook desde tu perfil.", Toast.LENGTH_LONG).show();
                    } else {
                        Log.e("FACEBOOK_LOGIN", "signInWithCredential:failure", e);
                        Toast.makeText(this, "Error autenticando con Facebook", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    }

    private void loginWithFacebookBackend(String firebaseIdToken) {
        AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
        GoogleLoginRequest request = new GoogleLoginRequest(firebaseIdToken);
        authService.loginFacebook(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String token = response.body().getToken();
                    SharedPreferences prefs = getSharedPreferences("myPrefs", MODE_PRIVATE);
                    prefs.edit().putString("token", token).apply();
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    String errorMsg = "No se pudo iniciar sesión con Facebook.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += "\n" + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        errorMsg += "\nError leyendo el error del backend.";
                    }
                    Toast.makeText(LoginActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("FacebookLogin", "Código: " + response.code() + ", error: " + errorMsg);
                }
            }
            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Error de red al iniciar sesión: " + t.getMessage(), Toast.LENGTH_LONG).show();
                Log.e("FacebookLogin", "onFailure: ", t);
            }
        });
    }
}
