// keyword.js
// Keyword model logic.

var neo4j = require('neo4j');
var graph_db = new neo4j.GraphDatabase('http://neo4j:1234@localhost:7474');

var model_keyword = require('../model/keyword.js');
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


Keyword.prototype.follow = function (other, data, callback) {
    this._node.createRelationshipTo(other._node, 'follows', data, function (err, rel) {
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

    graph_db.query(query, params, function (err) {
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

    graph_db.query(query, params, function (err, results) {
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
    graph_db.getNodeById(id, function (err, node) {
        if (err) return callback(err);
        callback(null, new Keyword(node));
    });
};

Keyword.getByKeyword = function (keyword, term, batch_time, callback) {
    // console.log("in getByKeyword keyword = " + keyword + " term = " + term + " batch_time = "+ batch_time);
    var query = [
        'MATCH (keyword:Keyword {id:{keyword}, term:{term}, batch_time:{batch_time}})',
        'RETURN keyword',
    ].join('\n');

    var params = {
        'keyword': keyword,
        'term': term,
        'batch_time': batch_time
    };

    graph_db.query(query, params, function (err, results) {     
        if (err) {
            // console.log("in getByKeyword err : " + err);
            return callback(err);
        }           
        var keyword = new Keyword(results[0]['keyword']);
        callback(keyword);
    });
};

Keyword.getAll = function (term, batch_time, callback) {
    var query = [
        'MATCH (keyword:Keyword {term:"'+term+'", batch_time:'+batch_time+'})',
        'RETURN keyword',
    ].join('\n');

    graph_db.query(query, null, function (err, results) {
        if (err) return callback(err);
        var keywords = results.map(function (result) {
            return new Keyword(result['keyword']);
        });
        callback(null, keywords);
    });
};

Keyword.getPairs = function (term, batch_time, callback) {
    var query = [
        'MATCH (n:Keyword {term:"'+term+'", batch_time:'+batch_time+'})-[r]->(m:Keyword {term:"'+term+'", batch_time:'+batch_time+'})',
        'RETURN n, m',
    ].join('\n');

    var params = {
        'term': term,
        'batch_time': batch_time
    };

    graph_db.query(query, null, function (err, results) {        
        if (err) return callback(err);
        var keywords = results.map(function (result) {
            var n = new Keyword(result['n']);
            var m = new Keyword(result['m']);
            return [n.keyword, m.keyword];
        });
        callback(null, keywords);
    });
};

// creates the keyword and persists (saves) it to the db, incl. indexing it:
Keyword.create = function (data, callback) {
    // construct a new instance of our class with the data, so it can
    // validate and extend it, etc., if we choose to do that in the future:    
    // var node = graph_db.createNode(data);
    // var keyword = new Keyword(node._data.data);        
    // but we do the actual persisting with a Cypher query, so we can also
    // apply a label at the same time. (the save() method doesn't support
    // that, since it uses Neo4j's REST API, which doesn't support that.)
    var query = [        
        'CREATE (keyword:Keyword {data})',
        'RETURN keyword',
    ].join('\n');

    var params = {
        'data': data
    };

    graph_db.query(query, params, function (err, results) {
        if (err) {
            return callback(err);
        }
        var keyword = new Keyword(results[0]['keyword']);
        callback(null, keyword);
    });
};

Keyword.create_nodes = function (next) {
    return function(db, term, batch_time, keyword_list) {                
        // console.log("term = " + term + " batch_time = " + batch_time +  " keywords = " + keyword_list);
        // var keywords_with_article = {};     // {'keyword': [article_id,], }
        var keyword_list_length = keyword_list.length;

        keyword_list.forEach(function (item, i) {                            
            // model_keyword.get_article_ids_by_keyword(function(arr) {                                
                
                // make node                                                                
                item.id = item._id;
                delete item._id;
                item.ntf = item.value.ntf;
                item.cnt = item.value.cnt;
                item.ntfidf = item.value.ntfidf;
                item.ntfidf1 = item.value.ntfidf1;
                item.ntfidf2 = item.value.ntfidf2;
                delete item.value;
                item.term = term;
                item.batch_time = batch_time.getTime();
         
                Keyword.create(item, function(err, keyword_result) {
                    if (err) {
                        // console.log(err);
                        return;
                    }

                    // keywords_with_article[item.id] = arr;

                    if (keyword_list_length - 1 == i) {
                        next(db, term, keyword_list, batch_time);
                    }
                });  
     
            // })(db, item._id, term, batch_time);  // HOUR FROM NOW            
        });
        
    }
}

Keyword.create_graph = function (next) {    
    return function(db, term, keyword_list, batch_time) {           
        // console.log(keyword_list);
        keyword_list.forEach(function (keyword_out, i) {                                                                
            keyword_list.forEach(function (keyword_in, j) {
                if (j<=i) {
                    // console.log("same or less pass");
                    return;
                } else {
                    // console.log(i + " " + j);
                    model_keyword.get_intersection_count_by_keywords(function(result) {                        
                        // console.log(i + " : " + keyword_out.id + ",  " + result.keyword1_cnt + " out " + j + " : " + keyword_in.id + ",  " + result.keyword2_cnt);
                        if (result.intersection_cnt == 0 || keyword_out.cnt == 0 || keyword_in.cnt == 0)
                            return;

                        var cohesion_value = (result.intersection_cnt/Math.min(keyword_out.cnt, keyword_in.cnt)).toPrecision(8);
                        // console.log("intersection_cnt : " + result.intersection_cnt + "   cohesion_value : " + cohesion_value);

                        if (cohesion_value > 0.3) {
                            Keyword.getByKeyword(keyword_out.id, term, keyword_out.batch_time, function (m){
                                Keyword.getByKeyword(keyword_in.id, term, keyword_in.batch_time, function (n) {
                                    console.log("make follow = " + keyword_out.id + "  " + keyword_in.id);
                                    m.follow(n, {'cohesion_value': cohesion_value}, function(err) {
                                        if (err) {
                                            // console.log("error in follow callback func : " + err);
                                            return next(err);
                                        }
                                    });
                                
                                });
                                
                            });
                        }
                    })(db, keyword_out.id, keyword_in.id, term, batch_time);
                    
                }
            });
        });
        return next(term);
    }
}