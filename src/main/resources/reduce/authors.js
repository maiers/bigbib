function(key, value) {
    coauthors = {};        
    for (var i = 0; i < value.length; i++) {
        var key = Object.keys(value[i])[0];
        if (!coauthors[key]) {
            coauthors[key] = value[i][key];
        } else {
            coauthors[key].count += value[i][key].count;
            value[i][key].publications.forEach(function(d,i) {                    
                coauthors[key].publications.push(d);
            });
        }
    } 
    return coauthors;
}