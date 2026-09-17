package com.example.sga;

import android.os.Bundle;
import android.text.InputType;
import android.widget.ImageView;
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

import java.util.ArrayList;
import java.util.List;


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

    private LinearLayout cardNenhumExercicio;


    // ============================================================
    // CAMPOS
    // ============================================================

    private TextView btnVoltar;

    private TextView txtNomeFicha;

    private LinearLayout containerExercicios;

    private TextView btnAdicionarExercicio;

    private TextView btnEditarSalvar;

    // IDs de exercícios já existentes no banco que foram marcados
    // para exclusão durante a edição atual (só são excluídos de
    // verdade quando o usuário clicar em SALVAR).
    private List<Integer> idsParaExcluir = new ArrayList<>();


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

        cardNenhumExercicio =
                findViewById(
                        R.id.cardNenhumExercicio
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

            // Some com a mensagem de "nenhum exercício" ao criar o primeiro campo
            cardNenhumExercicio.setVisibility(View.GONE);

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

            mostrarEstadoVazio();

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

                int idExercicio =
                        exercicio.optInt(
                                "id_exercicios",
                                0
                        );


                adicionarExercicio(
                        idExercicio,
                        nome,
                        series,
                        repeticoes
                );


            } catch (Exception erro) {

                erro.printStackTrace();

            }

        }

        cardNenhumExercicio.setVisibility(View.GONE);
        bloquearCampos();

    }


    // ============================================================
    // ADICIONAR EXERCÍCIO VAZIO
    // ============================================================

    private void adicionarExercicioVazio() {

        adicionarExercicio(
                0,
                "",
                0,
                0
        );

    }


    // ============================================================
    // ADICIONAR EXERCÍCIO
    // ============================================================

    private void adicionarExercicio(
            int idExercicio,
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

        ImageView btnExcluir =
                view.findViewById(
                        R.id.btnExcluirExercicio
                );


        // Guarda o ID do exercício na própria view (0 = ainda não existe no banco)
        view.setTag(
                idExercicio
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


        edtNome.setEnabled(modoEdicao);
        edtSeries.setEnabled(modoEdicao);
        edtRepeticoes.setEnabled(modoEdicao);

        btnExcluir.setVisibility(
                modoEdicao ? View.VISIBLE : View.GONE
        );

        // ========================================================
        // EXCLUIR (apenas localmente — a exclusão de verdade no
        // banco só acontece quando o usuário clicar em SALVAR)
        // ========================================================

        btnExcluir.setOnClickListener(v -> {

            new android.app.AlertDialog.Builder(ExerciciosProfessorActivity.this)
                    .setMessage("Deseja excluir esse exercício?")
                    .setPositiveButton("Sim", (dialog, which) -> {

                        int idSalvo =
                                (int) view.getTag();

                        if (idSalvo > 0) {

                            // Já existe no banco → só marca para excluir no próximo SALVAR
                            idsParaExcluir.add(idSalvo);

                        }

                        // Remove da tela de qualquer forma (existente ou não)
                        containerExercicios.removeView(view);
                        renumerarExercicios();

                        if (containerExercicios.getChildCount() == 0) {
                            cardNenhumExercicio.setVisibility(View.VISIBLE);
                        }

                    })
                    .setNegativeButton("Cancelar", null)
                    .show();

        });


        containerExercicios.addView(
                view
        );

    }


    // ============================================================
    // ATIVAR EDIÇÃO
    // ============================================================

    private void ativarEdicao() {

        idsParaExcluir.clear();

        modoEdicao = true;

        // Se estiver vazio, mantém a mensagem visível e só libera o botão de adicionar
        if (containerExercicios.getChildCount() == 0) {

            btnAdicionarExercicio.setVisibility(View.VISIBLE);
            btnEditarSalvar.setText("SALVAR DADOS");

            return;
        }

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

            ImageView btnExcluir =
                    view.findViewById(
                            R.id.btnExcluirExercicio
                    );


            edtNome.setEnabled(true);
            edtSeries.setEnabled(true);
            edtRepeticoes.setEnabled(true);

            btnExcluir.setVisibility(View.VISIBLE);

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

        int quantidade = containerExercicios.getChildCount();

        if (quantidade == 0) {

            mostrarEstadoVazio();
            return;
        }

        for (int i = 0; i < quantidade; i++) {

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

            ImageView btnExcluir =
                    view.findViewById(
                            R.id.btnExcluirExercicio
                    );


            edtNome.setEnabled(false);
            edtSeries.setEnabled(false);
            edtRepeticoes.setEnabled(false);

            btnExcluir.setVisibility(View.GONE);

        }


        btnAdicionarExercicio.setVisibility(
                View.GONE
        );

        btnEditarSalvar.setText(
                "EDITAR DADOS"
        );

    }

    private void mostrarEstadoVazio() {

        modoEdicao = false;

        containerExercicios.removeAllViews();

        cardNenhumExercicio.setVisibility(View.VISIBLE);

        btnAdicionarExercicio.setVisibility(View.GONE);

        btnEditarSalvar.setText("EDITAR DADOS");

    }


    // ============================================================
    // SALVAR EXERCÍCIOS
    // ============================================================


// ============================================================
// SALVAR EXERCÍCIOS
// ============================================================

    private void salvarExercicios() {

        int quantidade = containerExercicios.getChildCount();

        List<View> viewsVazias = new ArrayList<>();

        // Cada item:
        // [0] = id_exercicios
        // [1] = nome
        // [2] = series
        // [3] = repeticoes
        List<String[]> valoresPreenchidos = new ArrayList<>();

        try {

            for (int i = 0; i < quantidade; i++) {

                View view = containerExercicios.getChildAt(i);

                EditText edtNome =
                        view.findViewById(R.id.edtNomeExercicio);

                EditText edtSeries =
                        view.findViewById(R.id.edtSeriesExercicio);

                EditText edtRepeticoes =
                        view.findViewById(R.id.edtRepeticoesExercicio);


                String nome =
                        edtNome.getText().toString().trim();

                String textoSeries =
                        edtSeries.getText().toString().trim();

                String textoRepeticoes =
                        edtRepeticoes.getText().toString().trim();


                boolean tudoVazio =
                        nome.isEmpty()
                                && textoSeries.isEmpty()
                                && textoRepeticoes.isEmpty();


                // ----------------------------------------------------
                // CAMPO COMPLETAMENTE VAZIO
                // ----------------------------------------------------

                if (tudoVazio) {

                    int idSalvo =
                            (int) view.getTag();

                    if (idSalvo > 0) {

                        if (!idsParaExcluir.contains(idSalvo)) {

                            idsParaExcluir.add(idSalvo);

                        }

                    }

                    viewsVazias.add(view);

                    continue;
                }


                // ----------------------------------------------------
                // VALIDAR NOME
                // ----------------------------------------------------

                if (nome.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Preencha o nome do exercício " + (i + 1) + ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // ----------------------------------------------------
                // VALIDAR SÉRIES
                // ----------------------------------------------------

                if (textoSeries.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Informe as séries do exercício " + (i + 1) + ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // ----------------------------------------------------
                // VALIDAR REPETIÇÕES
                // ----------------------------------------------------

                if (textoRepeticoes.isEmpty()) {

                    Toast.makeText(
                            this,
                            "Informe as repetições do exercício " + (i + 1) + ".",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                int series =
                        Integer.parseInt(textoSeries);

                int repeticoes =
                        Integer.parseInt(textoRepeticoes);


                if (series <= 0) {

                    Toast.makeText(
                            this,
                            "As séries devem ser maiores que zero.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                if (repeticoes <= 0) {

                    Toast.makeText(
                            this,
                            "As repetições devem ser maiores que zero.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                // ----------------------------------------------------
                // PEGAR ID ORIGINAL
                // ----------------------------------------------------

                int idExercicio =
                        (int) view.getTag();


                valoresPreenchidos.add(
                        new String[]{
                                String.valueOf(idExercicio),
                                nome,
                                String.valueOf(series),
                                String.valueOf(repeticoes)
                        }
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


        // ============================================================
        // REMOVER CAMPOS VAZIOS DA TELA
        // ============================================================

        for (View viewVazia : viewsVazias) {

            containerExercicios.removeView(
                    viewVazia
            );

        }

        renumerarExercicios();


        // ============================================================
        // MONTAR JSON
        // ============================================================

        JSONArray exercicios =
                new JSONArray();

        try {

            for (
                    int i = 0;
                    i < valoresPreenchidos.size();
                    i++
            ) {

                String[] valores =
                        valoresPreenchidos.get(i);

                JSONObject exercicio =
                        new JSONObject();


                int idExercicio =
                        Integer.parseInt(
                                valores[0]
                        );


                /*
                 * ID > 0:
                 * exercício já existe no banco.
                 *
                 * ID = 0:
                 * exercício novo.
                 */

                exercicio.put(
                        "id_exercicios",
                        idExercicio
                );

                exercicio.put(
                        "nome",
                        valores[1]
                );

                exercicio.put(
                        "series",
                        Integer.parseInt(
                                valores[2]
                        )
                );

                exercicio.put(
                        "repeticoes",
                        Integer.parseInt(
                                valores[3]
                        )
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
                    "Erro ao preparar os exercícios.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ============================================================
        // EXERCÍCIOS EXCLUÍDOS
        // ============================================================

        JSONArray excluidos =
                new JSONArray();


        for (int id : idsParaExcluir) {

            if (id > 0) {

                excluidos.put(id);

            }

        }


        // ============================================================
        // MONTAR OBJETO FINAL
        // ============================================================

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

            dados.put(
                    "excluidos",
                    excluidos
            );


        } catch (Exception erro) {

            Toast.makeText(
                    this,
                    "Erro ao preparar os dados.",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }


        // ============================================================
        // ENVIAR
        // ============================================================

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


                        // --------------------------------------------------------
                        // IMPORTANTE:
                        // Recarrega os exercícios diretamente do banco.
                        //
                        // Isso faz com que exercícios novos recebam o
                        // id_exercicios real gerado pelo banco.
                        //
                        // Também garante que exercícios excluídos realmente
                        // desapareçam da lista.
                        // --------------------------------------------------------

                        idsParaExcluir.clear();

                        carregarExercicios();


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

    private void renumerarExercicios() {

        int quantidade = containerExercicios.getChildCount();

        for (int i = 0; i < quantidade; i++) {

            View view = containerExercicios.getChildAt(i);

            TextView txtNumero = view.findViewById(R.id.txtNumeroExercicio);

            txtNumero.setText("Exercício " + (i + 1));
        }
    }

}