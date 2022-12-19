import tasksData from './tasks-data.mjs';

const data = tasksData()

const user = await data.resolveUserRole({"id": "ghi@gmail.com"});
