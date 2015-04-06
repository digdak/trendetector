

// DOM Ready =============================================================
$(document).ready(function () {
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
    $.getJSON('/articlelist', { 'page': page }, function (data) {
        // /contents?community=CL&board_id=1&article_no=36747974
        // For each item in our JSON, add a table row and cells to the content string
        $.each(data, function () {
            tableContent += '<tr>';
            tableContent += '<td>' + this.community + '</td>';
            tableContent += '<td><a href="' +
                '/view?page=' + page +
                '&community=' + this.community +
                '&board_id=' + this.board_id +
                '&article_no=' + this.article_no + '">' + this.subject + '</a></td>';
            tableContent += '<td>' + this.author + '</td>';
            tableContent += '<td>' + this.date + '</td>';
            tableContent += '<td>' + this.hit + '</td>';
            tableContent += '</tr>';
        });

        // Inject the whole content string into our existing HTML table
        $('#articleList table tbody').html(tableContent);

    });
};


// Show Contents
function showContents() {
    $.getJSON('/contents', { board_id: board_id, article_no: article_no }, function (data) {
        console.log(data);
        $('#contentsUrl').html('<a href="' + data.url + '">' + data.url + '</a>');
        $('#contentsBody').html(data.contents);
    });
};
