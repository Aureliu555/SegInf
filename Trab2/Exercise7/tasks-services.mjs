import {newEnforcer} from 'casbin';

const USER = {email: String, role: String}

const e = await newEnforcer("./rbac/basic_model.conf", "./rbac/basic_policy.csv");
function hasPermission(role, asset, action){
    return e.enforce(role, asset, action)
}


console.log(await hasPermission("aiurcu5@gmail.com", "tasks", "read"))
console.log(e)
