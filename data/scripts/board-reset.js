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
		active: true,
		imagedown: true
	},
	{
		community: 'CL',
		name: '새로운소식',
		url: 'http://www.clien.net/cs2/bbs/board.php?bo_table=news',
		active: true,
		imagedown: true
	},
	{
		community: 'SR',
		name: '자유게시판',
		url: 'http://www.slrclub.com/bbs/zboard.php?id=free',
		active: true,
		imagedown: true
	},
	{
		community: 'OU',
		name: '베스트게시물',
		url: 'http://www.todayhumor.co.kr/board/list.php?table=humorbest',
		active: true
	}
]
db.board.insert(rows);