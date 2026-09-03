package com.example.sga;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class FotoPerfilProfessorRepository {

    private static final String URL_PERFIL =
            "https://sga-api.miguel-r-hoff.workers.dev/atualizar-perfil";

    private final Context context;
    private final RequestQueue requestQueue;

    public FotoPerfilProfessorRepository(Context context) {

        this.context = context;
        this.requestQueue =
                Volley.newRequestQueue(context);
    }

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

    public interface FotoCallback {

        void onFotoCarregada(
                String fotoUrl
        );

        void onFotoNaoEncontrada();

        void onError(
                String mensagem
        );
    }
}