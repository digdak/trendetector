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
    console.log("in getByKeyword keyword = " + keyword + " term = " + term + " batch_time = "+ batch_time);
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
            console.log("in getByKeyword err : " + err);
            return callback(err);
        }           
        var keyword = new Keyword(results[0]['keyword']);
        console.log("in getByKeyword get : " + keyword);
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
    console.log(query);

    graph_db.query(query, null, function (err, results) {
        // console.log(results);
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
    var node = graph_db.createNode(data);
    var keyword = new Keyword(node._data.data);        
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
            console.log("error in create node db query : " + err);            
            console.log(params);            
            return callback(err);
        }
        var keyword = new Keyword(results[0]['keyword']);
        callback(null, keyword);
    });
};

Keyword.create_nodes = function (next) {    
    return function(db, term, batch_time, keyword_list) {                
        // console.log("term = " + term + " batch_time = " + batch_time +  " keywords = " + keyword_list);
        var keywords_with_article = {};     // {'keyword': [article_id,], }
        var keyword_list_length = keyword_list.length;
        console.log("length = " + keyword_list_length);     
        keyword_list.forEach(function (item, i) {                            
            model_keyword.get_article_ids_by_keyword(function(arr) {                                
                
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
                        console.log(err);
                        return;
                    }

                    keywords_with_article[item.id] = arr;
                    // console.log("i = "+ i +"  length = " + Object.keys(keywords_with_article).length + " arr = "  + arr);  
                    if (keyword_list_length == Object.keys(keywords_with_article).length) {                                                
                        console.log("gogo create graph");
                        next(term, keyword_list, keywords_with_article);
                    }
                });  
     
            })(db, item._id, term, batch_time);  // HOUR FROM NOW            
        });
        
    }
}

Keyword.create_graph = function (next) {    
    return function(term, keyword_list, keywords_with_article) {           
        keyword_list.forEach(function(keyword_out, i) {                                                                
            var article_size_with_out = keywords_with_article[keyword_out.id].length;
            console.log("out iterator  = " + i + " keyword = " + keyword_out.id + " size = " + article_size_with_out); 
            
            if (article_size_with_out == 0) {
                console.log("out 0 pass");
                return;
            }
            var out_article_ids = keywords_with_article[keyword_out.id].map(function(obj) {return obj._id + ""});

            keyword_list.forEach(function (keyword_in, j) {                
                if (j==i) {
                    console.log("same pass");        
                    return;
                } else {
                    // check intersection with articles
                    // if condition is satisfied, make edge                                        
                    var article_size_with_in = keywords_with_article[keyword_in.id].length;
                    console.log("out iterator  = " + i + " keyword = " + keyword_out.id +" inner iterator  = " + j + " keyword = " + keyword_in.id  + " size = " + article_size_with_in); 

                    if (article_size_with_in == 0) {
                        console.log("inner 0 pass");
                        return;
                    }

                    // if (i == 2 && j == 3) {    
                    //     console.log(keywords_with_article[keyword_out.id].map(function(obj) {return obj._id}));
                    //     console.log(keywords_with_article[keyword_in.id].map(function(obj) {return obj._id}));
                    // }

                    var intersection = out_article_ids.filter(function(n) {
                        return (keywords_with_article[keyword_in.id].map(function(obj) {return obj._id + ""}).indexOf(n) != -1)
                    });

                    console.log("intersection_size = " + intersection.length);

                    var intersection_size = intersection.length;
                    var a = (intersection_size/article_size_with_out).toPrecision(8);
                    var b = (intersection_size/article_size_with_in).toPrecision(8);
                    console.log("calculate a b : " + a + "  " + b);

                    var cohesion_value = (intersection_size/Math.min(article_size_with_out, article_size_with_in)).toPrecision(8);

                    if (cohesion_value > 0.3) {
                    // if (intersection_size > 0) {
                        Keyword.getByKeyword(keyword_out.id, term, keyword_out.batch_time, function (m){
                            Keyword.getByKeyword(keyword_in.id, term, keyword_in.batch_time, function (n) {
                                console.log("make follow = " + keyword_out.id + "  " + keyword_in.id);
                                m.follow(n, function(err) {                                    
                                    // flagFollowEnd = true;
                                    if (err) {
                                        console.log("error in follow callback func : " + err);
                                        return next(err);
                                    } 
                                    console.log("create keyword edge : " + m.id + "  " + n.id);        

                                });
                            
                            });
                            
                        });
                    }

                    if (i == keyword_list.length - 1 && j == keyword_list.length - 1) {
                        console.log("set time out for end at i = " + i + " j = " + j);
                        return next(term);
                        setTimeout(function() {
                            console.log("gogo end create graph!!!");
                            return next(term);
                        }, 3000);
                    }
                    
                }
            
            });
        });
        
    }
}