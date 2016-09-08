angular.module('pfad').factory('MemberResource', function($resource) {
    var resource = $resource('../rest/members/:MemberId', {
	MemberId : '@id'
    }, {
	'queryAll' : {
	    method : 'GET',
	    isArray : true
	},
	'filtered' : {
	    url : '../rest/members/filtered',
	    method : 'GET',
	    isArray : true
	},
	'query' : {
	    method : 'GET',
	    isArray : false
	},
	'update' : {
	    method : 'PUT'
	},
	'siblings' : {
	    url : '../rest/members/siblings/:MemberId',
	    method : 'GET',
	    isArray : true
	},
	'parents' : {
	    url : '../rest/members/parents/:MemberId',
	    method : 'GET',
	    isArray : true
	}
    });
    return resource;
});