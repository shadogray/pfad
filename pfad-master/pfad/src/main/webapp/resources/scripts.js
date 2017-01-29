function autoFilter(subString, value) {
	if (subString.length >= 1) {
		var split = subString.split(" ");
		var match = true;
		for (var i = 0; i < split.length; i++) {
			match = match && value.match(new RegExp(split[i], 'i'));
		}
		return match;
	}
	return false;
}

function test(elem, event, params) {
	var key = event.which || event.keyCode;
	alert('begin:' + this + ' event: ' + event + ' key: ' + key);
	if (key == 13) {
		return true;
	}
	return false;
}

function submitSearch(event) {
	//console.log("added search: " + event);
	if (event.which == '13') {
		//console.log("enter found: " + event.which);
		var search = $('#search\\:search');
		if (search.length > 0) {
			//console.log('found search: '+search.length);
			try {
				search.click();
				//console.log('clicked search: '+search.length);
			} catch (err) {
				console.log('error happened: '+err);
			}
		}
	}
}

$(function() {
	$('form#search').on('keypress', function(event) {
		submitSearch(event);
	});
	$('.stopKeyPress').keypress(function(event) {
		event.stopPropagation();
	});
});
