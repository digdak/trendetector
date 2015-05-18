conn = new Mongo();
db = conn.getDB("trendetector");

db.community.drop();
var rows = 
[
	{
		_id: 'CL',
		name: '클리앙'
	},
	{
		_id: 'SR',
		name: 'SLR클럽'
	},
	{
		_id: 'OU',
		name: '오늘의유머'
	},
	{
		_id: 'DD',
		name: '개드립'
	}
];
db.community.insert(rows);

db.board.drop();
var rows =
[
	{
		community: 'CL',
		name: '모두의공원',
		url: 'http://www.clien.net/cs2/bbs/board.php?bo_table=park',
		imagedown: false,
		active: true
	},
	{
		community: 'CL',
		name: '새로운소식',
		url: 'http://www.clien.net/cs2/bbs/board.php?bo_table=news',
		imagedown: false,
		active: true
	},
	{
		community: 'SR',
		name: '자유게시판',
		url: 'http://www.slrclub.com/bbs/zboard.php?id=free',
		imagedown: false,
		active: true
	},
	{
		community: 'OU',
		name: '베스트게시물',
		url: 'http://www.todayhumor.co.kr/board/list.php?table=humorbest',
		active: true
	},
	{
		community: 'DD',
		name: '개드립',
		url: 'http://www.dogdrip.net/dogdrip',
		active: true
	}
]
db.board.insert(rows);

db.article.drop();
db.article.ensureIndex(
	{ board_id: 1, article_no: 1 },
	{ unique: true }
);

db.statistics.drop();
db.statistics.insert({ _id: 0, totalcnt: 0 });

db.statistics.keywords.drop();

db.system.js.remove({});
// ======== 1주일 전 데이터들 통계 누적 =========
db.system.js.save({
	_id: "removeAndStatisticsBeforeWeek",
	value: function () {
		if (db.statistics_temp.count() != 0) {
			return { ok: false, error: "already exists statistics_temp." };
		}

		var map = function () {
			this.keywords.forEach(function (data) {
				var key = data.keyword;
				if (key.length < 2 || key.length > 15) {
					return;
				}

				emit(key, 1);
			});
		};

		var reduce = function (key, values) {
			var count = 0;
			values.forEach(function (cnt) {
				count += cnt;
			});
			return count;
		};

		var now = new ISODate();
		now.setDate(now.getDate() - 7);

		var result = 
		db.article.mapReduce(map, reduce, {
			out: "statistics_temp",
			query: { 
				keywords: { $exists: true, $not: { $eq: false } },
				date: { $lt : now }
			}
		});

		if (result.ok != 1) {
			db.statistics_temp.drop();
			return { ok: false, result: result };
		}
		var inc_cnt = result.counts.input;
		if (inc_cnt == 0) {
			return { ok: true, result: "no data" };
		}

		db.article.remove({ date: { $lt: now } });

		var result = db.statistics.update(
				{ _id: 0 }, 
				{ $inc: { totalcnt: inc_cnt } },
				{ upsert: true }
			);

		if (result.nMatched == 0 
		&& result.nUpserted == 0 
		&& result.nModified == 0) {
			return { ok: false, result: result };
		}

		var bulkUpdate = db.statistics.keywords.initializeUnorderedBulkOp();
		db.statistics_temp.find().forEach(function (doc) {
			bulkUpdate.find({_id: doc._id}).upsert().update(
				{ $inc: { cnt: doc.value } }
			);
		});
		var result = bulkUpdate.execute();

		if (result.nMatched == 0 
		&& result.nUpserted == 0 
		&& result.nModified == 0) {
			return { ok: false, result: result };
		}

		db.statistics_temp.drop();
	}
});
// ====================================