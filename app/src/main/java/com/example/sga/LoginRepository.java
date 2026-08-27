package com.example.sga;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LoginRepository {

    private static final String URL_LOGIN =
            "https://sga-api.miguel-r-hoff.workers.dev/login";

    private final RequestQueue requestQueue;

    public LoginRepository(Context context) {
        requestQueue = Volley.newRequestQueue(context);
    }

    public void fazerLogin(
            String usuario,
            String senha,
            LoginCallback callback
    ) {

        JSONObject dados = new JSONObject();

        try {
            dados.put("usuario", usuario);
            dados.put("senha", senha);

        } catch (JSONException e) {
            callback.onError("Erro ao preparar os dados do login.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_LOGIN,
                dados,

                response -> {

                    try {

                        boolean sucesso =
                                response.getBoolean("sucesso");

                        if (sucesso) {

                            JSONObject usuarioJson =
                                    response.getJSONObject("usuario");

                            String token =
                                    response.getString("token");

                            callback.onSuccess(
                                    usuarioJson,
                                    token
                            );

                        } else {

                            String erro =
                                    response.optString(
                                            "error",
                                            "Erro ao realizar login."
                                    );

                            callback.onError(erro);
                        }

                    } catch (JSONException e) {

                        callback.onError(
                                "Resposta inválida da API."
                        );
                    }
                },

                error -> {

                    if (error.networkResponse != null) {

                        int codigo =
                                error.networkResponse.statusCode;

                        switch (codigo) {

                            case 400:
                                callback.onError(
                                        "Usuário e senha são obrigatórios."
                                );
                                break;

                            case 401:
                                callback.onError(
                                        "Usuário ou senha incorretos."
                                );
                                break;

                            case 404:
                                callback.onError(
                                        "Rota /login não encontrada na API."
                                );
                                break;

                            case 500:
                                callback.onError(
                                        "Erro interno no servidor."
                                );
                                break;

                            default:
                                callback.onError(
                                        "Erro na API: HTTP " + codigo
                                );
                                break;
                        }

                    } else {

                        callback.onError(
                                "Não foi possível conectar à API."
                        );
                    }
                }
        ) {

            @Override
            public Map<String, String> getHeaders() {

                Map<String, String> headers =
                        new HashMap<>();

                headers.put(
                        "Content-Type",
                        "application/json"
                );

                return headers;
            }
        };

        requestQueue.add(request);
    }

    public interface LoginCallback {

        void onSuccess(
                JSONObject usuario,
                String token
        );

        void onError(String mensagem);
    }
}