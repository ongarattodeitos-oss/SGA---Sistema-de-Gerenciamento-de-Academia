package com.example.sga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText edtUsuario;
    private EditText edtSenha;
    private CheckBox checkLembrar;
    private Button btnLogin;
    private TextView txtCriarConta;

    private LoginRepository loginRepository;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login);

        // ==========================================
        // REFERÊNCIAS DO XML
        // ==========================================

        edtUsuario = findViewById(R.id.edtUsuario);
        edtSenha = findViewById(R.id.edtSenha);
        checkLembrar = findViewById(R.id.checkLembrar);
        btnLogin = findViewById(R.id.btnLogin);
        txtCriarConta = findViewById(R.id.txtCriarConta);

        // ==========================================
        // REPOSITORY
        // ==========================================

        loginRepository = new LoginRepository(this);

        // ==========================================
        // PREFERÊNCIAS
        // ==========================================

        preferences = getSharedPreferences(
                "login",
                MODE_PRIVATE
        );

        carregarLoginSalvo();

        // ==========================================
        // BOTÃO LOGIN
        // ==========================================

        btnLogin.setOnClickListener(v -> fazerLogin());

        // ==========================================
        // CRIAR CONTA
        // ==========================================

        txtCriarConta.setOnClickListener(v -> {

            Intent intent = new Intent(
                    LoginActivity.this,
                    CadastroActivity.class
            );

            startActivity(intent);
        });
    }

    // ==========================================
    // FAZER LOGIN
    // ==========================================

    private void fazerLogin() {

        String usuario =
                edtUsuario.getText()
                        .toString()
                        .trim();

        String senha =
                edtSenha.getText()
                        .toString();

        // ==========================================
        // VALIDA USUÁRIO
        // ==========================================

        if (usuario.isEmpty()) {

            edtUsuario.setError(
                    "Digite seu usuário ou email"
            );

            edtUsuario.requestFocus();

            return;
        }

        // ==========================================
        // VALIDA SENHA
        // ==========================================

        if (senha.isEmpty()) {

            edtSenha.setError(
                    "Digite sua senha"
            );

            edtSenha.requestFocus();

            return;
        }

        // ==========================================
        // DESABILITA BOTÃO
        // ==========================================

        btnLogin.setEnabled(false);
        btnLogin.setText("ENTRANDO...");

        // ==========================================
        // ENVIA PARA API
        // ==========================================

        loginRepository.fazerLogin(
                usuario,
                senha,
                new LoginRepository.LoginCallback() {

                    @Override
                    public void onSuccess(
                            JSONObject usuarioJson
                    ) {

                        btnLogin.setEnabled(true);
                        btnLogin.setText("ENTRAR");

                        try {

                            int idUser =
                                    usuarioJson.getInt(
                                            "id_user"
                                    );

                            String nomeCompleto =
                                    usuarioJson.getString(
                                            "nome_completo"
                                    );

                            String nomeUser =
                                    usuarioJson.getString(
                                            "nome_user"
                                    );

                            String email =
                                    usuarioJson.getString(
                                            "email"
                                    );

                            String tipo =
                                    usuarioJson.getString(
                                            "tipo"
                                    );

                            // ==================================
                            // SALVA LOGIN SE "LEMBRAR-ME"
                            // ==================================

                            if (checkLembrar.isChecked()) {

                                preferences.edit()
                                        .putBoolean(
                                                "lembrar",
                                                true
                                        )
                                        .putString(
                                                "usuario",
                                                usuario
                                        )
                                        .apply();

                            } else {

                                preferences.edit()
                                        .clear()
                                        .apply();
                            }

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login realizado com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            // ==========================================
// ABRIR MAIN ACTIVITY
// ==========================================

                            Intent intent = new Intent(
                                    LoginActivity.this,
                                    MainActivity.class
                            );

                            intent.putExtra(
                                    "id_user",
                                    idUser
                            );

                            intent.putExtra(
                                    "nome_completo",
                                    nomeCompleto
                            );

                            intent.putExtra(
                                    "nome_user",
                                    nomeUser
                            );

                            intent.putExtra(
                                    "email",
                                    email
                            );

                            intent.putExtra(
                                    "tipo",
                                    tipo
                            );

                            startActivity(intent);

                            finish();

                        } catch (Exception e) {

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Erro ao processar os dados do usuário.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }

                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        btnLogin.setEnabled(true);
                        btnLogin.setText("ENTRAR");

                        Toast.makeText(
                                LoginActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // ==========================================
    // CARREGAR USUÁRIO SALVO
    // ==========================================

    private void carregarLoginSalvo() {

        boolean lembrar =
                preferences.getBoolean(
                        "lembrar",
                        false
                );

        if (lembrar) {

            String usuario =
                    preferences.getString(
                            "usuario",
                            ""
                    );

            edtUsuario.setText(usuario);
            checkLembrar.setChecked(true);
        }
    }
}