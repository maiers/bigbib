/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

$(document).ready(function() {
   
    // 
    var minusplus = $('input[data-custom=minusplus]');
   
    minusplus.each(function(i,d) {
        
        // orginal element
        var thiz = $(d);
        
        // bounds
        var max = (thiz.attr('max')) ? parseFloat(thiz.attr('max')) : MAX_VALUE;
        var min = (thiz.attr('min')) ? parseFloat(thiz.attr('min')) : MIN_VALUE;
        var step = (thiz.attr('step')) ? parseFloat(thiz.attr('step')) : 1;
        
        // parse numbers
        var parse = function(v) {
            if (thiz.attr('type') === 'number') {
                return parseFloat(v);
            }
            return v;
        }
        
        // scaffolding
        var wrap = $('<div>').addClass('input-group');
        var minus = $('<button>').addClass('btn btn-default').attr('type', 'button').append($('<i>').addClass('glyphicon glyphicon-minus'));
        var plus= $('<button>').addClass('btn btn-default').attr('type', 'button').append($('<i>').addClass('glyphicon glyphicon-plus'));
        var sensor = $('<div>').addClass('form-control').css('width', '100%');
        var fill = $('<div>').css('height', '100%').css('background', 'lightblue');
        fill.css('text-align', 'right');
        fill.css('padding-right', '.6em');
        fill.css('color', 'white');
        fill.css('overflow', 'visible');
        wrap.css('user-select', 'none');        
        sensor.append(fill);
        
        // spin through
        var interval = undefined;
        minus.on('mousedown', function() {
            if (interval === undefined) {
                inc(thiz, -1);
                interval = window.setInterval(function() {
                    inc(thiz, -1);
                }, 150);
            }
        });
        minus.on('mouseup', function() {
            window.clearInterval(interval);
            interval = undefined;
        });
        plus.on('mousedown', function() {
            if (interval === undefined) {
                inc(thiz, 1);
                interval = window.setInterval(function() {
                    inc(thiz, 1);
                }, 150);
            }
        })
        plus.on('mouseup', function() {
            window.clearInterval(interval);
            interval = undefined;
        });
        
        // sensor area
        var sensing = false;
        sensor.on('click', function(e) {
            updateSensor(e);
        });
        sensor.on('mousedown', function(e) {
            sensing = true;            
        });
        $(document).on('mousemove', function(e) {
            if (sensing) {
                updateSensor(e);
            }
        });
        $(document).on('mouseup', function(e) {
            sensing = false;
        });
        
        function updateSensor(e) {
            var p = (e.offsetX) / sensor.outerWidth();
            p = Math.max(Math.min(p, 1), 0);
            setValue((max * p - min * p) + min);
            thiz.data('p', p);
            thiz.change(); 
        }
        
        function setValue(val) {
            fill.css('background', 'steelblue');
            // ensure step alignment
            val = Math.round(val / step) * step;
            thiz.val(val);
            return val;
        }
        
        wrap.append($('<span>').addClass('input-group-btn').append(minus));
        wrap.append(sensor);
        wrap.append($('<span>').addClass('input-group-btn').append(plus));        
        
        // replace with wrapper
        thiz.hide();
        wrap.insertAfter(thiz);        
        
        // draw some nice background like a progress bar
        thiz.on('change', function(d) {            
            var p = (thiz.data('p')) ? thiz.data('p') : (thiz.val() - min) / (max - min);
            var val = (max * p - min * p) + min;
            // ensure step alignment
            val = Math.round(val / step) * step;
            p = (val - min) / (max-min);
            fill.text(val);
            fill.css('width', p * 100 + '%');
        });
        
        // init
        thiz.data('p', (thiz.data('custom-default-value')) ? (parse(thiz.data('custom-default-value')) - min) / (max-min) : (max-min) / 2 + min);
        thiz.change();
        
        var inc = function(d, i) {

            // increment based on step attr
            var amount = function(v) {
                return step * v;
            }
            
            var p = Math.max(Math.min(d.data('p') + amount(i) / (max-min), 1), 0);
            d.data('p', p);
            setValue((max * p - min * p) + min);
            // trigger change event
            d.change();

        };

    });
    
});