import express from 'express';
import cookieParser from 'cookie-parser';
import webFunctions from './tasks-web.mjs';


const web = webFunctions();
const port = 3001
const CALLBACK = 'home'
const app = express()

app.set('views', 'views')
app.set('view engine', 'hbs')
app.use(express.json())
app.use(express.urlencoded({extended : false}))
app.use(express.static('public'))
app.use(cookieParser());
app.get('/', web.login)
app.get('/login', web.loginForm)
app.get('/page', web.page)
app.get('/'+CALLBACK, web.home)
app.get('/tasks', web.getTasks)
app.get('/insertTask', web.insertTaskForm)
app.post('/insertTask', web.addTasks) 
app.get('/notAllowed', web.notAllowed)

app.listen(port, (err) => {
    if (err) return console.log('something bad happened')
    console.log(`Server is listening on ${port}: http://localhost:3001/`)
})