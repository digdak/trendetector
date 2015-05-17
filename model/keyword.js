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

exports.get_keyword_info = function (next) {
    return function (db, keyword) {
        if(keyword === undefined) {
            return next(undefined);
        }

        db.collection('keyword').findOne({ _id: keyword }, {}, function (err, data) {
            if (err) {
                throw err;
            }
            next(data);
        });
    }
}