package com.example.sga;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.os.Handler;
import android.os.Looper;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;

public class AlunoDetalhesActivity extends AppCompatActivity {

    // =========================================================
    // COMPONENTES
    // =========================================================

    private ImageView imgFotoAluno;

    private TextView txtNomeAluno;
    private TextView txtNomeUser;
    private TextView txtEmail;
    private TextView txtTelefone;
    private TextView txtCpf;
    private TextView txtData;
    private TextView txtPeso;
    private TextView txtAltura;
    private TextView txtImc;
    private TextView txtPlano;
    private TextView txtStatus;

    private Button btnVoltar;

    private final int COR_SELECIONADO = 0xFF03C6FC;
    private final int COR_NORMAL = 0xFF657086;


    // =========================================================
    // ID DO ALUNO
    // =========================================================

    private int idAluno;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_aluno_detalhes);

        // =====================================================
        // BARRAS DO SISTEMA
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
        // RECEBER O ID
        // =====================================================

        idAluno = getIntent().getIntExtra(
                "id_alunos",
                -1
        );

        // =====================================================
        // PEGAR COMPONENTES DO XML
        // =====================================================

        imgFotoAluno = findViewById(R.id.imgFotoAluno);

        txtNomeAluno = findViewById(R.id.txtNomeAluno);
        txtNomeUser = findViewById(R.id.txtNomeUser);
        txtEmail = findViewById(R.id.txtEmail);
        txtTelefone = findViewById(R.id.txtTelefone);
        txtCpf = findViewById(R.id.txtCpf);
        txtData = findViewById(R.id.txtData);
        txtPeso = findViewById(R.id.txtPeso);
        txtAltura = findViewById(R.id.txtAltura);
        txtImc = findViewById(R.id.txtImc);
        txtPlano = findViewById(R.id.txtPlano);
        txtStatus = findViewById(R.id.txtStatusAluno);

        btnVoltar = findViewById(R.id.btnVoltar);

        // =====================================================
        // BOTÃO VOLTAR
        // =====================================================

        btnVoltar.setOnClickListener(v -> finish());

        // =====================================================
        // VALIDAR ID
        // =====================================================

        if (idAluno == -1) {

            txtNomeAluno.setText(
                    "Aluno não encontrado"
            );

            return;
        }
        // =====================================================
        // PRÓXIMO PASSO:
        // BUSCAR OS DADOS NA API
        // =====================================================

        buscarDadosAluno();
    }

    // =========================================================
// BUSCAR DADOS DO ALUNO
// =========================================================

    private void buscarDadosAluno() {

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                // =================================================
                // URL DA API
                // =================================================

                String urlApi =
                        "https://sga-api.miguel-r-hoff.workers.dev/alunos-lista?id_alunos="
                                + idAluno;

                URL url = new URL(urlApi);

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setRequestMethod("GET");
                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);

                conexao.setRequestProperty(
                        "Accept",
                        "application/json"
                );

                // =================================================
                // VERIFICAR RESPOSTA
                // =================================================

                int codigoResposta =
                        conexao.getResponseCode();

                InputStream inputStream;

                if (codigoResposta >= 200 &&
                        codigoResposta < 300) {

                    inputStream =
                            conexao.getInputStream();

                } else {

                    inputStream =
                            conexao.getErrorStream();
                }

                // =================================================
                // LER RESPOSTA
                // =================================================

                BufferedReader reader =
                        new BufferedReader(
                                new InputStreamReader(
                                        inputStream
                                )
                        );

                StringBuilder resposta =
                        new StringBuilder();

                String linha;

                while ((linha = reader.readLine()) != null) {

                    resposta.append(linha);
                }

                reader.close();

                // =================================================
                // TRANSFORMAR EM JSON
                // =================================================

                JSONObject json =
                        new JSONObject(
                                resposta.toString()
                        );

                // =================================================
                // VERIFICAR SUCESSO
                // =================================================

                boolean sucesso =
                        json.optBoolean(
                                "sucesso",
                                false
                        );

                if (!sucesso) {

                    String mensagem =
                            json.optString(
                                    "mensagem",
                                    "Aluno não encontrado."
                            );

                    executarNaTela(() -> {

                        txtNomeAluno.setText(
                                mensagem
                        );

                    });

                    return;
                }

                // =================================================
                // PEGAR OBJETO DO ALUNO
                // =================================================

                JSONObject aluno =
                        json.getJSONObject("aluno");

                // =================================================
                // DADOS
                // =================================================

                String nome =
                        aluno.optString(
                                "nome_completo",
                                "-"
                        );

                String nomeUser =
                        aluno.optString(
                                "nome_user",
                                "-"
                        );

                String email =
                        aluno.optString(
                                "email",
                                "-"
                        );

                String data =
                        aluno.optString(
                                "data",
                                "-"
                        );

                String telefone =
                        aluno.optString(
                                "telefone",
                                "-"
                        );

                String cpf =
                        aluno.optString(
                                "cpf",
                                "-"
                        );

                String plano =
                        aluno.optString(
                                "plano",
                                "-"
                        );

                String tipo =
                        aluno.optString(
                                "tipo",
                                "aluno"
                        );

                String fotoUrl =
                        aluno.optString(
                                "foto_url",
                                ""
                        );

                // =================================================
                // DADOS NUMÉRICOS
                // =================================================

                double peso =
                        aluno.optDouble(
                                "peso",
                                0
                        );

                double altura =
                        aluno.optDouble(
                                "altura",
                                0
                        );

                double imc =
                        aluno.optDouble(
                                "imc",
                                0
                        );

                // =================================================
                // ATUALIZAR A INTERFACE
                // =================================================

                executarNaTela(() -> {

                    txtNomeAluno.setText(
                            nome
                    );

                    txtNomeUser.setText(
                            "Usuário: " + nomeUser
                    );

                    txtEmail.setText(
                            "E-mail: " + email
                    );

                    txtData.setText(
                            "Data: " + data
                    );

                    txtTelefone.setText(
                            "Telefone: " + telefone
                    );

                    txtCpf.setText(
                            "CPF: " + cpf
                    );

                    txtPlano.setText(
                            "Plano: " + plano
                    );

                    txtStatus.setText(
                            tipo.toUpperCase()
                    );

                    // =============================================
                    // PESO
                    // =============================================

                    if (peso > 0) {

                        txtPeso.setText(
                                String.format(
                                        "%.1f kg",
                                        peso
                                )
                        );

                    } else {

                        txtPeso.setText(
                                "- kg"
                        );
                    }

                    // =============================================
                    // ALTURA
                    // =============================================

                    if (altura > 0) {

                        txtAltura.setText(
                                String.format(
                                        "%.2f m",
                                        altura
                                )
                        );

                    } else {

                        txtAltura.setText(
                                "- m"
                        );
                    }

                    // =============================================
                    // IMC
                    // =============================================

                    if (imc > 0) {

                        txtImc.setText(
                                String.format(
                                        "%.2f",
                                        imc
                                )
                        );

                    } else {

                        txtImc.setText(
                                "-"
                        );
                    }

                    // =============================================
                    // FOTO
                    // =============================================

                    if (fotoUrl != null && !fotoUrl.trim().isEmpty()) {

                        // Tem foto cadastrada
                        carregarFotoAluno(fotoUrl);

                    } else {

                        // Não tem foto cadastrada
                        setarIconePadraoRedondo();
                    }

                });

            } catch (Exception erro) {

                erro.printStackTrace();

                executarNaTela(() -> {

                    txtNomeAluno.setText(
                            "Erro ao carregar aluno"
                    );

                    txtNomeUser.setText(
                            "Não foi possível conectar à API."
                    );

                });

            } finally {

                if (conexao != null) {

                    conexao.disconnect();
                }
            }

        }).start();
    }


    // =========================================================
