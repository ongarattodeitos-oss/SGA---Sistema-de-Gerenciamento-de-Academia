package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class ProfessorActivity extends AppCompatActivity {

    // Cores
    private final int COR_SELECIONADO = 0xFF03C6FC;
    private final int COR_NORMAL = 0xFF657086;

    private Button btnInicio;
    private Button btnAlunos;
    private Button btnTreinos;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_professor);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

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
        // PEGA OS BOTÕES DO INCLUDE
        // =========================================================

        btnInicio = findViewById(R.id.btnInicioProfessor);
        btnAlunos = findViewById(R.id.btnAlunosProfessor);
        btnTreinos = findViewById(R.id.btnTreinosProfessor);
        btnPerfil = findViewById(R.id.btnPerfilProfessor);

        // =========================================================
        // BOTÃO INÍCIO
        // =========================================================

        btnInicio.setOnClickListener(v -> {

            selecionarBotao(btnInicio);

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
        // DEIXA INÍCIO SELECIONADO AO ABRIR
        // =========================================================

        selecionarBotao(btnInicio);
    }

    // =============================================================
    // FUNÇÃO PARA MUDAR A COR DOS BOTÕES
    // =============================================================

    private void selecionarBotao(Button botaoSelecionado) {

        // Primeiro deixa todos normais

        btnInicio.setTextColor(COR_NORMAL);
        btnAlunos.setTextColor(COR_NORMAL);
        btnTreinos.setTextColor(COR_NORMAL);
        btnPerfil.setTextColor(COR_NORMAL);

        // Depois deixa o selecionado azul

        botaoSelecionado.setTextColor(COR_SELECIONADO);
    }
}