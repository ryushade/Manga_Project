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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.manga_project.R;
import com.example.manga_project.databinding.ActivityLoginBinding;
import com.example.manga_project.Api_cliente.ApiClient;
import com.example.manga_project.Api_cliente.AuthService;
import com.example.manga_project.Modelos.LoginRequest;
import com.example.manga_project.Modelos.LoginResponse;
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

        // 🔧 Configurar ApiClient para usar local o remoto
        ApiClient.setContext(getApplicationContext());
        ApiClient.usarBackendLocal(true); // ✅ true para local (http://10.0.2.2), false para PythonAnywhere

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Configura el inicio de sesión de Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mAuth = FirebaseAuth.getInstance();

        // Botón login con Google
        binding.btnGoogle.setOnClickListener(v -> signInWithGoogle());

        // Botón login normal
        binding.btnCorreo.setOnClickListener(v -> loginUser(
                binding.txtEmail.getText().toString(),
                binding.txtContrasena.getText().toString()
        ));

        setupRegisterLink();
    }

    private void signInWithGoogle() {
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

                                        navigateToMainScreen();
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
                    saveToken(response.body().getToken());
                    saveUserEmail(email);

                    SharedPreferences prefs = getSharedPreferences("myPrefs", Context.MODE_PRIVATE);
                    prefs.edit()
                            .putBoolean("is_google_user", false)
                            .putInt("id_rol", response.body().getIdRol())
                            .apply();

                    navigateToMainScreen();
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
}
