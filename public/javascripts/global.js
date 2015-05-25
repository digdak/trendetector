



// DOM Ready =============================================================
$(document).ready(function () {
    delete window.document.referrer;
    window.document.__defineGetter__('referrer', function () {
        return "http://www.slrclub.com/bbs/vx2.php?id=free&divpage=5650&no=34281607";
    });


    // Contents load
    if ($(location).attr('pathname') === '/view') {
        showContents();
    }

    // Populate the article table on initial page load
    populateTable();
});

// Functions =============================================================
function makePagenation(max_page) {
    for (var i = parseInt(page)-2; i < parseInt(page); i++) {
        if (i <= 0)
            continue;
        $("#pagination").append("<a href='/list?page="+i+"'>"+i+"</a>");
    }

    $("#pagination").append("<a class='selected' href='/list?page="+page+"'>"+page+"</a>");    

    for (var i = parseInt(page)+1; i < parseInt(page)+4 && i < max_page; i++) {
        $("#pagination").append("<a href='/list?page="+i+"'>"+i+"</a>");
    }

    if(page == parseInt(max_page)) {
        $("#next_page").remove();
    } else if (page == 1) {
        $("#previous_page").remove();
    }

}

// Fill table with data
function populateTable() {

    // Empty content string
    var tableContent = '';
    var query = { 'page': page };

    if(community !== "") {
        query.community = community;
    }
    // jQuery AJAX call for JSON
    $.getJSON('/article/list', query, function (data) {
        // this variable declare on scripit DOM at layout.jade
        // using for pagination
        var total =  parseInt(data.totalcount);
        var limit =  parseInt(data.limits);
        var max_page = (total%limit == 0) ? total/limit : total/limit + 1;        
        makePagenation(max_page);

        // /contents?community=CL&board_id=1&article_no=36747974
        // For each item in our JSON, add a table row and cells to the content string
        $.each(data.datas, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.community + '</td>';
            tableContent += '<td class="subject"><a href="' +
                '/view?page=' + page +
                '&article_id=' + this._id + '">' + this.subject + '</a>';
//            tableContent += '<td class="subject">';
//            tableContent += '<a href="' + this.url + '">' + this.subject + '</a>';
            if (this.replies > 0) {
                tableContent += ' [' + this.replies + ']';
            }
            tableContent += '</td>';
            tableContent += '<td>' + this.author + '</td>';
            tableContent += '<td>' + new Date(this.date).format('y-m-d H:i:s') + '</td>';
            tableContent += '<td>' + this.hit + '</td>';
            tableContent += '<td>' + this.votes + '</td>';
            tableContent += '</tr>';
        });

        // Inject the whole content string into our existing HTML table
        $('#articleList table tbody').html(tableContent);

    });
};


// Show Contents
function showContents() {
    $.getJSON('/article/' + article_id, function (data) {
        data.keywords.sort(function(a, b) {
           return b.tf - a.tf;
        });
        $('#contentsUrl').html('<a href="' + data.url + '">' + data.url + '</a>');
        $('#contentsSubject').html(data.subject);
        data.keywords.forEach(function (keyword) {
            if (keyword.keyword.length < 2 || keyword.keyword.length > 15) {
                return;
            }
            $('#keywords').append(keyword.keyword + ' : ' + keyword.tf + '</br>');
        });
        $('#contentsBody').html(data.contents);
    });
};
