package com.example.sga;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import android.text.Editable;
import android.text.TextWatcher;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class AlunoActivity extends AppCompatActivity {

    private static final int SELECIONAR_DOCUMENTO = 100;

    private Button btnInicio;
    private Button btnTreinos;
    private Button btnPlanos;
    private Button btnPerfil;
    private Button btnAnexarDocumento;
    private EditText edtPeso, edtAltura;
    private LinearLayout listaExames;

    private TextView txtNenhumDocumento;
    private TextView txtNomeDocumento;
    private TextView txtTipoDocumento;

    private TextView txtIMC;
    private TextView txtClassificacaoIMC;

    private Button btnSalvarDadosFisicos;
    private ExameRepository exameRepository;
    private static final String URL_PERFIL = "https://sga-api.miguel-r-hoff.workers.dev/perfil?id_user=";
    private int idUser = -1;

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno);

        // Referências do XML
        listaExames = findViewById(R.id.listaExames);
        txtNenhumDocumento = findViewById(R.id.txtNenhumDocumento);
        txtNomeDocumento = findViewById(R.id.txtNomeDocumento);
        txtTipoDocumento = findViewById(R.id.txtTipoDocumento);
        edtPeso = findViewById(R.id.edtPeso);
        edtAltura = findViewById(R.id.edtAltura);
        txtIMC = findViewById(R.id.txtIMC);
        txtClassificacaoIMC = findViewById(R.id.txtClassificacaoIMC);
        btnSalvarDadosFisicos = findViewById(R.id.btnSalvarDadosFisicos);
        btnAnexarDocumento = findViewById(R.id.btnAnexarDocumento);
        btnInicio = findViewById(R.id.btnInicio);
        btnTreinos = findViewById(R.id.btnTreinos);
        btnPlanos = findViewById(R.id.btnPlanos);
        btnPerfil = findViewById(R.id.btnPerfil);

        exameRepository = new ExameRepository(this);

        // Recupera ID do usuário
        idUser = getIntent().getIntExtra("id_usuario", -1);

        if (idUser == -1) {
            Toast.makeText(this, "Usuário não identificado.", Toast.LENGTH_LONG).show();
        } else {
            // Usa apenas ESTA função para buscar os dados ao abrir a tela
            buscarDadosFisicos();
        }

        // Cálculo de IMC em tempo real
        TextWatcher calculadoraIMC = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                calcularIMC();
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };

        edtPeso.addTextChangedListener(calculadoraIMC);
        edtAltura.addTextChangedListener(calculadoraIMC);

        // Botões
        if (btnSalvarDadosFisicos != null) {
            btnSalvarDadosFisicos.setOnClickListener(v -> processarESalvarDados());
        }

        btnAnexarDocumento.setOnClickListener(v -> abrirSeletorDocumento());

        btnTreinos.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "treinos");
            startActivity(intent);
        });

        btnPlanos.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "planos");
            startActivity(intent);
        });

        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, PerfilActivity.class);
            intent.putExtra("opcao", "perfil");
            startActivity(intent);
        });

        carregarExames();
    }

    // ==========================================
    // VALIDAR CAMPOS E SALVAR PESO/ALTURA
    // ==========================================

    // ==========================================
    // VALIDAR CAMPOS E INICIAR SALVAMENTO
    // ==========================================
    private void processarESalvarDados() {
        try {
            String pesoTexto = edtPeso.getText().toString().trim().replace(",", ".");
            String alturaTexto = edtAltura.getText().toString().trim().replace(",", ".");

            if (pesoTexto.isEmpty() || alturaTexto.isEmpty()) {
                Toast.makeText(this, "Informe peso e altura.", Toast.LENGTH_SHORT).show();
                return;
            }

            double peso = Double.parseDouble(pesoTexto);
            double altura = Double.parseDouble(alturaTexto);

            if (peso <= 0 || altura <= 0) {
                Toast.makeText(this, "Informe valores válidos.", Toast.LENGTH_SHORT).show();
                return;
            }

            calcularIMC();

            // Bloqueia os dois botões possíveis para evitar múltiplos cliques
            if (btnSalvarDadosFisicos != null) {
                btnSalvarDadosFisicos.setEnabled(false);
                btnSalvarDadosFisicos.setText("SALVANDO...");
            }

            salvarPesoEAltura(peso, altura);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Digite apenas números válidos.", Toast.LENGTH_SHORT).show();
        }
    }

    // ==========================================
    // SALVAR DADOS FÍSICOS NA API
    // ==========================================
    private void salvarPesoEAltura(double novoPeso, double novaAltura) {

        if (idUser == -1) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Usuário não identificado.", Toast.LENGTH_SHORT).show();
                restaurarBotaoSalvar();
            });
            return;
        }

        // Busca o token salvo no login
        String token = getSharedPreferences("login", MODE_PRIVATE).getString("token", null);

        if (token == null || token.isEmpty()) {
            runOnUiThread(() -> {
                Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
                restaurarBotaoSalvar();
            });
            return;
        }

        new Thread(() -> {
            HttpURLConnection conexao = null;
            try {
                String urlApi = "https://sga-api.miguel-r-hoff.workers.dev/atualizar-perfil?id_alunos=" + idUser;

                URL url = new URL(urlApi);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("POST");
                conexao.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
                conexao.setRequestProperty("Accept", "application/json");
                conexao.setRequestProperty("Authorization", "Bearer " + token);
                conexao.setDoOutput(true);

                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);

                JSONObject body = new JSONObject();
                body.put("peso", novoPeso);
                body.put("altura", novaAltura);

                java.io.OutputStream os = conexao.getOutputStream();
                os.write(body.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int codigo = conexao.getResponseCode();

                java.io.InputStream is = (codigo >= 200 && codigo < 300) ? conexao.getInputStream() : conexao.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is));
                StringBuilder resposta = new StringBuilder();
                String linha;
                while ((linha = reader.readLine()) != null) {
                    resposta.append(linha);
                }
                reader.close();

                if (codigo == 200) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Dados salvos com sucesso!", Toast.LENGTH_SHORT).show();
                        restaurarBotaoSalvar();
                    });
                } else if (codigo == 401) {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Sessão expirada. Faça login novamente.", Toast.LENGTH_SHORT).show();
                        restaurarBotaoSalvar();
                    });
                } else {
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Erro " + codigo + " ao salvar.", Toast.LENGTH_SHORT).show();
                        restaurarBotaoSalvar();
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro de conexão ao salvar dados.", Toast.LENGTH_SHORT).show();
                    restaurarBotaoSalvar();
                });
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }
        }).start();
    }

    // ==========================================
    // RESTAURAR BOTÕES
    // ==========================================
    private void restaurarBotaoSalvar() {
        if (btnSalvarDadosFisicos != null) {
            btnSalvarDadosFisicos.setEnabled(true);
            btnSalvarDadosFisicos.setText("SALVAR");
        }

    }
    private void buscarDadosFisicos() {

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                // ==========================================
                // URL DA API
                // ==========================================

                String urlApi =
                        "https://sga-api.miguel-r-hoff.workers.dev/alunos-lista?id_alunos="
                                + idUser;

                URL url = new URL(urlApi);

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);

                // ==========================================
                // RESPOSTA
                // ==========================================

                int codigoResposta = conexao.getResponseCode();

                if (codigoResposta != HttpURLConnection.HTTP_OK) {

                    runOnUiThread(() -> {

                        edtPeso.setText("");
                        edtAltura.setText("");
                        txtIMC.setText("--");

                        txtClassificacaoIMC.setText(
                                "Não foi possível carregar os dados."
                        );

                    });

                    return;
                }

                // ==========================================
                // LER RESPOSTA
                // ==========================================

                BufferedReader leitor =
                        new BufferedReader(
                                new InputStreamReader(
                                        conexao.getInputStream()
                                )
                        );

                StringBuilder resultado = new StringBuilder();

                String linha;

                while ((linha = leitor.readLine()) != null) {
                    resultado.append(linha);
                }

                leitor.close();

                // ==========================================
                // CONVERTER JSON
                // ==========================================

                JSONObject resposta =
                        new JSONObject(resultado.toString());

                boolean sucesso =
                        resposta.optBoolean("sucesso", false);

                if (!sucesso) {

                    runOnUiThread(() -> {

                        edtPeso.setText("");
                        edtAltura.setText("");
                        txtIMC.setText("--");

                        txtClassificacaoIMC.setText(
                                "Não foi possível carregar os dados."
                        );

                    });

                    return;
                }

                // ==========================================
                // OBJETO ALUNO
                // ==========================================

                JSONObject aluno =
                        resposta.getJSONObject("aluno");

                // ==========================================
                // DADOS FÍSICOS
                // ==========================================

                double peso =
                        aluno.optDouble("peso", 0);

                double altura =
                        aluno.optDouble("altura", 0);

                double imc =
                        aluno.optDouble("imc", 0);

                // ==========================================
                // ATUALIZAR TELA
                // ==========================================

                runOnUiThread(() -> {

                    // ======================================
                    // PESO
                    // ======================================

                    if (peso > 0) {

                        edtPeso.setText(
                                String.format(
                                        java.util.Locale.getDefault(),
                                        "%.1f",
                                        peso
                                )
                        );

                    } else {

                        edtPeso.setText("");
                    }

                    // ======================================
                    // ALTURA
                    // ======================================

                    if (altura > 0) {

                        edtAltura.setText(
                                String.format(
                                        java.util.Locale.getDefault(),
                                        "%.2f",
                                        altura
                                )
                        );

                    } else {

                        edtAltura.setText("");
                    }

                    // ======================================
                    // IMC
                    // ======================================

                    if (imc > 0) {

                        txtIMC.setText(
                                String.format(
                                        java.util.Locale.getDefault(),
                                        "%.2f",
                                        imc
                                )
                        );

                        txtClassificacaoIMC.setText(
                                obterClassificacaoIMC(imc)
                        );

                    } else {

                        txtIMC.setText("--");

                        txtClassificacaoIMC.setText(
                                "Informe peso e altura para calcular."
                        );
                    }

                });

            } catch (Exception e) {

                e.printStackTrace();

                runOnUiThread(() -> {

                    edtPeso.setText("");
                    edtAltura.setText("");
                    txtIMC.setText("--");

                    txtClassificacaoIMC.setText(
                            "Não foi possível carregar os dados."
                    );

                    Toast.makeText(
                            AlunoActivity.this,
                            "Não foi possível carregar os dados físicos.",
                            Toast.LENGTH_SHORT
                    ).show();

                });

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }
            }

        }).start();
    }

    // ==========================================
