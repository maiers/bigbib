<%@include file="WEB-INF/jspf/header.jsp" %>
<body>
    <jsp:include page="WEB-INF/jspf/nav.jsp">
        <jsp:param name="page" value="index" />
    </jsp:include>

    <script src="inc/js/dropzone.js"></script>
   
    <div class="container">
        
        <div id="facetes" class="col-md-3"></div>        
        
        <div id="bibliography" class="col-md-9">

            <script>

                function listPublications() {

                    var size = $.cookie('pub-list-size');
                    if (!size) size = 25;
                    var page = 0;
                    var format = $.cookie('pub-list-format');
                    if (!format) format = 'single';
                    var query = '<%= request.getParameter("query") %>';

                    if (query !== undefined && query !== 'null') {
                        $('input[name=query]').val(query);            
                    } else {
                        query = '';
                    }

                    updateSize(size, false);
                    $('.pub-list-navigator .pub-pagesizer ul li a').on('click', function() {
                        updateSize($(this).text());
                    });

                    $('.pagination').on('click', function(evt) {
                        var $e = $(evt.toElement);
                        if ($e.parent().hasClass('disabled')) {
                            return false;
                        }
                        updatePage($e.data('page'))
                        return false;
                    });

                    $('.pub-list-format button').on('click', function() {
                        updateFormat($(this).data('format'));
                    });

//                    $('#form-global-search').submit(function() {
//                        query = $('#form-global-search input[name=query]').val();
//                        page = 0;
//                        
//                        getData();
//                        return false;
//                    });

                    updateFormat(format);

                    function getData() {
                        $.get('${pageContext.request.contextPath}/api/bib?page=' + page + '&size=' + size + '&query=' + query, function(d) {
                            console.info(d);
                            renderPublications(d.items);
                            renderPagination(d.page, d.totalPages);
                        });
                    };

                    function updateFormat(format) {
                        $('.pub-list-format button').removeClass('active');
                        $('.pub-list-format button[data-format=' + format + ']').addClass('active');
                        $('#publications').removeClass('pub-list-single pub-list-multiple pub-list-image');
                        $('#publications').addClass('pub-list-' + format);
                        $.cookie('pub-list-format', format, { expires: 28 });
                    }

                    function updateSize(newSize, update) {
                        if (update === undefined) {
                            update = true;
                        }
                        var newPage = Math.ceil(page * size / newSize);
                        updatePage(newPage, false);
                        size = newSize;
                        $('.pub-list-navigator .pub-pagesizer .pub-pagesize').text(size);
                        $.cookie('pub-list-size', size, { expires: 28 });
                        if (update) {
                            getData();
                        }
                    };

                    function updatePage(newPage, update) {
                        if (update === undefined) {
                            update = true;
                        }
                        page = newPage;
                        if (update) {
                            getData();
                        }
                    };

                    function renderPagination(currentPage, totalPages) {
                        var $pager = $('.pagination')
                        $pager.html('');

                        var prev = $('<li>').append($('<a>').attr('href', '#').html('&laquo;').data('page', currentPage - 1)).toggleClass('disabled', currentPage <= 0);
                        var next = $('<li>').append($('<a>').attr('href', '#').html('&raquo;').data('page', currentPage + 1)).toggleClass('disabled', currentPage >= totalPages - 1);

                        $pager.append(prev);
                        var visiblePages = 3;

                        if (totalPages > visiblePages) {
                            var lower = Math.max(Math.min(currentPage - Math.floor(visiblePages / 2), totalPages - visiblePages),0);
                            for (var i = lower; i < lower + visiblePages; i++) {
                                $pager.append($('<li>').append($('<a>').attr('href', '#').text(i+1).data('page', i)).toggleClass('active', currentPage === i));
                            }
                        } else {
                            for (var i = 0; i < totalPages; i++) {
                                $pager.append($('<li>').append($('<a>').attr('href', '#').text(i+1).data('page', i)).toggleClass('active', currentPage === i));
                            }
                        }                
                        $pager.append(next);
                    }

                    function renderPublications(pubs) {

                        var $list = $('#publications');
                        $list.html('');

                        $.each(pubs, function(i,d) {
                            var $pub = $('<li>').addClass('publication list-group-item');
                            $pub.append($('<div>').addClass('authors').text(d.fields.author));
                            $pub.append($('<div>').addClass('title').text(d.fields.title));
                            $pub.append($('<div>').addClass('journal').text(d.fields.journal));
                            $pub.append($('<div>').addClass('year').text(d.fields.year));
                            $pub.append($('<div>').addClass('abstract').text(d.fields['abstract']));
                            var $actions = $('<div>').addClass('actions btn-group');
                            $actions.append($('<a>').addClass('btn btn-default btn-xs').append($('<span>').addClass('glyphicon glyphicon-globe')).append('&nbsp;').attr('href', 'https://www.google.com/#q="' + d.fields.title + '" ' + d.fields.author + '+filetype:pdf&safe=off'));
                            $actions.append($('<a>').addClass('btn btn-danger btn-xs').append($('<span>').addClass('glyphicon glyphicon-trash')).append('&nbsp;').attr('href', '#'));
                            //$pub.append($actions);
                            $list.append($pub);
                            $pub.on('click', function() {
                                window.location = 'pub.jsp?id=' + d.id;
                            });
                            $pub.dropzone({ 
                                url : '${pageContext.request.contextPath}/api/bib/' + d.id + '/files'
                            });                    
                        });                

                    }

                    getData();

                }

                $(document).ready(listPublications);

                

            </script>

            <div class="row pub-list-navigator">
                <div class="btn-toolbar pull-right" role="toolbar">
                    
                        <div class="btn-group pub-pagesizer">
                            <button type="button" class="btn btn-default dropdown-toggle" data-toggle="dropdown">
                                <span class="pub-pagesize">25</span> items per page <span class="caret"></span>
                            </button>
                            <ul class="dropdown-menu" role="menu">
                                <li><a href="#">10</a></li>
                                <li><a href="#">25</a></li>
                                <li><a href="#">50</a></li>
                                <li><a href="#">100</a></li>
                            </ul>
                        </div>
                    <div class="btn-group pub-list-format">
                        <button type="button" class="btn btn-default" data-format="single"><span class="glyphicon glyphicon-list"></span> Oneline</button>
                        <button type="button" class="btn btn-default" data-format="multiple"><span class="glyphicon glyphicon-th-list"></span> Multiline</button>
                        <button type="button" class="btn btn-default" data-format="image"><span class="glyphicon glyphicon-th-large"></span> Images</button>                        
                    </div>
                </div>
                
            </div>

            <div class="row top-buffer">
                <ul class="list-group" id="publications">                   
                </ul>
            </div>
            
            <ul class="pagination floating"></ul>

        </div>
    </div>

</body>
</html>
