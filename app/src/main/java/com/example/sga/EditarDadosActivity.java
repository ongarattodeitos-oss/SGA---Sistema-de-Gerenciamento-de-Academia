package com.example.sga;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class EditarDadosActivity extends AppCompatActivity {

    private ImageView imgFotoPerfil;

    private EditText edtNome;
    private EditText edtUsuario;
    private EditText edtEmail;
    private EditText edtSenha;

    private Button btnVoltar;
    private Button btnAlterarFoto;
    private Button btnSalvarAlteracoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_editar_dados);

        // =============================================
        // VINCULAR COMPONENTES
        // =============================================

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);

        edtNome = findViewById(R.id.edtNome);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnAlterarFoto = findViewById(R.id.btnAlterarFoto);
        btnSalvarAlteracoes =
                findViewById(R.id.btnSalvarAlteracoes);


        // =============================================
        // VOLTAR
        // =============================================

        btnVoltar.setOnClickListener(v -> finish());


        // =============================================
        // CARREGAR DADOS ATUAIS
        // =============================================

        carregarDados();


        // =============================================
        // ALTERAR FOTO
        // =============================================

        btnAlterarFoto.setOnClickListener(v -> {

            Toast.makeText(
                    this,
                    "Função de alterar foto será adicionada",
                    Toast.LENGTH_SHORT
            ).show();

        });


        // =============================================
        // SALVAR ALTERAÇÕES
        // =============================================

        btnSalvarAlteracoes.setOnClickListener(v -> {

            String nome =
                    edtNome.getText().toString().trim();

            String usuario =
                    edtUsuario.getText().toString().trim();

            String email =
                    edtEmail.getText().toString().trim();

            String senha =
                    edtSenha.getText().toString().trim();


            // =========================================
            // VALIDAÇÕES
            // =========================================

            if (nome.isEmpty()) {

                edtNome.setError("Digite seu nome");

                edtNome.requestFocus();

                return;

            }

            if (usuario.isEmpty()) {

                edtUsuario.setError("Digite seu usuário");

                edtUsuario.requestFocus();

                return;

            }

            if (email.isEmpty()) {

                edtEmail.setError("Digite seu e-mail");

                edtEmail.requestFocus();

                return;

            }


            // =========================================
            // ENVIAR PARA API
            // =========================================

            salvarAlteracoes(
                    nome,
                    usuario,
                    email,
                    senha
            );

        });

    }


    // =================================================
    // CARREGAR DADOS DO USUÁRIO
    // =================================================

    private void carregarDados() {

        /*
         * Aqui vamos usar a mesma API que vocês já usam
         * na tela de Perfil.
         *
         * A ideia é buscar:
         *
         * nome
         * usuario
         * email
         * foto
         *
         * E preencher:
         *
         * edtNome.setText(...)
         * edtUsuario.setText(...)
         * edtEmail.setText(...)
         */

    }


    // =================================================
    // SALVAR ALTERAÇÕES
    // =================================================

    private void salvarAlteracoes(
            String nome,
            String usuario,
            String email,
            String senha
    ) {

        /*
         * Aqui vamos criar/conectar o endpoint:
         *
         * PUT /perfil
         *
         * enviando:
         *
         * {
         *     "nome": "...",
         *     "usuario": "...",
         *     "email": "...",
         *     "senha": "..."
         * }
         *
         * Se senha estiver vazia,
         * não alteramos a senha.
         */

    }

}