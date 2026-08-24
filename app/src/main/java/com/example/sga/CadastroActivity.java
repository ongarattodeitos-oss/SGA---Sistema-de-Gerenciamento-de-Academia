package com.example.sga;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.text.Editable;
import android.text.TextWatcher;

public class CadastroActivity extends AppCompatActivity {

    private EditText edtNome;
    private EditText edtEmail;
    private EditText edtTelefone;
    private EditText edtCpf;
    private EditText edtNascimento;
    private EditText edtUsuario;
    private EditText edtSenha;
    private EditText edtConfirmarSenha;

    private Button btnCadastrar;


    // ==========================================
    // URL DO SEU WORKER
    // ==========================================

    private static final String URL_CADASTRO =
            "https://sga-api.miguel-r-hoff.workers.dev/cadastro";


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_cadastro);


        // ==========================================
        // PEGAR OS COMPONENTES DA TELA
        // ==========================================

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCpf = findViewById(R.id.edtCpf);
        edtNascimento = findViewById(R.id.edtNascimento);

        aplicarMascaraCpf();
        aplicarMascaraData();

        edtUsuario = findViewById(R.id.edtUsuario);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);

        btnCadastrar = findViewById(R.id.btnCadastrar);
        TextView txtLogin = findViewById(R.id.txtLogin);

        String texto = "Já possui uma conta? Entrar";

        SpannableString spannable = new SpannableString(texto);

        int inicio = texto.indexOf("Entrar");
        int fim = inicio + "Entrar".length();

        spannable.setSpan(
                new ForegroundColorSpan(Color.rgb(38, 217, 22)),
                inicio,
                fim,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        txtLogin.setText(spannable);


        // ==========================================
        // BOTÃO CADASTRAR
        // ==========================================

        btnCadastrar.setOnClickListener(v -> {

            cadastrar();

        });


        // ==========================================
        // LINK LOGIN
        // ==========================================

        txtLogin.setOnClickListener(v -> {

            finish();

        });
    }


    // ==========================================
    // FUNÇÃO DE CADASTRO
    // ==========================================

    private void cadastrar() {

        String nome = edtNome.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String cpf = edtCpf.getText().toString().trim();
        String nascimento = edtNascimento.getText().toString().trim();
        String usuario = edtUsuario.getText().toString().trim();
        String senha = edtSenha.getText().toString();
        String confirmarSenha = edtConfirmarSenha.getText().toString();


        // ==========================================
        // VALIDAÇÕES
        // ==========================================

        if (nome.isEmpty()) {

            edtNome.setError("Digite seu nome");

            edtNome.requestFocus();

            return;
        }


        if (email.isEmpty()) {

            edtEmail.setError("Digite seu e-mail");

            edtEmail.requestFocus();

            return;
        }


        if (telefone.isEmpty()) {

            edtTelefone.setError("Digite seu telefone");

            edtTelefone.requestFocus();

            return;
        }


        if (cpf.isEmpty()) {

            edtCpf.setError("Digite seu CPF");

            edtCpf.requestFocus();

            return;
        }


        if (nascimento.isEmpty()) {

            edtNascimento.setError("Digite sua data de nascimento");

            edtNascimento.requestFocus();

            return;
        }


        if (usuario.isEmpty()) {

            edtUsuario.setError("Digite um nome de usuário");

            edtUsuario.requestFocus();

            return;
        }


        if (senha.isEmpty()) {

            edtSenha.setError("Digite uma senha");

            edtSenha.requestFocus();

            return;
        }


        if (!senha.equals(confirmarSenha)) {

            edtConfirmarSenha.setError("As senhas não são iguais");

            edtConfirmarSenha.requestFocus();

            return;
        }


        // ==========================================
        // DESABILITA BOTÃO
        // ==========================================

        btnCadastrar.setEnabled(false);


        // ==========================================
        // ENVIA PARA O WORKER
        // ==========================================

        new Thread(() -> {

            try {

                JSONObject dados = new JSONObject();

                dados.put("nome_completo", nome);
                dados.put("nome_user", usuario);
                dados.put("email", email);
                dados.put("senha", senha);
                dados.put("data", nascimento);
                dados.put("telefone", telefone);
                dados.put("cpf", cpf);


                URL url = new URL(URL_CADASTRO);

                HttpURLConnection conexao =
                        (HttpURLConnection) url.openConnection();


                conexao.setRequestMethod("POST");

                conexao.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );

                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                conexao.setDoOutput(true);

                conexao.setConnectTimeout(10000);

                conexao.setReadTimeout(10000);


                // ==========================================
                // ENVIA JSON
                // ==========================================

                OutputStream outputStream =
                        conexao.getOutputStream();

                outputStream.write(
                        dados.toString().getBytes("UTF-8")
                );

                outputStream.flush();

                outputStream.close();


                // ==========================================
                // LÊ RESPOSTA
                // ==========================================

                int codigo =
                        conexao.getResponseCode();


                InputStream inputStream;

                if (codigo >= 200 && codigo < 400) {

                    inputStream =
                            conexao.getInputStream();

                } else {

                    inputStream =
                            conexao.getErrorStream();
                }


                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream
                                )
                        );


                StringBuilder resposta =
                        new StringBuilder();

                String linha;


                while ((linha = reader.readLine()) != null) {

                    resposta.append(linha);

                }


                reader.close();


                JSONObject resultado =
                        new JSONObject(
                                resposta.toString()
                        );


                boolean sucesso =
                        resultado.optBoolean(
                                "sucesso",
                                false
                        );


                String mensagem =
                        resultado.optString(
                                "mensagem",
                                "Resposta desconhecida"
                        );


                // ==========================================
                // VOLTA PARA A THREAD PRINCIPAL
                // ==========================================

                runOnUiThread(() -> {

                    btnCadastrar.setEnabled(true);


                    Toast.makeText(
                            CadastroActivity.this,
                            mensagem,
                            Toast.LENGTH_LONG
                    ).show();


                    if (sucesso) {

                        finish();

                    }

                });


                conexao.disconnect();


            } catch (Exception e) {

                e.printStackTrace();


                runOnUiThread(() -> {

                    btnCadastrar.setEnabled(true);


                    Toast.makeText(
                            CadastroActivity.this,
                            "Não foi possível conectar ao servidor.",
                            Toast.LENGTH_LONG
                    ).show();

                });

            }

        }).start();
    }

    // ==========================================
