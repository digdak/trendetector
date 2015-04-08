conn = new Mongo();
db = conn.getDB("trendetector");

db.article.drop();
db.article.ensureIndex(
	{ board_id: 1, article_no: 1 },
	{ unique: true }
);