package com.example.sga;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.android.volley.toolbox.ImageRequest;
import android.os.Bundle;
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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UsuarioProfessorActivity extends AppCompatActivity {

    private FotoPerfilProfessorRepository fotoPerfilProfessorRepository;
    private final int COR_SELECIONADO = 0xFF03C6FC;
    private final int COR_NORMAL = 0xFF657086;
    private RequestQueue requestQueue;
    private ImageView imgFotoProfessor;
    private TextView txtNomeProfessor;
    private TextView txtCargoProfessor;
    private TextView txtUsuario;
    private TextView txtEmailProfessor;

    private LinearLayout btnEditarDados;
    private LinearLayout btnCadastrarProfessor;
    private LinearLayout btnSairConta;

    private Button btnInicio;
    private Button btnAlunos;
    private Button btnTreinos;
    private Button btnPerfil;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_usuario_professor);

        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                }
        );

        requestQueue = Volley.newRequestQueue(this);

        imgFotoProfessor = findViewById(R.id.imgFotoProfessor);

        fotoPerfilProfessorRepository =
                new FotoPerfilProfessorRepository(this);

        txtNomeProfessor = findViewById(R.id.txtNomeProfessor);
        txtCargoProfessor = findViewById(R.id.txtCargoProfessor);
        txtUsuario = findViewById(R.id.txtUsuario);
        txtEmailProfessor = findViewById(R.id.txtEmailProfessor);

        btnEditarDados = findViewById(R.id.btnEditarDados);
        btnCadastrarProfessor = findViewById(R.id.btnCadastrarProfessor);
        btnSairConta = findViewById(R.id.btnSairConta);

        btnInicio = findViewById(R.id.btnInicioProfessor);
        btnAlunos = findViewById(R.id.btnAlunosProfessor);
        btnTreinos = findViewById(R.id.btnTreinosProfessor);
        btnPerfil = findViewById(R.id.btnPerfilProfessor);

        configurarMenuInferior();
        selecionarBotao(btnPerfil);

        btnEditarDados.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioProfessorActivity.this, EditarDadosActivity.class);
            startActivity(intent);
        });

        btnCadastrarProfessor.setOnClickListener(v -> {
            // Futuro cadastro
        });

        btnSairConta.setOnClickListener(v -> confirmarSaida());

        carregarDadosProfessor();
    }

    private void configurarMenuInferior() {
        btnInicio.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioProfessorActivity.this, ProfessorActivity.class);
            startActivity(intent);
            finish();
        });

        btnAlunos.setOnClickListener(v -> {
            Intent intent = new Intent(UsuarioProfessorActivity.this, AlunosListaActivity.class);
            startActivity(intent);
            finish();
        });

        btnTreinos.setOnClickListener(v -> {
            selecionarBotao(btnTreinos);
            Toast.makeText(UsuarioProfessorActivity.this, "Área de treinos", Toast.LENGTH_SHORT).show();
        });

        btnPerfil.setOnClickListener(v -> selecionarBotao(btnPerfil));
    }

    private void selecionarBotao(Button botaoSelecionado) {
        btnInicio.setTextColor(COR_NORMAL);
        btnAlunos.setTextColor(COR_NORMAL);
        btnTreinos.setTextColor(COR_NORMAL);
        btnPerfil.setTextColor(COR_NORMAL);

        botaoSelecionado.setTextColor(COR_SELECIONADO);
    }

    private void carregarDadosProfessor() {

        SharedPreferences preferences =
                getSharedPreferences(
                        "login",
                        MODE_PRIVATE
                );

        String nomeCompleto =
                preferences.getString(
                        "nome_completo",
                        "Professor"
                );

        String cargo =
                preferences.getString(
                        "tipo",
                        "PROFESSOR"
                ).toUpperCase();

        String usuario =
                preferences.getString(
                        "nome_user",
                        "sem_usuario"
                );

        String email =
                preferences.getString(
                        "email",
                        "email@nao.informado"
                );


        // ========================================================
        // DADOS
        // ========================================================

        txtNomeProfessor.setText(nomeCompleto);
        txtCargoProfessor.setText(cargo);
        txtUsuario.setText(usuario);
        txtEmailProfessor.setText(email);


        // ========================================================
        // FOTO
        // ========================================================

        carregarFotoProfessor();
    }

    private void carregarFotoProfessor() {

        fotoPerfilProfessorRepository.carregarFoto(
                new FotoPerfilProfessorRepository.FotoCallback() {

                    @Override
                    public void onFotoCarregada(String fotoUrl) {

                        ImageRequest request =
                                new ImageRequest(
                                        fotoUrl,

                                        bitmap -> {

                                            imgFotoProfessor.setImageBitmap(
                                                    bitmap
                                            );
                                        },

                                        0,
                                        0,

                                        ImageView.ScaleType.CENTER_CROP,

                                        Bitmap.Config.RGB_565,

                                        error -> {

                                            imgFotoProfessor.setImageResource(
                                                    R.drawable.img_perfil
                                            );
                                        }
                                );

                        request.setShouldCache(false);

                        requestQueue.add(request);
                    }

                    @Override
                    public void onFotoNaoEncontrada() {

                        imgFotoProfessor.setImageResource(
                                R.drawable.img_perfil
                        );
                    }

                    @Override
                    public void onError(String mensagem) {

                        imgFotoProfessor.setImageResource(
                                R.drawable.img_perfil
                        );
                    }
                }
        );
    }

    private void confirmarSaida() {
        new AlertDialog.Builder(this)
                .setTitle("Sair da conta")
                .setMessage("Deseja realmente sair da sua conta?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Sair", (dialog, which) -> sairDaConta())
                .show();
    }

    private void sairDaConta() {
        SharedPreferences preferences = getSharedPreferences("login", MODE_PRIVATE);
        preferences.edit().clear().apply();

        Toast.makeText(this, "Sessão encerrada.", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(UsuarioProfessorActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}