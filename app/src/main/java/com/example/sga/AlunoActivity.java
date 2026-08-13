package com.example.sga;

import android.os.Bundle;
import android.content.Intent;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class AlunoActivity extends AppCompatActivity {

    private Button btnInicio;
    private Button btnTreinos;
    private Button btnPlanos;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // ==========================================
        // ABRIR O LAYOUT
        // ==========================================

        setContentView(R.layout.activity_aluno);


        // ==========================================
        // PEGAR OS BOTÕES DO XML
        // ==========================================

        btnInicio = findViewById(R.id.btnInicio);
        btnTreinos = findViewById(R.id.btnTreinos);
        btnPlanos = findViewById(R.id.btnPlanos);
        btnPerfil = findViewById(R.id.btnPerfil);


        // ==========================================
        // BOTÃO INÍCIO
        // ==========================================

        btnInicio.setOnClickListener(v -> {

            // Já estamos na tela inicial

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

            intent.putExtra("opcao", "treinos");

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

            intent.putExtra("opcao", "planos");

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

            intent.putExtra("opcao", "perfil");

            startActivity(intent);
        });
    }
}