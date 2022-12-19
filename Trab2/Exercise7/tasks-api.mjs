import express from 'express';
import cookieParser from 'cookie-parser';
import webFunctions from './tasks-web.mjs';



//----------------------------- CONSTANTS DEFINITIONS -----------------------------------
const web = webFunctions();
const port = 3001
const CALLBACK = 'home'
const app = express()


app.use(cookieParser());
app.get('/', web.login)
app.get('/login', web.loginForm)
app.get('/'+CALLBACK, web.home)
app.get('/tasks', web.getTasks)
//app.post('/tasks', web.addTasks) 

app.listen(port, (err) => {
    if (err) return console.log('something bad happened')
    console.log(`Server is listening on ${port}: http://localhost:3001/`)
})