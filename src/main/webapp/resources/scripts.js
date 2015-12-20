function autoFilter(subString, value) {
	if (subString.length >= 1) {
		var split = subString.split(" ");
		var match = true;
		for (var i=0; i<split.length; i++) {
			match = match && value.match(new RegExp(split[i],'i'));
		}
		return match;
	}
	return false;
};