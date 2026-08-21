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
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

    private LinearLayout listaExames;

    private TextView txtNenhumDocumento;
    private TextView txtNomeDocumento;
    private TextView txtTipoDocumento;

    private ExameRepository exameRepository;

    private int idUser = -1;

    // ==========================================
    // CICLO DE VIDA
    // ==========================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_aluno);

        // ==========================================
        // REFERÊNCIAS DO XML
        // ==========================================
        listaExames = findViewById(R.id.listaExames);
        txtNenhumDocumento = findViewById(R.id.txtNenhumDocumento);
        txtNomeDocumento = findViewById(R.id.txtNomeDocumento);
        txtTipoDocumento = findViewById(R.id.txtTipoDocumento);
        btnAnexarDocumento = findViewById(R.id.btnAnexarDocumento);
        btnInicio = findViewById(R.id.btnInicio);
        btnTreinos = findViewById(R.id.btnTreinos);
        btnPlanos = findViewById(R.id.btnPlanos);
        btnPerfil = findViewById(R.id.btnPerfil);

        // ==========================================
        // RECUPERA ID DO USUÁRIO
        // ==========================================
        idUser = getIntent().getIntExtra("id_user", -1);

        Toast.makeText(this, "ID DO ALUNO: " + idUser, Toast.LENGTH_LONG).show();

        // ==========================================
        // VERIFICA ID
        // ==========================================
        if (idUser == -1) {
            Toast.makeText(this, "Usuário não identificado.", Toast.LENGTH_LONG).show();
        }

        // ==========================================
        // REPOSITORY DOS EXAMES
        // ==========================================
        exameRepository = new ExameRepository(this);

        // ==========================================
        // CARREGA EXAMES / BOTÃO ANEXAR DOCUMENTO
        // ==========================================
        carregarExames();

        btnAnexarDocumento.setOnClickListener(v -> abrirSeletorDocumento());

        // ==========================================
        // BOTÃO INÍCIO
        // ==========================================
        btnInicio.setOnClickListener(v -> {
            // Já estamos na tela inicial.
        });

        // ==========================================
        // BOTÃO TREINOS
        // ==========================================
        btnTreinos.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "treinos");
            startActivity(intent);
        });

        // ==========================================
        // BOTÃO PLANOS
        // ==========================================
        btnPlanos.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "planos");
            startActivity(intent);
        });

        // ==========================================
        // BOTÃO PERFIL
        // ==========================================
        btnPerfil.setOnClickListener(v -> {
            Intent intent = new Intent(AlunoActivity.this, PerfilActivity.class);
            intent.putExtra("opcao", "perfil");
            startActivity(intent);
        });
    }

    // ==========================================
    // CARREGAR EXAMES
    // ==========================================

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
    // PEGAR NOME DO ARQUIVO
    // ==========================================

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