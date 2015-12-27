function() { 
    if (!this.fields.author) return;
    var authors = this.fields.author.split(' and ');
    for (var i = 0; i < authors.length; i++) {
        for (var j = 0; j < authors.length; j++) {
            if (i != j) {
                var coauthor = authors[j].trim().replace('\n',' ');
                var o = {};
                o[coauthor] = {
                        author: coauthor,
                        count: 1,
                        publications: [this['_id']]
                    }
                emit(authors[i].trim().replace('\n',' '), o);
            }
        }
    }
}