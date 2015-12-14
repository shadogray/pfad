
function autoFilter(subString, value) {
    if (subString.length >= 1) {
        return value.match('/'+subString+'/i');
    } else
        return false;
};