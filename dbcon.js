var db = {};
var mongodb = require('mongodb').MongoClient;
var mysql = require('mysql');


db.mysql = mysql.createConnection({
    host: '127.0.0.1',
    port: 3306,
    user: 'root',
    password: 'root12!#',
    database: 'trendetector'
});

db.mysql.connect(function (err) {
    if (err) {
        console.error('mysql connection error.');
        console.error(err);
        throw err;
    }
});

mongodb.connect('mongodb://localhost:27017/trendetector', function (err, conn) {
    if (err) {
        console.error('mongodb connection error.');
        console.error(err);
        throw err;
    }

    db.mongodb = conn;
});

module.exports = db;

exports.errors = {
    INVALID_DATA: 0,
    DBMS_ERROR: 1
};