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

// use it for making graph
exports.get_article_ids_by_keyword = function (next) {
    return function (db, keyword, term, batch_time) {

        if(keyword === undefined) {
            return next(undefined);
        }

        var split_result = term.split("_");
        var m = Number(split_result[1]);
        var n = Number(split_result[2]);
        var where = {
            "keywords.keyword": keyword
        };

        var maxdate = new Date(batch_time.getTime());
        var mindate = new Date(batch_time.getTime());

        maxdate.setHours(maxdate.getHours()-m);
        mindate.setHours(mindate.getHours()-n);

        where.date = {
            "$gt": mindate,
            "$lt": maxdate
        };

        db.collection('article').find(where, { _id: true, date: true }).toArray(function (err, article_list) {
            if (err) {
                throw err;
            }

            next(article_list);
        });
    }
}

exports.get_intersection_count_by_keywords = function (next) {
    return function (db, keyword1, keyword2, term, batch_time) {

        if(keyword1 === undefined) {
            return next(undefined);
        }

        if(keyword2 === undefined) {
            return next(undefined);
        }

        var split_result = term.split("_");
        var m = Number(split_result[1]);
        var n = Number(split_result[2]);


        var maxdate = new Date(batch_time.getTime());
        var mindate = new Date(batch_time.getTime());

        maxdate.setHours(maxdate.getHours()-m);
        mindate.setHours(mindate.getHours()-n);

        var where = {
            "$and": [
                {
                    "keywords.keyword": keyword1
                },
                {
                    "keywords.keyword": keyword2
                },
                {
                    "date": {
                        "$gt": mindate,
                        "$lt": maxdate
                    }
                }
            ]
        };

        db.collection('article').count(where, function (err, cnt) {
            if (err) {
                throw err;
            }

            next(cnt);
        });
    }
}

exports.get_article_count_by_keyword = function (next) {
    return function (db, keyword, term, batch_time) {

        if(keyword === undefined) {
            return next(undefined);
        }

        var split_result = term.split("_");
        var m = Number(split_result[1]);
        var n = Number(split_result[2]);
        var where = {
            "keywords.keyword": keyword
        };

        var maxdate = new Date(batch_time.getTime());
        var mindate = new Date(batch_time.getTime());

        maxdate.setHours(maxdate.getHours()-m);
        mindate.setHours(mindate.getHours()-n);

        where.date = {
            "$gt": mindate,
            "$lt": maxdate
        };

        db.collection('article').count(where, function (err, cnt) {
            if (err) {
                throw err;
            }

            next(cnt);
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
    return function (db, term) {        
        db.collection('batch_log').findOne({ _id: term }, {}, function (err, doc) {
            if (err) {
                console.log(err);
                throw err;
            }

            if (doc === undefined || doc === null) {
                return next(undefined);
            }

            if (doc.batch_time === undefined) {
                return next(undefined);
            }

            db.collection(term)
                .find({rank: {$exists: true}})
                .sort({rank: 1}).limit(20).toArray(function (err, keyword_list) {
                    if (err) {
                        throw err;
                    }
                    
                    next(db, term, doc.batch_time, keyword_list);
            });
        });
    }
}
