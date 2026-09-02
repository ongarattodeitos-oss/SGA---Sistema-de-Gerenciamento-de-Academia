package com.example.sga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
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

        String texto = "Não tem uma conta? Criar conta";
        SpannableString spannable = new SpannableString(texto);

        int inicio = texto.indexOf("Criar conta");
        int fim = inicio + "Criar conta".length();

        spannable.setSpan(
                new ForegroundColorSpan(Color.rgb(38, 217, 22)),
                inicio,
                fim,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        txtCriarConta.setText(spannable);

        // ==========================================
        // REPOSITORY E PREFERÊNCIAS
        // ==========================================

        loginRepository = new LoginRepository(this);
        preferences = getSharedPreferences("login", MODE_PRIVATE);

        carregarLoginSalvo();

        // ==========================================
        // LISTENERS
        // ==========================================

        btnLogin.setOnClickListener(v -> fazerLogin());

        txtCriarConta.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, CadastroActivity.class);
            startActivity(intent);
        });
    }

    // ==========================================
    // FAZER LOGIN
    // ==========================================

    private void fazerLogin() {

        String usuario = edtUsuario.getText().toString().trim();
        String senha = edtSenha.getText().toString();

        if (usuario.isEmpty()) {
            edtUsuario.setError("Digite seu usuário ou email");
            edtUsuario.requestFocus();
            return;
        }

        if (senha.isEmpty()) {
            edtSenha.setError("Digite sua senha");
            edtSenha.requestFocus();
            return;
        }

        btnLogin.setEnabled(false);
        btnLogin.setText("ENTRANDO...");

        loginRepository.fazerLogin(
                usuario,
                senha,
                new LoginRepository.LoginCallback() {

                    @Override
                    public void onSuccess(JSONObject usuarioJson, String token) {

                        btnLogin.setEnabled(true);
                        btnLogin.setText("ENTRAR");

                        try {
                            int idUser = usuarioJson.getInt("id_user");
                            String nomeCompleto = usuarioJson.getString("nome_completo");
                            String nomeUser = usuarioJson.getString("nome_user");
                            String email = usuarioJson.getString("email");
                            String tipo = usuarioJson.getString("tipo");

                            // ==========================================
                            // SALVA DADOS NO SHAREDPFERENCES
                            // ==========================================
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt("id_user", idUser);
                            editor.putString("token", token);
                            editor.putString("nome_completo", nomeCompleto);
                            editor.putString("nome_user", nomeUser);
                            editor.putString("email", email);
                            editor.putString("tipo", tipo);

                            if (checkLembrar.isChecked()) {
                                editor.putBoolean("lembrar", true);
                                editor.putString("usuario", usuario);
                            } else {
                                editor.remove("lembrar");
                                editor.remove("usuario");
                            }

                            editor.apply();

                            Toast.makeText(
                                    LoginActivity.this,
                                    "Login realizado com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();

                            Intent intent;

                            if ("aluno".equalsIgnoreCase(tipo)) {
                                intent = new Intent(LoginActivity.this, AlunoActivity.class);
                            } else if ("professor".equalsIgnoreCase(tipo)) {
                                intent = new Intent(LoginActivity.this, ProfessorActivity.class);
                            } else {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Tipo de usuário inválido.",
                                        Toast.LENGTH_LONG
                                ).show();
                                return;
                            }

                            intent.putExtra("id_user", idUser);
                            intent.putExtra("nome_completo", nomeCompleto);
                            intent.putExtra("nome_user", nomeUser);
                            intent.putExtra("email", email);

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
                    public void onError(String mensagem) {
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

    private void carregarLoginSalvo() {
        boolean lembrar = preferences.getBoolean("lembrar", false);

        if (lembrar) {
            String usuario = preferences.getString("usuario", "");
            edtUsuario.setText(usuario);
            checkLembrar.setChecked(true);
        }
    }
}