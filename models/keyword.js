// keyword.js
// Keyword model logic.

var neo4j = require('neo4j');
var db = new neo4j.GraphDatabase('http://neo4j:1234@localhost:7474');

// private constructor:

var Keyword = module.exports = function Keyword(_node) {
	this._node = _node;
}

Object.defineProperty(Keyword.prototype, 'id', {
	get: function () { return this._node.id; }
});

Object.defineProperty(Keyword.prototype, 'keyword', {
	get: function () { return this._node.data['id']; }
});

Object.defineProperty(Keyword.prototype, 'value', {
	get: function () { return this._node.data['value']; }
});

Object.defineProperty(Keyword.prototype, 'rank', {
	get: function () { return this._node.data['rank']; }
});


Keyword.prototype.follow = function (other, callback) {
    this._node.createRelationshipTo(other._node, 'follows', {}, function (err, rel) {
        callback(err);
    });
};

Keyword.prototype.unfollow = function (other, callback) {
    var query = [
        'MATCH (keyword:Keyword) -[rel:follows]-> (other:Keyword)',
        'WHERE ID(keyword) = {kewordId} AND ID(other) = {otherId}',
        'DELETE rel',
    ].join('\n')

    var params = {
        kewordId: this.id,
        otherId: other.id,
    };

    db.query(query, params, function (err) {
        callback(err);
    });
};

Keyword.prototype.getFollowingAndOthers = function (callback) {
    // query all users and whether we follow each one or not:
    var query = [
        'MATCH (keyword:Keyword), (other:Keyword)',
        'OPTIONAL MATCH (keyword) -[rel:RELATED]-> (other)',
        'WHERE ID(keyword) = {kewordId}',
        'RETURN other, COUNT(rel)', // COUNT(rel) is a hack for 1 or 0
    ].join('\n')

    var params = {
        kewordId: this.id,
    };

    var keyword = this;
    db.query(query, params, function (err, results) {    	
        if (err) return callback(err);

        var following = [];
        var others = [];

        for (var i = 0; i < results.length; i++) {
            var other = new Keyword(results[i]['other']);
            var follows = results[i]['COUNT(rel)'];

            if (keyword.id === other.id) {
                continue;
            } else if (follows) {
                following.push(other);
            } else {
                others.push(other);
            }
        }

        callback(null, following, others);
    });
};

// static methods:

Keyword.get = function (id, callback) {
    db.getNodeById(id, function (err, node) {    	
        if (err) return callback(err);
        callback(null, new Keyword(node));
    });
};

Keyword.getAll = function (time, callback) {
    var query = [
        'MATCH (keyword:Keyword {time:'+time+'})',
        'RETURN keyword',
    ].join('\n');

    db.query(query, null, function (err, results) {    	
        if (err) return callback(err);
        var keywords = results.map(function (result) {
            return new Keyword(result['keyword']);
        });
        callback(null, keywords);
    });
};

Keyword.getPairs = function (callback) {
    var query = [
        'MATCH (n:Keyword)-[r]->(m:Keyword)',
        'RETURN n, m',
    ].join('\n');

    db.query(query, null, function (err, results) {    	
    	console.log(results);
        if (err) return callback(err);
        var keywords = results.map(function (result) {
        	var n = new Keyword(result['n']);
        	var m = new Keyword(result['m'])
            return [n.keyword, m.keyword];
        });
        callback(null, keywords);
    });
};

// creates the keyword and persists (saves) it to the db, incl. indexing it:
Keyword.create = function (data, callback) {
    // construct a new instance of our class with the data, so it can
    // validate and extend it, etc., if we choose to do that in the future:
    var node = db.createNode(data);
    var keyword = new Keyword(node);

    // but we do the actual persisting with a Cypher query, so we can also
    // apply a label at the same time. (the save() method doesn't support
    // that, since it uses Neo4j's REST API, which doesn't support that.)
    var query = [
        'CREATE (keyword:Keyword {data})',
        'RETURN keyword',
    ].join('\n');

    var params = {
        data: data
    };

    db.query(query, params, function (err, results) {
        if (err) return callback(err);
        var keyword = new Keyword(results[0]['keyword']);
        callback(null, keyword);
    });
};