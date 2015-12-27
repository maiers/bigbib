function() { 
    if (!this.fields.author) return;
    this.fields.author.split(' and ').forEach(function(d) { emit(d,1)}); 
}