package com.example.sga;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class CadastroProfessorActivity extends AppCompatActivity {

    private EditText edtNome, edtTelefone, edtCpf, edtSalario;
    private EditText edtUsuario, edtEmail, edtSenha, edtConfirmarSenha;
    private Button btnVoltar;
    private AppCompatButton btnCadastrar;

    private CadastroProfessorRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_professor);

        repository = new CadastroProfessorRepository(this);

        edtNome = findViewById(R.id.edtNome);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCpf = findViewById(R.id.edtCpf);
        edtSalario = findViewById(R.id.edtSalario);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtEmail = findViewById(R.id.edtEmail);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);

        btnVoltar = findViewById(R.id.btnVoltar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnVoltar.setOnClickListener(v -> finish());
        btnCadastrar.setOnClickListener(v -> validarECadastrar());
    }

    private void validarECadastrar() {

        String nome = edtNome.getText().toString().trim();
        String telefone = edtTelefone.getText().toString().trim();
        String cpf = edtCpf.getText().toString().trim();
        String salarioStr = edtSalario.getText().toString().trim().replace(",", ".");
        String usuario = edtUsuario.getText().toString().trim();
        String email = edtEmail.getText().toString().trim();
        String senha = edtSenha.getText().toString().trim();
        String confirmarSenha = edtConfirmarSenha.getText().toString().trim();

        if (TextUtils.isEmpty(nome)) { edtNome.setError("Digite o nome completo"); edtNome.requestFocus(); return; }
        if (TextUtils.isEmpty(telefone)) { edtTelefone.setError("Digite o telefone"); edtTelefone.requestFocus(); return; }
        if (TextUtils.isEmpty(cpf)) { edtCpf.setError("Digite o CPF"); edtCpf.requestFocus(); return; }
        if (TextUtils.isEmpty(salarioStr)) { edtSalario.setError("Digite o salário"); edtSalario.requestFocus(); return; }
        if (TextUtils.isEmpty(usuario)) { edtUsuario.setError("Digite o usuário"); edtUsuario.requestFocus(); return; }

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            edtEmail.setError("Digite um e-mail válido");
            edtEmail.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(senha) || senha.length() < 6) {
            edtSenha.setError("A senha deve ter ao menos 6 caracteres");
            edtSenha.requestFocus();
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            edtConfirmarSenha.setError("As senhas não coincidem");
            edtConfirmarSenha.requestFocus();
            return;
        }

        double salario;
        try {
            salario = Double.parseDouble(salarioStr);
        } catch (NumberFormatException e) {
            edtSalario.setError("Salário inválido");
            edtSalario.requestFocus();
            return;
        }

        btnCadastrar.setEnabled(false);
        btnCadastrar.setText("CADASTRANDO...");
        btnCadastrar.setAlpha(0.5f);

        repository.cadastrarProfessor(
                nome, usuario, email, senha, telefone, cpf, salario,
                new CadastroProfessorRepository.CadastroCallback() {
                    @Override
                    public void onSuccess(String mensagem) {
                        Toast.makeText(CadastroProfessorActivity.this, "Professor cadastrado com sucesso!", Toast.LENGTH_SHORT).show();
                        finish();
                    }

                    @Override
                    public void onError(String mensagem) {
                        Toast.makeText(CadastroProfessorActivity.this, mensagem, Toast.LENGTH_SHORT).show();
                        restaurarBotao();
                    }
                }
        );
    }

    private void restaurarBotao() {
        btnCadastrar.setEnabled(true);
        btnCadastrar.setText("CADASTRAR PROFESSOR");
        btnCadastrar.setAlpha(1.0f);
    }
}