package com.example.sga;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class FotoPerfilRepository {

    // ============================================================
    // URL DA FOTO DE PERFIL DO ALUNO
    // ============================================================

    private static final String URL_FOTO =
            "https://sga-api.miguel-r-hoff.workers.dev/perfil/foto";

    private final RequestQueue requestQueue;
    private final SharedPreferences preferences;

    // ============================================================
    // CONSTRUTOR
    // ============================================================

    public FotoPerfilRepository(Context context) {

        requestQueue = Volley.newRequestQueue(context);

        preferences = context.getSharedPreferences(
                "login",
                Context.MODE_PRIVATE
        );
    }

    // ============================================================
    // ENVIAR FOTO DO ALUNO
    // ============================================================

    public void enviarFoto(byte[] imagem, FotoCallback callback) {

        // --------------------------------------------------------
        // VERIFICAR TOKEN
        // --------------------------------------------------------

        String token = preferences.getString("token", null);

        if (token == null || token.isEmpty()) {

            callback.onError("Usuário não autenticado.");

            return;
        }

        // --------------------------------------------------------
        // VERIFICAR IMAGEM
        // --------------------------------------------------------

        if (imagem == null || imagem.length == 0) {

            callback.onError("Imagem inválida.");

            return;
        }

        // --------------------------------------------------------
        // CRIAR REQUEST
        // --------------------------------------------------------

        MultipartRequest request = new MultipartRequest(

                Request.Method.POST,

                URL_FOTO,

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

                imagem,

                token
        );

        // --------------------------------------------------------
        // ADICIONAR NA FILA
        // --------------------------------------------------------

        requestQueue.add(request);
    }

    // ============================================================
    // CALLBACK
    // ============================================================

    public interface FotoCallback {

        void onSuccess(String resposta);

        void onError(String mensagem);
    }

    // ============================================================
    // MULTIPART REQUEST
    // ============================================================

    private static class MultipartRequest
            extends Request<NetworkResponse> {

        private final Response.Listener<NetworkResponse> listener;

        private final byte[] imagem;

        private final String token;

        private final String boundary =
                "----SGABoundary" + System.currentTimeMillis();

        // --------------------------------------------------------
        // CONSTRUTOR
        // --------------------------------------------------------

        MultipartRequest(
                int method,
                String url,
                Response.Listener<NetworkResponse> listener,
                Response.ErrorListener errorListener,
                byte[] imagem,
                String token
        ) {

            super(method, url, errorListener);

            this.listener = listener;
            this.imagem = imagem;
            this.token = token;
        }

        // --------------------------------------------------------
        // CONTENT TYPE
        // --------------------------------------------------------

        @Override
        public String getBodyContentType() {

            return "multipart/form-data; boundary=" + boundary;
        }

        // --------------------------------------------------------
        // BODY
        // --------------------------------------------------------

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

        // --------------------------------------------------------
        // HEADERS
        // --------------------------------------------------------

        @Override
        public Map<String, String> getHeaders()
                throws AuthFailureError {

            Map<String, String> headers =
                    new HashMap<>();

            headers.put(
                    "Authorization",
                    "Bearer " + token
            );

            return headers;
        }

        // --------------------------------------------------------
        // PARSE RESPONSE
        // --------------------------------------------------------

        @Override
        protected Response<NetworkResponse> parseNetworkResponse(
                NetworkResponse response
        ) {

            return Response.success(
                    response,
                    null
            );
        }

        // --------------------------------------------------------
        // ENTREGAR RESPONSE
        // --------------------------------------------------------

        @Override
        protected void deliverResponse(
                NetworkResponse response
        ) {

            listener.onResponse(response);
        }
    }
}