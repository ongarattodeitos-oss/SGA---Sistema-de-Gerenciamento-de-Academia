package com.example.sga;

import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class ExerciciosProfessorActivity extends AppCompatActivity {

    // ============================================================
    // API
    // ============================================================

    private static final String URL_EXERCICIOS =
            "https://sga-api.miguel-r-hoff.workers.dev/exercicios-ficha";


    // ============================================================
    // DADOS
    // ============================================================

    private int idFicha;
    private int idAluno;

    private String nomeFicha;


    // ============================================================
    // CAMPOS
    // ============================================================

    private TextView btnVoltar;

    private TextView txtNomeFicha;

    private LinearLayout containerExercicios;

    private TextView btnAdicionarExercicio;

    private TextView btnEditarSalvar;


    // ============================================================
    // CONTROLE
    // ============================================================

    private boolean modoEdicao = false;


    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_exercicios_professor
        );


        // ========================================================
        // INSETS
        // ========================================================

        View main = findViewById(
                R.id.main
        );

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
                        R.id.btnVoltarExercicios
                );

        txtNomeFicha =
                findViewById(
                        R.id.txtNomeFichaExercicios
                );

        containerExercicios =
                findViewById(
                        R.id.containerExercicios
                );

        btnAdicionarExercicio =
                findViewById(
                        R.id.btnAdicionarExercicio
                );

        btnEditarSalvar =
                findViewById(
                        R.id.btnEditarSalvar
                );


        // ========================================================
        // RECEBER DADOS
        // ========================================================

        idFicha =
                getIntent().getIntExtra(
                        "id_ficha",
                        -1
                );

        idAluno =
                getIntent().getIntExtra(
                        "id_alunos",
                        -1
                );

        nomeFicha =
                getIntent().getStringExtra(
                        "nome_ficha"
                );


        if (idFicha == -1) {

            Toast.makeText(
                    this,
                    "Erro: ficha não encontrada.",
                    Toast.LENGTH_LONG
            ).show();

            finish();

            return;
        }


        if (
                nomeFicha == null ||
                        nomeFicha.trim().isEmpty()
        ) {

            nomeFicha = "Ficha de treino";

        }


        txtNomeFicha.setText(
                nomeFicha
        );


        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltar.setOnClickListener(v -> {

            finish();

        });


        // ========================================================
        // EDITAR / SALVAR
        // ========================================================

        btnEditarSalvar.setOnClickListener(v -> {

            if (!modoEdicao) {

                ativarEdicao();

            } else {

                salvarExercicios();

            }

        });


        // ========================================================
        // ADICIONAR EXERCÍCIO
        // ========================================================

        btnAdicionarExercicio.setOnClickListener(v -> {

            if (!modoEdicao) {
                return;
            }

            adicionarExercicioVazio();

        });


        // ========================================================
        // CARREGAR
        // ========================================================

        carregarExercicios();

    }


    // ============================================================
    // CARREGAR EXERCÍCIOS
    // ============================================================

    private void carregarExercicios() {

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                String endereco =
                        URL_EXERCICIOS +
                                "?id_ficha=" +
                                idFicha;

                URL url =
                        new URL(endereco);

                conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setRequestMethod("GET");

                conexao.setConnectTimeout(
                        10000
                );

                conexao.setReadTimeout(
                        10000
                );


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
                        (linha = reader.readLine())
                                != null
                ) {

                    resposta.append(linha);

                }


                reader.close();


                if (
                        codigo >= 200 &&
                                codigo < 300
                ) {

                    processarExercicios(
                            resposta.toString()
                    );

                } else {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                ExerciciosProfessorActivity.this,
                                "Erro ao carregar exercícios.",
                                Toast.LENGTH_LONG
                        ).show();

                    });

                }


            } catch (Exception erro) {

                erro.printStackTrace();

                runOnUiThread(() -> {

                    Toast.makeText(
                            ExerciciosProfessorActivity.this,
                            "Erro de conexão.",
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


    // ============================================================
    // PROCESSAR JSON
    // ============================================================

    private void processarExercicios(
            String resposta
    ) {

        try {

            JSONObject json =
                    new JSONObject(resposta);

            boolean sucesso =
                    json.optBoolean(
                            "sucesso",
                            false
                    );

            if (!sucesso) {

                runOnUiThread(() -> {

                    Toast.makeText(
                            ExerciciosProfessorActivity.this,
                            json.optString(
                                    "mensagem",
                                    "Erro ao buscar exercícios."
                            ),
                            Toast.LENGTH_LONG
                    ).show();

                });

                return;
            }


            JSONArray exercicios =
                    json.optJSONArray(
                            "exercicios"
                    );

            if (exercicios == null) {

                exercicios =
                        new JSONArray();

            }


            JSONArray finalExercicios =
                    exercicios;


            runOnUiThread(() -> {

                mostrarExercicios(
                        finalExercicios
                );

            });


        } catch (Exception erro) {

            erro.printStackTrace();

        }

    }


    // ============================================================
    // MOSTRAR EXERCÍCIOS
    // ============================================================

    private void mostrarExercicios(
            JSONArray exercicios
    ) {

        containerExercicios.removeAllViews();


        if (exercicios.length() == 0) {

            adicionarExercicioVazio();

            return;
        }


        for (
                int i = 0;
                i < exercicios.length();
                i++
        ) {

            try {

                JSONObject exercicio =
                        exercicios.getJSONObject(i);


                String nome =
                        exercicio.optString(
                                "nome",
                                ""
                        );

                int series =
                        exercicio.optInt(
                                "series",
                                0
                        );

                int repeticoes =
                        exercicio.optInt(
                                "repeticoes",
                                0
                        );


                adicionarExercicio(
                        nome,
                        series,
                        repeticoes
                );


            } catch (Exception erro) {

                erro.printStackTrace();

            }

        }


        bloquearCampos();

    }


    // ============================================================
    // ADICIONAR EXERCÍCIO VAZIO
    // ============================================================

    private void adicionarExercicioVazio() {

        adicionarExercicio(
                "",
                0,
                0
        );

    }


    // ============================================================
    // ADICIONAR EXERCÍCIO
    // ============================================================

    private void adicionarExercicio(
            String nome,
            int series,
            int repeticoes
    ) {

        View view =
                LayoutInflater.from(this).inflate(
                        R.layout.item_exercicio_professor,
                        containerExercicios,
                        false
                );


        TextView txtNumero =
                view.findViewById(
                        R.id.txtNumeroExercicio
                );

        EditText edtNome =
                view.findViewById(
                        R.id.edtNomeExercicio
                );

        EditText edtSeries =
                view.findViewById(
                        R.id.edtSeriesExercicio
                );

        EditText edtRepeticoes =
                view.findViewById(
                        R.id.edtRepeticoesExercicio
                );


        int numero =
                containerExercicios.getChildCount() + 1;


        txtNumero.setText(
                "Exercício " + numero
        );


        edtNome.setText(
                nome
        );


        if (series > 0) {

            edtSeries.setText(
                    String.valueOf(series)
            );

        }


        if (repeticoes > 0) {

            edtRepeticoes.setText(
                    String.valueOf(repeticoes)
            );

        }


        containerExercicios.addView(
                view
        );

    }


    // ============================================================
    // ATIVAR EDIÇÃO
    // ============================================================

    private void ativarEdicao() {

        modoEdicao = true;


        int quantidade =
                containerExercicios.getChildCount();


        for (
                int i = 0;
                i < quantidade;
                i++
        ) {

            View view =
                    containerExercicios.getChildAt(i);

            EditText edtNome =
                    view.findViewById(
                            R.id.edtNomeExercicio
                    );

            EditText edtSeries =
                    view.findViewById(
                            R.id.edtSeriesExercicio
                    );

            EditText edtRepeticoes =
                    view.findViewById(
                            R.id.edtRepeticoesExercicio
                    );


            edtNome.setEnabled(true);
            edtSeries.setEnabled(true);
            edtRepeticoes.setEnabled(true);

        }


        btnAdicionarExercicio.setVisibility(
                View.VISIBLE
        );

        btnEditarSalvar.setText(
                "SALVAR DADOS"
        );

    }


    // ============================================================
    // BLOQUEAR CAMPOS
    // ============================================================

    private void bloquearCampos() {

        modoEdicao = false;


        int quantidade =
                containerExercicios.getChildCount();


        for (
                int i = 0;
                i < quantidade;
                i++
        ) {

            View view =
                    containerExercicios.getChildAt(i);

            EditText edtNome =
                    view.findViewById(
                            R.id.edtNomeExercicio
                    );

            EditText edtSeries =
                    view.findViewById(
                            R.id.edtSeriesExercicio
                    );

            EditText edtRepeticoes =
                    view.findViewById(
                            R.id.edtRepeticoesExercicio
                    );


            edtNome.setEnabled(false);
            edtSeries.setEnabled(false);
            edtRepeticoes.setEnabled(false);

        }


        btnAdicionarExercicio.setVisibility(
                View.GONE
        );

        btnEditarSalvar.setText(
                "EDITAR DADOS"
        );

    }


    // ============================================================
    // SALVAR EXERCÍCIOS
    // ============================================================

    private void salvarExercicios() {

        int quantidade =
                containerExercicios.getChildCount();


        if (quantidade == 0) {

            Toast.makeText(
                    this,
                    "Adicione pelo menos um exercício.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        JSONArray exercicios =
                new JSONArray();


        try {

            for (
                    int i = 0;
                    i < quantidade;
                    i++
            ) {

                View view =
                        containerExercicios.getChildAt(i);


                EditText edtNome =
                        view.findViewById(
                                R.id.edtNomeExercicio
                        );

                EditText edtSeries =
                        view.findViewById(
                                R.id.edtSeriesExercicio
                        );

                EditText edtRepeticoes =
                        view.findViewById(
                                R.id.edtRepeticoesExercicio
                        );


                String nome =
                        edtNome.getText()
                                .toString()
                                .trim();


                String textoSeries =
                        edtSeries.getText()
                                .toString()
                                .trim();


                String textoRepeticoes =
                        edtRepeticoes.getText()
                                .toString()
                                .trim();


                if (nome.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Preencha o nome do exercício " +
                                    (i + 1) +
                                    ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (textoSeries.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Informe as séries do exercício " +
                                    (i + 1) +
                                    ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (textoRepeticoes.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Informe as repetições do exercício " +
                                    (i + 1) +
                                    ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                int series =
                        Integer.parseInt(
                                textoSeries
                        );

                int repeticoes =
                        Integer.parseInt(
                                textoRepeticoes
                        );


                JSONObject exercicio =
                        new JSONObject();


                exercicio.put(
                        "nome",
                        nome
                );

                exercicio.put(
                        "series",
                        series
                );

                exercicio.put(
                        "repeticoes",
                        repeticoes
                );


                exercicio.put(
                        "ordem",
                        i + 1
                );


                exercicios.put(
                        exercicio
                );

            }


        } catch (Exception erro) {

            Toast.makeText(
                    this,
                    "Verifique os valores informados.",
                    Toast.LENGTH_SHORT
            ).show();

            return;

        }


        JSONObject dados =
                new JSONObject();


        try {

            dados.put(
                    "id_ficha",
                    idFicha
            );

            dados.put(
                    "id_alunos",
                    idAluno
            );

            dados.put(
                    "exercicios",
                    exercicios
            );

        } catch (Exception erro) {

            return;
        }


        enviarExercicios(
                dados
        );

    }


    // ============================================================
    // ENVIAR PARA API
    // ============================================================

    private void enviarExercicios(
            JSONObject dados
    ) {

        btnEditarSalvar.setEnabled(
                false
        );


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                URL url =
                        new URL(
                                URL_EXERCICIOS
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


                OutputStream output =
                        conexao.getOutputStream();


                output.write(
                        dados.toString()
                                .getBytes(
                                        "UTF-8"
                                )
                );


                output.flush();
                output.close();


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
                        (linha = reader.readLine())
                                != null
                ) {

                    resposta.append(
                            linha
                    );

                }


                reader.close();


                JSONObject json =
                        new JSONObject(
                                resposta.toString()
                        );


                boolean sucesso =
                        json.optBoolean(
                                "sucesso",
                                false
                        );


                runOnUiThread(() -> {

                    btnEditarSalvar.setEnabled(
                            true
                    );


                    if (
                            sucesso &&
                                    codigo >= 200 &&
                                    codigo < 300
                    ) {

                        Toast.makeText(
                                ExerciciosProfessorActivity.this,
                                "Dados salvos com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();


                        bloquearCampos();


                    } else {

                        Toast.makeText(
                                ExerciciosProfessorActivity.this,
                                json.optString(
                                        "mensagem",
                                        "Erro ao salvar os exercícios."
                                ),
                                Toast.LENGTH_LONG
                        ).show();

                    }

                });


            } catch (Exception erro) {

                erro.printStackTrace();

                runOnUiThread(() -> {

                    btnEditarSalvar.setEnabled(
                            true
                    );

                    Toast.makeText(
                            ExerciciosProfessorActivity.this,
                            "Erro ao salvar os exercícios.",
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