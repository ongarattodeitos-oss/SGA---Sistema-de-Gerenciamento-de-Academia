package com.example.sga;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FotoPerfilProfessorRepository {

    private static final String URL_PERFIL =
            "https://sga-api.miguel-r-hoff.workers.dev/atualizar-perfil";

    private static final String URL_FOTO =
            "https://sga-api.miguel-r-hoff.workers.dev/atualizar-perfil/foto";

    private final Context context;
    private final RequestQueue requestQueue;

    public FotoPerfilProfessorRepository(Context context) {

        this.context = context;
        this.requestQueue =
                Volley.newRequestQueue(context);
    }

    // ============================================================
    // CARREGAR FOTO (GET)
    // ============================================================

    public void carregarFoto(
            FotoCallback callback
    ) {

        // ==========================================
        // PEGA O ID DO PROFESSOR
        // ==========================================

        SharedPreferences preferences =
                context.getSharedPreferences(
                        "login",
                        Context.MODE_PRIVATE
                );

        int idFuncionario =
                preferences.getInt(
                        "id_funcionario",
                        -1
                );

        // ==========================================
        // VERIFICA ID
        // ==========================================

        if (idFuncionario == -1) {

            callback.onError(
                    "ID do professor não encontrado."
            );

            return;
        }

        // ==========================================
        // URL CORRETA
        // ==========================================

        String url =
                URL_PERFIL
                        + "?id_funcionario="
                        + idFuncionario;

        // ==========================================
        // REQUEST
        // ==========================================

        JsonObjectRequest request =
                new JsonObjectRequest(
                        Request.Method.GET,
                        url,
                        null,

                        response -> {

                            try {

                                boolean sucesso =
                                        response.optBoolean(
                                                "sucesso",
                                                false
                                        );

                                if (!sucesso) {

                                    callback.onFotoNaoEncontrada();
                                    return;
                                }

                                JSONObject usuario =
                                        response.optJSONObject(
                                                "usuario"
                                        );

                                if (usuario == null) {

                                    callback.onFotoNaoEncontrada();
                                    return;
                                }

                                String fotoUrl =
                                        usuario.optString(
                                                "foto_url",
                                                ""
                                        );

                                if (
                                        fotoUrl == null ||
                                                fotoUrl.trim().isEmpty() ||
                                                fotoUrl.equals("null")
                                ) {

                                    callback.onFotoNaoEncontrada();
                                    return;
                                }

                                callback.onFotoCarregada(
                                        fotoUrl
                                );

                            } catch (Exception e) {

                                e.printStackTrace();

                                callback.onError(
                                        "Erro ao processar a foto."
                                );
                            }
                        },

                        error -> {

                            error.printStackTrace();

                            callback.onError(
                                    "Não foi possível carregar a foto."
                            );
                        }
                );

        request.setShouldCache(false);

        requestQueue.add(request);
    }

    // ============================================================
    // ENVIAR FOTO (POST multipart)
    // ============================================================

    public void enviarFoto(byte[] imagem, UploadCallback callback) {

        SharedPreferences preferences =
                context.getSharedPreferences(
                        "login",
                        Context.MODE_PRIVATE
                );

        int idFuncionario =
                preferences.getInt(
                        "id_funcionario",
                        -1
                );

        if (idFuncionario == -1) {

            callback.onError("Professor não identificado.");

            return;
        }

        if (imagem == null || imagem.length == 0) {

            callback.onError("Imagem inválida.");

            return;
        }

        String url = URL_FOTO + "?id_funcionario=" + idFuncionario;

        MultipartRequest request = new MultipartRequest(

                Request.Method.POST,

                url,

                response -> {

                    try {

                        String resposta = new String(
                                response.data,
                                StandardCharsets.UTF_8
                        );

                        callback.onSuccess(resposta);

                    } catch (Exception e) {

                        callback.onError(
                                "Resposta inválida do servidor."
                        );
                    }
                },

                error -> {

                    if (error.networkResponse != null) {

                        int codigo =
                                error.networkResponse.statusCode;

                        callback.onError(
                                "Erro na API: HTTP " + codigo
                        );

                    } else {

                        callback.onError(
                                "Não foi possível conectar à API."
                        );
                    }
                },

                imagem
        );

        requestQueue.add(request);
    }

    // ============================================================
    // CALLBACKS
    // ============================================================

    public interface FotoCallback {

        void onFotoCarregada(
                String fotoUrl
        );

        void onFotoNaoEncontrada();

        void onError(
                String mensagem
        );
    }

    public interface UploadCallback {

        void onSuccess(String resposta);

        void onError(String mensagem);
    }

    // ============================================================
    // MULTIPART REQUEST (interna, usada só pelo enviarFoto)
    // ============================================================

    private static class MultipartRequest
            extends Request<NetworkResponse> {

        private final Response.Listener<NetworkResponse> listener;

        private final byte[] imagem;

        private final String boundary =
                "----SGABoundary" + System.currentTimeMillis();

        MultipartRequest(
                int method,
                String url,
                Response.Listener<NetworkResponse> listener,
                Response.ErrorListener errorListener,
                byte[] imagem
        ) {

            super(method, url, errorListener);

            this.listener = listener;
            this.imagem = imagem;
        }

        @Override
        public String getBodyContentType() {

            return "multipart/form-data; boundary=" + boundary;
        }

        @Override
        public byte[] getBody() throws AuthFailureError {

            try {

                ByteArrayOutputStream output =
                        new ByteArrayOutputStream();

                String inicio =
                        "--" + boundary + "\r\n" +
                                "Content-Disposition: form-data; " +
                                "name=\"foto\"; " +
                                "filename=\"perfil.jpg\"\r\n" +
                                "Content-Type: image/jpeg\r\n\r\n";

                output.write(
                        inicio.getBytes(StandardCharsets.UTF_8)
                );

                output.write(imagem);

                String finalMultipart =
                        "\r\n--" + boundary + "--\r\n";

                output.write(
                        finalMultipart.getBytes(
                                StandardCharsets.UTF_8
                        )
                );

                return output.toByteArray();

            } catch (IOException e) {

                throw new AuthFailureError();
            }
        }

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(
                NetworkResponse response
        ) {

            return Response.success(
                    response,
                    null
            );
        }

        @Override
        protected void deliverResponse(
                NetworkResponse response
        ) {

            listener.onResponse(response);
        }
    }
}