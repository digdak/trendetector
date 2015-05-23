/**
 * Created by 은화 on 2015-05-17.
 */
var ObjectId = require('mongodb').ObjectId;

exports.get_article_by_id = function (next) {
    return function (db, article_id ) {
        if (article_id === undefined || article_id === '') {
            throw new Error('Not Valid!');
        }
        db.collection('article').findOne({ _id: new ObjectId(article_id) }, {}, function (err, article) {
            if (err) {
                throw err;
            }
            next(article);
        });
    }
}


exports.get_articlelist = function (next) {
    return function (db, page, limit, where) {
        if (where === undefined) {
            where = {};
        }

        db.collection('article').find(where, {'contents': false}).sort({ date: -1 }).skip(page).limit(limit).toArray(
            function (err, article_list) {
                if (err) {
                    throw err;
                }

                db.collection('article').find(where).count(function (err, cnt) {
                    if (err) {
                        throw err;
                    }
                    next(article_list, cnt);
                });
            }
        );
    }
};

