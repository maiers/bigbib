<%@include file="WEB-INF/jspf/header.jsp" %>
    <body>
        <jsp:include page="WEB-INF/jspf/nav.jsp">
            <jsp:param name="page" value="index" />
        </jsp:include>
        
        <script>
            
            $(document).ready(function() {
               
               $('#form-add-bibtex').submit(function() {
                   var i = $(this).find('#copypaste').val();
                   //console.info(i);
                   $.post('${pageContext.request.contextPath}/api/bib', i, function(d) {
                       console.info(d);
                   });
                   return false;
               });
               
            });
           
        </script>
                
        <div class="container">
            <form method="post" id="form-add-bibtex" target="${pageContext.request.contextPath}/api/bib">
                <div id="howto" class="col-md-3">
                    <input type="submit" value="import">                           
                </div>
                <div class="col-md-9">
                    <textarea id="copypaste" placeholder="Paste bibtex here"></textarea>
                </div>
            </form>
        </div>
                
    </body>
</html>
