package com.example.sga;

import android.content.Context;
import android.net.Uri;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class ExameRepository {

    private static final String URL_EXAMES =
            "https://sga-api.miguel-r-hoff.workers.dev/exames";

    private final RequestQueue requestQueue;
    private final Context context;

    public ExameRepository(Context context) {
        this.context = context.getApplicationContext();
        requestQueue = Volley.newRequestQueue(this.context);
    }

    // =================================================
    // CADASTRAR EXAME
    // =================================================

    public void cadastrarExame(
            int idUser,
            Uri arquivoUri,
            String nomeArquivo,
            String tipoArquivo,
            String descricao,
            ExameCallback callback
    ) {
        try {

            // ==========================================
            // LER ARQUIVO
            // ==========================================
            byte[] arquivoBytes = lerArquivo(arquivoUri);

            if (arquivoBytes == null || arquivoBytes.length == 0) {
                callback.onError("Não foi possível ler o arquivo.");
                return;
            }

            // ==========================================
            // CRIAR REQUEST
            // ==========================================
            MultipartRequest request = new MultipartRequest(
                    Request.Method.POST,
                    URL_EXAMES,

                    response -> {
                        try {
                            boolean sucesso = response.getBoolean("success");

                            if (sucesso) {
                                JSONObject exame = response.getJSONObject("exame");
                                callback.onSuccess(exame);
                            } else {
                                String erro = response.optString("error", "Erro ao cadastrar exame.");
                                callback.onError(erro);
                            }
                        } catch (JSONException e) {
                            callback.onError("Resposta inválida da API.");
                        }
                    },

                    error -> tratarErro(error, callback)
            );

            // ==========================================
            // DADOS DO EXAME
            // ==========================================
            request.addStringPart("id_user", String.valueOf(idUser));
            request.addStringPart("nome_arquivo", nomeArquivo);
            request.addStringPart("tipo_arquivo", tipoArquivo);
            request.addStringPart("descricao", descricao != null ? descricao : "");

            // ==========================================
            // ARQUIVO
            // ==========================================
            request.addFilePart("arquivo", nomeArquivo, tipoArquivo, arquivoBytes);

            // ==========================================
            // ENVIA
            // ==========================================
            requestQueue.add(request);

        } catch (Exception e) {
            callback.onError("Erro ao preparar o arquivo: " + e.getMessage());
        }
    }

    // =================================================
    // LISTAR EXAMES DO USUÁRIO
    // =================================================

    public void listarExames(int idUser, ListaExamesCallback callback) {
        String url = URL_EXAMES + "/" + idUser;

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,

                response -> {
                    try {
                        boolean sucesso = response.getBoolean("success");

                        if (sucesso) {
                            JSONArray exames = response.getJSONArray("exames");
                            callback.onSuccess(exames);
                        } else {
                            String erro = response.optString("error", "Erro ao buscar exames.");
                            callback.onError(erro);
                        }
                    } catch (JSONException e) {
                        callback.onError("Resposta inválida da API.");
                    }
                },

                error -> callback.onError("Não foi possível buscar os exames.")
        );

        requestQueue.add(request);
    }

    // =================================================
    // LER ARQUIVO
    // =================================================

    private byte[] lerArquivo(Uri uri) throws IOException {
        InputStream inputStream = context.getContentResolver().openInputStream(uri);

        if (inputStream == null) {
            return null;
        }

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int quantidade;

        while ((quantidade = inputStream.read(buffer)) != -1) {
            output.write(buffer, 0, quantidade);
        }

        inputStream.close();

        return output.toByteArray();
    }

    // =================================================
    // TRATAR ERROS
    // =================================================

    private void tratarErro(VolleyError error, ExameCallback callback) {
        if (error.networkResponse != null) {
            int codigo = error.networkResponse.statusCode;

            switch (codigo) {
                case 400:
                    callback.onError("Dados do exame inválidos.");
                    break;

                case 404:
                    callback.onError("Usuário não encontrado.");
                    break;

                case 405:
                    callback.onError("Método não permitido.");
                    break;

                case 413:
                    callback.onError("Arquivo muito grande.");
                    break;

                case 500:
                    callback.onError("Erro interno no servidor.");
                    break;

                default:
                    callback.onError("Erro na API: HTTP " + codigo);
                    break;
            }
        } else {
            callback.onError("Não foi possível conectar à API.");
        }
    }

    // =================================================
    // CALLBACKS
    // =================================================

    public interface ExameCallback {
        void onSuccess(JSONObject exame);

        void onError(String mensagem);
    }

    public interface ListaExamesCallback {
        void onSuccess(JSONArray exames);

        void onError(String mensagem);
    }

    // =================================================
    // MULTIPART REQUEST
    // =================================================

    private static class MultipartRequest extends Request<JSONObject> {

        private final Response.Listener<JSONObject> listener;
        private final Map<String, String> params = new HashMap<>();
        private final Map<String, FilePart> files = new HashMap<>();
        private final String boundary;

        public MultipartRequest(
                int method,
                String url,
                Response.Listener<JSONObject> listener,
                Response.ErrorListener errorListener
        ) {
            super(method, url, errorListener);

            this.listener = listener;

            // ==========================================
            // CRIA UM ÚNICO BOUNDARY
            // ==========================================
            this.boundary = "----SGAFormBoundary" + System.currentTimeMillis();
        }

        // =================================================
        // CAMPO DE TEXTO
        // =================================================

        public void addStringPart(String name, String value) {
            params.put(name, value);
        }

        // =================================================
        // ARQUIVO
        // =================================================

        public void addFilePart(String fieldName, String fileName, String mimeType, byte[] data) {
            files.put(fieldName, new FilePart(fileName, mimeType, data));
        }

        // =================================================
        // CONTENT TYPE
        // =================================================

        @Override
        public String getBodyContentType() {
            return "multipart/form-data; boundary=" + boundary;
        }

        // =================================================
        // BODY
        // =================================================

        @Override
        public byte[] getBody() throws AuthFailureError {
            ByteArrayOutputStream output = new ByteArrayOutputStream();

            try {

                // ==========================================
                // CAMPOS DE TEXTO
                // ==========================================
                for (Map.Entry<String, String> entry : params.entrySet()) {
                    output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));

                    output.write(
                            ("Content-Disposition: form-data; name=\"" + entry.getKey() + "\"\r\n\r\n")
                                    .getBytes(StandardCharsets.UTF_8)
                    );

                    output.write(entry.getValue().getBytes(StandardCharsets.UTF_8));
                    output.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }

                // ==========================================
                // ARQUIVOS
                // ==========================================
                for (Map.Entry<String, FilePart> entry : files.entrySet()) {
                    FilePart file = entry.getValue();

                    output.write(("--" + boundary + "\r\n").getBytes(StandardCharsets.UTF_8));

                    output.write(
                            ("Content-Disposition: form-data; name=\"" + entry.getKey()
                                    + "\"; filename=\"" + file.fileName + "\"\r\n")
                                    .getBytes(StandardCharsets.UTF_8)
                    );

                    output.write(
                            ("Content-Type: " + file.mimeType + "\r\n\r\n")
                                    .getBytes(StandardCharsets.UTF_8)
                    );

                    output.write(file.data);
                    output.write("\r\n".getBytes(StandardCharsets.UTF_8));
                }

                // ==========================================
                // FINALIZA O MULTIPART
                // ==========================================
                output.write(("--" + boundary + "--\r\n").getBytes(StandardCharsets.UTF_8));

                return output.toByteArray();

            } catch (IOException e) {
                throw new AuthFailureError(e.getMessage());
            }
        }

        @Override
        protected Response<JSONObject> parseNetworkResponse(NetworkResponse response) {
            try {
                String json = new String(response.data, StandardCharsets.UTF_8);
                return Response.success(new JSONObject(json), null);
            } catch (Exception e) {
                return Response.error(new VolleyError(e));
            }
        }

        @Override
        protected void deliverResponse(JSONObject response) {
            listener.onResponse(response);
        }

        // =================================================
        // CLASSE DO ARQUIVO
        // =================================================

        private static class FilePart {

            String fileName;
            String mimeType;
            byte[] data;

            FilePart(String fileName, String mimeType, byte[] data) {
                this.fileName = fileName;
                this.mimeType = mimeType;
                this.data = data;
            }
        }
    }
}