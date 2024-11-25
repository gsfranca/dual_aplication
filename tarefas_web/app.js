const express = require("express")
const app = express()
const handlebars = require("express-handlebars").engine
const bodyParser = require("body-parser")
const Handlebars = require('handlebars');

var admin = require("firebase-admin");
const { initializeApp, applicationDefault, cert } = require('firebase-admin/app')
const { getFirestore, Timestamp, FieldValue } = require('firebase-admin/firestore')
const serviceAccount = require('./google-services.json')

admin.initializeApp({
    credential: admin.credential.cert(serviceAccount)
  });

const db = getFirestore()
const tarefasRef = db.collection('tarefas');

app.engine("handlebars", handlebars({defaultLayout: "main"}))
app.set("view engine", "handlebars")
app.use(bodyParser.urlencoded({extended: false}))
app.use(bodyParser.json())

Handlebars.registerHelper('eq', function(a, b) 
{
    return a === b;
});
  
app.get("/", function(req, res)
{
    res.render("create")
})
app.get("/read", async function(req, res, )
{
    const snapshot = await tarefasRef.get()
    const data = [];
    snapshot.forEach(doc => 
    {
        data.push({
            id: doc.id,
            nome_tarefa: doc.get('nome_tarefa'),
            dataInicio_tarefa: doc.get('dataInicio_tarefa'),
            dataFinal_tarefa: doc.get('dataFinal_tarefa')
        })
    })
    res.render("read", { data })
})


app.get("/delete/:id", async function(req, res)
{
    tarefasRef
    .doc(req.params.id)
    .delete()
    .then(function () 
    {
        res.redirect('/read');
    });
})
app.post("/create", function(req, res)
{
    var result = tarefasRef.add(
    {
        nome_tarefa: req.body.nome_tarefa,
        dataInicio_tarefa: req.body.dataInicio_tarefa,
        dataFinal_tarefa: req.body.dataFinal_tarefa,
    })
    .then(function()
    {
        console.log('Added document');
        res.redirect('/read')
    })
})
app.get("/edit/:id", async function(req, res)
{
    const dataSnapshot = await tarefasRef.doc(req.params.id).get();
    const data = 
    {
        id: dataSnapshot.id,
        nome_tarefa: dataSnapshot.get('nome_tarefa'),
        dataInicio_tarefa: dataSnapshot.get('dataInicio_tarefa'),
        dataFinal_tarefa: dataSnapshot.get('dataFinal_tarefa')
    };
    res.render("edit", { data });
})
app.post("/update", function(req, res){
    var result = db
      .collection("tarefas")
      .doc(req.body.id)
      .update({
        nome_tarefa: req.body.nome_tarefa,
        dataInicio_tarefa: req.body.dataInicio_tarefa,
        dataFinal_tarefa: req.body.dataFinal_tarefa
      })
      .then(function () {
        res.redirect("/read");
      });
})
app.listen(8081, function(){
    console.log("Servidor ativo!")
})