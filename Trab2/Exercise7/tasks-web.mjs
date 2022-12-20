import cookieParser from 'cookie-parser';
import axios from 'axios';
import FormData from 'form-data';
import jwt from 'jsonwebtoken';
import {newEnforcer} from 'casbin';

//-----------------------------------------------------------------------------------------------------------------
const CLIENT_ID = '725975013772-2gbsl365ntj46gode0hq2jsq1c9vvfbk.apps.googleusercontent.com'
const CLIENT_SECRET = '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'
const API_KEY = 'AIzaSyB8Oz7v_H-Ak8-TfDhUBhyzvYUL0OAfo1U'
const CALLBACK = 'home'
const e = await newEnforcer("./rbac/basic_model.conf", "./rbac/basic_policy.csv");
const TASKS = {
    "kind": "Trivial",
    "items": [
        {"kind": "Urgent", "title": "Wash the dishes", "status": "undone"},
        {"kind": "Not important", "title": "Study more", "status": "undone"}
    ]
  }

//-----------------------------------------------------------------------------------------------------------------
export default function webFunctions(){
    return {
        login: login,
        loginForm: loginForm,
        getTasks: getTasks,
        home: home,

    }

    function hasPermission(role, asset, action){
        return e.enforce(role, asset, action)
}

    async function checkRole(user){  
        let roles = await e.getRolesForUser(user)
        return roles[0]
        
}

    function getTasks(req, rsp){
        axios.get(`https://tasks.googleapis.com/tasks/v1/users/@me/lists?access_token=${req.cookies.ACCESS_TOKEN}&key=${API_KEY}`,
         {headers: { Authorization: 'Bearer ' + req.cookies.ACCESS_TOKEN, Accept: 'application/json' }})
        .then(function (resp){
            resp.render('/tasks')
        })
        .catch(function (err){
            rsp.send(err)
        })
    }

/*     async function addTasks(tasks, newTask){     
        tasks.items.push(newTask)
        return Promise.resolve(newTask)
    } 
 */
    function login (req, rsp) { 
        rsp.send('<a href=/login>Use Google Account</a>')
    }

    function loginForm(req, rsp){
        rsp.redirect(302,
            'https://accounts.google.com/o/oauth2/v2/auth?'
            + 'client_id='+ CLIENT_ID +'&'
            + 'scope=openid%20email&'
            + 'state=value-based-on-user-session&'
            + 'response_type=code&'
            + 'redirect_uri=http://localhost:3001/'+CALLBACK)
    }
    function home(req, rsp) {
        //
        // TODO: check if 'state' is correct for this session
        //
        //console.log('making request to token endpoint')
    
        const form = new FormData();
        form.append('code', req.query.code);
        form.append('client_id', CLIENT_ID);
        form.append('client_secret', CLIENT_SECRET);
        form.append('redirect_uri', 'http://localhost:3001/'+CALLBACK);
        form.append('grant_type', 'authorization_code');
        axios.post('https://www.googleapis.com/oauth2/v3/token', 
            // body parameters in form url encoded
            form,
            { headers: form.getHeaders() }, 
          )
          .then(async function (response) {
            var jwt_payload = jwt.decode(response.data.id_token)
            //console.log(jwt_payload)
            //const email = jwt_payload.email
            //console.log('AVAILABLE EMAIL:', email)
    
            // a simple cookie example
            //console.log("ACCESS TOKEN ------->", response.data.access_token)
            //console.log("BEARER TOKEN ------->", response.data)
            rsp.cookie("ACCESS_TOKEN", response.data.access_token)
            rsp.cookie("BEARER_TOKEN", response.data.id_token)
            rsp.cookie("USER_EMAIL", jwt_payload.email)

            let role = await checkRole(jwt_payload.email)
            // HTML response with the code and access token received from the authorization server
            rsp.send(
                '<div> Hi <b>' + jwt_payload.email + '</b> </div><br>' +
                '<div> <a>User Status: '+ role +' </a> </div>'+
                '<div> <a href="/tasks">See tasks</a> </div>'+
                'Go back to <a href="/">Home screen</a>'
            );
          })
          .catch(
            function (error) {
            //console.log(error)
                rsp.send(error)
          });
    }

}


