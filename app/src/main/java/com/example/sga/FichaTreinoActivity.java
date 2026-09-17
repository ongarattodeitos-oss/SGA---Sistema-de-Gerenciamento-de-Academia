package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

public class FichaTreinoActivity extends AppCompatActivity {

    // ============================================================
    // API
    // ============================================================

    private static final String URL_FICHAS =
            "https://sga-api.miguel-r-hoff.workers.dev/fichas-treino";


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
    private TextView btnEditarFichas;

    private TextView txtTituloFichas;
    private TextView txtStatusListaFichas;

    private LinearLayout containerFichas;
    private LinearLayout cardNenhumaFicha;


    // ============================================================
    // CONTROLE DE EDIÇÃO
    // ============================================================

    private boolean modoEdicao = false;

    private final ArrayList<FichaItem> listaFichas = new ArrayList<>();

    private final ArrayList<Integer> fichasExcluidas = new ArrayList<>();


    // ============================================================
    // MODELO DA FICHA
    // ============================================================
    private static class FichaItem {

        int idFicha;

        String nomeFicha;

        String dataCriacao;

        int quantidadeExercicios;

        EditText campoNome;


        FichaItem(
                int idFicha,
                String nomeFicha,
                String dataCriacao,
                int quantidadeExercicios
        ) {

            this.idFicha = idFicha;

            this.nomeFicha = nomeFicha;

            this.dataCriacao = dataCriacao;

            this.quantidadeExercicios = quantidadeExercicios;

            this.campoNome = null;
        }
    }



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

            Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            v.setPadding(
                    systemBars.left,
                    systemBars.top,
                    systemBars.right,
                    systemBars.bottom
            );

