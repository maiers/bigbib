<%@page import="java.util.List"%>
<%@page import="de.comci.bigbib.entity.FileInfo"%>
<%@page import="org.jbibtex.Value"%>
<%@page import="java.util.Map"%>
<%@page import="org.bson.types.ObjectId"%>
<%@page import="de.comci.bigbib.BibService"%>
<%@page import="de.comci.bigbib.PeristentBibTexEntry"%>
<%@page import="de.comci.bigbib.entity.FilesResult"%>
<%@page import="org.jbibtex.Key"%>
<%@page import="org.jbibtex.BibTeXEntry"%>
<%@page import="javax.naming.InitialContext"%>
<%@include file="WEB-INF/jspf/header.jsp" %>
<body>
    <jsp:include page="WEB-INF/jspf/nav.jsp">
        <jsp:param name="page" value="index" />
    </jsp:include>

    <%

        PeristentBibTexEntry pub = null;
        List<FileInfo> files = null;
        try {
            BibService bib = new BibService();
            bib.init();            
            ObjectId id = new ObjectId(request.getParameter("id"));
            pub = bib.getSingleBib(id);
            files = bib.getFileInfo(id);
            
            request.setAttribute("id", id.toString());
            request.setAttribute("fields", pub.getStringFields());
            request.setAttribute("files", files);
            request.setAttribute("type", pub.getType());
            request.setAttribute("key", pub.getKey());
            
            bib.destroy();
        } catch (Exception e) {
            e.printStackTrace();
        }

    %>

    <script src="inc/js/dropzone.js"></script>
    
    <script>
            
        $(document).ready(function() {
               
            $('#files ul.files li.filedrop').dropzone({ 
                url : '${pageContext.request.contextPath}/api/bib/${id}/files',
                previewsContainer : '#files ul.files'
            });
            
            $('#files .action-delete').on('click', function() {
                var id = $(this).data('id');
                var $self = $(this);
                $.ajax({
                    url : '${pageContext.request.contextPath}/api/bib/${id}/files/' + id,
                    type : 'DELETE',
                    success : function(d) {                        
                        $self.parents('li').remove();
                    }
                });
                return false;
            });
            
            // related authors
            $.get('${pageContext.request.contextPath}/api/analytics/related/authors/pub/${id}', function(d) {
                console.debug('related authors', d.relatedAuthors);
                
                var max = d3.max(d.relatedAuthors, function(d) {
                   return d.value; 
                });
                var x = d3.scale.linear().domain([0, max+1]).range(['20%','80%']);
                
                d3.select('ul#related-authors').selectAll('li').data(d.relatedAuthors)
                    .enter()
                    .append('li')                    
                    .append('div')
                        .attr('class', 'bar')
                        .style('width', function(d) { return x(d.value); })
                        .text(function(d) { return d.key + ' (' + d.value + ')'; });
            });
            
            $.get('${pageContext.request.contextPath}/api/analytics/related/works/pub/${id}', function(d) {
                console.debug('related works', d);
            });
            
            $.get('${pageContext.request.contextPath}/api/bib/${id}/images', function(d) {
                console.debug('images', d);
                d3.select('#publication-images').selectAll('li')
                    .data(d.files).enter()
                        .append('li')
                        .append('a')
                            .attr('href', function(d) { return '${pageContext.request.contextPath}/api/bib/${id}/images/' + d.id ; })
                        .append('img')
                            .attr('src', function(d) { return '${pageContext.request.contextPath}/api/bib/${id}/images/' + d.thumbnailId ; });
            });
            
            $('.app-action-delete').on('click', function() {
                $.ajax({
                    url : '${pageContext.request.contextPath}/api/bib/${id}',
                    type : 'DELETE',
                    success : function(d) {                        
                        window.history.back();
                    }
                });
            });
            
            $('.app-action-query-papers').on('click', function() {
                $.get('${pageContext.request.contextPath}/api/bib/${id}/actions/qpaper', function() {
                    window.location.reload();
                });
            });
               
        });
           
    </script>

    <div class="container publication">

        <div class="col-md-7">
        
            <div class=""><span class="type"><%= pub.getType() %></span> [<span class="key"><%= pub.getKey() %></span>]</div>
            <div class="authors"><c:out value='${fields["author"]}'></c:out></div>
            <div class="title"><c:out value='${fields["title"]}'></c:out></div>
            <div class="journal"><c:out value='${fields["journal"]}'></c:out></div>        
            <div class="abstract"><c:out value='${fields["abstract"]}' default="-none-"></c:out></div>
            
            <h3>Images</h3>
            <ul id="publication-images"></ul>
            
            <h3>Files</h3>
            <ul class="files list-group">                
                <c:forEach items="${files}" var="file">
                    <li class="file list-group-item">
                        <a  href="${pageContext.request.contextPath}/api/bib/${id}/files/${file.id}">
                            <div class="file-name"><c:out value="${file.name}"></c:out></div>                        
                            <div class="file-content-type"><c:out value="${file.contentType}"></c:out></div>
                        </a>
                        <button type="button" class="btn btn-warning action-delete" data-id="${file.id}"><span class="glyphicon glyphicon-trash"></span> Delete</button>                        
                    </li>
                </c:forEach>
                <li class="filedrop list-group-item">Click to open file dialog, or drop a file here.</li>
            </ul>

        </div>
        
        <div class="col-md-5">
            <h3>Tagged</h3>
            <ul id="publication-tags"></ul>
            <h3>Notes</h3>
            <ul id="publication-notes"></ul>
            <h3>Related Authors</h3>
            <ul id="related-authors" class="diagram diagram-bar list-unstyled"></ul>
            <h3>Related Works</h3>
            <ul id="related-works" class="diagram diagram-bar list-unstyled"></ul>
        </div>
            
        <div class="floating actions">
            <div class="btn-group">
                <button type="button" class="btn btn-default app-action-edit"><span class="glyphicon glyphicon-edit"></span> Edit</button>
                <button type="button" class="btn btn-default app-action-query-papers"><span class="glyphicon glyphicon-download"></span> Crawl for PDF</button>
                <button type="button" class="btn btn-danger app-action-delete"><span class="glyphicon glyphicon-trash"></span> Delete</button>                
            </div>    
        </div>
                
    </div>

</body>
</html>
