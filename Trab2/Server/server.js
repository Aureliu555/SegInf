// Built-in HTTPS support
const https = require("https");
// Handling GET request (npm install express)
const express = require("express");
// Load of files from the local file system
const fs = require('fs'); 

const PORT = 4433;
const app = express();

// Get request for resource /
app.get('/', function (req, res) {
    console.log(
        req.socket.remoteAddress
        //+ ' ' + req.socket.getPeerCertificate().subject.CN
        + ' ' + req.method
        + ' ' + req.url);
    res.send("<html><body>Secure Hello World with node.js</body></html>");
})

// configure TLS handshake
const options = {
    key: fs.readFileSync('./keyStore.pem'),
    cert: fs.readFileSync('./certificate.pem'),
    //ca: fs.readFileSync('<server trustbase PEM (root CA)>'), 
    requestCert: false, 
    rejectUnauthorized: false
};

// Create HTTPS server
https.createServer(options, app).listen(PORT, 
    function (req, res) {
        console.log("Server started at port " + PORT);
    }
);