// CLASSIFICAÇÃO DO IMC
// ==========================================

    private String obterClassificacaoIMC(double imc) {

        if (imc < 18.5) {
            return "Abaixo de 18,5";
        }

        if (imc < 25.0) {
            return "Faixa de 18,5 a 24,9";
        }

        if (imc < 30.0) {
            return "Faixa de 25,0 a 29,9";
        }

        return "30,0 ou mais";
    }
    // ==========================================
// CALCULAR IMC AUTOMATICAMENTE
// ==========================================

    private void calcularIMC() {

        try {

            String pesoTexto = edtPeso.getText().toString().trim();
            String alturaTexto = edtAltura.getText().toString().trim();

            // Aceita vírgula ou ponto
            pesoTexto = pesoTexto.replace(",", ".");
            alturaTexto = alturaTexto.replace(",", ".");

            if (pesoTexto.isEmpty() || alturaTexto.isEmpty()) {

                txtIMC.setText("--");

                txtClassificacaoIMC.setText(
                        "Informe peso e altura para calcular."
                );

                return;
            }

            double peso = Double.parseDouble(pesoTexto);
            double altura = Double.parseDouble(alturaTexto);

            if (peso <= 0 || altura <= 0) {

                txtIMC.setText("--");

                txtClassificacaoIMC.setText(
                        "Informe valores válidos."
                );

                return;
            }

            // ======================================
            // CÁLCULO
            // ======================================

            double imc = peso / (altura * altura);

            // ======================================
            // MOSTRAR IMC
            // ======================================

            txtIMC.setText(
                    String.format(
                            java.util.Locale.getDefault(),
                            "%.2f",
                            imc
                    )
            );

            txtClassificacaoIMC.setText(
                    obterClassificacaoIMC(imc)
            );

        } catch (NumberFormatException e) {

            txtIMC.setText("--");

            txtClassificacaoIMC.setText(
                    "Digite apenas números."
            );
        }
    }
    private void carregarExames() {
        if (idUser <= 0) {
            txtNenhumDocumento.setText("Usuário não identificado.");
            return;
        }

        exameRepository.listarExames(idUser, new ExameRepository.ListaExamesCallback() {

            @Override
            public void onSuccess(JSONArray exames) {
                runOnUiThread(() -> {
                    listaExames.removeAllViews();

                    if (exames.length() == 0) {
                        txtNenhumDocumento.setVisibility(View.VISIBLE);
                        txtNenhumDocumento.setText("Nenhum documento anexado.");
                        return;
                    }

                    txtNenhumDocumento.setVisibility(View.GONE);

                    for (int i = 0; i < exames.length(); i++) {
                        try {
                            JSONObject exame = exames.getJSONObject(i);
                            adicionarExameNaTela(exame);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void onError(String mensagem) {
                runOnUiThread(() -> {
                    txtNenhumDocumento.setVisibility(View.VISIBLE);
                    txtNenhumDocumento.setText("Não foi possível carregar os documentos.");
                });
            }
        });
    }

    // ==========================================
    // ADICIONAR EXAME NA TELA
    // ==========================================

    private void adicionarExameNaTela(
            JSONObject exame
    ) {

        try {

            int idExame =
                    exame.getInt("id_exame");

            String nome =
                    exame.optString(
                            "nome_arquivo",
                            "Documento"
                    );

            String tipo =
                    exame.optString(
                            "tipo_arquivo",
                            ""
                    );

            String url =
                    exame.optString(
                            "url_arquivo",
                            ""
                    );


            // =========================================
            // ITEM DO DOCUMENTO
            // =========================================

            LinearLayout item =
                    new LinearLayout(this);

            item.setOrientation(
                    LinearLayout.VERTICAL
            );

            item.setPadding(
                    16,
                    16,
                    16,
                    16
            );

            item.setBackgroundColor(
                    Color.rgb(
                            14,
                            23,
                            23
                    )
            );


            LinearLayout.LayoutParams parametros =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );

            parametros.setMargins(
                    0,
                    8,
                    0,
                    8
            );

            item.setLayoutParams(parametros);


            // =========================================
            // NOME
            // =========================================

            TextView nomeTextView =
                    new TextView(this);

            nomeTextView.setText(nome);

            nomeTextView.setTextSize(15);

            nomeTextView.setTypeface(
                    null,
                    Typeface.BOLD
            );

            nomeTextView.setTextColor(
                    Color.WHITE
            );


            // =========================================
            // TIPO
            // =========================================

            TextView tipoTextView =
                    new TextView(this);

            tipoTextView.setText(
                    "Tipo: " + tipo
            );

            tipoTextView.setTextSize(12);

            tipoTextView.setTextColor(
                    Color.rgb(
                            45,
                            247,
                            51
                    )
            );


            item.addView(
                    nomeTextView
            );

            item.addView(
                    tipoTextView
            );


            // =========================================
            // BOTÃO EXCLUIR
            // =========================================

            Button btnExcluir =
                    new Button(this);

            btnExcluir.setText(
                    "EXCLUIR DOCUMENTO"
            );

            btnExcluir.setTextSize(11);

            btnExcluir.setTextColor(
                    Color.WHITE
            );

            btnExcluir.setBackgroundColor(
                    Color.rgb(
                            80,
                            30,
                            30
                    )
            );


            int alturaBotao = (int) (56 * getResources().getDisplayMetrics().density);

            LinearLayout.LayoutParams parametrosBotao =
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            alturaBotao
                    );

            parametrosBotao.setMargins(
                    0,
                    14,
                    0,
                    0
            );

            btnExcluir.setLayoutParams(
                    parametrosBotao
            );

            item.addView(
                    btnExcluir
            );


            // =========================================
            // ABRIR DOCUMENTO
            // =========================================

            if (!url.isEmpty() &&
                    !url.equals("null")) {

                nomeTextView.setOnClickListener(
                        v -> {

                            try {

                                Intent intent =
                                        new Intent(
                                                Intent.ACTION_VIEW,
                                                Uri.parse(url)
                                        );

                                startActivity(intent);

                            } catch (Exception e) {

                                Toast.makeText(
                                        this,
                                        "Não foi possível abrir o documento.",
                                        Toast.LENGTH_SHORT
                                ).show();
                            }
                        }
                );

                nomeTextView.setClickable(true);
            }


            // =========================================
            // EXCLUIR
            // =========================================

            btnExcluir.setOnClickListener(
                    v -> confirmarExclusao(
                            idExame,
                            item
                    )
            );


            // =========================================
            // ADICIONA NA LISTA
            // =========================================

            listaExames.addView(
                    item
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }


    private void confirmarExclusao(
            int idExame,
            LinearLayout item
    ) {

        new AlertDialog.Builder(this)

                .setTitle("Excluir documento")

                .setMessage(
                        "Deseja realmente excluir este documento?"
                )

                .setNegativeButton(
                        "CANCELAR",
                        null
                )

                .setPositiveButton(
                        "EXCLUIR",
                        (dialog, which) -> {

                            excluirExame(
                                    idExame,
                                    item
                            );
                        }
                )

                .show();
    }

    private void excluirExame(
            int idExame,
            LinearLayout item
    ) {

        Toast.makeText(
                this,
                "Excluindo documento...",
                Toast.LENGTH_SHORT
        ).show();


        exameRepository.excluirExame(

                idExame,

                new ExameRepository.ExcluirExameCallback() {

                    @Override
                    public void onSuccess() {

                        runOnUiThread(() -> {

                            listaExames.removeView(
                                    item
                            );

                            Toast.makeText(
                                    AlunoActivity.this,
                                    "Documento excluído com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();


                            // ==================================
                            // SE NÃO HOUVER MAIS DOCUMENTOS
                            // ==================================

                            if (listaExames.getChildCount() == 0) {

                                txtNenhumDocumento
                                        .setVisibility(
                                                View.VISIBLE
                                        );

                                txtNenhumDocumento.setText(
                                        "Nenhum documento anexado."
                                );
                            }
                        });
                    }


                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        runOnUiThread(() -> {

                            Toast.makeText(
                                    AlunoActivity.this,
                                    mensagem,
                                    Toast.LENGTH_LONG
                            ).show();
                        });
                    }
                }
        );
    }
    // ==========================================
    // ABRIR SELETOR DE DOCUMENTO
    // ==========================================

    private void abrirSeletorDocumento() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, new String[]{
                "application/pdf",
                "image/jpeg",
                "image/png"
        });

        startActivityForResult(intent, SELECIONAR_DOCUMENTO);
    }

    // ==========================================
    // RESULTADO DO SELETOR
    // ==========================================

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == SELECIONAR_DOCUMENTO && resultCode == RESULT_OK && data != null) {

            Uri arquivoSelecionado = data.getData();

            if (arquivoSelecionado != null) {

                // ==================================
                // NOME DO ARQUIVO
                // ==================================
                String nomeArquivo = obterNomeArquivo(arquivoSelecionado);

                // ==================================
                // TIPO DO ARQUIVO
                // ==================================
                String tipoArquivo = getContentResolver().getType(arquivoSelecionado);

                if (tipoArquivo == null) {
                    tipoArquivo = "application/octet-stream";
                }

                // ==================================
                // MOSTRA NA TELA
                // ==================================
                txtNomeDocumento.setText(nomeArquivo);
                txtTipoDocumento.setText("Tipo: " + tipoArquivo);

                // ==================================
                // ENVIA O ARQUIVO
                // ==================================
                enviarExame(arquivoSelecionado, nomeArquivo, tipoArquivo);
            }
        }
    }

    // ==========================================
    // ENVIAR EXAME PARA API
    // ==========================================

    private void enviarExame(Uri arquivoUri, String nomeArquivo, String tipoArquivo) {

        // ==========================================
        // VERIFICA USUÁRIO
        // ==========================================
        if (idUser == -1) {
            Toast.makeText(this, "Usuário não identificado.", Toast.LENGTH_LONG).show();
            return;
        }

        // ==========================================
        // DESABILITA BOTÃO
        // ==========================================
        btnAnexarDocumento.setEnabled(false);
        btnAnexarDocumento.setText("ENVIANDO...");

        // ==========================================
        // ENVIA PARA O REPOSITORY
        // ==========================================
        exameRepository.cadastrarExame(
                idUser,
                arquivoUri,
                nomeArquivo,
                tipoArquivo,
                "",
                new ExameRepository.ExameCallback() {

                    @Override
                    public void onSuccess(JSONObject exame) {
                        btnAnexarDocumento.setEnabled(true);
                        btnAnexarDocumento.setText("+  ANEXAR EXAME OU LAUDO");

                        Toast.makeText(
                                AlunoActivity.this,
                                "Documento registrado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }

                    @Override
                    public void onError(String mensagem) {
                        btnAnexarDocumento.setEnabled(true);
                        btnAnexarDocumento.setText("+  ANEXAR EXAME OU LAUDO");

                        Toast.makeText(
                                AlunoActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }

    // ==========================================
// BUSCAR PERFIL E CALCULAR IMC
// ==========================================
// A API não usa token/Bearer: a identificação é feita pelo id_user,
// exatamente como em buscarDadosFisicos(). Por isso usamos aqui a
// mesma constante URL_PERFIL que já existia na classe.
    private void carregarPerfilAluno() {

        if (idUser == -1) {
            runOnUiThread(() -> txtIMC.setText("--"));
            return;
        }

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                URL url = new URL(URL_PERFIL + idUser);
                conexao = (HttpURLConnection) url.openConnection();
                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(8000);
                conexao.setReadTimeout(8000);

                int codigo = conexao.getResponseCode();

                if (codigo == 200) {

                    BufferedReader reader = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                    StringBuilder resposta = new StringBuilder();
                    String linha;

                    while ((linha = reader.readLine()) != null) {
                        resposta.append(linha);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(resposta.toString());

                    if (json.optBoolean("sucesso", false)) {
                        JSONObject usuario = json.getJSONObject("usuario");

                        double peso = usuario.optDouble("peso", 0.0);
                        double altura = usuario.optDouble("altura", 0.0);

                        runOnUiThread(() -> {

                            if (peso > 0) {
                                edtPeso.setText(String.format(java.util.Locale.getDefault(), "%.1f", peso));
                            }

                            if (altura > 0) {
                                edtAltura.setText(String.format(java.util.Locale.getDefault(), "%.2f", altura));
                            }

                            calcularEExibirIMC(peso, altura);
                        });

                    } else {
                        runOnUiThread(() -> txtIMC.setText("--"));
                    }

                } else {
                    runOnUiThread(() -> txtIMC.setText("--"));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> txtIMC.setText("--"));
            } finally {
                if (conexao != null) {
                    conexao.disconnect();
                }
            }

        }).start();
    }

    private void calcularEExibirIMC(double peso, double altura) {
        if (peso > 0 && altura > 0) {
            // Se a altura estiver cadastrada em centímetros (ex: 175 cm), converte para metros (1.75 m)
            if (altura > 3.0) {
                altura = altura / 100.0;
            }

            double imc = peso / (altura * altura);
            txtIMC.setText(String.format(java.util.Locale.getDefault(), "%.1f", imc));
        } else {
            txtIMC.setText("--");
        }
    }

    // A API identifica o aluno pelo id_user (sem token). Usamos a mesma
    // URL_PERFIL usada em carregarPerfilAluno().


    private String obterNomeArquivo(Uri uri) {
        String nome = null;

        if ("content".equals(uri.getScheme())) {
            Cursor cursor = getContentResolver().query(uri, null, null, null, null);

            if (cursor != null) {
                try {
                    int indiceNome = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);

                    if (indiceNome >= 0 && cursor.moveToFirst()) {
                        nome = cursor.getString(indiceNome);
                    }
                } finally {
                    cursor.close();
                }
            }
        }

        if (nome == null) {
            nome = uri.getLastPathSegment();
        }
        return nome != null ? nome : "Arquivo selecionado";
    }
}