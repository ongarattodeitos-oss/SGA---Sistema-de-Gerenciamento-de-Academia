package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.LayoutInflater;
import android.widget.ImageView;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class FichaTreinoActivity extends AppCompatActivity {

    // ============================================================
    // API
    // ============================================================

    private static final String URL_FICHAS = "https://sga-api.miguel-r-hoff.workers.dev/fichas-treino";


    // ============================================================
    // CAMPOS
    // ============================================================

    private int idAluno;

    private String nomeAluno;
    private String nomeUsuario;

    private TextView btnVoltarFicha;

    private TextView txtNomeAlunoFicha;
    private TextView txtUsuarioAlunoFicha;

    private TextView txtQuantidadeFichas;
    private TextView txtStatusFichas;

    private TextView btnNovaFicha;

    private TextView txtTituloFichas;
    private TextView txtStatusListaFichas;

    private LinearLayout containerFichas;
    private LinearLayout cardNenhumaFicha;


    // ============================================================
    // CICLO DE VIDA
    // ============================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_ficha_treino);


        // ========================================================
        // INSETS
        // ========================================================

        View main = findViewById(R.id.main);

        ViewCompat.setOnApplyWindowInsetsListener(main, (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);

            return insets;
        });


        // ========================================================
        // REFERÊNCIAS XML
        // ========================================================

        btnVoltarFicha = findViewById(R.id.btnVoltarFicha);

        txtNomeAlunoFicha = findViewById(R.id.txtNomeAlunoFicha);

        txtUsuarioAlunoFicha = findViewById(R.id.txtUsuarioAlunoFicha);

        txtQuantidadeFichas = findViewById(R.id.txtQuantidadeFichas);

        txtStatusFichas = findViewById(R.id.txtStatusFichas);

        btnNovaFicha = findViewById(R.id.btnNovaFicha);

        txtTituloFichas = findViewById(R.id.txtTituloFichas);

        txtStatusListaFichas = findViewById(R.id.txtStatusListaFichas);

        containerFichas = findViewById(R.id.containerFichas);

        cardNenhumaFicha = findViewById(R.id.cardNenhumaFicha);




        // ========================================================
        // RECEBER DADOS DA TELA ANTERIOR
        // ========================================================

        idAluno = getIntent().getIntExtra("id_alunos", -1);

        nomeAluno = getIntent().getStringExtra("nome_completo");

        nomeUsuario = getIntent().getStringExtra("nome_user");


        // ========================================================
        // VALIDAR ID
        // ========================================================

        if (idAluno == -1) {

            Toast.makeText(this, "Erro: aluno não encontrado.", Toast.LENGTH_LONG).show();

            finish();

            return;
        }


        // ========================================================
        // PREENCHER ALUNO
        // ========================================================

        if (nomeAluno == null || nomeAluno.trim().isEmpty()) {

            nomeAluno = "Aluno";
        }

        txtNomeAlunoFicha.setText(nomeAluno);


        if (nomeUsuario != null && !nomeUsuario.trim().isEmpty()) {

            txtUsuarioAlunoFicha.setText("@" + nomeUsuario);

        } else {

            txtUsuarioAlunoFicha.setText("Usuário não informado");

        }


        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltarFicha.setOnClickListener(v -> {

            finish();

        });


        // ========================================================
        // NOVA FICHA
        // ========================================================

        btnNovaFicha.setOnClickListener(v -> {

            Intent intent = new Intent(FichaTreinoActivity.this, CriarFichaTreinoActivity.class);

            intent.putExtra("id_alunos", idAluno);

            intent.putExtra("nome_completo", nomeAluno);

            intent.putExtra("nome_user", nomeUsuario);

            startActivity(intent);

        });
    }

    @Override
    protected void onResume() {

        super.onResume();

        if (idAluno != -1) {

            carregarFichas();

        }
    }

    // ============================================================
    // BUSCAR FICHAS NA API
    // ============================================================

    private void carregarFichas() {

        txtStatusFichas.setText("Carregando...");

        txtStatusListaFichas.setText("Buscando fichas de treino...");

        cardNenhumaFicha.setVisibility(View.GONE);


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                // ------------------------------------------------
                // MONTAR URL
                // ------------------------------------------------

                String endereco = URL_FICHAS + "?id_alunos=" + idAluno;


                URL url = new URL(endereco);

                conexao = (HttpURLConnection) url.openConnection();


                conexao.setRequestMethod("GET");

                conexao.setConnectTimeout(10000);

                conexao.setReadTimeout(10000);


                // ------------------------------------------------
                // RESPOSTA
                // ------------------------------------------------

                int codigoResposta = conexao.getResponseCode();


                InputStream inputStream;


                if (codigoResposta >= 200 && codigoResposta < 300) {

                    inputStream = conexao.getInputStream();

                } else {

                    inputStream = conexao.getErrorStream();

                }


                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));


                StringBuilder resposta = new StringBuilder();


                String linha;


                while ((linha = reader.readLine()) != null) {

                    resposta.append(linha);

                }


                reader.close();


                // ------------------------------------------------
                // PROCESSAR
                // ------------------------------------------------

                if (codigoResposta >= 200 && codigoResposta < 300) {

                    processarFichas(resposta.toString());

                } else {

                    runOnUiThread(() -> {

                        txtStatusFichas.setText("Erro");

                        txtStatusListaFichas.setText("Não foi possível carregar as fichas.");

                        Toast.makeText(FichaTreinoActivity.this, "Erro na API: " + codigoResposta, Toast.LENGTH_LONG).show();

                    });

                }


            } catch (Exception erro) {

                erro.printStackTrace();


                runOnUiThread(() -> {

                    txtStatusFichas.setText("Erro");

                    txtStatusListaFichas.setText("Não foi possível conectar ao servidor.");

                    Toast.makeText(FichaTreinoActivity.this, "Erro de conexão com o servidor.", Toast.LENGTH_LONG).show();

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

    private void processarFichas(String resposta) {

        try {

            JSONObject json = new JSONObject(resposta);


            boolean sucesso = json.optBoolean("sucesso", false);


            if (!sucesso) {

                String mensagem = json.optString("mensagem", "Erro ao buscar fichas.");


                runOnUiThread(() -> {

                    txtStatusFichas.setText("Erro");

                    txtStatusListaFichas.setText(mensagem);

                    cardNenhumaFicha.setVisibility(View.VISIBLE);

                });

                return;
            }


            JSONArray fichas = json.optJSONArray("fichas");


            if (fichas == null) {

                fichas = new JSONArray();

            }


            JSONArray fichasFinal = fichas;


            runOnUiThread(() -> {

                mostrarFichas(fichasFinal);

            });


        } catch (Exception erro) {

            erro.printStackTrace();


            runOnUiThread(() -> {

                txtStatusFichas.setText("Erro");

                txtStatusListaFichas.setText("Resposta inválida do servidor.");

                Toast.makeText(FichaTreinoActivity.this, "Erro ao processar as fichas.", Toast.LENGTH_LONG).show();

            });

        }

    }


    // ============================================================
    // MOSTRAR FICHAS
    // ============================================================

    private void mostrarFichas(JSONArray fichas) {

        containerFichas.removeAllViews();


        int quantidade = fichas.length();


        // --------------------------------------------------------
        // QUANTIDADE
        // --------------------------------------------------------

        txtQuantidadeFichas.setText(String.valueOf(quantidade));


        // --------------------------------------------------------
        // NENHUMA FICHA
        // --------------------------------------------------------

        if (quantidade == 0) {

            txtStatusFichas.setText("Nenhuma");

            txtTituloFichas.setText("Fichas do aluno");

            txtStatusListaFichas.setText("Este aluno ainda não possui fichas.");

            cardNenhumaFicha.setVisibility(View.VISIBLE);

            return;
        }


        // --------------------------------------------------------
        // EXISTEM FICHAS
        // --------------------------------------------------------

        txtStatusFichas.setText("Ativas");

        txtTituloFichas.setText("Fichas do aluno");

        txtStatusListaFichas.setText(quantidade == 1 ? "1 ficha encontrada." : quantidade + " fichas encontradas.");


        cardNenhumaFicha.setVisibility(View.GONE);


        // --------------------------------------------------------
        // INFLATER
        // --------------------------------------------------------

        LayoutInflater inflater = LayoutInflater.from(this);


        // --------------------------------------------------------
        // LOOP
        // --------------------------------------------------------

        for (int i = 0; i < quantidade; i++) {

            try {

                JSONObject ficha = fichas.getJSONObject(i);


                // ------------------------------------------------
                // INFLAR CARD
                // ------------------------------------------------

                View card = inflater.inflate(R.layout.item_ficha_treino, containerFichas, false);


                // ------------------------------------------------
                // CAMPOS
                // ------------------------------------------------

                TextView txtNomeFicha = card.findViewById(R.id.txtNomeFicha);


                TextView txtInfoFicha = card.findViewById(R.id.txtInfoFicha);


                TextView txtDataFicha = card.findViewById(R.id.txtDataFicha);


                TextView txtAcaoFicha = card.findViewById(R.id.txtAcaoFicha);

                ImageView btnExcluirFicha = card.findViewById(R.id.btnExcluirFicha);


                // ------------------------------------------------
                // DADOS
                // ------------------------------------------------

                int idFicha = ficha.optInt("id_ficha", -1);


                String nomeFicha = ficha.optString("nome_ficha", "Ficha de treino");


                String dataCriacao = ficha.optString("data_criacao", "");


                int quantidadeExercicios = ficha.optInt("quantidade_exercicios", 0);


                // ------------------------------------------------
                // NOME
                // ------------------------------------------------

                txtNomeFicha.setText(nomeFicha);


                // ------------------------------------------------
                // EXERCÍCIOS
                // ------------------------------------------------

                if (quantidadeExercicios == 0) {

                    txtInfoFicha.setText("Nenhum exercício");

                } else if (quantidadeExercicios == 1) {

                    txtInfoFicha.setText("1 exercício");

                } else {

                    txtInfoFicha.setText(quantidadeExercicios + " exercícios");

                }


                // ------------------------------------------------
                // DATA
                // ------------------------------------------------

                txtDataFicha.setText(formatarData(dataCriacao));


                // ------------------------------------------------
                // AÇÃO
                // ------------------------------------------------

                txtAcaoFicha.setText("›");


                // ------------------------------------------------
                // CLICK
                // ------------------------------------------------

                final int idFichaSelecionada = idFicha;


                final String nomeFichaSelecionada = nomeFicha;

                btnExcluirFicha.setOnClickListener(v -> {

                    confirmarExclusaoFicha(
                            idFichaSelecionada,
                            nomeFichaSelecionada
                    );

                });


                card.setOnClickListener(v -> {

                    if (idFichaSelecionada == -1) {

                        Toast.makeText(
                                FichaTreinoActivity.this,
                                "ID da ficha inválido.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    Intent intent = new Intent(
                            FichaTreinoActivity.this,
                            ExerciciosProfessorActivity.class
                    );

                    intent.putExtra(
                            "id_ficha",
                            idFichaSelecionada
                    );

                    intent.putExtra(
                            "id_alunos",
                            idAluno
                    );

                    intent.putExtra(
                            "nome_ficha",
                            nomeFichaSelecionada
                    );

                    startActivity(intent);

                });


                // ------------------------------------------------
                // ADICIONAR
                // ------------------------------------------------

                containerFichas.addView(card);


            } catch (Exception erro) {

                erro.printStackTrace();

            }

        }

    }


    // ============================================================
    // FORMATAR DATA
    // ============================================================

    private String formatarData(String data) {

        if (data == null || data.trim().isEmpty()) {

            return "Data não informada";

        }


        try {

            // SQLite normalmente retorna:
            // 2026-09-14 13:20:00

            if (data.length() >= 10) {

                String ano = data.substring(0, 4);

                String mes = data.substring(5, 7);

                String dia = data.substring(8, 10);


                return "Criado em " + dia + "/" + mes + "/" + ano;

            }


        } catch (Exception erro) {

            erro.printStackTrace();

        }


        return "Criado em " + data;

    }

    private void confirmarExclusaoFicha(
            int idFicha,
            String nomeFicha
    ) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Excluir ficha")
                .setMessage(
                        "Deseja realmente excluir a ficha \"" +
                                nomeFicha +
                                "\"?\n\n" +
                                "Todos os exercícios dessa ficha também serão excluídos."
                )
                .setNegativeButton(
                        "Cancelar",
                        null
                )
                .setPositiveButton(
                        "Excluir",
                        (dialog, which) -> {

                            excluirFicha(idFicha);

                        }
                )
                .show();
    }

    private void excluirFicha(int idFicha) {

        runOnUiThread(() -> {

            Toast.makeText(
                    FichaTreinoActivity.this,
                    "Excluindo ficha...",
                    Toast.LENGTH_SHORT
            ).show();

        });


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                // ------------------------------------------------
                // URL
                // ------------------------------------------------

                String endereco =
                        URL_FICHAS +
                                "?id_ficha=" +
                                idFicha +
                                "&id_alunos=" +
                                idAluno;


                URL url = new URL(endereco);

                conexao =
                        (HttpURLConnection) url.openConnection();


                // ------------------------------------------------
                // CONFIGURAÇÃO
                // ------------------------------------------------

                conexao.setRequestMethod("DELETE");

                conexao.setConnectTimeout(10000);

                conexao.setReadTimeout(10000);

                conexao.setRequestProperty(
                        "Content-Type",
                        "application/json"
                );


                // ------------------------------------------------
                // RESPOSTA
                // ------------------------------------------------

                int codigoResposta =
                        conexao.getResponseCode();


                InputStream inputStream;


                if (
                        codigoResposta >= 200 &&
                                codigoResposta < 300
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
                        (linha = reader.readLine()) != null
                ) {

                    resposta.append(linha);

                }


                reader.close();


                // ------------------------------------------------
                // PROCESSAR RESPOSTA
                // ------------------------------------------------

                JSONObject json =
                        new JSONObject(
                                resposta.toString()
                        );


                boolean sucesso =
                        json.optBoolean(
                                "sucesso",
                                false
                        );


                String mensagem =
                        json.optString(
                                "mensagem",
                                "Erro ao excluir ficha."
                        );


                if (
                        codigoResposta >= 200 &&
                                codigoResposta < 300 &&
                                sucesso
                ) {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                FichaTreinoActivity.this,
                                "Ficha excluída com sucesso.",
                                Toast.LENGTH_SHORT
                        ).show();


                        // Atualiza a lista
                        carregarFichas();

                    });


                } else {

                    runOnUiThread(() -> {

                        Toast.makeText(
                                FichaTreinoActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();

                    });

                }


            } catch (Exception erro) {

                erro.printStackTrace();


                runOnUiThread(() -> {

                    Toast.makeText(
                            FichaTreinoActivity.this,
                            "Erro ao excluir ficha.",
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