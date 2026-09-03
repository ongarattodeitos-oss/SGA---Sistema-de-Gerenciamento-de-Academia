package com.example.sga;
import com.bumptech.glide.Glide;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PerfilActivity extends AppCompatActivity {

    // ==========================================
    // CAMPOS DO PERFIL
    // ==========================================

    private TextView txtNomeCompleto;
    private TextView txtNomeUser;
    private TextView txtEmail;
    private Button btnInicio;
    private Button btnTreinos;
    private Button btnPlanos;
    private Button btnPerfil;
    private TextView txtTelefone;
    private TextView txtCpf;
    private TextView txtData;
    private TextView txtPeso;
    private TextView txtAltura;


    // ==========================================
    // FOTO
    // ==========================================

    private ImageView imgPerfil;
    private Button btnAlterarFoto;

    private static final int PICK_IMAGE = 1001;


    // ==========================================
    // REPOSITORIES
    // ==========================================

    private PerfilRepository perfilRepository;
    private FotoPerfilRepository fotoPerfilRepository;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_perfil);


        // ==========================================
        // COMPONENTES
        // ==========================================
        btnInicio = findViewById(R.id.btnInicio);
        btnTreinos = findViewById(R.id.btnTreinos);
        btnPlanos = findViewById(R.id.btnPlanos);
        btnPerfil = findViewById(R.id.btnPerfil);
        btnInicio.setOnClickListener(v -> finish());

        btnTreinos.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "treinos");
            startActivity(intent);
            finish();
        });

        btnPlanos.setOnClickListener(v -> {
            Intent intent = new Intent(PerfilActivity.this, OpcoesActivity.class);
            intent.putExtra("opcao", "planos");
            startActivity(intent);
            finish();
        });

        btnPerfil.setOnClickListener(v -> {
            // Já está na tela de perfil
        });
        imgPerfil =
                findViewById(R.id.imgPerfil);

        btnAlterarFoto =
                findViewById(R.id.btnAlterarFoto);

        txtNomeCompleto =
                findViewById(R.id.txtNomeCompleto);

        txtNomeUser =
                findViewById(R.id.txtNomeUser);

        txtEmail =
                findViewById(R.id.txtEmail);

        txtTelefone =
                findViewById(R.id.txtTelefone);

        txtCpf =
                findViewById(R.id.txtCpf);

        txtData =
                findViewById(R.id.txtData);

        txtPeso =
                findViewById(R.id.txtPeso);

        txtAltura =
                findViewById(R.id.txtAltura);


        // ==========================================
        // REPOSITORIES
        // ==========================================

        perfilRepository =
                new PerfilRepository(this);

        fotoPerfilRepository =
                new FotoPerfilRepository(this);


        // ==========================================
        // BOTÃO ALTERAR FOTO
        // ==========================================

        btnAlterarFoto.setOnClickListener(v -> {

            abrirGaleria();

        });


        // ==========================================
        // CARREGAR PERFIL
        // ==========================================

        carregarPerfil();
    }


    // ============================================================
    // ABRIR GALERIA
    // ============================================================

    private void abrirGaleria() {

        Intent intent =
                new Intent(
                        Intent.ACTION_PICK
                );

        intent.setDataAndType(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                "image/*"
        );

        startActivityForResult(
                intent,
                PICK_IMAGE
        );
    }


    // ============================================================
    // RECEBER IMAGEM DA GALERIA
    // ============================================================

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
                requestCode == PICK_IMAGE &&
                        resultCode == RESULT_OK &&
                        data != null
        ) {

            Uri imagemUri =
                    data.getData();


            if (imagemUri != null) {

                enviarImagem(imagemUri);
            }
        }
    }


    // ============================================================
    // ENVIAR IMAGEM
    // ============================================================

    private void enviarImagem(
            Uri imagemUri
    ) {

        try {

            InputStream inputStream =
                    getContentResolver()
                            .openInputStream(
                                    imagemUri
                            );


            if (inputStream == null) {

                Toast.makeText(
                        this,
                        "Não foi possível abrir a imagem.",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }


            // ==========================================
            // TRANSFORMAR IMAGEM EM BYTES
            // ==========================================

            ByteArrayOutputStream output =
                    new ByteArrayOutputStream();


            byte[] buffer =
                    new byte[4096];


            int quantidade;


            while (
                    (quantidade =
                            inputStream.read(buffer))
                            != -1
            ) {

                output.write(
                        buffer,
                        0,
                        quantidade
                );
            }


            inputStream.close();


            byte[] imagem =
                    output.toByteArray();


            // ==========================================
            // MOSTRAR FOTO IMEDIATAMENTE
            // ==========================================

            Glide.with(PerfilActivity.this)
                    .load(imagemUri)
                    .circleCrop()
                    .into(imgPerfil);

            // ==========================================
            // ENVIAR PARA API
            // ==========================================

            fotoPerfilRepository.enviarFoto(

                    imagem,

                    new FotoPerfilRepository.FotoCallback() {

                        @Override
                        public void onSuccess(
                                String resposta
                        ) {

                            Toast.makeText(
                                    PerfilActivity.this,
                                    "Foto atualizada com sucesso!",
                                    Toast.LENGTH_SHORT
                            ).show();
                        }


                        @Override
                        public void onError(
                                String mensagem
                        ) {

                            Toast.makeText(
                                    PerfilActivity.this,
                                    mensagem,
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }
            );


        } catch (Exception e) {

            e.printStackTrace();

            Toast.makeText(
                    this,
                    "Erro ao selecionar a imagem.",
                    Toast.LENGTH_LONG
            ).show();
        }
    }


    // ============================================================
    // CARREGAR PERFIL
    // ============================================================

    private void carregarPerfil() {

        perfilRepository.buscarPerfil(

                new PerfilRepository.PerfilCallback() {

                    @Override
                    public void onSuccess(
                            JSONObject usuario
                    ) {

                        try {

                            // ==================================
                            // NOME
                            // ==================================

                            txtNomeCompleto.setText(
                                    usuario.optString(
                                            "nome_completo",
                                            "-"
                                    )
                            );


                            // ==================================
                            // USUÁRIO
                            // ==================================

                            txtNomeUser.setText(
                                    usuario.optString(
                                            "nome_user",
                                            "-"
                                    )
                            );


                            // ==================================
                            // EMAIL
                            // ==================================

                            txtEmail.setText(
                                    usuario.optString(
                                            "email",
                                            "-"
                                    )
                            );


                            // ==================================
                            // TELEFONE
                            // ==================================

                            txtTelefone.setText(
                                    usuario.optString(
                                            "telefone",
                                            "-"
                                    )
                            );


                            // ==================================
                            // CPF
                            // ==================================

                            txtCpf.setText(
                                    usuario.optString(
                                            "cpf",
                                            "-"
                                    )
                            );


                            // ==================================
                            // DATA
                            // ==================================

                            txtData.setText(
                                    usuario.optString(
                                            "data",
                                            "-"
                                    )
                            );


                            // ==================================
                            // PESO
                            // ==================================

                            double peso =
                                    usuario.optDouble(
                                            "peso",
                                            0
                                    );


                            if (peso > 0) {

                                txtPeso.setText(
                                        peso + " kg"
                                );

                            } else {

                                txtPeso.setText(
                                        "Não informado"
                                );
                            }


                            // ==================================
                            // ALTURA
                            // ==================================

                            double altura =
                                    usuario.optDouble(
                                            "altura",
                                            0
                                    );


                            if (altura > 0) {

                                txtAltura.setText(
                                        altura + " cm"
                                );

                            } else {

                                txtAltura.setText(
                                        "Não informado"
                                );
                            }


                            // ==================================
                            // FOTO DE PERFIL
                            // ==========================================

                            String fotoUrl =
                                    usuario.optString(
                                            "foto_url",
                                            ""
                                    );


                            if (
                                    !fotoUrl.isEmpty() &&
                                            !fotoUrl.equals("null")
                            ) {

                                Glide.with(PerfilActivity.this)
                                        .load(fotoUrl)
                                        .circleCrop()
                                        .into(imgPerfil);
                            }

                        } catch (Exception e) {

                            e.printStackTrace();

                            Toast.makeText(
                                    PerfilActivity.this,
                                    "Erro ao carregar perfil.",
                                    Toast.LENGTH_LONG
                            ).show();
                        }
                    }


                    @Override
                    public void onError(
                            String mensagem
                    ) {

                        Toast.makeText(
                                PerfilActivity.this,
                                mensagem,
                                Toast.LENGTH_LONG
                        ).show();
                    }
                }
        );
    }
}