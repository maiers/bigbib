function() { 
    if (this.fields.journal) {
        emit(this.fields.journal, 1);
    }
}