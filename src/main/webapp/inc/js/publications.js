
function listPublications(ctx, query) {

    var size = $.cookie('pub-list-size');
    if (!size)
        size = 25;
    var page = 0;
    var format = $.cookie('pub-list-format');
    if (!format)
        format = 'single';

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
        updatePage($e.data('page'));
        return false;
    });

    $('.pub-list-format button').on('click', function() {
        updateFormat($(this).data('format'));
    });

    updateFormat(format);

    function getData(facets) {
        var q = query + ((facets !== undefined) ? '&' + facets : '');
        $.get(ctx + '/api/bib?page=' + page + '&size=' + size + '&query=' + q, function(d) {
            renderPublications(d.items);
            renderPagination(d.page, d.totalPages);
        });
    }

    function getFacets() {
        $.get(ctx + '/api/bib/facets?query=' + query, function(d) {
            renderFacets(d);
        });
    }

    var x = null;
    var facetSelectionModel = {
        model: {},
        updateListener: [],
        registerFacet: function(facet, data) {
            if (!this.model.hasOwnProperty(facet)) {
                this.model[facet] = data;
            }
        },
        addSelection: function(facet, selection) {
            console.debug('addSelection', facet, selection);
            this.model[facet].forEach(function(d, i) {
                if (d.key === selection) {
                    d.selected = true;
                }
            });
            this.fireUpdate();
        },
        removeSelection: function(facet, selection) {
            console.debug('removeSelection', facet, selection);
            this.model[facet].forEach(function(d, i) {
                if (d.key === selection) {
                    d.selected = false;
                }
            });
            this.fireUpdate();
        },
        toggleSelection: function(facet, selection) {
            console.debug('toggleSelection', facet, selection);
            if (this.isSelected(facet, selection)) {
                this.removeSelection(facet, selection);
            } else {
                this.addSelection(facet, selection);
            }
        },
        isSelected: function(facet, selection) {
            for (var i = 0; i < this.model[facet].length; i++) {
                if (this.model[facet][i].key === selection && this.model[facet][i].selected) {
                    return true;
                }
            }
            return false;
        },
        getQueryParameters: function() {
            var params = {};
            $.each(this.model, function(k, v) {
                var selection = v.filter(function(d) {
                    return d.selected;
                }).map(function(d) {
                    return encodeURIComponent(d.key);
                });
                if (selection.length > 0) {
                    params['facet-' + k] = selection;
                }
            });
            var p = '';
            $.each(params, function(k, v) {
                v.forEach(function(d) {
                    if (p.length > 0) {
                        p += '&';
                    }
                    p += k + '=' + d;
                });
            })
            return p;
//            return params.reduce(function(p,c) {
//                return (p !== '') ? '&' : '' + c.key + '=' + c.value.join(',');
//            }, '');
        },
        registerUpdateListener: function(l) {
            this.updateListener.push(l);
        },
        fireUpdate: function() {
            this.updateListener.forEach(function(d) {
                return d();
            });
        }
    };

    function updateQuery() {
        var facets = facetSelectionModel.getQueryParameters();
        //window.location.href = ctx + '?query=' + query + '&' + facets;
        page = 0;
        getData(facets);
    }

    function updateFacets() {
        d3.select('#facets').selectAll('li')
                .classed('selected', function(d) {
                    return d.selected;
                });
    }

    function renderFacets(ajax) {

        x = d3.scale.linear().domain([0, ajax.numItems]).range(['0%', '100%']);
        delete ajax.numItems;
        var data = d3.entries(ajax);

        facetSelectionModel.registerUpdateListener(updateFacets);
        facetSelectionModel.registerUpdateListener(updateQuery);

        d3.select('#facets').selectAll('div.facet-container')
                .data(data, function(d) {
                    return d.key;
                })
                .enter().append('div')
                .attr('class', 'facet-container')
                .append('ul')
                .attr('class', function(d) {
                    return 'facet-' + d.key;
                })
                .each(renderFacet);

    }

    function renderFacet(d) {
        var d3s = d3.select(this);
        var facet = d.key;
        var data = d3.entries(d.value);
        if (facet !== 'year') {
            data.sort(function(a, b) {
                return a.key > b.key;
            });
        }
        facetSelectionModel.registerFacet(facet, data);
        d3s.append('h3').text(facet);

        var li = d3s.selectAll('li').data(data)
                .enter()
                .append('li')
                .classed('selected', function(d) {
                    return facetSelectionModel.isSelected(facet, d.key);
                })
                .attr('title', function(d) {
                    return d.key + ' (' + d.value + ')';
                })
                .on('mouseenter', function(d) {
                    $(this).tooltip({
                        container : 'body',
                        placement: 'right',
                        delay : 50
                    });
                })
                .on('mouseout', function() {
                    $(this).tooltip('hide');
                })
                .on('click', function(d) {
                    facetSelectionModel.toggleSelection(facet, d.key);
                });

        li.append('div')
                .attr('class', 'bar')
                .style('width', function(d) {
                    return x(d.value);
                });

        li.append('div')
                .attr('class', 'text')
                .text(function(d) {
                    return d.key;
                });
    }

    function updateFormat(format) {
        $('.pub-list-format button').removeClass('active');
        $('.pub-list-format button[data-format=' + format + ']').addClass('active');
        $('#publications').removeClass('pub-list-single pub-list-multiple pub-list-image');
        $('#publications').addClass('pub-list-' + format);
        $.cookie('pub-list-format', format, {expires: 28});
    }

    function updateSize(newSize, update) {
        if (update === undefined) {
            update = true;
        }
        var newPage = Math.ceil(page * size / newSize);
        updatePage(newPage, false);
        size = newSize;
        $('.pub-list-navigator .pub-pagesizer .pub-pagesize').text(size);
        $.cookie('pub-list-size', size, {expires: 28});
        if (update) {
            getData();
        }
    }

    function updatePage(newPage, update) {
        if (update === undefined) {
            update = true;
        }
        page = newPage;
        if (update) {
            getData();
        }
    }

    function renderPagination(currentPage, totalPages) {
        var $pager = $('.pagination');
        $pager.html('');

        var prev = $('<li>').append($('<a>').attr('href', '#').html('&laquo;').data('page', currentPage - 1)).toggleClass('disabled', currentPage <= 0);
        var next = $('<li>').append($('<a>').attr('href', '#').html('&raquo;').data('page', currentPage + 1)).toggleClass('disabled', currentPage >= totalPages - 1);

        $pager.append(prev);
        var visiblePages = 3;

        if (totalPages > visiblePages) {
            var lower = Math.max(Math.min(currentPage - Math.floor(visiblePages / 2), totalPages - visiblePages), 0);
            for (var i = lower; i < lower + visiblePages; i++) {
                $pager.append($('<li>').append($('<a>').attr('href', '#').text(i + 1).data('page', i)).toggleClass('active', currentPage === i));
            }
        } else {
            for (var i = 0; i < totalPages; i++) {
                $pager.append($('<li>').append($('<a>').attr('href', '#').text(i + 1).data('page', i)).toggleClass('active', currentPage === i));
            }
        }
        $pager.append(next);
    }

    function renderPublications(pubs) {

        var $list = $('#publications');
        $list.html('');

        $.each(pubs, function(i, d) {
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
                url: ctx + '/api/bib/' + d.id + '/files'
            });
        });

    }

    getData();
    getFacets();

}