// MÁSCARA DE CPF
// ==========================================

    private void aplicarMascaraCpf() {

        edtCpf.addTextChangedListener(new TextWatcher() {

            private boolean editando = false;

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editando) {
                    return;
                }

                editando = true;

                // Remove tudo que não for número
                String cpf = s.toString().replaceAll("[^0-9]", "");

                // Limita a 11 números
                if (cpf.length() > 11) {
                    cpf = cpf.substring(0, 11);
                }

                String cpfFormatado = "";

                if (cpf.length() > 0) {
                    cpfFormatado = cpf.substring(0, Math.min(3, cpf.length()));
                }

                if (cpf.length() > 3) {
                    cpfFormatado += "." + cpf.substring(3, Math.min(6, cpf.length()));
                }

                if (cpf.length() > 6) {
                    cpfFormatado += "." + cpf.substring(6, Math.min(9, cpf.length()));
                }

                if (cpf.length() > 9) {
                    cpfFormatado += "-" + cpf.substring(9, Math.min(11, cpf.length()));
                }

                edtCpf.setText(cpfFormatado);

                // Mantém o cursor no final
                edtCpf.setSelection(edtCpf.length());

                editando = false;
            }
        });
    }

    // ==========================================
// MÁSCARA DE DATA DE NASCIMENTO
// ==========================================

    private void aplicarMascaraData() {

        edtNascimento.addTextChangedListener(new TextWatcher() {

            private boolean editando = false;

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (editando) {
                    return;
                }

                editando = true;

                // Remove tudo que não for número
                String data = s.toString().replaceAll("[^0-9]", "");

                // Limita a 8 números
                if (data.length() > 8) {
                    data = data.substring(0, 8);
                }

                String dataFormatada = "";

                // DIA
                if (data.length() > 0) {
                    dataFormatada =
                            data.substring(
                                    0,
                                    Math.min(2, data.length())
                            );
                }

                // MÊS
                if (data.length() > 2) {
                    dataFormatada += "/" +
                            data.substring(
                                    2,
                                    Math.min(4, data.length())
                            );
                }

                // ANO
                if (data.length() > 4) {
                    dataFormatada += "/" +
                            data.substring(
                                    4,
                                    Math.min(8, data.length())
                            );
                }

                edtNascimento.setText(dataFormatada);

                // Mantém o cursor no final
                edtNascimento.setSelection(
                        edtNascimento.length()
                );

                editando = false;
            }
        });
    }

}