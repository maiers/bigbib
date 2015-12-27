<%@page import="java.util.List"%>
<%@page import="de.comci.bigbib.FilesResult.FileInfo"%>
<%@page import="org.jbibtex.Value"%>
<%@page import="java.util.Map"%>
<%@page import="org.bson.types.ObjectId"%>
<%@page import="de.comci.bigbib.BibService"%>
<%@page import="de.comci.bigbib.PeristentBibTexEntry"%>
<%@page import="de.comci.bigbib.FilesResult"%>
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
        List<FilesResult.FileInfo> files = null;
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
               
            // related authors
            $.get('${pageContext.request.contextPath}/api/analytics/related/authors/author/${id}', function(d) {
                console.debug('related authors', d.relatedAuthors);
                
                var max = d3.max(d.relatedAuthors, function(d) {
                   return d.value; 
                });
                var x = d3.scale.linear().domain([1, max]).range(['20%','80%']);
                
                d3.select('ul#related-authors').selectAll('li').data(d.relatedAuthors)
                    .enter()
                    .append('li')                    
                    .append('div')
                        .attr('class', 'bar')
                        .style('width', function(d) { return x(d.value); })
                        .text(function(d) { return d.key; });
            });
            
            $.get('${pageContext.request.contextPath}/api/analytics/related/works/author/${id}', function(d) {
                console.debug('related works', d);
            });
               
        });
           
    </script>

    <div class="container publication">

        
                
    </div>

</body>
</html>
