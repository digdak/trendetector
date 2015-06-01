/**
 * Created by 은화 on 2015-05-17.
 */


exports.get_keywordlist_by_community = function (next) {
    return function (db, community) {
        var where = {};
        if (community !== undefined) {
            where.community = community;
        }
        db.collection('keyword').find(where).sort({ score: -1 }).toArray(
            function (err, keyword_list) {
                if (err) {
                    throw err;
                }
                next(keyword_list);
            }
        );
    }
}

exports.get_keyword_info = function (next) {
    return function (db, keyword) {
        if(keyword === undefined) {
            return next(undefined);
        }

        db.collection('keyword').findOne({ _id: keyword }, {}, function (err, data) {
            if (err) {
                throw err;
            }
            next(data);
        });
    }
}

exports.get_article_ids_by_keyword = function (next) {
    return function (db, keyword, hour) {
        if(keyword === undefined) {
            return next(undefined);
        }

        var where = {
            "keywords.keyword": keyword
        };

        if (hour !== undefined) {
            var date = new Date();
            date.setHours(date.getHours() - hour);
            where.date = {
                "$gt": date
            };
        }

        db.collection('article').find(where, { _id: true }).toArray(function (err, article_list) {
            if (err) {
                throw err;
            }

            next(article_list);
        });
    }
}

exports.get_keywords_batch_times_by_term = function (next) {
    return function (db, term) {
        if (term === undefined || term === '') {
            throw new Error('time not Valid!');
        }
        db.collection('stastics').findOne({ _id: new ObjectId(term) }, {}, function (err, batch_times) {
            if (err) {
                throw err;
            }
            next(batch_times);
        });
    }
}

exports.get_keywords = function (next) {
    console.log("getgetget");
    console.log(next);
    return function (db, hour) {
        console.log("get_keywords");
        console.log("hour = " + hour);
        db.collection('batch_log').findOne({ _id: Number(hour) }, {}, function (err, doc) {
            if (err) {
                throw err;
            }

            console.log("i am hour");
            console.log(doc);
            if (doc === undefined) {
                return next(undefined);
            }

            if (doc.batch_time === undefined) {
                return next(undefined);
            }

            db.collection('keyword_' + hour).find().sort({rank: 1}).toArray(function (err, keyword_list) {
                if (err) {
                    throw err;
                }
                console.log("get_keywords next");        
                next(hour, doc.batch_time, keyword_list);
            });
        });
    }
}

exports.create_nodes = function (next) {
    console.log("create_nodes ccc");
    console.log(next);
    return function(term, batch_time, keywords) {
        console.log("create_nodes");        
        console.log("term = " + term + " batch_time = " + batch_time + " keywords = " + keywords);        
        var keywords_with_article = {};     // {'keyword': [article_id,], }
        for (var i=0; i<keywords.length; i++) {                    
            model_keyword.get_article_ids_by_keyword(function(arr) {
                keywords_with_article[keywords[i]] = arr;

                // make node
                keywords[i]['created_time'] = batch_time;
                Keyword.create(keywords[i], function(err, keyword) {
                    if (err) return next(err);
                    
                    console.log("create keyword node : " + keyword);
                    if (i == keywords.length - 1) {
                        console.log("create_nodes next");     
                        next(term, keywords, keywords_with_article);
                    }
                });
            })(req.db, keywords[i], term);  // HOUR FROM NOW
        }
    }
}

exports.create_graph = function (next) {
    console.log("create_graph ccc");
    console.log(next);
    return function(term, keywords, keywords_with_article) {
        for (var i=0; i<keywords.length; i++) {
            var keyword_out = keywords[i]; 
            var article_size_with_out = keywords_with_article[keyword_out].length;
            
            if (article_size_with_out == 0)
                continue;

            for (var j=0; j<keywords.length; j++) {
                if (j==i) {
                    continue;
                } else {
                    // check intersection with articles
                    // if condition is satisfied, make edge
                    
                    var keyword_in = keywords[j];                    
                    var article_size_with_in = keywords_with_article[keyword_in].length;

                    if (article_size_with_in == 0)
                        continue;

                    var intersection_size = keywords_with_article[keyword_out].filter(function(n) {
                        return (keywords_with_article[keyword_in].indexOf(n) != -1)
                    }).length;

                    
                    if (intersection_size/article_size_with_out > 0.6 || 
                        intersection_size/article_size_with_in > 0.6) {                        
                        Keyword.getByKeyword(keyword_out[_id]).follow(Keyword.getByKeyword(keyword_out[_id]),
                            function(err) {
                                if (err) return next(err);
                                console.log("create keyword edge : " + keyword_out + "  " + keyword_in);
                            });
                    }
                }
            
            }   
        }

        next(term);
    }
}