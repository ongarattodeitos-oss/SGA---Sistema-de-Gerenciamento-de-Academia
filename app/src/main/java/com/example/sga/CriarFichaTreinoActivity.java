package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.content.Context;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class CriarFichaTreinoActivity extends AppCompatActivity {

    // ============================================================
    // API
    // ============================================================

    private static final String URL_CRIAR_FICHA =
            "https://sga-api.miguel-r-hoff.workers.dev/fichas-treino";


    // ============================================================
    // CAMPOS
    // ============================================================

    private int idAluno;

    private String nomeAluno;
    private String nomeUsuario;

    private TextView btnVoltar;

    private TextView txtNomeAluno;
    private TextView txtUsuarioAluno;

    private EditText edtNomeFicha;

    private TextView btnCriarFicha;

    private TextView btnSugestaoTreinoA;
    private TextView btnSugestaoTreinoB;
    private TextView btnSugestaoFullBody;
    private TextView btnSugestaoTreinoC;


    // ============================================================
    // ON CREATE
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_criar_ficha_treino
        );


        // ========================================================
        // INSETS
        // ========================================================

        View main =
                findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(
                main,
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(
                                    WindowInsetsCompat.Type.systemBars()
                            );

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                }
        );


        // ========================================================
        // REFERÊNCIAS
        // ========================================================

        btnVoltar =
                findViewById(
                        R.id.btnVoltarCriarFicha
                );

        txtNomeAluno =
                findViewById(
                        R.id.txtNomeAlunoCriarFicha
                );

        txtUsuarioAluno =
                findViewById(
                        R.id.txtUsuarioAlunoCriarFicha
                );

        edtNomeFicha =
                findViewById(
                        R.id.edtNomeFicha
                );

        btnCriarFicha =
                findViewById(
                        R.id.btnCriarFicha
                );

        btnSugestaoTreinoA =
                findViewById(
                        R.id.btnSugestaoTreinoA
                );

        btnSugestaoTreinoB =
                findViewById(
                        R.id.btnSugestaoTreinoB
                );

        btnSugestaoFullBody =
                findViewById(
                        R.id.btnSugestaoFullBody
                );

        btnSugestaoTreinoC = findViewById(R.id.btnSugestaoTreinoC);


        // ========================================================
        // RECEBER DADOS
        // ========================================================

        idAluno =
                getIntent().getIntExtra(
                        "id_alunos",
                        -1
                );

        nomeAluno =
                getIntent().getStringExtra(
                        "nome_completo"
                );

        nomeUsuario =
                getIntent().getStringExtra(
                        "nome_user"
                );


        // ========================================================
        // VALIDAR ALUNO
        // ========================================================

        if (idAluno == -1) {

            Toast.makeText(
                    this,
                    "Erro: aluno não encontrado.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        // ========================================================
        // PREENCHER ALUNO
        // ========================================================

        if (
                nomeAluno == null ||
                        nomeAluno.trim().isEmpty()
        ) {

            nomeAluno = "Aluno";

        }

        txtNomeAluno.setText(
                nomeAluno
        );


        if (
                nomeUsuario != null &&
                        !nomeUsuario.trim().isEmpty()
        ) {

            txtUsuarioAluno.setText(
                    "@" + nomeUsuario
            );

        } else {

            txtUsuarioAluno.setText(
                    "Usuário não informado"
            );

        }


        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltar.setOnClickListener(v -> {

            finish();

        });


        // ========================================================
        // SUGESTÕES
        // ========================================================

        btnSugestaoTreinoA.setOnClickListener(v -> {

            edtNomeFicha.setText(
                    "Treino A"
            );

            edtNomeFicha.setSelection(
                    edtNomeFicha.length()
            );

            edtNomeFicha.requestFocus();

        });


        btnSugestaoTreinoB.setOnClickListener(v -> {

            edtNomeFicha.setText(
                    "Treino B"
            );

            edtNomeFicha.setSelection(
                    edtNomeFicha.length()
            );

            edtNomeFicha.requestFocus();

        });

        btnSugestaoTreinoC.setOnClickListener(v -> {

            edtNomeFicha.setText(
                    "Treino C"
            );

            edtNomeFicha.setSelection(
                    edtNomeFicha.length()
            );

            edtNomeFicha.requestFocus();

        });


        btnSugestaoFullBody.setOnClickListener(v -> {

            edtNomeFicha.setText(
                    "Full Body"
            );

            edtNomeFicha.setSelection(
                    edtNomeFicha.length()
            );

            edtNomeFicha.requestFocus();

        });


        // ========================================================
        // CRIAR FICHA
        // ========================================================

        btnCriarFicha.setOnClickListener(v -> {

            criarFicha();

        });

    }


    // ============================================================
    // CRIAR FICHA
    // ============================================================

    private void criarFicha() {

        String nomeFicha =
                edtNomeFicha
                        .getText()
                        .toString()
                        .trim();


        // ========================================================
        // VALIDAR NOME
        // ========================================================

        if (nomeFicha.isEmpty()) {

            edtNomeFicha.setError(
                    "Informe o nome da ficha."
            );

            edtNomeFicha.requestFocus();

            return;
        }


        if (nomeFicha.length() > 100) {

            edtNomeFicha.setError(
                    "Máximo de 100 caracteres."
            );

            edtNomeFicha.requestFocus();

            return;
        }


        // ========================================================
        // ESCONDER TECLADO
        // ========================================================

        InputMethodManager teclado =
                (InputMethodManager)
                        getSystemService(
                                Context.INPUT_METHOD_SERVICE
                        );

        if (teclado != null) {

            teclado.hideSoftInputFromWindow(
                    edtNomeFicha.getWindowToken(),
                    0
            );

        }


        // ========================================================
        // DESABILITAR BOTÃO
        // ========================================================

        btnCriarFicha.setEnabled(false);

        btnCriarFicha.setAlpha(
                0.5f
        );


        // ========================================================
        // THREAD
        // ========================================================

        final String nomeFinal =
                nomeFicha;


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                // ------------------------------------------------
                // CONEXÃO
                // ------------------------------------------------

                URL url =
                        new URL(
                                URL_CRIAR_FICHA
                        );


                conexao =
                        (HttpURLConnection)
                                url.openConnection();


                conexao.setRequestMethod(
                        "POST"
                );

                conexao.setConnectTimeout(
                        10000
                );

                conexao.setReadTimeout(
                        10000
                );

                conexao.setDoOutput(
                        true
                );


                conexao.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );


                // ------------------------------------------------
                // JSON
                // ------------------------------------------------

                JSONObject dados =
                        new JSONObject();


                dados.put(
                        "id_alunos",
                        idAluno
                );


                dados.put(
                        "nome_ficha",
                        nomeFinal
                );


                String json =
                        dados.toString();


                // ------------------------------------------------
                // ENVIAR
                // ------------------------------------------------

                OutputStream output =
                        conexao.getOutputStream();


                output.write(
                        json.getBytes(
                                StandardCharsets.UTF_8
                        )
                );


                output.flush();

                output.close();


                // ------------------------------------------------
                // RESPOSTA
                // ------------------------------------------------

                int codigo =
                        conexao.getResponseCode();


                InputStream inputStream;


                if (
                        codigo >= 200 &&
                                codigo < 300
                ) {

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


                while (
                        (linha =
                                reader.readLine())
                                != null
                ) {

                    resposta.append(
                            linha
                    );

                }


                reader.close();


                // ------------------------------------------------
                // PROCESSAR
                // ------------------------------------------------

                JSONObject jsonResposta =
                        new JSONObject(
                                resposta.toString()
                        );


                boolean sucesso =
                        jsonResposta.optBoolean(
                                "sucesso",
                                false
                        );


                String mensagem =
                        jsonResposta.optString(
                                "mensagem",
                                "Erro ao criar ficha."
                        );


                if (
                        sucesso &&
                                codigo >= 200 &&
                                codigo < 300
                ) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                CriarFichaTreinoActivity.this,
                                "Ficha criada com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();


                        // Volta para a lista
                        finish();

                    });


                } else {

                    runOnUiThread(() -> {

                        btnCriarFicha.setEnabled(
                                true
                        );

                        btnCriarFicha.setAlpha(
                                1f
                        );


                        Toast.makeText(
                                CriarFichaTreinoActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();

                    });

                }


            } catch (Exception erro) {

                erro.printStackTrace();


                runOnUiThread(() -> {

                    btnCriarFicha.setEnabled(
                            true
                    );

                    btnCriarFicha.setAlpha(
                            1f
                    );


                    Toast.makeText(
                            CriarFichaTreinoActivity.this,
                            "Erro de conexão com o servidor.",
                            Toast.LENGTH_LONG
                    ).show();

                });


            } finally {

                if (conexao != null) {

                    conexao.disconnect();

                }

            }

        }).start();

    }

}