            return insets;
        });


        // ========================================================
        // REFERÊNCIAS XML
        // ========================================================

        btnVoltarFicha =
                findViewById(R.id.btnVoltarFicha);

        txtNomeAlunoFicha =
                findViewById(R.id.txtNomeAlunoFicha);

        txtUsuarioAlunoFicha =
                findViewById(R.id.txtUsuarioAlunoFicha);

        txtQuantidadeFichas =
                findViewById(R.id.txtQuantidadeFichas);

        txtStatusFichas =
                findViewById(R.id.txtStatusFichas);

        btnNovaFicha =
                findViewById(R.id.btnNovaFicha);

        btnEditarFichas =
                findViewById(R.id.btnEditarFichas);

        txtTituloFichas =
                findViewById(R.id.txtTituloFichas);

        txtStatusListaFichas =
                findViewById(R.id.txtStatusListaFichas);

        containerFichas =
                findViewById(R.id.containerFichas);

        cardNenhumaFicha =
                findViewById(R.id.cardNenhumaFicha);


        // ========================================================
        // RECEBER DADOS
        // ========================================================

        idAluno =
                getIntent().getIntExtra("id_alunos", -1);

        nomeAluno =
                getIntent().getStringExtra("nome_completo");

        nomeUsuario =
                getIntent().getStringExtra("nome_user");


        // ========================================================
        // VALIDAR ID
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

        if (nomeAluno == null ||
                nomeAluno.trim().isEmpty()) {

            nomeAluno = "Aluno";
        }

        txtNomeAlunoFicha.setText(nomeAluno);


        if (nomeUsuario != null &&
                !nomeUsuario.trim().isEmpty()) {

            txtUsuarioAlunoFicha.setText(
                    "@" + nomeUsuario
            );

        } else {

            txtUsuarioAlunoFicha.setText(
                    "Usuário não informado"
            );
        }


        // ========================================================
        // VOLTAR
        // ========================================================

        btnVoltarFicha.setOnClickListener(v -> {

            if (modoEdicao) {

                sairModoEdicao();

            } else {

                finish();

            }

        });


        // ========================================================
        // NOVA FICHA
        // ========================================================

        btnNovaFicha.setOnClickListener(v -> {

            if (!modoEdicao) {

                entrarModoEdicao();
            }

            adicionarNovaFicha();

        });


        // ========================================================
        // EDITAR / SALVAR
        // ========================================================

        btnEditarFichas.setOnClickListener(v -> {

            if (!modoEdicao) {

                entrarModoEdicao();

            } else {

                salvarFichas();

            }

        });

    }


    // ============================================================
    // RESUME
    // ============================================================

    @Override
    protected void onResume() {

        super.onResume();

        if (idAluno != -1 &&
                !modoEdicao) {

            carregarFichas();

        }

    }


    // ============================================================
    // ENTRAR NO MODO DE EDIÇÃO
    // ============================================================

    private void entrarModoEdicao() {

        modoEdicao = true;

        fichasExcluidas.clear();

        btnEditarFichas.setText("SALVAR");

        txtStatusFichas.setText("Editando");

        txtStatusListaFichas.setText(
                "Edite as fichas ou adicione novas."
        );

        mostrarFichasEdicao();

    }


    // ============================================================
    // SAIR DO MODO DE EDIÇÃO
    // ============================================================

    private void sairModoEdicao() {

        modoEdicao = false;

        fichasExcluidas.clear();

        btnEditarFichas.setText("EDITAR");

        mostrarFichasNormal();

    }


    // ============================================================
    // BUSCAR FICHAS
    // ============================================================

    private void carregarFichas() {

        txtStatusFichas.setText("Carregando...");

        txtStatusListaFichas.setText(
                "Buscando fichas de treino..."
        );

        cardNenhumaFicha.setVisibility(View.GONE);


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                String endereco =
                        URL_FICHAS +
                                "?id_alunos=" +
                                idAluno;

                URL url =
                        new URL(endereco);

                conexao =
                        (HttpURLConnection)
                                url.openConnection();

                conexao.setRequestMethod("GET");

                conexao.setConnectTimeout(10000);

                conexao.setReadTimeout(10000);


                int codigoResposta =
                        conexao.getResponseCode();


                InputStream inputStream;

                if (codigoResposta >= 200 &&
                        codigoResposta < 300) {

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

                while ((linha =
                        reader.readLine()) != null) {

                    resposta.append(linha);
                }

                reader.close();


                if (codigoResposta >= 200 &&
                        codigoResposta < 300) {

                    processarFichas(
                            resposta.toString()
                    );

                } else {

                    runOnUiThread(() -> {

                        txtStatusFichas.setText(
                                "Erro"
                        );

                        txtStatusListaFichas.setText(
                                "Não foi possível carregar as fichas."
                        );

                        Toast.makeText(
                                FichaTreinoActivity.this,
                                "Erro na API: " +
                                        codigoResposta,
                                Toast.LENGTH_LONG
                        ).show();

                    });

                }

            } catch (Exception erro) {

                erro.printStackTrace();

                runOnUiThread(() -> {

                    txtStatusFichas.setText(
                            "Erro"
                    );

                    txtStatusListaFichas.setText(
                            "Não foi possível conectar ao servidor."
                    );

                    Toast.makeText(
                            FichaTreinoActivity.this,
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


    // ============================================================
    // PROCESSAR JSON
    // ============================================================

    private void processarFichas(String resposta) {

        try {

            JSONObject json =
                    new JSONObject(resposta);


            boolean sucesso =
                    json.optBoolean(
                            "sucesso",
                            false
                    );


            if (!sucesso) {

                String mensagem =
                        json.optString(
                                "mensagem",
                                "Erro ao buscar fichas."
                        );


                runOnUiThread(() -> {

                    txtStatusFichas.setText(
                            "Erro"
                    );

                    txtStatusListaFichas.setText(
                            mensagem
                    );

                    cardNenhumaFicha.setVisibility(
                            View.VISIBLE
                    );

                });

                return;
            }


            JSONArray fichas =
                    json.optJSONArray("fichas");


            if (fichas == null) {

                fichas =
                        new JSONArray();
            }


            listaFichas.clear();


            for (int i = 0;
                 i < fichas.length();
                 i++) {

                JSONObject ficha =
                        fichas.getJSONObject(i);


                int idFicha =
                        ficha.optInt(
                                "id_ficha",
                                -1
                        );


                String nomeFicha =
                        ficha.optString(
                                "nome_ficha",
                                "Ficha de treino"
                        );


                String dataCriacao =
                        ficha.optString(
                                "data_criacao",
                                ""
                        );


                int quantidadeExercicios =
                        ficha.optInt(
                                "quantidade_exercicios",
                                0
                        );


                listaFichas.add(
                        new FichaItem(
                                idFicha,
                                nomeFicha,
                                dataCriacao,
                                quantidadeExercicios
                        )
                );
            }


            runOnUiThread(() ->
                    mostrarFichasNormal()
            );


        } catch (Exception erro) {

            erro.printStackTrace();

            runOnUiThread(() -> {

                txtStatusFichas.setText(
                        "Erro"
                );

                txtStatusListaFichas.setText(
                        "Resposta inválida do servidor."
                );

                Toast.makeText(
                        FichaTreinoActivity.this,
                        "Erro ao processar as fichas.",
                        Toast.LENGTH_LONG
                ).show();

            });

        }

    }


    // ============================================================
    // MOSTRAR FICHAS NORMALMENTE
    // ============================================================

    private void mostrarFichasNormal() {

        containerFichas.removeAllViews();


        int quantidade =
                listaFichas.size();


        txtQuantidadeFichas.setText(
                String.valueOf(quantidade)
        );


        if (quantidade == 0) {

            txtStatusFichas.setText(
                    "Nenhuma"
            );

            txtTituloFichas.setText(
                    "Fichas do aluno"
            );

            txtStatusListaFichas.setText(
                    "Este aluno ainda não possui fichas."
            );

            cardNenhumaFicha.setVisibility(
                    View.VISIBLE
            );

            return;
        }


        txtStatusFichas.setText(
                "Ativas"
        );

        txtTituloFichas.setText(
                "Fichas do aluno"
        );

        txtStatusListaFichas.setText(
                quantidade == 1
                        ? "1 ficha encontrada."
                        : quantidade +
                        " fichas encontradas."
        );


        cardNenhumaFicha.setVisibility(
                View.GONE
        );


        LayoutInflater inflater =
                LayoutInflater.from(this);


        for (FichaItem ficha :
                listaFichas) {

            View card =
                    inflater.inflate(
                            R.layout.item_ficha_treino,
                            containerFichas,
                            false
                    );


            TextView txtNomeFicha =
                    card.findViewById(
                            R.id.txtNomeFicha
                    );


            TextView txtInfoFicha =
                    card.findViewById(
                            R.id.txtInfoFicha
                    );


            TextView txtDataFicha =
                    card.findViewById(
                            R.id.txtDataFicha
                    );


            TextView txtAcaoFicha =
                    card.findViewById(
                            R.id.txtAcaoFicha
                    );


            TextView txtIconeFicha =
                    card.findViewById(
                            R.id.txtIconeFicha
                    );


            txtNomeFicha.setText(
                    ficha.nomeFicha
            );


            if (ficha.quantidadeExercicios == 0) {

                txtInfoFicha.setText(
                        "Nenhum exercício"
                );

            } else if (
                    ficha.quantidadeExercicios == 1
            ) {

                txtInfoFicha.setText(
                        "1 exercício"
                );

            } else {

                txtInfoFicha.setText(
                        ficha.quantidadeExercicios +
                                " exercícios"
                );
            }


            txtDataFicha.setText(
                    formatarData(
                            ficha.dataCriacao
                    )
            );


            txtIconeFicha.setText("▣");

            txtAcaoFicha.setText("›");


            // ====================================================
            // CLICK DA FICHA
            // ====================================================

            card.setOnClickListener(v -> {

                if (modoEdicao) {

                    return;
                }


                if (ficha.idFicha <= 0) {

                    Toast.makeText(
                            FichaTreinoActivity.this,
                            "ID da ficha inválido.",
                            Toast.LENGTH_SHORT
                    ).show();

                    return;
                }


                Intent intent =
                        new Intent(
                                FichaTreinoActivity.this,
                                ExerciciosProfessorActivity.class
                        );


                intent.putExtra(
                        "id_ficha",
                        ficha.idFicha
                );


                intent.putExtra(
                        "id_alunos",
                        idAluno
                );


                intent.putExtra(
                        "nome_ficha",
                        ficha.nomeFicha
                );


                startActivity(intent);

            });


            containerFichas.addView(card);
        }

    }

    private void mostrarFichasEdicao() {

        containerFichas.removeAllViews();

        int quantidade = listaFichas.size();

        txtQuantidadeFichas.setText(
                String.valueOf(quantidade)
        );


        if (quantidade == 0) {

            cardNenhumaFicha.setVisibility(
                    View.VISIBLE
            );

        } else {

            cardNenhumaFicha.setVisibility(
                    View.GONE
            );
        }


        LayoutInflater inflater =
                LayoutInflater.from(this);


        for (int i = 0; i < listaFichas.size(); i++) {

            FichaItem ficha =
                    listaFichas.get(i);


            View card =
                    inflater.inflate(
                            R.layout.item_ficha_treino,
                            containerFichas,
                            false
                    );


            TextView txtNomeFicha =
                    card.findViewById(
                            R.id.txtNomeFicha
                    );

            TextView txtInfoFicha =
                    card.findViewById(
                            R.id.txtInfoFicha
                    );

            TextView txtDataFicha =
                    card.findViewById(
                            R.id.txtDataFicha
                    );

            TextView txtAcaoFicha =
                    card.findViewById(
                            R.id.txtAcaoFicha
                    );

            TextView txtIconeFicha =
                    card.findViewById(
                            R.id.txtIconeFicha
                    );


            // ========================================================
            // LOCAL ONDE FICAVA O NOME
            // ========================================================

            ViewGroup parent =
                    (ViewGroup) txtNomeFicha.getParent();


            int posicaoNome =
                    parent.indexOfChild(
                            txtNomeFicha
                    );


            // ========================================================
            // EDITTEXT
            // ========================================================

            EditText editNome =
                    new EditText(this);


            ViewGroup.LayoutParams parametros =
                    txtNomeFicha.getLayoutParams();


            editNome.setLayoutParams(
                    parametros
            );


            editNome.setText(
                    ficha.nomeFicha
            );


            editNome.setTextColor(
                    android.graphics.Color.WHITE
            );


            editNome.setTextSize(16);


            editNome.setSingleLine(true);


            editNome.setPadding(
                    14,
                    0,
                    14,
                    0
            );


            editNome.setHint(
                    "Nome da ficha"
            );


            editNome.setHintTextColor(
                    android.graphics.Color.parseColor(
                            "#657086"
                    )
            );


            // ========================================================
            // FUNDO DO INPUT
            // ========================================================

            editNome.setBackgroundResource(
                    R.drawable.bg_input_professor
            );


            // ========================================================
            // GUARDAR O EDITTEXT NA FICHA
            // ========================================================

            ficha.campoNome = editNome;


            // ========================================================
            // SUBSTITUIR TEXTVIEW PELO EDITTEXT
            // ========================================================

            parent.removeView(
                    txtNomeFicha
            );


            parent.addView(
                    editNome,
                    posicaoNome
            );


            // ========================================================
            // INFORMAÇÕES
            // ========================================================

            if (ficha.quantidadeExercicios == 0) {

                txtInfoFicha.setText(
                        "Nenhum exercício"
                );

            } else if (
                    ficha.quantidadeExercicios == 1
            ) {

                txtInfoFicha.setText(
                        "1 exercício"
                );

            } else {

                txtInfoFicha.setText(
                        ficha.quantidadeExercicios +
                                " exercícios"
                );
            }


            // ========================================================
            // DATA
            // ========================================================

            txtDataFicha.setText(
                    formatarData(
                            ficha.dataCriacao
                    )
            );


            // ========================================================
            // LIXEIRA
            // ========================================================

            txtIconeFicha.setText("🗑");

            txtIconeFicha.setTextSize(22);


            txtIconeFicha.setOnClickListener(v -> {

                int indice =
                        containerFichas.indexOfChild(
                                card
                        );


                if (indice < 0 ||
                        indice >= listaFichas.size()) {

                    return;
                }


                FichaItem fichaRemovida =
                        listaFichas.get(indice);


                if (fichaRemovida.idFicha > 0) {

                    if (!fichasExcluidas.contains(
                            fichaRemovida.idFicha
                    )) {

                        fichasExcluidas.add(
                                fichaRemovida.idFicha
                        );
                    }
                }


                listaFichas.remove(
                        indice
                );


                mostrarFichasEdicao();

            });


            // ========================================================
            // ESCONDER SETA
            // ========================================================

            txtAcaoFicha.setText("");


            // ========================================================
            // DESABILITAR CLICK DO CARD
            // ========================================================

            card.setOnClickListener(v -> {
                // Nada
            });


            // ========================================================
            // ADICIONAR CARD
            // ========================================================

            containerFichas.addView(
                    card
            );
        }


        txtStatusListaFichas.setText(
                "Edite os nomes ou exclua fichas."
        );
    }



    // ============================================================
    // ADICIONAR NOVA FICHA
    // ============================================================

    private void adicionarNovaFicha() {

        FichaItem novaFicha =
                new FichaItem(
                        0,
                        "",
                        "",
                        0
                );


        listaFichas.add(
                novaFicha
        );


        cardNenhumaFicha.setVisibility(
                View.GONE
        );


        mostrarFichasEdicao();


        // ========================================================
        // FOCAR NO ÚLTIMO EDITTEXT
        // ========================================================

        containerFichas.post(() -> {

            int ultimo =
                    containerFichas.getChildCount() - 1;


            if (ultimo < 0) {
                return;
            }


            View card =
                    containerFichas.getChildAt(
                            ultimo
                    );


            TextView txtNome =
                    card.findViewById(
                            R.id.txtNomeFicha
                    );


            if (txtNome != null) {

                txtNome.requestFocus();

            } else {

                EditText edit =
                        encontrarEditText(card);

                if (edit != null) {

                    edit.requestFocus();

                }
            }

        });

    }


    // ============================================================
    // ENCONTRAR EDITTEXT
    // ============================================================

    private EditText encontrarEditText(View view) {

        if (view instanceof EditText) {

            return (EditText) view;
        }


        if (view instanceof ViewGroup) {

            ViewGroup grupo =
                    (ViewGroup) view;


            for (int i = 0;
                 i < grupo.getChildCount();
                 i++) {

                EditText resultado =
                        encontrarEditText(
                                grupo.getChildAt(i)
                        );


                if (resultado != null) {

                    return resultado;
                }
            }
        }


        return null;
    }


    // ============================================================
    // SALVAR FICHAS
    // ============================================================

    private void salvarFichas() {

        // ========================================================
        // PEGAR NOMES DOS EDITTEXTS
        // ========================================================

        for (int i = 0;
             i < containerFichas.getChildCount();
             i++) {

            View card =
                    containerFichas.getChildAt(i);


            EditText editNome =
                    encontrarEditText(card);


            if (editNome == null) {
                continue;
            }


            String nome =
                    editNome.getText()
                            .toString()
                            .trim();


            if (nome.isEmpty()) {

                Toast.makeText(
                        this,
                        "Todas as fichas precisam ter um nome.",
                        Toast.LENGTH_LONG
                ).show();

                editNome.requestFocus();

                return;
            }


            if (nome.length() > 100) {

                Toast.makeText(
                        this,
                        "O nome da ficha deve ter no máximo 100 caracteres.",
                        Toast.LENGTH_LONG
                ).show();

                editNome.requestFocus();

                return;
            }


            listaFichas.get(i).nomeFicha =
                    nome;
        }


        // ========================================================
        // EVITAR DUPLO CLIQUE
        // ========================================================

        btnEditarFichas.setEnabled(false);

        btnNovaFicha.setEnabled(false);

        txtStatusFichas.setText(
                "Salvando..."
        );

        txtStatusListaFichas.setText(
                "Salvando fichas no banco..."
        );


        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                JSONObject corpo =
                        new JSONObject();


                corpo.put(
                        "id_alunos",
                        idAluno
                );


                JSONArray fichasJson =
                        new JSONArray();


                // ==================================================
                // FICHAS
                // ==================================================

                for (FichaItem ficha :
                        listaFichas) {

                    JSONObject objeto =
                            new JSONObject();


                    objeto.put(
                            "id_ficha",
                            ficha.idFicha
                    );


                    objeto.put(
                            "nome_ficha",
                            ficha.nomeFicha
                    );


                    fichasJson.put(
                            objeto
                    );
                }


                corpo.put(
                        "fichas",
                        fichasJson
                );


                // ==================================================
                // EXCLUÍDAS
                // ==================================================

                JSONArray excluidasJson =
                        new JSONArray();


                for (Integer id :
                        fichasExcluidas) {

                    excluidasJson.put(id);
                }


                corpo.put(
                        "excluidas",
                        excluidasJson
                );


                // ==================================================
                // CONEXÃO
                // ==================================================

                URL url =
                        new URL(URL_FICHAS);


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
                        "application/json; charset=UTF-8"
                );


                // ==================================================
                // ENVIAR
                // ==================================================

                OutputStream output =
                        conexao.getOutputStream();


                output.write(
                        corpo.toString()
                                .getBytes("UTF-8")
                );


                output.flush();

                output.close();


                // ==================================================
                // RESPOSTA
                // ==================================================

                int codigoResposta =
                        conexao.getResponseCode();


                InputStream inputStream;


                if (codigoResposta >= 200 &&
                        codigoResposta < 300) {

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


                while ((linha =
                        reader.readLine()) != null) {

                    resposta.append(linha);
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


                String mensagem =
                        json.optString(
                                "mensagem",
                                "Erro ao salvar fichas."
                        );


                if (codigoResposta >= 200 &&
                        codigoResposta < 300 &&
                        sucesso) {


                    // ==================================================
                    // PEGAR DADOS DEVOLVIDOS PELA API
                    // ==================================================

                    JSONArray fichasSalvas =
                            json.optJSONArray(
                                    "fichas"
                            );


                    if (fichasSalvas != null) {

                        listaFichas.clear();


                        for (int i = 0;
                             i < fichasSalvas.length();
                             i++) {

                            JSONObject ficha =
                                    fichasSalvas.getJSONObject(i);


                            listaFichas.add(
                                    new FichaItem(
                                            ficha.optInt(
                                                    "id_ficha",
                                                    -1
                                            ),
                                            ficha.optString(
                                                    "nome_ficha",
                                                    "Ficha de treino"
                                            ),
                                            ficha.optString(
                                                    "data_criacao",
                                                    ""
                                            ),
                                            ficha.optInt(
                                                    "quantidade_exercicios",
                                                    0
                                            )
                                    )
                            );
                        }
                    }


                    runOnUiThread(() -> {

                        modoEdicao = false;

                        fichasExcluidas.clear();

                        btnEditarFichas.setText(
                                "EDITAR"
                        );

                        btnEditarFichas.setEnabled(
                                true
                        );

                        btnNovaFicha.setEnabled(
                                true
                        );


                        mostrarFichasNormal();


                        Toast.makeText(
                                FichaTreinoActivity.this,
                                "Fichas salvas com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();

                    });


                } else {

                    runOnUiThread(() -> {

                        btnEditarFichas.setEnabled(
                                true
                        );

                        btnNovaFicha.setEnabled(
                                true
                        );

                        txtStatusFichas.setText(
                                "Erro"
                        );

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

                    btnEditarFichas.setEnabled(
                            true
                    );

                    btnNovaFicha.setEnabled(
                            true
                    );


                    txtStatusFichas.setText(
                            "Erro"
                    );


                    Toast.makeText(
                            FichaTreinoActivity.this,
                            "Erro ao salvar as fichas.",
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
    // FORMATAR DATA
    // ============================================================

    private String formatarData(String data) {

        if (data == null ||
                data.trim().isEmpty()) {

            return "Data não informada";
        }


        try {

            if (data.length() >= 10) {

                String ano =
                        data.substring(0, 4);

                String mes =
                        data.substring(5, 7);

                String dia =
                        data.substring(8, 10);


                return "Criado em " +
                        dia +
                        "/" +
                        mes +
                        "/" +
                        ano;
            }

        } catch (Exception erro) {

            erro.printStackTrace();
        }


        return "Criado em " + data;
    }

}
