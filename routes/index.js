var express = require('express');
var fs = require('fs');
var ObjectId = require('mongodb').ObjectId;

var router = express.Router();

/* GET home page. */
router.get('/list', function (req, res, next) {
    var page = req.query.page;

    if (page === undefined || page === '') {
        page = 1;
    }

    res.render('index', { title: 'Trendetector', page: page });
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

    if (page === undefined || page === '') {
        page = 1;
    }
    page = (page - 1) * 50

    db.collection('article').find({ contents: { $exists: true } }, { contents: false })
        .sort({ date: -1 }).skip(page).limit(50).toArray(
        function (err, article_list) {
            if (err) {
                throw err;
            }
            res.json(article_list);
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
