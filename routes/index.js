var express = require('express');
var fs = require('fs');
var router = express.Router();

/* GET home page. */
router.get('/list', function (req, res, next) {
    var page = req.query.page;

    if (page === undefined) {
        page = 1;
    }

    res.render('index', { title: 'Trendetector', page: page });
});

router.get('/view', function (req, res, next) {
    var page = req.query.page;
    var board_id = req.query.board_id;
    var article_no = req.query.article_no;

    if (page === undefined) {
        page = 1;
    }

    res.render('view', { title: article_no, page: page, board_id: board_id, article_no: article_no });
});



router.get('/articlelist', function (req, res, next) {
    var db = req.db;
    var page = req.query.page;

    if (page === undefined) {
        page = 1;
    }
    page = (page - 1) * 50

    db.mysql.query('SELECT * FROM trendetector.v_complated_article order by date desc limit ' + page + ', 50', function (err, result) {
       res.json(result);
    });
});

router.get('/contents', function (req, res, next) {
    var db = req.db;
    var board_id = req.query.board_id;
    var article_no = req.query.article_no;

    console.log(board_id);
    console.log(article_no);
    board_id = Number(board_id);
    article_no = Number(article_no);

    db.mongodb.collection('contents').findOne({ board_id: board_id, article_no: article_no }, {}, function (err, data) {
        if (err) {
            throw err;
        }

        res.json(data);
    });
});

module.exports = router;
