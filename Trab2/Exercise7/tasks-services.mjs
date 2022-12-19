import newEnforcer from 'casbin';

const e = await newEnforcer("./rbac/basic_model.conf", "./rbac/basic_policy.csv");
function hasPermition(role, asset, action){
    return e.enforce(role, asset, action)
}