package com.example.sga;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class AlunoActivity extends AppCompatActivity {

    private Button btnInicio;
    private Button btnTreinos;
    private Button btnPlanos;
    private Button btnPerfil;
    private Button btnAnexarDocumento;

    private TextView txtNomeDocumento;
    private TextView txtTipoDocumento;

    private ExameRepository exameRepository;

    private int idUser = -1;

    private static final int SELECIONAR_DOCUMENTO = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_aluno);

        // ==========================================
        // REFERÊNCIAS DO XML
        // ==========================================

        txtNomeDocumento =
                findViewById(R.id.txtNomeDocumento);

        txtTipoDocumento =
                findViewById(R.id.txtTipoDocumento);

        btnAnexarDocumento =
                findViewById(R.id.btnAnexarDocumento);

        btnInicio =
                findViewById(R.id.btnInicio);

        btnTreinos =
                findViewById(R.id.btnTreinos);

        btnPlanos =
                findViewById(R.id.btnPlanos);

        btnPerfil =
                findViewById(R.id.btnPerfil);


        // ==========================================
        // RECUPERA ID DO USUÁRIO
        // ==========================================

        idUser = getIntent().getIntExtra(
                "id_user",
                -1
        );

        Toast.makeText(
                this,
                "ID DO ALUNO: " + idUser,
                Toast.LENGTH_LONG
        ).show();


        // ==========================================
        // VERIFICA ID
        // ==========================================

        if (idUser == -1) {

            Toast.makeText(
                    this,
                    "Usuário não identificado.",
                    Toast.LENGTH_LONG
            ).show();
        }


        // ==========================================
        // REPOSITORY DOS EXAMES
        // ==========================================

        exameRepository =
                new ExameRepository(this);


        // ==========================================
        // BOTÃO ANEXAR DOCUMENTO
        // ==========================================

        btnAnexarDocumento.setOnClickListener(v -> {

            abrirSeletorDocumento();

        });


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

            Intent intent =
                    new Intent(
                            AlunoActivity.this,
                            OpcoesActivity.class
                    );

            intent.putExtra(
                    "opcao",
                    "treinos"
            );

            startActivity(intent);

        });


        // ==========================================
        // BOTÃO PLANOS
        // ==========================================

        btnPlanos.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AlunoActivity.this,
                            OpcoesActivity.class
                    );

            intent.putExtra(
                    "opcao",
                    "planos"
            );

            startActivity(intent);

        });


        // ==========================================
        // BOTÃO PERFIL
        // ==========================================

        btnPerfil.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            AlunoActivity.this,
                            OpcoesActivity.class
                    );

            intent.putExtra(
                    "opcao",
                    "perfil"
            );

            startActivity(intent);

        });
    }


    // ==========================================
    // ABRIR SELETOR DE DOCUMENTO
    // ==========================================

    private void abrirSeletorDocumento() {

        Intent intent =
                new Intent(Intent.ACTION_OPEN_DOCUMENT);

        intent.addCategory(
                Intent.CATEGORY_OPENABLE
        );

        intent.setType("*/*");

        intent.putExtra(
                Intent.EXTRA_MIME_TYPES,
                new String[]{
                        "application/pdf",
                        "image/jpeg",
                        "image/png"
                }
        );

        startActivityForResult(
                intent,
                SELECIONAR_DOCUMENTO
        );
    }


    // ==========================================
    // RESULTADO DO SELETOR
    // ==========================================

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data
    ) {

        super.onActivityResult(
                requestCode,
                resultCode,
                data
        );

        if (
                requestCode == SELECIONAR_DOCUMENTO &&
                        resultCode == RESULT_OK &&
                        data != null
        ) {

            Uri arquivoSelecionado =
                    data.getData();

            if (arquivoSelecionado != null) {

                // ==================================
                // NOME DO ARQUIVO
                // ==================================

                String nomeArquivo =
                        obterNomeArquivo(
                                arquivoSelecionado
                        );


                // ==================================
                // TIPO DO ARQUIVO
                // ==================================

                String tipoArquivo =
                        getContentResolver()
                                .getType(
                                        arquivoSelecionado
                                );

                if (tipoArquivo == null) {

                    tipoArquivo =
                            "application/octet-stream";
                }


                // ==================================
                // MOSTRA NA TELA
                // ==================================

                txtNomeDocumento.setText(
                        nomeArquivo
                );

                txtTipoDocumento.setText(
                        "Tipo: " + tipoArquivo
                );


                // ==================================
                // ENVIA O ARQUIVO
                // ==================================

                enviarExame(
                        arquivoSelecionado,
                        nomeArquivo,
                        tipoArquivo
                );
            }
        }
    }


    // ==========================================
    // ENVIAR EXAME PARA API
    // ==========================================

    private void enviarExame(
            Uri arquivoUri,
            String nomeArquivo,
            String tipoArquivo
    ) {

        // ==========================================
        // VERIFICA USUÁRIO
        // ==========================================

        if (idUser == -1) {

            Toast.makeText(
                    this,
                    "Usuário não identificado.",
                    Toast.LENGTH_LONG
            ).show();

            return;
        }


        // ==========================================
        // DESABILITA BOTÃO
        // ==========================================

        btnAnexarDocumento.setEnabled(false);

        btnAnexarDocumento.setText(
                "ENVIANDO..."
        );


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
                    public void onSuccess(
                            JSONObject exame
                    ) {

                        btnAnexarDocumento.setEnabled(
                                true
                        );

                        btnAnexarDocumento.setText(
                                "+  ANEXAR EXAME OU LAUDO"
                        );

                        Toast.makeText(
                                AlunoActivity.this,
                                "Documento registrado com sucesso!",
                                Toast.LENGTH_SHORT
                        ).show();
                    }


                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        btnAnexarDocumento.setEnabled(
                                true
                        );

                        btnAnexarDocumento.setText(
                                "+  ANEXAR EXAME OU LAUDO"
                        );

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

    private String obterNomeArquivo(
            Uri uri
    ) {

        String nome = null;

        if ("content".equals(uri.getScheme())) {

            Cursor cursor =
                    getContentResolver().query(
                            uri,
                            null,
                            null,
                            null,
                            null
                    );

            if (cursor != null) {

                try {

                    int indiceNome =
                            cursor.getColumnIndex(
                                    OpenableColumns.DISPLAY_NAME
                            );

                    if (
                            indiceNome >= 0 &&
                                    cursor.moveToFirst()
                    ) {

                        nome =
                                cursor.getString(
                                        indiceNome
                                );
                    }

                } finally {

                    cursor.close();
                }
            }
        }


        if (nome == null) {

            nome =
                    uri.getLastPathSegment();
        }


        return nome != null
                ? nome
                : "Arquivo selecionado";
    }
}

