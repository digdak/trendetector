var express = require('express');
var fs = require('fs');
var ObjectId = require('mongodb').ObjectId;

var model_keyword = require('../model/keyword.js');
var model_article = require('../model/article.js');

var router = express.Router();
var LIMIT = 50;

var Keyword = require('../models/keyword.js');

router.get('/graph/nodes', function (req, res, next) {
    Keyword.getAll(function (err, keywords) {        
        if (err) return next(err);
        // res.json(keywords);
        res.render('keywords', {'keywords': keywords});
    });
});

router.get('/graph/nodes_with_rel/:term.json', function (req, res, next) {
    var term = req.params.term;
    // Check is there graph on time 
    // if not make graph,
    Keyword.getAll(term, function (err, keywords) {        
        if (err) return next(err);
        
        if (keywords.length != 0) {
            // get orgin graph from db
            Keyword.getPairs(function (err, results) {        
                if (err) return next(err);
                res.json({keywords:keywords,relations:results});            
            });
        } else {
            // make new graph
            model_keyword.get_article_ids_by_keyword(function(arr) {
                Keyword.getAll(function (err, keywords) {        
                    if (err) return next(err);
                    
                    Keyword.getPairs(function (err, results) {        
                        if (err) return next(err);
                        res.json({keywords:keywords,relations:results});            
                    });
                    // res.render('keywords', {'keywords': keywords});
                });
            })(req.db, keyword, term);  // HOUR FROM NOW
        }
        // res.render('keywords', {'keywords': keywords});
    });

    
    
});

router.get('/graph/relations', function (req, res, next) {
    Keyword.getPairs(function (err, results) {        
        if (err) return next(err);
        res.json(results);
        // res.render('keywords', {'keywords': keywords});
    });
});

router.get('/graph/nodes/:id', function (req, res, next) {
    var node_id = req.params.id;
    Keyword.get(node_id, function (err, keyword) {                
        if (err) return next(err);
        // res.json(keyword);
        keyword.getFollowingAndOthers(function (err, following, others) {
            if (err) return next(err);            
            res.render('keyword', {
                keyword: keyword,
                following: following,
                others: others
            });
        });
        
    });
});

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