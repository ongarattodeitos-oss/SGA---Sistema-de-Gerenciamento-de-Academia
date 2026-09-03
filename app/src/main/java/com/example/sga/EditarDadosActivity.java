package com.example.sga;

import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EditarDadosActivity extends AppCompatActivity {

    private ImageView imgFotoPerfil;
    private EditText edtNome, edtUsuario, edtEmail, edtSenha;
    private Button btnVoltar, btnAlterarFoto, btnSalvarAlteracoes;

    private SharedPreferences preferences;
    private FotoPerfilRepository fotoRepository;

    private String originalNome = "";
    private String originalUsuario = "";
    private String originalEmail = "";

    private Uri fotoSelecionadaUri = null;
    private ActivityResultLauncher<String> seletorGaleriaLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editar_dados);

        preferences = getSharedPreferences("login", MODE_PRIVATE);
        fotoRepository = new FotoPerfilRepository(this);

        imgFotoPerfil = findViewById(R.id.imgFotoPerfil);
        edtNome = findViewById(R.id.edtNome);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnAlterarFoto = findViewById(R.id.btnAlterarFoto);
        btnSalvarAlteracoes = findViewById(R.id.btnSalvarAlteracoes);

        seletorGaleriaLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        fotoSelecionadaUri = uri;
                        imgFotoPerfil.setImageURI(uri);
                        verificarAlteracoes();
                    }
                }
        );

        btnVoltar.setOnClickListener(v -> finish());
        btnAlterarFoto.setOnClickListener(v -> seletorGaleriaLauncher.launch("image/*"));
        btnSalvarAlteracoes.setOnClickListener(v -> validarESalvar());

        carregarDados();
        configurarMonitoresDeTexto();
        verificarAlteracoes();
    }

    private void carregarDados() {
        originalNome = preferences.getString("nome_completo", "");
        originalUsuario = preferences.getString("nome_user", "");
        originalEmail = preferences.getString("email", "");
        String fotoUrl = preferences.getString("foto_url", "");

        edtNome.setText(originalNome);
        edtUsuario.setText(originalUsuario);
        edtEmail.setText(originalEmail);

        if (!fotoUrl.isEmpty()) {
            carregarImagemDeUrl(fotoUrl);
        }
    }

    private void carregarImagemDeUrl(String urlFoto) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executor.execute(() -> {
            try {
                InputStream in = new URL(urlFoto).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                handler.post(() -> imgFotoPerfil.setImageBitmap(bitmap));
            } catch (Exception ignored) {}
        });
    }

    private void configurarMonitoresDeTexto() {
        TextWatcher watcher = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) { verificarAlteracoes(); }
            @Override public void afterTextChanged(Editable s) {}
        };

        edtNome.addTextChangedListener(watcher);
        edtUsuario.addTextChangedListener(watcher);
        edtEmail.addTextChangedListener(watcher);
        edtSenha.addTextChangedListener(watcher);
    }

    private void verificarAlteracoes() {
        String nomeAtual = edtNome.getText().toString().trim();
        String usuarioAtual = edtUsuario.getText().toString().trim();
        String emailAtual = edtEmail.getText().toString().trim();
        String senhaAtual = edtSenha.getText().toString().trim();

        boolean alterouNome = !nomeAtual.equals(originalNome);
        boolean alterouUsuario = !usuarioAtual.equals(originalUsuario);
        boolean alterouEmail = !emailAtual.equals(originalEmail);
        boolean digitouSenha = !senhaAtual.isEmpty();
        boolean alterouFoto = fotoSelecionadaUri != null;

        boolean houveAlteracao = alterouNome || alterouUsuario || alterouEmail || digitouSenha || alterouFoto;

        btnSalvarAlteracoes.setEnabled(houveAlteracao);
        btnSalvarAlteracoes.setAlpha(houveAlteracao ? 1.0f : 0.5f);
    }

    private void validarESalvar() {
        String nome = edtNome.getText().toString().trim();
        String usuario = edtUsuario.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();

        if (nome.isEmpty()) { edtNome.setError("Digite seu nome"); edtNome.requestFocus(); return; }
        if (usuario.isEmpty()) { edtUsuario.setError("Digite seu usuário"); edtUsuario.requestFocus(); return; }
        if (email.isEmpty()) { edtEmail.setError("Digite seu e-mail"); edtEmail.requestFocus(); return; }

        processarAtualizacao(nome, usuario, email, senha);
    }

    private void processarAtualizacao(String nome, String usuario, String email, String senha) {
        btnSalvarAlteracoes.setEnabled(false);
        btnSalvarAlteracoes.setText("SALVANDO...");
        btnSalvarAlteracoes.setAlpha(0.5f);

        if (fotoSelecionadaUri != null) {
            byte[] bytesImagem = uriParaByteArray(fotoSelecionadaUri);

            if (bytesImagem != null) {
                fotoRepository.enviarFoto(bytesImagem, new FotoPerfilRepository.FotoCallback() {
                    @Override
                    public void onSuccess(String resposta) {
                        try {
                            JSONObject json = new JSONObject(resposta);
                            String fotoUrl = json.optString("foto_url", null);

                            if (fotoUrl != null && !fotoUrl.isEmpty()) {
                                preferences.edit().putString("foto_url", fotoUrl).apply();
                            }

                            enviarDadosTextoServidor(nome, usuario, email, senha);
                        } catch (Exception e) {
                            Toast.makeText(EditarDadosActivity.this, "Erro ao processar resposta da foto.", Toast.LENGTH_SHORT).show();
                            restaurarBotaoSalvar();
                        }
                    }

                    @Override
                    public void onError(String mensagem) {
                        Toast.makeText(EditarDadosActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                        restaurarBotaoSalvar();
                    }
                });
            } else {
                Toast.makeText(this, "Erro ao processar imagem.", Toast.LENGTH_SHORT).show();
                restaurarBotaoSalvar();
            }
        } else {
            enviarDadosTextoServidor(nome, usuario, email, senha);
        }
    }

    private void enviarDadosTextoServidor(String nome, String usuario, String email, String senha) {
        String token = preferences.getString("token", "");
        int idUser = preferences.getInt("id_user", -1);
        String tipo = preferences.getString("tipo", "professor");

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            try {
                boolean sucesso = atualizarDadosTexto(idUser, nome, usuario, email, senha, tipo, token);
                handler.post(() -> {
                    if (sucesso) {
                        SharedPreferences.Editor editor = preferences.edit();
                        editor.putString("nome_completo", nome);
                        editor.putString("nome_user", usuario);
                        editor.putString("email", email);
                        editor.apply();

                        Toast.makeText(EditarDadosActivity.this, "Dados atualizados com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(EditarDadosActivity.this, "Erro ao atualizar dados.", Toast.LENGTH_SHORT).show();
                        restaurarBotaoSalvar();
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    Toast.makeText(EditarDadosActivity.this, "Erro: " + e.getLocalizedMessage(), Toast.LENGTH_LONG).show();
                    restaurarBotaoSalvar();
                });
            }
        });
    }

    private boolean atualizarDadosTexto(int idUser, String nome, String usuario, String email, String senha, String tipo, String token) throws Exception {
        URL url = new URL("https://sga-api.miguel-r-hoff.workers.dev/atualizar-perfil");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        if (!token.isEmpty()) conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setDoOutput(true);

        JSONObject body = new JSONObject();
        body.put("id_user", idUser);
        body.put("nome_completo", nome);
        body.put("nome_user", usuario);
        body.put("email", email);
        body.put("tipo", tipo);
        if (!senha.isEmpty()) body.put("senha", senha);

        try (OutputStream os = conn.getOutputStream()) {
            byte[] input = body.toString().getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        int code = conn.getResponseCode();
        conn.disconnect();
        return code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED;
    }

    private byte[] uriParaByteArray(Uri uri) {
        try {
            InputStream inputStream = getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            if (bitmap == null) return null;

            Bitmap bitmapRedimensionado = Bitmap.createScaledBitmap(bitmap, 600, 600, true);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmapRedimensionado.compress(Bitmap.CompressFormat.JPEG, 80, baos);
            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void restaurarBotaoSalvar() {
        btnSalvarAlteracoes.setText("SALVAR ALTERAÇÕES");
        verificarAlteracoes();
    }
}