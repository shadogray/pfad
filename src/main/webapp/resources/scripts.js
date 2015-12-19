function autoFilter(subString, value) {
	if (subString.length >= 1) {
		var split = subString.split(" ");
		for (var i=0; i<split.length; i++) {
			var match = value.match(new RegExp(split[i],'i'));
			if (match) {
				return true;
			}
		}
	}
	return false;
};