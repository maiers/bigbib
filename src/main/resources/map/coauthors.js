function() {  
    var self = this; 
    Object.keys(this.value).forEach(function(d) { 
        emit(self.value[d].author, self.value[d].count); 
    });
}