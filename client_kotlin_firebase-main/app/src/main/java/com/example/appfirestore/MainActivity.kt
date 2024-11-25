package com.example.appfirestore

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.appfirestore.ui.theme.AppFirestoreTheme
import com.example.appfirestore.ui.theme.background
import com.example.appfirestore.ui.theme.dark_black
import com.example.appfirestore.ui.theme.green
import com.example.appfirestore.ui.theme.light_black
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import androidx.compose.material3.Text as Text1
import com.example.appfirestore.MainScreen as MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AppFirestoreTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ){
                    MainScreen()
                }
            }
        }
    }
}
@Composable
fun Login(auth: FirebaseAuth, db: FirebaseFirestore, onLoginSuccess: () -> Unit) {
    var email_user by remember { mutableStateOf("") }
    var senha_user by remember { mutableStateOf("") }

    Column(
        Modifier
            .fillMaxWidth()
            .background(background)
    ) {
        Row(
            Modifier
                .background(color = dark_black)
                .padding(0.dp, 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text1(
                text = "REGISTRO DE TAREFAS",
                fontSize = 25.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Column(
            Modifier
                .fillMaxWidth()
                .padding(30.dp)
        ) {
            Text1(
                text = "Email:",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextField(
                value = email_user,
                onValueChange = { email_user = it },
                placeholder = { Text1(text = "Email") },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = light_black,
                    unfocusedContainerColor = light_black,
                    focusedIndicatorColor = Color.Transparent,
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text1(
                text = "Senha:",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
            TextField(
                value = senha_user,
                onValueChange = { senha_user = it },
                placeholder = { Text1(text = "Senha") },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = light_black,
                    unfocusedContainerColor = light_black,
                    focusedIndicatorColor = Color.Transparent,
                ),
                visualTransformation = PasswordVisualTransformation()  // Adiciona a transformação visual

            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    auth.signInWithEmailAndPassword(email_user, senha_user)
                        .addOnSuccessListener {
                            // Chamando a função de sucesso quando o login for bem-sucedido
                            onLoginSuccess()
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Login falhou", e)
                        }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(7.dp)
            ) {
                Text1(
                    text = "Logar",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

@Composable
fun MainScreen() {
    var currentScreen by remember { mutableStateOf("Login") }

    when (currentScreen) {
        "Login" -> Login(
            auth = Firebase.auth,
            db = Firebase.firestore,
            onLoginSuccess = { currentScreen = "App" } // Mudando para a tela "App" após sucesso no login
        )
        "App" -> App(db = Firebase.firestore)
    }
}


@Composable
fun App(db: FirebaseFirestore) {
    var nome_tarefa by remember { mutableStateOf("") }
    var dataInicio_tarefa by remember { mutableStateOf("") }
    var dataFinal_tarefa by remember { mutableStateOf("") }
    var tarefaId by remember { mutableStateOf<String?>(null) }
    val tarefas = remember { mutableStateListOf<Pair<String, HashMap<String, String>>>() }

    // Lê as tarefas do Firestore ao iniciar o Composable
    LaunchedEffect(Unit) {
        db.collection("tarefas")
            .get()
            .addOnSuccessListener { result ->
                tarefas.clear() // Limpa a lista antes de adicionar os itens
                for (document in result) {
                    tarefas.add(Pair(document.id, document.data as HashMap<String, String>))
                }
                Log.d(TAG, "Tarefas carregadas com sucesso!")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Erro ao carregar tarefas", e)
            }
    }

    Column(
        Modifier
            .fillMaxWidth()
            .background(background)
    ) {
        // Cabeçalho
        Row(
            Modifier
                .background(color = dark_black)
                .padding(0.dp, 40.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text1(
                text = "REGISTRO DE TAREFAS",
                fontSize = 25.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Column(Modifier.fillMaxWidth().padding(30.dp)) {
            // Inputs de Nome da Tarefa
            Text1("Nome da Tarefa:", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            TextField(
                value = nome_tarefa,
                onValueChange = { nome_tarefa = it },
                placeholder = { Text1("Nome da Tarefa") },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = light_black,
                    unfocusedContainerColor = light_black,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Inputs de Data de Início
            Text1("Data de Início:", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            TextField(
                value = dataInicio_tarefa,
                onValueChange = { dataInicio_tarefa = it },
                placeholder = { Text1("Data de Início") },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = light_black,
                    unfocusedContainerColor = light_black,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Inputs de Data Final
            Text1("Data Final:", fontSize = 20.sp, color = Color.White, fontWeight = FontWeight.Bold)
            TextField(
                value = dataFinal_tarefa,
                onValueChange = { dataFinal_tarefa = it },
                placeholder = { Text1("Data Final") },
                textStyle = TextStyle(color = Color.White, fontSize = 20.sp),
                modifier = Modifier.fillMaxWidth(),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = light_black,
                    unfocusedContainerColor = light_black,
                    focusedIndicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botão Salvar
            Button(
                onClick = {
                    if (tarefaId == null) {
                        // Adiciona nova tarefa
                        val novaTarefa = hashMapOf(
                            "nome_tarefa" to nome_tarefa,
                            "dataInicio_tarefa" to dataInicio_tarefa,
                            "dataFinal_tarefa" to dataFinal_tarefa
                        )
                        db.collection("tarefas")
                            .add(novaTarefa)
                            .addOnSuccessListener { documentReference ->
                                tarefas.add(Pair(documentReference.id, novaTarefa))
                                Log.d(TAG, "Tarefa adicionada com sucesso!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao adicionar tarefa", e)
                            }
                    } else {
                        // Atualiza tarefa existente
                        val tarefaAtualizada = hashMapOf(
                            "nome_tarefa" to nome_tarefa,
                            "dataInicio_tarefa" to dataInicio_tarefa,
                            "dataFinal_tarefa" to dataFinal_tarefa
                        )
                        db.collection("tarefas")
                            .document(tarefaId!!)
                            .set(tarefaAtualizada)
                            .addOnSuccessListener {
                                val index = tarefas.indexOfFirst { it.first == tarefaId }
                                if (index != -1) {
                                    tarefas[index] = Pair(tarefaId!!, tarefaAtualizada)
                                }
                                tarefaId = null // Reseta o ID para evitar conflitos
                                Log.d(TAG, "Tarefa atualizada com sucesso!")
                            }
                            .addOnFailureListener { e ->
                                Log.w(TAG, "Erro ao atualizar tarefa", e)
                            }
                    }

                    // Limpa os campos após salvar
                    nome_tarefa = ""
                    dataInicio_tarefa = ""
                    dataFinal_tarefa = ""
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = green),
                shape = RoundedCornerShape(7.dp)
            ) {
                Text1(
                    text = if (tarefaId == null) "Salvar" else "Atualizar",
                    fontSize = 20.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lista de Tarefas
        LazyColumn(modifier = Modifier.fillMaxWidth().padding(40.dp, 0.dp)) {
            items(tarefas) { (id, tarefa) ->
                Column(
                    Modifier.border(2.dp, Color.White, RoundedCornerShape(10.dp)),) {
                    Column(modifier = Modifier.padding(10.dp))
                    {
                        Text1("Nome: ${tarefa["nome_tarefa"]}", fontSize = 20.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.padding(10.dp))
                    {
                        Text1("Início: ${tarefa["dataInicio_tarefa"]}", fontSize = 20.sp, color = Color.White)
                    }
                    Column(modifier = Modifier.padding(10.dp))
                    {
                        Text1("Final: ${tarefa["dataFinal_tarefa"]}", fontSize = 20.sp, color = Color.White)
                    }

                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                        Button(
                            onClick = {
                                // Preenche os inputs com os dados da tarefa selecionada
                                tarefaId = id
                                nome_tarefa = tarefa["nome_tarefa"] ?: ""
                                dataInicio_tarefa = tarefa["dataInicio_tarefa"] ?: ""
                                dataFinal_tarefa = tarefa["dataFinal_tarefa"] ?: ""
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text1("Editar", fontSize = 20.sp, color = Color.White)
                        }

                        Button(
                            onClick = {
                                // Deleta a tarefa
                                db.collection("tarefas")
                                    .document(id)
                                    .delete()
                                    .addOnSuccessListener {
                                        tarefas.removeIf { it.first == id }
                                        Log.d(TAG, "Tarefa deletada com sucesso!")
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w(TAG, "Erro ao deletar tarefa", e)
                                    }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                            shape = RoundedCornerShape(7.dp)
                        ) {
                            Text1("Deletar", fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}
