package com.example.sga;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class CadastroProfessorRepository {

    private static final String URL_CADASTRO_PROFESSOR =
            "https://sga-api.miguel-r-hoff.workers.dev/cadastrar-professor";

    private final Context context;
    private final RequestQueue requestQueue;

    public CadastroProfessorRepository(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
    }

    public void cadastrarProfessor(
            String nomeCompleto,
            String nomeUser,
            String email,
            String senha,
            String telefone,
            String cpf,
            double salario,
            CadastroCallback callback
    ) {

        SharedPreferences preferences =
                context.getSharedPreferences("login", Context.MODE_PRIVATE);
        String token = preferences.getString("token", "");

        JSONObject body = new JSONObject();
        try {
            body.put("nome_completo", nomeCompleto);
            body.put("nome_user", nomeUser);
            body.put("email", email);
            body.put("senha", senha);
            body.put("telefone", telefone);
            body.put("cpf", cpf);
            body.put("salario", salario);
        } catch (JSONException e) {
            callback.onError("Erro ao montar os dados.");
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                URL_CADASTRO_PROFESSOR,
                body,

                response -> {
                    boolean sucesso = response.optBoolean("sucesso", false);
                    if (sucesso) {
                        callback.onSuccess(response.optString("mensagem", "Cadastrado com sucesso."));
                    } else {
                        callback.onError(response.optString("mensagem", "Não foi possível cadastrar o professor."));
                    }
                },

                error -> {
                    if (error.networkResponse != null) {
                        int codigo = error.networkResponse.statusCode;

                        if (codigo == 409) {
                            callback.onError("Já existe um professor com esse e-mail ou usuário.");
                        } else if (codigo == 400) {
                            callback.onError("Preencha todos os campos corretamente.");
                        } else {
                            callback.onError("Erro na API: HTTP " + codigo);
                        }
                    } else {
                        callback.onError("Não foi possível conectar à API.");
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                if (!token.isEmpty()) {
                    headers.put("Authorization", "Bearer " + token);
                }
                return headers;
            }
        };

        request.setShouldCache(false);
        requestQueue.add(request);
    }

    public interface CadastroCallback {
        void onSuccess(String mensagem);
        void onError(String mensagem);
    }
}