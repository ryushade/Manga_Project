package com.example.ebook_proyect.activities;

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

import com.example.ebook_proyect.R;
import com.example.ebook_proyect.databinding.ActivityLoginBinding;
import com.example.ebook_proyect.Api_cliente.ApiClient;
import com.example.ebook_proyect.Api_cliente.AuthService;
import com.example.ebook_proyect.Modelos.LoginRequest;
import com.example.ebook_proyect.Modelos.LoginResponse;
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

import org.json.JSONObject;

import java.io.FileOutputStream;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private static final int RC_SIGN_IN = 9001;
    private ActivityLoginBinding binding;
    private GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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

        // Configura el botón para el inicio de sesión de Google
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Configura el texto con enlace para ir al registro
        setupRegisterLink();

        // Configura el botón para iniciar sesión con correo y contraseña
        binding.btnCorreo.setOnClickListener(v -> loginUser(
                binding.txtEmail.getText().toString(),
                binding.txtContrasena.getText().toString()
        ));
    }

    private void signInWithGoogle() {
        // Cierra sesión para asegurarte de que se muestre el selector de cuenta
        mGoogleSignInClient.signOut().addOnCompleteListener(this, task -> {
            Intent signInIntent = mGoogleSignInClient.getSignInIntent();
            startActivityForResult(signInIntent, RC_SIGN_IN);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Log.w("TAG", "Google sign in failed", e);
                //Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();

                        if (user != null && user.getEmail() != null) {
                            saveUserEmail(user.getEmail());  // Guarda el correo aquí
                        }

                        if (user != null) {
                            saveUserToken(user);

                            //navigateToMainScreen();
                            //Toast.makeText(this, "Logueado", Toast.LENGTH_SHORT).show();
                            // Enviar las credenciales al backend para obtener un token de acceso (email + password)
                            String email = "admin@gmail.com";
                            String password = "admin";  // Aquí puedes obtener o generar la contraseña, dependiendo de tu lógica

                            // Crear una solicitud para obtener el token
                            AuthService authService = ApiClient.getClientSinToken().create(AuthService.class);
                            LoginRequest loginRequest = new LoginRequest(email, password);

                            // Realizar la solicitud
                            authService.login(loginRequest).enqueue(new Callback<LoginResponse>() {
                                @Override
                                public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                                    if (response.isSuccessful()) {
                                        String token = response.body().getToken();
                                        saveToken(token);  // Guarda el token recibido del backend
                                        navigateToMainScreen();  // Navega a la pantalla principal
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
                        //Toast.makeText(this, "Authentication failed.", Toast.LENGTH_SHORT).show();
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
                    saveToken(response.body().getToken());
                    saveUserEmail(email);

                    // Elimina la marca de usuario de Google
                    SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    prefs.edit().putBoolean("is_google_user", false).apply();

                    navigateToMainScreen();
                    //Toast.makeText(LoginActivity.this, "Login normal", Toast.LENGTH_SHORT).show();
                } else {
                    try {
                        if (response.errorBody() != null) {
                            JSONObject errorObj = new JSONObject(response.errorBody().string());
                            String errorMessage = errorObj.optString("message", "Error en el login");
                            //Toast.makeText(LoginActivity.this, "Error en el login: " + errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        //Toast.makeText(LoginActivity.this, "Error procesando la respuesta del servidor", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                //Toast.makeText(LoginActivity.this, "Error en la conexión: " + t.getMessage(), Toast.LENGTH_SHORT).show();
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
                // Código para navegar al fragmento de registro
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
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("token", user.getUid());
            editor.apply();
        }
    }

    private void saveUserEmail(String email) {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("email_user", email);
        editor.putBoolean("is_google_user", true); // Marca como usuario de Google
        editor.apply();

        Log.d("LoginActivity", "Correo guardado: " + email);  // Log para verificar
        //Toast.makeText(this, "Correo guardado en SharedPreferences", Toast.LENGTH_SHORT).show();
    }

    private void saveToken(String token) {
        SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("token", token);
        editor.apply();
    }

    private void navigateToMainScreen() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}