



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

    // select multiple community
    selectMultipleCommunity();    
    get_keyword_list();
});

// Functions =============================================================
function makePagenation(max_page) {
    $("#pagination").html("");
    for (var i = parseInt(page)-2; i < parseInt(page); i++) {
        if (i <= 0)
            continue;
        $("#pagination").append("<a href='#' value='"+i+"'>"+i+"</a>");
    }

    $("#pagination").append("<a class='selected' href='#' value='"+i+"'>"+page+"</a>");    

    for (var i = parseInt(page)+1; i < parseInt(page)+4 && i < max_page; i++) {
        $("#pagination").append("<a href='#' value='"+i+"'>"+i+"</a>");
    }

    $("#previous_page").attr("value", parseInt(page)-1);
    $("#next_page").attr("value", parseInt(page)+1);

    if(page == parseInt(max_page)) {
        $("#next_page").remove();
    } else if (page == 1) {
        $("#previous_page").remove();
    }
    selectPage();
}

// Fill table with data
function populateTable() {
    // Empty content string
    var tableContent = '';
    var query = { };

    if (page === undefined || page === '') {
        page = 1;
    }
    if(community != undefined && community !== "") {
        query.community = community;
    }
    if(keyword != undefined && keyword !== "") {
        query.keyword = keyword;
    }
    // jQuery AJAX call for JSON
    $.post('/article/list?page='+page, query, function (data) {
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
            tableContent += '<td class="community '+this.community+'">' + this.community + '</td>';
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
        /*
        data.keywords.forEach(function (keyword) {
            if (keyword.keyword.length < 2 || keyword.keyword.length > 15) {
                return;
            }
            $('#keywords').append(keyword.keyword + ' : ' + keyword.tf + '</br>');
        });
        */
        $('#contentsBody').html(data.contents);
    });
};

function selectMultipleCommunity() {
    $('.site_select ul li').on("click", function() {
        $(this).toggleClass("selected");
        $(this).removeClass("unselected");
        $('.site_select ul li:not(.selected)').addClass("unselected")
        page = 1;
        community = $('.site_select ul li.selected').map(function() {
            return $(this).attr("value");
        }).get();
        keyword = undefined;        
        populateTable();
    });
};

function selectPage() {
    $('#pagination a').on("click", function() {
        var page_num = $(this).attr("value");
        page = page_num;
        $(this).addClass("selected")
        $('#pagination a:not(.selected)').removeClass("selected");
        populateTable();
    });
}

function get_keyword_list() {
    // console.log("called get_keyword_list");
    var term_list = ["keyword_0_3", "keyword_0_6", "keyword_0_12", "keyword_0_24", "keyword_24_72", "keyword_72_168"];
    $("#keyword_list").html("");       

    term_list.forEach(function(term) {
        $.getJSON('/keyword/list?term=' + term, function (data) {
            var batch_time = new Date(data.batch_time);
            // console.log("getGraph TIME = " + batch_time.getTime());
            if (term == term_list[0]) {
                getGraph(term, batch_time.getTime());
            }
            var result = "";
            var title_tokens = term.split("_");
            var title = title_tokens[title_tokens.length-1];
            result += "<div class='col-xs-6 keyword_list'><h4 class='btn btn-info' onclick=getGraph('"+term+"',"+batch_time.getTime()+");>최근 "+title+"시간 키워드</h4><ol>";
            $.each(data.keywords, function () {
                result+="<li>";
                result+=this._id;
                result+="</li>";
            }); 
            result += "</ol></div>";
            // console.log(result);
            $("#keyword_list").append(result);       
        });
    });
}