var express = require('express');
var fs = require('fs');
var ObjectId = require('mongodb').ObjectId;

var model_keyword = require('../model/keyword.js');
var model_article = require('../model/article.js');

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
    var page = req.query.page;
    var community = req.query.community;
    var keyword = req.query.keyword;

    if (page === undefined || page === '') {
        page = 1;
    }
    page = (page - 1) * LIMIT;

    model_keyword.get_keyword_info(function (data) {
        var where = { 'contents': { '$exists': true } };

        if (data !== undefined) {
            where._id = {'$in': data.article};
        }

        if (community !== undefined) {
            where.community = community;
        }

        model_article.get_articlelist(function (article_list, total_cnt) {
            var result = {};
            result.totalcount = total_cnt;
            result.limits = LIMIT;
            result.datas = article_list;
            res.json(result);
        })(req.db, page, LIMIT, where);

    })(req.db, keyword);

});


router.get('/article/:id', function (req, res, next) {
    var article_id = req.params.id;

    model_article.get_article_by_id(function (data){
        res.json(data);
    })(req.db, article_id);
});

router.get('/keyword/list', function (req, res, next) {
    model_keyword.get_keywordlist_by_community(function (data) {
        res.json(data);
    })(req.db);
});


module.exports = router;