package com.example.sga;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
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
import java.net.HttpURLConnection;
import java.net.URL;

public class ProfessorListaTreinosActivity extends AppCompatActivity {

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
    // MENU
    // =========================================================

    private Button btnInicio;
    private Button btnAlunos;
    private Button btnTreinos;
    private Button btnPerfil;

    // =========================================================
    // COMPONENTES
    // =========================================================

    private LinearLayout containerAlunos;

    private TextView txtQuantidadeAlunos;
    private TextView txtStatusLista;
    private TextView txtTituloResultado;

    private LinearLayout cardNenhumAluno;

    private EditText edtPesquisarAluno;

    // =========================================================
    // TODOS OS ALUNOS
    // =========================================================

    private JSONArray todosAlunos = new JSONArray();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_professor_lista_treinos);

        // =====================================================
        // BARRAS DO SISTEMA
        // =====================================================

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

        // =====================================================
        // COMPONENTES
        // =====================================================

        containerAlunos =
                findViewById(R.id.containerAlunosTreino);

        txtQuantidadeAlunos =
                findViewById(R.id.txtQuantidadeAlunosTreino);

        txtStatusLista =
                findViewById(R.id.txtStatusListaTreino);

        txtTituloResultado =
                findViewById(R.id.txtTituloResultado);

        cardNenhumAluno =
                findViewById(R.id.cardNenhumAlunoTreino);

        edtPesquisarAluno =
                findViewById(R.id.edtPesquisarAlunoTreino);

        // =====================================================
        // MENU INFERIOR
        // =====================================================

        btnInicio =
                findViewById(R.id.btnInicioProfessor);

        btnAlunos =
                findViewById(R.id.btnAlunosProfessor);

        btnTreinos =
                findViewById(R.id.btnTreinosProfessor);

        btnPerfil =
                findViewById(R.id.btnPerfilProfessor);

        // =====================================================
        // BOTÃO INÍCIO
        // =====================================================

        btnInicio.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfessorListaTreinosActivity.this,
                    ProfessorActivity.class
            );

            startActivity(intent);
            finish();
        });

        // =====================================================
        // BOTÃO ALUNOS
        // =====================================================

        btnAlunos.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfessorListaTreinosActivity.this,
                    AlunosListaActivity.class
            );

            startActivity(intent);
            finish();
        });

        // =====================================================
        // BOTÃO TREINOS
        // =====================================================

        btnTreinos.setOnClickListener(v -> {

            selecionarBotao(btnTreinos);

        });

        // =====================================================
        // BOTÃO PERFIL
        // =====================================================

        btnPerfil.setOnClickListener(v -> {

            Intent intent = new Intent(
                    ProfessorListaTreinosActivity.this,
                    UsuarioProfessorActivity.class
            );

            startActivity(intent);
            finish();
        });

        // =====================================================
        // TREINOS SELECIONADO
        // =====================================================

        selecionarBotao(btnTreinos);

        // =====================================================
        // CARREGAR ALUNOS
        // =====================================================

        carregarAlunos();

        // =====================================================
        // PESQUISA
        // =====================================================

        edtPesquisarAluno.addTextChangedListener(
                new TextWatcher() {

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

                        filtrarAlunos(
                                s.toString()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {
                    }
                }
        );
    }


    // =========================================================
    // SELECIONAR BOTÃO
    // =========================================================

    private void selecionarBotao(Button botaoSelecionado) {

        btnInicio.setTextColor(COR_NORMAL);
        btnAlunos.setTextColor(COR_NORMAL);
        btnTreinos.setTextColor(COR_NORMAL);
        btnPerfil.setTextColor(COR_NORMAL);

        botaoSelecionado.setTextColor(
                COR_SELECIONADO
        );
    }


    // =========================================================
    // CARREGAR ALUNOS
    // =========================================================

    private void carregarAlunos() {

        txtStatusLista.setText(
                "Carregando alunos..."
        );

        cardNenhumAluno.setVisibility(
                View.GONE
        );

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                URL url =
                        new URL(URL_ALUNOS);

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
                        (linha = reader.readLine())
                                != null
                ) {

                    resposta.append(linha);
                }

                reader.close();

                if (
                        codigoResposta >= 200 &&
                                codigoResposta < 300
                ) {

                    processarRespostaAlunos(
                            resposta.toString()
                    );

                } else {

                    runOnUiThread(() -> {

                        txtStatusLista.setText(
                                "Erro ao carregar alunos"
                        );

                        Toast.makeText(
                                ProfessorListaTreinosActivity.this,
                                "Erro na API: "
                                        + codigoResposta,
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
                            ProfessorListaTreinosActivity.this,
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


    // =========================================================
    // PROCESSAR RESPOSTA
    // =========================================================

    private void processarRespostaAlunos(
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

                    txtStatusLista.setText(
                            "Erro ao buscar alunos"
                    );

                    cardNenhumAluno.setVisibility(
                            View.VISIBLE
                    );
                });

                return;
            }

            JSONArray alunos =
                    json.optJSONArray(
                            "alunos"
                    );

            if (alunos == null) {

                alunos =
                        new JSONArray();
            }

            JSONArray alunosFinal =
                    alunos;

            runOnUiThread(() -> {

                todosAlunos =
                        alunosFinal;

                mostrarAlunos(
                        todosAlunos
                );
            });

        } catch (Exception erro) {

            erro.printStackTrace();

            runOnUiThread(() -> {

                txtStatusLista.setText(
                        "Resposta inválida do servidor"
                );

                Toast.makeText(
                        ProfessorListaTreinosActivity.this,
                        "Erro ao processar os dados.",
                        Toast.LENGTH_LONG
                ).show();

            });
        }
    }


    // =========================================================
    // MOSTRAR ALUNOS
    // =========================================================

    private void mostrarAlunos(
            JSONArray alunos
    ) {

        containerAlunos.removeAllViews();

        int quantidade =
                alunos.length();

        int quantidadeTotal =
                todosAlunos.length();

        // =====================================================
        // QUANTIDADE
        // =====================================================

        txtQuantidadeAlunos.setText(
                String.valueOf(
                        quantidadeTotal
                )
        );

        // =====================================================
        // NENHUM ALUNO
        // =====================================================

        if (quantidade == 0) {

            txtTituloResultado.setText(
                    "Nenhum aluno encontrado"
            );

            txtStatusLista.setText(
                    "Tente pesquisar por outro nome."
            );

            cardNenhumAluno.setVisibility(
                    View.VISIBLE
            );

            return;
        }

        cardNenhumAluno.setVisibility(
                View.GONE
        );

        txtTituloResultado.setText(
                "Selecione um aluno"
        );

        txtStatusLista.setText(
                "Toque em um aluno para gerenciar o treino."
        );

        // =====================================================
        // INFLATER
        // =====================================================

        LayoutInflater inflater =
                LayoutInflater.from(this);

        // =====================================================
        // LOOP
        // =====================================================

        for (
                int i = 0;
                i < quantidade;
                i++
        ) {

            try {

                JSONObject aluno =
                        alunos.getJSONObject(i);

                // =====================================================
                // INFLAR CARD DO ALUNO PARA TREINOS
                // =====================================================

                View card =
                        inflater.inflate(
                                R.layout.item_aluno_treino,
                                containerAlunos,
                                false
                        );

                // =====================================================
                // COMPONENTES
                // =====================================================

                TextView txtNome =
                        card.findViewById(
                                R.id.txtNomeAlunoTreino
                        );

                TextView txtUsuario =
                        card.findViewById(
                                R.id.txtEmailAlunoTreino
                        );

                // =====================================================
                // DADOS DO ALUNO
                // =====================================================

                String nome =
                        aluno.optString(
                                "nome_completo",
                                "Aluno"
                        );

                String nomeUser =
                        aluno.optString(
                                "nome_user",
                                ""
                        );

                txtNome.setText(nome);

                if (!nomeUser.isEmpty()) {

                    txtUsuario.setText(
                            "@" + nomeUser
                    );

                } else {

                    txtUsuario.setText(
                            "Usuário não informado"
                    );
                }

                // =====================================================
                // CLIQUE NO ALUNO
                // =====================================================

                final JSONObject alunoSelecionado =
                        aluno;

                card.setOnClickListener(v -> {

                    int idAluno =
                            alunoSelecionado.optInt(
                                    "id_alunos",
                                    -1
                            );

                    String usuarioAluno =
                            alunoSelecionado.optString(
                                    "nome_user",
                                    ""
                            );

                    // -------------------------------------------------
                    // VERIFICAR ID
                    // -------------------------------------------------

                    if (idAluno == -1) {

                        Toast.makeText(
                                ProfessorListaTreinosActivity.this,
                                "Erro: ID do aluno não encontrado.",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    // -------------------------------------------------
                    // ABRIR TELA DE FICHAS
                    // -------------------------------------------------

                    Intent intent =
                            new Intent(
                                    ProfessorListaTreinosActivity.this,
                                    FichasTreinoActivity.class
                            );

                    intent.putExtra(
                            "id_alunos",
                            idAluno
                    );

                    intent.putExtra(
                            "nome_user",
                            usuarioAluno
                    );

                    intent.putExtra(
                            "nome_completo",
                            alunoSelecionado.optString(
                                    "nome_completo",
                                    "Aluno"
                            )
                    );

                    startActivity(intent);
                });

                // =====================================================
                // ADICIONAR CARD NA LISTA
                // =====================================================

                containerAlunos.addView(card);

            } catch (Exception erro) {

                erro.printStackTrace();
            }

        }
    }


    // =========================================================
    // FILTRAR ALUNOS
    // =========================================================

    private void filtrarAlunos(
            String textoPesquisa
    ) {

        JSONArray alunosFiltrados =
                new JSONArray();

        String pesquisa =
                textoPesquisa
                        .trim()
                        .toLowerCase();

        // =====================================================
        // PESQUISA VAZIA
        // =====================================================

        if (pesquisa.isEmpty()) {

            mostrarAlunos(
                    todosAlunos
            );

            return;
        }

        // =====================================================
        // PROCURAR
        // =====================================================

        for (
                int i = 0;
                i < todosAlunos.length();
                i++
        ) {

            try {

                JSONObject aluno =
                        todosAlunos.getJSONObject(i);

                String nome =
                        aluno.optString(
                                "nome_completo",
                                ""
                        );

                String nomeUser =
                        aluno.optString(
                                "nome_user",
                                ""
                        );

                boolean encontrouNome =
                        nome.toLowerCase()
                                .contains(
                                        pesquisa
                                );

                boolean encontrouUsuario =
                        nomeUser.toLowerCase()
                                .contains(
                                        pesquisa
                                );

                if (
                        encontrouNome ||
                                encontrouUsuario
                ) {

                    alunosFiltrados.put(
                            aluno
                    );
                }

            } catch (Exception erro) {

                erro.printStackTrace();
            }
        }

        mostrarAlunos(
                alunosFiltrados
        );
    }
}