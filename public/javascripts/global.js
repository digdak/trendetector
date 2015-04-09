



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

// Fill table with data
function populateTable() {

    // Empty content string
    var tableContent = '';

    // jQuery AJAX call for JSON
    $.getJSON('/article/list', { 'page': page }, function (data) {
        // /contents?community=CL&board_id=1&article_no=36747974
        // For each item in our JSON, add a table row and cells to the content string
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.community + '</td>';
            tableContent += '<td class="subject"><a href="' +
                '/view?page=' + page +
                '&article_id=' + this._id + '">' + this.subject + '</a>';
            if (this.replies > 0) {
                tableContent += ' [' + this.replies + ']';
            }
            tableContent += '</td>';
            tableContent += '<td>' + this.author + '</td>';
            tableContent += '<td>' + new Date(this.date).format('y-m-d H:i:s') + '</td>';
            tableContent += '<td>' + this.hit + '</td>';
            tableContent += '</tr>';
        });

        // Inject the whole content string into our existing HTML table
        $('#articleList table tbody').html(tableContent);

    });
};


// Show Contents
function showContents() {
    $.getJSON('/article/' + article_id, function (data) {
        $('#contentsUrl').html('<a href="' + data.url + '">' + data.url + '</a>');
        $('#contentsSubject').html(data.subject);
        $('#contentsBody').html(data.contents);
    });
};