// EXECUTAR NA THREAD DA INTERFACE
// =========================================================

    private void executarNaTela(Runnable acao) {

        new Handler(
                Looper.getMainLooper()
        ).post(acao);
    }


    // =========================================================
// CARREGAR FOTO DO ALUNO A PARTIR DA URL
// =========================================================

    private void carregarFotoAluno(String urlFoto) {

        new Thread(() -> {

            HttpURLConnection conexao = null;

            try {

                URL url = new URL(urlFoto);

                conexao = (HttpURLConnection) url.openConnection();

                conexao.setConnectTimeout(10000);
                conexao.setReadTimeout(10000);
                conexao.setDoInput(true);
                conexao.connect();

                InputStream inputStream =
                        conexao.getInputStream();

                Bitmap bitmap =
                        BitmapFactory.decodeStream(inputStream);

                inputStream.close();

                if (bitmap != null) {

                    Bitmap bitmapRedondo =
                            deixarBitmapRedondo(bitmap);

                    executarNaTela(() ->
                            imgFotoAluno.setImageBitmap(bitmapRedondo)
                    );

                } else {

                    executarNaTela(() ->
                            imgFotoAluno.setImageResource(R.drawable.img_perfil)
                    );
                }

            } catch (Exception erro) {

                erro.printStackTrace();

                // Se der erro ao baixar, cai no ícone padrão
                executarNaTela(() ->
                        imgFotoAluno.setImageResource(R.drawable.img_perfil)
                );

            } finally {

                if (conexao != null) {
                    conexao.disconnect();
                }
            }

        }).start();
    }

    // =========================================================
// TRANSFORMAR BITMAP EM CÍRCULO
// =========================================================

    private Bitmap deixarBitmapRedondo(Bitmap bitmapOriginal) {

        int tamanho =
                Math.min(
                        bitmapOriginal.getWidth(),
                        bitmapOriginal.getHeight()
                );

        Bitmap bitmapRedondo =
                Bitmap.createBitmap(
                        tamanho,
                        tamanho,
                        Bitmap.Config.ARGB_8888
                );

        Canvas canvas = new Canvas(bitmapRedondo);

        Paint paint = new Paint();
        paint.setAntiAlias(true);

        BitmapShader shader =
                new BitmapShader(
                        bitmapOriginal,
                        Shader.TileMode.CLAMP,
                        Shader.TileMode.CLAMP
                );

        // Centraliza caso a imagem não seja quadrada
        int dx = (bitmapOriginal.getWidth() - tamanho) / 2;
        int dy = (bitmapOriginal.getHeight() - tamanho) / 2;

        if (dx != 0 || dy != 0) {

            android.graphics.Matrix matrix = new android.graphics.Matrix();
            matrix.setTranslate(-dx, -dy);
            shader.setLocalMatrix(matrix);
        }

        paint.setShader(shader);

        float raio = tamanho / 2f;

        canvas.drawCircle(raio, raio, raio, paint);

        return bitmapRedondo;
    }

    private void setarIconePadraoRedondo() {

        Bitmap bitmapPadrao =
                BitmapFactory.decodeResource(
                        getResources(),
                        R.drawable.img_perfil
                );

        imgFotoAluno.setImageBitmap(
                deixarBitmapRedondo(bitmapPadrao)
        );
    }
}