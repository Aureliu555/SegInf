import axios from 'axios';
import FormData from 'form-data';
import jwt from 'jsonwebtoken';
import {newEnforcer} from 'casbin';


const CLIENT_ID = '725975013772-2gbsl365ntj46gode0hq2jsq1c9vvfbk.apps.googleusercontent.com'
const CLIENT_SECRET = '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'
const CALLBACK = 'home'
const e = await newEnforcer("./rbac/basic_model.conf", "./rbac/basic_policy.csv");


export default function webFunctions(){
    return {
        login: login,
        loginForm: loginForm,
        getTasks: getTasks,
        home: home,
        insertTaskForm: insertTaskForm,
        addTasks: addTasks,
        page: page,
        notAllowed: notAllowed
    }

    async function checkRole(user){  
        let roles = await e.getRolesForUser(user)
        return roles[0]
        
}

    function getTasks(req, rsp){
        var str = "";
        axios.get(`https://tasks.googleapis.com/tasks/v1/users/@me/lists`, {headers: { Authorization: 'Bearer ' + req.cookies.ACCESS_TOKEN }})
        .then(function (resp){
            let taskListId = resp.data.items[0].id
                rsp.cookie("TASKLIST_ID", taskListId)
                axios.get(`https://tasks.googleapis.com/tasks/v1/lists/${taskListId}/tasks`, {headers: { Authorization: 'Bearer ' + req.cookies.ACCESS_TOKEN }})
                .then(function (resp2){
                        resp2.data.items.forEach((item, idx) => {
                            str += `<div>${idx+1}. ${item.title} </div>` 
                        });
                        rsp.send(str + '<div> <a href="/insertTask"> Add new </a> </div>'+ '<div> <a href="/page"> Go back </a> </div>')
                })
                .catch(function (err){
                    console.log(err)
                })
        })
    }

    function insertTaskForm(req, rsp) {
        rsp.render('insertTask', {})
    }

    function page(req, rsp) {
        rsp.render('page', {})
    }

    function notAllowed(req, rsp) {
        rsp.render('notAllowed', {})
    }

    async function addTasks(req, rsp){
        let notAllowed = await e.hasRoleForUser(req.cookies.USER_EMAIL, "free")
        if (notAllowed) 
            rsp.status(302).redirect('/notAllowed')
        
        axios.post(`https://tasks.googleapis.com/tasks/v1/lists/${req.cookies.TASKLIST_ID}/tasks`, {"title": req.body.tname} ,{headers: { Authorization: 'Bearer ' + req.cookies.ACCESS_TOKEN }})
        .then(function(resp){
            rsp.status(302).redirect('/tasks')
        })
        .catch((err) => {rsp.send(err)})
        

        
    } 

    function login (req, rsp) { 
        rsp.send('<a href=/login>Use Google Account</a>')
    }

    function loginForm(req, rsp){
        rsp.redirect(302,
            'https://accounts.google.com/o/oauth2/v2/auth?'
            + 'client_id='+ CLIENT_ID +'&'
            + 'scope=openid%20email%20https://www.googleapis.com/auth/tasks&'
            //+ 'state=value-based-on-user-session&'
            + 'response_type=code&'
            + 'redirect_uri=http://localhost:3001/'+CALLBACK)
    }
    
    function home(req, rsp) {
        const form = new FormData();
        form.append('code', req.query.code);
        form.append('client_id', CLIENT_ID);
        form.append('client_secret', CLIENT_SECRET);
        form.append('redirect_uri', 'http://localhost:3001/'+CALLBACK);
        form.append('grant_type', 'authorization_code');
        axios.post('https://www.googleapis.com/oauth2/v3/token', 
            form, { headers: form.getHeaders() }, )
          .then(async function (response) {
            const jwt_payload = jwt.decode(response.data.id_token)
    
            rsp.cookie("ACCESS_TOKEN", response.data.access_token)
            rsp.cookie("USER_EMAIL", jwt_payload.email)

            const role = await checkRole(jwt_payload.email)

            rsp.send(
                '<div> Hi <b>' + jwt_payload.email + '</b> </div><br>' +
                '<div> <a>User Status: '+ role +' </a> </div>'+
                '<div> <a href="/page">Home</a> </div>'+
                '<div> <a href="/">Logout</a> </div>'
            );
          })
          .catch(
            function (error) {
                rsp.send(error)
          });

        }      
}




