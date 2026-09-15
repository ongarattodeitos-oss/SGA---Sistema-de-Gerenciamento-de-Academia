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

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.animation.OvershootInterpolator;
import androidx.core.content.ContextCompat;

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

    private TextView[] botoesSugestao;
    private TransitionDrawable[] transicoesFundoChip;
    private TextView chipSelecionado = null;

    private static final int COR_TEXTO_PADRAO = 0xFF03C6FC;
    private static final int COR_TEXTO_SELECIONADO = 0xFF080B12;
    private static final int DURACAO_ANIMACAO = 220;

    private boolean[] estadoSelecionadoAtual;



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

        configurarSelecaoSugestoes();

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

    private void configurarSelecaoSugestoes() {

        botoesSugestao = new TextView[]{
                btnSugestaoTreinoA,
                btnSugestaoTreinoB,
                btnSugestaoTreinoC,
                btnSugestaoFullBody
        };

        estadoSelecionadoAtual = new boolean[botoesSugestao.length];
        transicoesFundoChip = new TransitionDrawable[botoesSugestao.length];

        for (int i = 0; i < botoesSugestao.length; i++) {

            TextView botao = botoesSugestao[i];

            Drawable normal =
                    ContextCompat.getDrawable(this, R.drawable.bg_chip_professor).mutate();

            Drawable selecionado =
                    ContextCompat.getDrawable(this, R.drawable.bg_chip_professor_selecionado).mutate();

            TransitionDrawable transicao =
                    new TransitionDrawable(new Drawable[]{normal, selecionado});

            transicao.setCrossFadeEnabled(true);

            botao.setBackground(transicao);

            transicoesFundoChip[i] = transicao;

            final int index = i;

            botao.setOnClickListener(v -> {

                edtNomeFicha.setText(botao.getText());
                edtNomeFicha.setSelection(edtNomeFicha.length());
                edtNomeFicha.requestFocus();

                selecionarChip(botao);

                // pequeno "bounce" de feedback ao tocar
                botao.animate()
                        .scaleX(0.93f)
                        .scaleY(0.93f)
                        .setDuration(80)
                        .withEndAction(() ->
                                botao.animate()
                                        .scaleX(1f)
                                        .scaleY(1f)
                                        .setDuration(160)
                                        .setInterpolator(new OvershootInterpolator())
                                        .start()
                        ).start();
            });
        }

        // Desmarca o chip se o texto for alterado manualmente
        edtNomeFicha.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {

                if (chipSelecionado == null) return;

                String textoAtual = s.toString();
                String textoChip = chipSelecionado.getText().toString();

                if (!textoAtual.equals(textoChip)) {
                    deselecionarTodos();
                }
            }
        });
    }

    private void selecionarChip(TextView clicado) {

        chipSelecionado = clicado;

        for (int i = 0; i < botoesSugestao.length; i++) {
            aplicarEstadoChip(i, botoesSugestao[i] == clicado);
        }
    }

    private void deselecionarTodos() {

        if (chipSelecionado == null) return;

        chipSelecionado = null;

        for (int i = 0; i < botoesSugestao.length; i++) {
            aplicarEstadoChip(i, false);
        }
    }

    private void aplicarEstadoChip(int index, boolean selecionado) {

        TextView botao = botoesSugestao[index];
        TransitionDrawable transicao = transicoesFundoChip[index];

        boolean estadoAnterior = estadoSelecionadoAtual[index];

        if (selecionado == estadoAnterior) {
            return; // já está no estado certo, não faz nada
        }

        if (selecionado) {

            transicao.startTransition(DURACAO_ANIMACAO);

        } else {

            // só reverte se ele já tinha sido selecionado antes
            // (ou seja, já tinha rodado startTransition alguma vez)
            transicao.reverseTransition(DURACAO_ANIMACAO);
        }

        estadoSelecionadoAtual[index] = selecionado;

        int corAtual = botao.getCurrentTextColor();
        int corFinal = selecionado ? COR_TEXTO_SELECIONADO : COR_TEXTO_PADRAO;

        if (corAtual != corFinal) {
            animarCorTexto(botao, corAtual, corFinal);
        }
    }

    private void animarCorTexto(TextView textView, int corAtual, int corFinal) {

        ValueAnimator animador = ValueAnimator.ofObject(new ArgbEvaluator(), corAtual, corFinal);
        animador.setDuration(DURACAO_ANIMACAO);
        animador.addUpdateListener(anim -> textView.setTextColor((int) anim.getAnimatedValue()));
        animador.start();
    }

}