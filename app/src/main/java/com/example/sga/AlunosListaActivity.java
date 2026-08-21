package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import java.text.Normalizer;

public class AlunosListaActivity extends AppCompatActivity {

    // =========================================================
    // URL DA API
    // =========================================================

    private static final String URL_ALUNOS =
            "https://sga-api.miguel-r-hoff.workers.dev/alunos-lista";

    // =========================================================
    // CORES
    // =========================================================

    private final int COR_SELECIONADO = 0xFF03C6FC;
    private final int COR_NORMAL = 0xFF657086;

    // =========================================================
    // BOTÕES DO MENU
    // =========================================================

    private Button btnInicio;
    private Button btnAlunos;
    private Button btnTreinos;
    private Button btnPerfil;

    // =========================================================
    // COMPONENTES DA LISTA
    // =========================================================

    private LinearLayout containerAlunos;
    private TextView txtQuantidadeAlunos;
    private TextView txtStatusLista;
    private LinearLayout cardNenhumAluno;

    private EditText edtPesquisarAluno;

    // Guarda todos os alunos carregados da API
    private JSONArray todosAlunos = new JSONArray();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_alunos_lista);

        // =========================================================
        // CONFIGURAÇÃO DAS BARRAS DO SISTEMA
        // =========================================================

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
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

        // =========================================================
        // COMPONENTES DA LISTA
        // =========================================================

        containerAlunos = findViewById(R.id.containerAlunos);
        txtQuantidadeAlunos = findViewById(R.id.txtQuantidadeAlunos);
        txtStatusLista = findViewById(R.id.txtStatusLista);
        cardNenhumAluno = findViewById(R.id.cardNenhumAluno);

        edtPesquisarAluno = findViewById(R.id.edtPesquisarAluno);

        // =========================================================
        // BOTÕES DO MENU
        // =========================================================

        btnInicio = findViewById(R.id.btnInicioProfessor);
        btnAlunos = findViewById(R.id.btnAlunosProfessor);
        btnTreinos = findViewById(R.id.btnTreinosProfessor);
        btnPerfil = findViewById(R.id.btnPerfilProfessor);

        edtPesquisarAluno.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(
                    CharSequence s,
                    int start,
                    int count,
                    int after
            ) {
            }

            @Override
            public void onTextChanged(
                    CharSequence s,
                    int start,
                    int before,
                    int count
            ) {

                filtrarAlunos(s.toString());

            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // =========================================================
        // BOTÃO INÍCIO
        // =========================================================

        btnInicio.setOnClickListener(v -> {

            Intent intent = new Intent(
                    AlunosListaActivity.this,
                    ProfessorActivity.class
            );

            startActivity(intent);
            finish();
        });

        // =========================================================
        // BOTÃO ALUNOS
        // =========================================================

        btnAlunos.setOnClickListener(v -> {

            selecionarBotao(btnAlunos);

        });

        // =========================================================
        // BOTÃO TREINOS
        // =========================================================

        btnTreinos.setOnClickListener(v -> {

            selecionarBotao(btnTreinos);

        });

        // =========================================================
        // BOTÃO PERFIL
        // =========================================================

        btnPerfil.setOnClickListener(v -> {

            selecionarBotao(btnPerfil);

        });

        // =========================================================
        // ALUNOS SELECIONADO
        // =========================================================

        selecionarBotao(btnAlunos);

        // =========================================================
        // CARREGAR ALUNOS
        // =========================================================

        carregarAlunos();
    }

    // =============================================================
    // SELECIONAR BOTÃO
    // =============================================================

    private void selecionarBotao(Button botaoSelecionado) {

        btnInicio.setTextColor(COR_NORMAL);
        btnAlunos.setTextColor(COR_NORMAL);
        btnTreinos.setTextColor(COR_NORMAL);
        btnPerfil.setTextColor(COR_NORMAL);

        botaoSelecionado.setTextColor(COR_SELECIONADO);
    }

    // =============================================================
    // CARREGAR ALUNOS
    // =============================================================

    private void carregarAlunos() {

        txtStatusLista.setText("Carregando alunos...");

        cardNenhumAluno.setVisibility(View.GONE);

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                URL url = new URL(URL_ALUNOS);

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);

                int codigoResposta = conexao.getResponseCode();

                InputStream inputStream;

                if (codigoResposta >= 200 && codigoResposta < 300) {
                    inputStream = conexao.getInputStream();
                } else {
                    inputStream = conexao.getErrorStream();
                }

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(inputStream)
                );

                StringBuilder resposta = new StringBuilder();

                String linha;

                while ((linha = reader.readLine()) != null) {
                    resposta.append(linha);
                }

                reader.close();

                if (codigoResposta >= 200 && codigoResposta < 300) {

                    processarRespostaAlunos(
                            resposta.toString()
                    );

                } else {

                    runOnUiThread(() -> {

                        txtStatusLista.setText(
                                "Erro ao carregar alunos"
                        );

                        Toast.makeText(
                                AlunosListaActivity.this,
                                "Erro na API: " + codigoResposta,
                                Toast.LENGTH_LONG
                        ).show();

                    });
                }

            } catch (Exception erro) {

                erro.printStackTrace();

                runOnUiThread(() -> {

                    txtStatusLista.setText(
                            "Não foi possível carregar os alunos"
                    );

                    Toast.makeText(
                            AlunosListaActivity.this,
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

    // =============================================================
    // PROCESSAR RESPOSTA DA API
    // =============================================================

    private void processarRespostaAlunos(String resposta) {

        try {

            JSONObject json = new JSONObject(resposta);

            boolean sucesso = json.optBoolean(
                    "sucesso",
                    false
            );

            if (!sucesso) {

                runOnUiThread(() -> {

                    txtStatusLista.setText(
                            "Erro ao buscar alunos"
                    );

                    cardNenhumAluno.setVisibility(
                            View.VISIBLE
                    );

                });

                return;
            }

            JSONArray alunos = json.optJSONArray("alunos");

            if (alunos == null) {
                alunos = new JSONArray();
            }

            todosAlunos = ordenarAlunosPorNome(alunos);

            runOnUiThread(() -> {

                mostrarAlunos(todosAlunos);

            });

        } catch (Exception erro) {

            erro.printStackTrace();

            runOnUiThread(() -> {

                txtStatusLista.setText(
                        "Resposta inválida do servidor"
                );

                Toast.makeText(
                        AlunosListaActivity.this,
                        "Erro ao processar os dados.",
                        Toast.LENGTH_LONG
                ).show();

            });
        }
    }

    // =============================================================
    // MOSTRAR ALUNOS
    // =============================================================

    private void mostrarAlunos(JSONArray alunos) {

        containerAlunos.removeAllViews();

        // Quantidade dos alunos que estão sendo exibidos
        int quantidade = alunos.length();

// Sempre mostra a quantidade TOTAL cadastrada
        txtQuantidadeAlunos.setText(
                String.valueOf(todosAlunos.length())
        );

        if (quantidade == 0) {

            txtStatusLista.setText(
                    "Nenhum aluno cadastrado"
            );

            cardNenhumAluno.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        cardNenhumAluno.setVisibility(
                View.GONE
        );

        txtStatusLista.setText(
                "alunos cadastrados"
        );

        LayoutInflater inflater =
                LayoutInflater.from(this);

        for (int i = 0; i < quantidade; i++) {

            try {

                JSONObject aluno =
                        alunos.getJSONObject(i);

                View card = inflater.inflate(
                        R.layout.item_aluno,
                        containerAlunos,
                        false
                );

                TextView txtNomeAluno =
                        card.findViewById(
                                R.id.txtNomeAluno
                        );

                TextView txtEmailAluno =
                        card.findViewById(
                                R.id.txtEmailAluno
                        );

                TextView txtStatusAluno =
                        card.findViewById(
                                R.id.txtStatusAluno
                        );

                // =============================================
                // DADOS DO ALUNO
                // =============================================

                String nome = aluno.optString(
                        "nome_completo",
                        "Aluno"
                );

                String email = aluno.optString(
                        "email",
                        ""
                );

                String plano = aluno.optString(
                        "plano",
                        ""
                );

                txtNomeAluno.setText(nome);

                txtEmailAluno.setText(email);

                if (!plano.isEmpty()) {

                    txtStatusAluno.setText(
                            "ALUNO ATIVO  •  " + plano
                    );

                } else {

                    txtStatusAluno.setText(
                            "ALUNO ATIVO"
                    );
                }

                // =============================================
                // CLIQUE NO ALUNO
                // =============================================

                final JSONObject alunoSelecionado =
                        aluno;

                card.setOnClickListener(v -> {

                    int idAluno =
                            alunoSelecionado.optInt(
                                    "id_alunos",
                                    -1
                            );

                    Toast.makeText(
                            AlunosListaActivity.this,
                            "Aluno selecionado: " + idAluno,
                            Toast.LENGTH_SHORT
                    ).show();

                    // Depois vamos usar esse ID
                    // para abrir o perfil do aluno.

                });

                containerAlunos.addView(card);

            } catch (Exception erro) {

                erro.printStackTrace();
            }
        }
    }

    // =============================================================
// FILTRAR ALUNOS PELO NOME
// =============================================================

    private void filtrarAlunos(String textoPesquisa) {

        JSONArray alunosFiltrados = new JSONArray();

        String pesquisaNormalizada =
                normalizarTexto(textoPesquisa);

        try {

            for (int i = 0; i < todosAlunos.length(); i++) {

                JSONObject aluno =
                        todosAlunos.getJSONObject(i);

                String nome =
                        aluno.optString(
                                "nome_completo",
                                ""
                        );

                String nomeNormalizado =
                        normalizarTexto(nome);

                // Se o nome contém o que foi pesquisado
                if (nomeNormalizado.contains(
                        pesquisaNormalizada
                )) {

                    alunosFiltrados.put(aluno);

                }
            }

        } catch (Exception erro) {

            erro.printStackTrace();

        }

        mostrarAlunos(alunosFiltrados);
    }

    // =============================================================
// FILTRAR ALUNOS PELO NOME
// =============================================================

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        String textoNormalizado =
                Normalizer.normalize(
                        texto,
                        Normalizer.Form.NFD
                );

        textoNormalizado =
                textoNormalizado.replaceAll(
                        "\\p{M}",
                        ""
                );

        return textoNormalizado
                .toLowerCase()
                .trim();
    }

    private JSONArray ordenarAlunosPorNome(JSONArray alunos) {

        List<JSONObject> listaAlunos = new ArrayList<>();

        try {

            // Coloca todos os alunos em uma lista
            for (int i = 0; i < alunos.length(); i++) {

                listaAlunos.add(
                        alunos.getJSONObject(i)
                );
            }

            // Ordena pelo nome
            Collections.sort(
                    listaAlunos,
                    (aluno1, aluno2) -> {

                        String nome1 = aluno1.optString(
                                "nome_completo",
                                ""
                        );

                        String nome2 = aluno2.optString(
                                "nome_completo",
                                ""
                        );

                        return normalizarTexto(nome1)
                                .compareTo(
                                        normalizarTexto(nome2)
                                );
                    }
            );

        } catch (Exception erro) {

            erro.printStackTrace();

        }

        // Cria um novo JSONArray já ordenado
        JSONArray alunosOrdenados = new JSONArray();

        for (JSONObject aluno : listaAlunos) {

            alunosOrdenados.put(aluno);

        }

        return alunosOrdenados;
    }

}