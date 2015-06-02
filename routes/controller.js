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

router.get('/graph/nodes_with_rel/:term', function (req, res, next) {
    var term = req.params.term;
    console.log("routing /graph/nodes_with_rel/" + term);
    // Check is there graph on time 
    // if not make graph,
    Keyword.getAll(term, function (err, keywords) {        
        if (err) {
            console.log(err);
            return next(err);
        }
        
        if (keywords.length != 0) {
            // get orgin graph from db
            Keyword.getPairs(function (err, results) {        

                console.log("get pair");
                if (err) return next(err);
                res.json({keywords:keywords,relations:results});            
            });
        } else {
            console.log("create keyword graph");
            res.redirect('/graph/create/'+term);
        }
            
    });    
});

// make new graph
router.get('/graph/create/:term', function(req, res, next) {    
    var term = req.params.term;
    console.log("routing /graph/create/" + term);
    model_keyword.get_keywords(Keyword.create_nodes(Keyword.create_graph(function(term) {
        console.log("do redirect");
        res.redirect('/graph/nodes_with_rel/'+term+'.json');
    })))(req.db, term); // end of get keywords
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

router.post('/article/list', function (req, res, next) {
    var page = req.query.page;
    var keyword = req.body.keyword;
    var community = req.body["community[]"];

    if (page === undefined || page === '') {
        page = 1;
    }
    page = (page - 1) * LIMIT;

    var where = {
        'contents': { '$exists': true },
        'keywords': { '$exists': true }
    };

    if (keyword !== undefined) {
        where["keywords.keyword"] = keyword;
    }

    if (community !== undefined && community.length > 0) {
        if (!Array.isArray(community)) {
            community = [ community ];
        }
        where.community = {'$in': community};
    }

    model_article.get_articlelist(function (article_list, total_cnt) {
        var result = {};
        result.totalcount = total_cnt;
        result.limits = LIMIT;
        result.datas = article_list;
        res.json(result);
    })(req.db, page, LIMIT, where);
});



router.get('/article/:id', function (req, res, next) {
    var article_id = req.params.id;

    model_article.get_article_by_id(function (data){
        res.json(data);
    })(req.db, article_id);
});


router.get('/keyword/list', function (req, res, next) {
    var term = req.query.term;
/*
    if (term == undefined) {
        var err = new Error('Not Valid');
        err.status = 403;
        return next(err);
    }
*/
    model_keyword.get_keywords(function (db, term, batch_time, keyword_list) {
        var result = {};
        result.batch_time = batch_time;
        result.keywords = keyword_list;     
        console.log(keyword_list);   
        res.json(result);

    })(req.db, term);

});

router.get('/community/list', function (req, res) {
    req.db.collection('community').find().toArray(
        function (err, community_list) {
            if (err) {
                throw err;
            }
            res.json(community_list);
        }
    );
});


module.exports = router;