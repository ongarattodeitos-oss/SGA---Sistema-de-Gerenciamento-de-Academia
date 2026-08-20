package com.example.sga;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class OpcoesActivity extends AppCompatActivity {

    private TextView txtTitulo;
    private TextView txtSubtitulo;

    private Button btnInicio;
    private Button btnTreinos;
    private Button btnPlanos;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_opcoes);

        // ==========================================
        // COMPONENTES
        // ==========================================

        txtTitulo = findViewById(R.id.txtTituloOpcao);
        txtSubtitulo = findViewById(R.id.txtSubtituloOpcao);

        btnInicio = findViewById(R.id.btnInicio);
        btnTreinos = findViewById(R.id.btnTreinos);
        btnPlanos = findViewById(R.id.btnPlanos);
        btnPerfil = findViewById(R.id.btnPerfil);


        // ==========================================
        // VERIFICAR OPÇÃO SELECIONADA
        // ==========================================

        String opcao = getIntent().getStringExtra("opcao");

        if (opcao == null) {
            opcao = "treinos";
        }


        // ==========================================
        // MOSTRAR CONTEÚDO
        // ==========================================

        mostrarOpcao(opcao);


        // ==========================================
        // TREINOS
        // ==========================================

        btnTreinos.setOnClickListener(v -> {

            mostrarOpcao("treinos");

        });


        // ==========================================
        // PLANOS
        // ==========================================

        btnPlanos.setOnClickListener(v -> {

            mostrarOpcao("planos");

        });


        // ==========================================
        // PERFIL
        // ==========================================

        btnPerfil.setOnClickListener(v -> {

            mostrarOpcao("perfil");

        });


        // ==========================================
        // INÍCIO
        // ==========================================

        btnInicio.setOnClickListener(v -> {

            finish();

        });
    }


    // ==========================================
    // ALTERAR CONTEÚDO
    // ==========================================

    private void mostrarOpcao(String opcao) {

        switch (opcao) {

            case "treinos":

                txtTitulo.setText("TREINOS");

                txtSubtitulo.setText(
                        "Acompanhe seus exercícios e treinos."
                );

                // INÍCIO fica desmarcado
                btnInicio.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                // TREINOS selecionado
                btnTreinos.setTextColor(
                        getColor(R.color.ciano_sga)
                );

                btnPlanos.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                btnPerfil.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                break;


            case "planos":

                txtTitulo.setText("PLANOS");

                txtSubtitulo.setText(
                        "Consulte seu plano e sua evolução."
                );

                // INÍCIO fica desmarcado
                btnInicio.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                btnTreinos.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                // PLANOS selecionado
                btnPlanos.setTextColor(
                        getColor(R.color.ciano_sga)
                );

                btnPerfil.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                break;


            case "perfil":

                txtTitulo.setText("PERFIL");

                txtSubtitulo.setText(
                        "Gerencie suas informações pessoais."
                );

                // INÍCIO fica desmarcado
                btnInicio.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                btnTreinos.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                btnPlanos.setTextColor(
                        getColor(R.color.cinza_sga)
                );

                // PERFIL selecionado
                btnPerfil.setTextColor(
                        getColor(R.color.ciano_sga)
                );

                break;
        }
    }
}