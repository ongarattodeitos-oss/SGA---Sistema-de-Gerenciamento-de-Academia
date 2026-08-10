package com.example.sga;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;


public class CadastroActivity extends AppCompatActivity {


    EditText edtNome;
    EditText edtEmail;
    EditText edtTelefone;
    EditText edtCpf;
    EditText edtNascimento;
    EditText edtUsuario;
    EditText edtSenha;
    EditText edtConfirmarSenha;

    Button btnCadastrar;

    TextView txtLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);

        setContentView(R.layout.activity_cadastro);


        ViewCompat.setOnApplyWindowInsetsListener(
                findViewById(R.id.main),
                (v, insets) -> {

                    Insets systemBars =
                            insets.getInsets(WindowInsetsCompat.Type.systemBars());

                    v.setPadding(
                            systemBars.left,
                            systemBars.top,
                            systemBars.right,
                            systemBars.bottom
                    );

                    return insets;
                });


        // Ligando XML com Java

        edtNome = findViewById(R.id.edtNome);
        edtEmail = findViewById(R.id.edtEmail);
        edtTelefone = findViewById(R.id.edtTelefone);
        edtCpf = findViewById(R.id.edtCpf);
        edtNascimento = findViewById(R.id.edtNascimento);
        edtUsuario = findViewById(R.id.edtUsuario);
        edtSenha = findViewById(R.id.edtSenha);
        edtConfirmarSenha = findViewById(R.id.edtConfirmarSenha);


        btnCadastrar = findViewById(R.id.btnCadastrar);

        txtLogin = findViewById(R.id.txtLogin);



        btnCadastrar.setOnClickListener(v -> {

            cadastrarUsuario();

        });



        txtLogin.setOnClickListener(v -> {

            finish();

        });


    }



    private void cadastrarUsuario(){


        String nome =
                edtNome.getText().toString().trim();

        String email =
                edtEmail.getText().toString().trim();

        String telefone =
                edtTelefone.getText().toString().trim();

        String cpf =
                edtCpf.getText().toString().trim();

        String nascimento =
                edtNascimento.getText().toString().trim();

        String usuario =
                edtUsuario.getText().toString().trim();

        String senha =
                edtSenha.getText().toString().trim();

        String confirmar =
                edtConfirmarSenha.getText().toString().trim();



        // Validação

        if(TextUtils.isEmpty(nome) ||
                TextUtils.isEmpty(email) ||
                TextUtils.isEmpty(usuario) ||
                TextUtils.isEmpty(senha)){


            Toast.makeText(
                    this,
                    "Preencha todos os campos obrigatórios",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }



        if(!senha.equals(confirmar)){


            Toast.makeText(
                    this,
                    "As senhas não coincidem",
                    Toast.LENGTH_SHORT
            ).show();


            return;

        }



        Toast.makeText(
                this,
                "Cadastro pronto para enviar para API",
                Toast.LENGTH_SHORT
        ).show();



        /*
        Aqui futuramente entra:

        API + Banco MySQL

        Exemplo:

        enviarCadastro(
        nome,
        email,
        telefone,
        cpf,
        nascimento,
        usuario,
        senha
        );

        */

    }


}