var express = require('express');
var fs = require('fs');
var ObjectId = require('mongodb').ObjectId;

var router = express.Router();
var LIMIT = 50;

router.get('/article/list', function (req, res, next) {
    var db = req.db;
    var page = req.query.page;
    var community = req.query.community;
    var keyword = req.query.keyword;

    var where = { 'contents': { '$exists': true } };

    if (page === undefined || page === '') {
        page = 1;
    }
    if (community !== undefined) {
        where.community = community;
    }
    if (keyword !== undefined) {
        db.collection('keyword').findOne({ _id: keyword }, {}, function (err, data) {
            if (err) {
                throw err;
            }
            where._id = { '$in' : data.article };
            console.log(where);

            page = (page - 1) * LIMIT;

            db.collection('article').find(where, { 'contents': false })
                .sort({ date: -1 }).skip(page).limit(LIMIT).toArray(
                function (err, article_list) {
                    if (err) {
                        throw err;
                    }

                    var result = {};
                    db.collection('article').find(where).count(function (err, cnt) {
                        if (err) {
                            throw err;
                            //next(err);
                        }

                        result.totalcount = cnt;
                        result.limits = LIMIT;
                        result.datas = article_list;
                        res.json(result);
                    });

                }
            );

        });
    }
    else {
        page = (page - 1) * LIMIT;

        db.collection('article').find(where, { 'contents': false })
            .sort({ date: -1 }).skip(page).limit(LIMIT).toArray(
            function (err, article_list) {
                if (err) {
                    throw err;
                }

                var result = {};
                db.collection('article').find(where).count(function (err, cnt) {
                    if (err) {
                        throw err;
                        //next(err);
                    }

                    result.totalcount = cnt;
                    result.limits = LIMIT;
                    result.datas = article_list;
                    res.json(result);
                });

            }
        );
    }
});

router.get('/article/:id', function (req, res, next) {
    var db = req.db;
    var article_id = req.params.id;

    db.collection('article').findOne({ _id: new ObjectId(article_id) }, {}, function (err, article) {
        if (err) {
            throw err;
        }
        res.json(article);
    });
});

router.get('/keyword/list', function (req, res, next) {
    var db = req.db;

    db.collection('keyword').find().sort({ score: -1 }).toArray(
        function (err, keyword_list) {
            if (err) {
                throw err;
            }
            res.json(keyword_list);
        }
    );
});


module.exports = router;