// Requiring fs module
import { readFileSync, writeFile } from 'fs'; 

export default function taskData() {
    var usersFile = readFileSync('./us/users.json');
    var usersObj = JSON.parse(usersFile);

    return {
        addUser: addUser,
        resolveUserRole: resolveUserRole,
        hasPermission: hasPermission
    }
    
    // Storing the JSON format data in myObject



    function addUser(user){  
        // Defining new data to be added
        const newUser = {"id": user.id, "roles": ["free"]};
    
        // Adding the new data to our object
        usersObj.push(newUser);
        
        // Writing to our JSON file
        var usersJSON = JSON.stringify(usersObj);
        writeFile('./us/users.json', usersJSON, (err) => {
        // Error checking
        if (err) throw err;
        console.log("New data added");
        });
        
        return newUser
    }

    function resolveUserRole(user){
        let userWithRole = usersObj.find((u) => u.id === user.id);
        if (!userWithRole){
            userWithRole = addUser(user);
        }
        return userWithRole.roles;
    }

    function hasPermission(action) {
        return (req, res, next) => {
          const { user } = req.body;
          const { asset } = req.params;
          const userRoles = resolveUserRole(user);
      
          const permissions = userRoles.reduce((perms, role) => {
            perms =
              userRoles[role] && userRoles[role][action]
                ? perms.concat(userRoles[role][action])
                : perms.concat([]);
            return perms;
          }, []);
      
          const allowed = permissions.includes(asset);
      
          allowed ? next() : res.status(302).redirect('/getPremium');
        };
      }

}

const data = taskData()
console.log(data.resolveUserRole({"id": "abc@gmail.com"}))