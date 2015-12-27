function(key, value) { 
    var count = 0; 
    value.forEach(function(d) { 
        count += d.value; 
    }); 
    return count; 
}