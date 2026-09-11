package com.example.sga;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class PerfilRepository {

    private static final String URL_PERFIL =
            "https://sga-api.miguel-r-hoff.workers.dev/perfil";

    private final RequestQueue requestQueue;
    private final SharedPreferences preferences;

    // ==========================================
    // CONSTRUTOR
    // ==========================================

    public PerfilRepository(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        preferences = context.getSharedPreferences("login", Context.MODE_PRIVATE);
    }

    // ==========================================
    // BUSCAR PERFIL
    // ==========================================

    public void buscarPerfil(PerfilCallback callback) {
        String token = preferences.getString("token", null);

        if (token == null || token.isEmpty()) {
            callback.onError("Usuário não autenticado.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                URL_PERFIL,
                null,
                response -> {
                    try {
                        boolean sucesso = response.getBoolean("sucesso");
                        if (sucesso) {
                            JSONObject usuario = response.getJSONObject("usuario");
                            callback.onSuccess(usuario);
                        } else {
                            callback.onError(
                                    response.optString("mensagem", "Erro ao buscar perfil.")
                            );
                        }
                    } catch (Exception e) {
                        callback.onError("Resposta inválida da API.");
                    }
                },
                error -> {
                    if (error.networkResponse != null) {
                        int codigo = error.networkResponse.statusCode;
                        if (codigo == 401) {
                            callback.onError("Sessão expirada. Faça login novamente.");
                        } else if (codigo == 404) {
                            callback.onError("Usuário não encontrado.");
                        } else {
                            callback.onError("Erro na API: HTTP " + codigo);
                        }
                    } else {
                        callback.onError("Não foi possível conectar à API.");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // ==========================================
    // ATUALIZAR PERFIL
    // ==========================================

    public void atualizarPerfil(JSONObject dadosAtualizados, PerfilCallback callback) {
        String token = preferences.getString("token", null);

        if (token == null || token.isEmpty()) {
            callback.onError("Usuário não autenticado.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.PUT,
                URL_PERFIL,
                dadosAtualizados,
                response -> {
                    try {
                        boolean sucesso = response.getBoolean("sucesso");
                        if (sucesso) {
                            callback.onSuccess(response.optJSONObject("usuario"));
                        } else {
                            callback.onError(
                                    response.optString("mensagem", "Erro ao atualizar perfil.")
                            );
                        }
                    } catch (Exception e) {
                        callback.onError("Resposta inválida da API.");
                    }
                },
                error -> callback.onError("Erro ao conectar à API para atualização.")
        ) {
            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
        };

        requestQueue.add(request);
    }

    // ==========================================
    // CALLBACK
    // ==========================================

    public interface PerfilCallback {
        void onSuccess(JSONObject usuario);
        void onError(String mensagem);
    }
}