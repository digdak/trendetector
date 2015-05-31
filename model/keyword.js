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

exports.get_keywords = function (next) {
    return function (db, term) {
        db.collection('batch_log').findOne({ _id: term }, {}, function (err, doc) {
            if (err) {
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
                    keyword_list.sort(function(a, b) {
                        return b.ntfidf - a.ntfidf;
                    });
                    next(doc.batch_time, keyword_list);
            });
        });
    }
}

