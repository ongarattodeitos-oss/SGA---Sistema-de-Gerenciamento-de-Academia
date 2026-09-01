package com.example.sga;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class UsuarioProfessorActivity extends AppCompatActivity {

    // =========================================================
    // CORES
    // =========================================================

    private final int COR_SELECIONADO = 0xFF03C6FC;
    private final int COR_NORMAL = 0xFF657086;

    // =========================================================
    // COMPONENTES DO PERFIL
    // =========================================================

    private ImageView imgFotoProfessor;

    private TextView txtNomeProfessor;
    private TextView txtCargoProfessor;
    private TextView txtUsuario;
    private TextView txtEmailProfessor;

    // =========================================================
    // AÇÕES DA CONTA
    // =========================================================

    private LinearLayout btnEditarDados;
    private LinearLayout btnCadastrarProfessor;
    private LinearLayout btnSairConta;

    // =========================================================
    // BOTÕES DO MENU INFERIOR
    // =========================================================

    private Button btnInicio;
    private Button btnAlunos;
    private Button btnTreinos;
    private Button btnPerfil;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // =====================================================
        // EDGE TO EDGE
        // =====================================================

        EdgeToEdge.enable(this);

        setContentView(
                R.layout.activity_usuario_professor
        );

        // =====================================================
        // CONFIGURAÇÃO DAS BARRAS DO SISTEMA
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
        // COMPONENTES DO PERFIL
        // =====================================================

        imgFotoProfessor =
                findViewById(
                        R.id.imgFotoProfessor
                );

        txtNomeProfessor =
                findViewById(
                        R.id.txtNomeProfessor
                );

        txtCargoProfessor =
                findViewById(
                        R.id.txtCargoProfessor
                );

        txtUsuario =
                findViewById(
                        R.id.txtUsuario
                );

        txtEmailProfessor =
                findViewById(
                        R.id.txtEmailProfessor
                );

        // =====================================================
        // BOTÕES / AÇÕES DA CONTA
        // =====================================================

        btnEditarDados =
                findViewById(
                        R.id.btnEditarDados
                );

        btnCadastrarProfessor =
                findViewById(
                        R.id.btnCadastrarProfessor
                );

        btnSairConta =
                findViewById(
                        R.id.btnSairConta
                );

        // =====================================================
        // BOTÕES DO MENU INFERIOR
        // =====================================================

        btnInicio =
                findViewById(
                        R.id.btnInicioProfessor
                );

        btnAlunos =
                findViewById(
                        R.id.btnAlunosProfessor
                );

        btnTreinos =
                findViewById(
                        R.id.btnTreinosProfessor
                );

        btnPerfil =
                findViewById(
                        R.id.btnPerfilProfessor
                );

        // =====================================================
        // CONFIGURAR MENU INFERIOR
        // =====================================================

        configurarMenuInferior();

        // =====================================================
        // PERFIL SELECIONADO
        // =====================================================

        selecionarBotao(btnPerfil);

        // =====================================================
        // BOTÃO EDITAR DADOS
        // =====================================================

        btnEditarDados.setOnClickListener(v -> {

           Intent intent = new Intent(UsuarioProfessorActivity.this, EditarDadosActivity.class);
             startActivity(intent);

        });

        // =====================================================
        // BOTÃO CADASTRAR PROFESSOR
        // =====================================================

        btnCadastrarProfessor.setOnClickListener(v -> {

           // Intent intent = new Intent(UsuarioProfessorActivity.this, CadastroProfessorActivity.class);

            //startActivity(intent);

        });

        // =====================================================
        // BOTÃO SAIR DA CONTA
        // =====================================================

        btnSairConta.setOnClickListener(v -> {

            confirmarSaida();

        });

        // =====================================================
        // CARREGAR DADOS INICIAIS
        // =====================================================

        carregarDadosProfessor();
    }


    // =============================================================
    // CONFIGURAR MENU INFERIOR
    // =============================================================

    private void configurarMenuInferior() {

        // =====================================================
        // BOTÃO INÍCIO
        // =====================================================

        btnInicio.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            UsuarioProfessorActivity.this,
                            ProfessorActivity.class
                    );

            startActivity(intent);

            finish();

        });

        // =====================================================
        // BOTÃO ALUNOS
        // =====================================================

        btnAlunos.setOnClickListener(v -> {

            Intent intent =
                    new Intent(
                            UsuarioProfessorActivity.this,
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

            // -------------------------------------------------
            // Aqui vamos colocar a Activity de treinos depois.
            // -------------------------------------------------

            Toast.makeText(
                    UsuarioProfessorActivity.this,
                    "Área de treinos",
                    Toast.LENGTH_SHORT
            ).show();

        });

        // =====================================================
        // BOTÃO PERFIL
        // =====================================================

        btnPerfil.setOnClickListener(v -> {

            selecionarBotao(btnPerfil);

        });
    }


    // =============================================================
    // SELECIONAR BOTÃO DO MENU
    // =============================================================

    private void selecionarBotao(Button botaoSelecionado) {

        btnInicio.setTextColor(
                COR_NORMAL
        );

        btnAlunos.setTextColor(
                COR_NORMAL
        );

        btnTreinos.setTextColor(
                COR_NORMAL
        );

        btnPerfil.setTextColor(
                COR_NORMAL
        );

        botaoSelecionado.setTextColor(
                COR_SELECIONADO
        );
    }


    // =============================================================
    // CARREGAR DADOS DO PROFESSOR
    // =============================================================

    private void carregarDadosProfessor() {

        /*
         * Por enquanto vamos colocar dados temporários
         * apenas para testar a interface.
         *
         * Depois vamos substituir este método pela chamada
         * da API /perfil usando o token salvo no aparelho.
         */

        txtNomeProfessor.setText(
                "Nome do Professor"
        );

        txtCargoProfessor.setText(
                "PROFESSOR"
        );

        txtUsuario.setText(
                "professor"
        );

        txtEmailProfessor.setText(
                "professor@email.com"
        );
    }


    // =============================================================
    // CONFIRMAR SAÍDA
    // =============================================================

    private void confirmarSaida() {

        new AlertDialog.Builder(
                UsuarioProfessorActivity.this
        )

                .setTitle(
                        "Sair da conta"
                )

                .setMessage(
                        "Deseja realmente sair da sua conta?"
                )

                .setNegativeButton(
                        "Cancelar",
                        null
                )

                .setPositiveButton(
                        "Sair",
                        (dialog, which) -> {

                            sairDaConta();

                        }
                )

                .show();
    }


    // =============================================================
    // SAIR DA CONTA
    // =============================================================

    private void sairDaConta() {

        /*
         * Aqui vamos limpar o token salvo no SharedPreferences.
         *
         * Quando você me mostrar como está salvando o token
         * atualmente, fazemos a limpeza exatamente de acordo
         * com o seu sistema de autenticação.
         */

        Toast.makeText(
                UsuarioProfessorActivity.this,
                "Sessão encerrada.",
                Toast.LENGTH_SHORT
        ).show();
    }
}