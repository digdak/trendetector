var express = require('express');
var fs = require('fs');
var ObjectId = require('mongodb').ObjectId;

var router = express.Router();
var LIMIT = 50;

/* GET home page. */
router.get('/list', function (req, res, next) {
    var page = req.query.page;
    var community = req.query.community;
    if (page === undefined || page === '') {
        page = 1;
    }

    res.render('index', { 'title': 'Trendetector', 'page': page, 'community': community });
});

router.get('/view', function (req, res, next) {
    var page = req.query.page;
    var article_id = req.query.article_id;

    if (page === undefined || page === '') {
        page = 1;
    }

    res.render('view', { title: article_id, page: page, article_id: article_id });
});



router.get('/article/list', function (req, res, next) {
    var db = req.db;
    var page = req.query.page;
    var community = req.query.community;
    var where = { "contents": { "$exists": true } };

    if (page === undefined || page === '') {
        page = 1;
    }
    if (community !== undefined) {
        where.community = community;
    }
    page = (page - 1) * LIMIT;

    db.collection('article').find(where, { "contents": false })
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

module.exports = router;
