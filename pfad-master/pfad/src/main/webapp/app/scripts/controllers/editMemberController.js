
angular.module('pfad').controller(
	'EditMemberController',
	function($scope, $routeParams, $location, flash, MemberResource, SquadResource, MemberResource, FunctionResource, MemberResource, PaymentResource,
		BookingResource) {
	    var self = this;
	    $scope.disabled = false;
	    $scope.$location = $location;

	    $scope.get = function() {
		var successCallback = function(data) {
		    self.original = data;
		    $scope.member = new MemberResource(self.original);
		    SquadResource.queryAll(function(items) {
			$scope.truppSelectionList = $.map(items, function(item) {
			    var wrappedObject = {
				id : item.id
			    };
			    var labelObject = {
				value : item.id,
				text : item.name
			    };
			    if ($scope.member.trupp && item.id == $scope.member.trupp.id) {
				$scope.truppSelection = labelObject;
				$scope.member.trupp = wrappedObject;
				self.original.trupp = $scope.member.trupp;
			    }
			    return labelObject;
			});
		    });
		    FunctionResource.queryAll(function(items) {
			$scope.funktionenSelectionList = $.map(items, function(item) {
			    var wrappedObject = {
				id : item.id
			    };
			    var labelObject = {
				value : item.id,
				text : item.name
			    };
			    if ($scope.member.funktionen) {
				$.each($scope.member.funktionen, function(idx, element) {
				    if (item.id == element.id) {
					$scope.funktionenSelection.push(labelObject);
					$scope.member.funktionen.push(wrappedObject);
				    }
				});
				self.original.funktionen = $scope.member.funktionen;
			    }
			    return labelObject;
			});
		    });

		};
		var errorCallback = function() {
		    flash.setMessage({
			'type' : 'error',
			'text' : 'The member could not be found.'
		    });
		    $location.path("/Members");
		};
		MemberResource.get({
		    MemberId : $routeParams.MemberId
		}, successCallback, errorCallback);
	    };

	    $scope.isClean = function() {
		return angular.equals(self.original, $scope.member);
	    };

	    $scope.filteredMembers = function(filter) {
		var results = MemberResource.filtered({
		    "filter" : filter
		});
		return results.$promise;
	    };

	    $scope.siblings = function() {
		return MemberResource(self.original).siblings().$promise;
	    };

	    $scope.parents = function() {
		return MemberResource(self.original).parents().$promise;
	    };

	    $scope.save = function() {
		var successCallback = function() {
		    flash.setMessage({
			'type' : 'success',
			'text' : 'The member was updated successfully.'
		    }, true);
		    $scope.get();
		};
		var errorCallback = function(response) {
		    if (response && response.data && response.data.message) {
			flash.setMessage({
			    'type' : 'error',
			    'text' : response.data.message
			}, true);
		    } else {
			flash.setMessage({
			    'type' : 'error',
			    'text' : 'Something broke. Retry, or cancel and start afresh.'
			}, true);
		    }
		};
		$scope.member.$update(successCallback, errorCallback);
	    };

	    $scope.cancel = function() {
		$location.path("/Members");
	    };

	    $scope.remove = function() {
		var successCallback = function() {
		    flash.setMessage({
			'type' : 'error',
			'text' : 'The member was deleted.'
		    });
		    $location.path("/Members");
		};
		var errorCallback = function(response) {
		    if (response && response.data && response.data.message) {
			flash.setMessage({
			    'type' : 'error',
			    'text' : response.data.message
			}, true);
		    } else {
			flash.setMessage({
			    'type' : 'error',
			    'text' : 'Something broke. Retry, or cancel and start afresh.'
			}, true);
		    }
		};
		$scope.member.$remove(successCallback, errorCallback);
	    };

	    $scope.geschlechtList = [ "W", "M", "X" ];
	    $scope.rolleList = [ "Scout", "Leader", "Assistant", "Gilde", "Support", "undef" ];
	    $scope.$watch("truppSelection", function(selection) {
		if (typeof selection != 'undefined') {
		    $scope.member.trupp = {};
		    $scope.member.trupp.id = selection.value;
		}
	    });
	    $scope.$watch("VollzahlerSelection", function(selection) {
		if (typeof selection != 'undefined') {
		    $scope.member.Vollzahler = {};
		    $scope.member.Vollzahler.id = selection.value;
		}
	    });
	    $scope.funktionenSelection = $scope.funktionenSelection || [];
	    $scope.$watch("funktionenSelection", function(selection) {
		if (typeof selection != 'undefined' && $scope.member) {
		    $scope.member.funktionen = [];
		    $.each(selection, function(idx, selectedItem) {
			var collectionItem = {};
			collectionItem.id = selectedItem.value;
			$scope.member.funktionen.push(collectionItem);
		    });
		}
	    });
	    $scope.siblingsSelection = $scope.siblingsSelection || [];
	    $scope.$watch("siblingsSelection", function(selection) {
		if (typeof selection != 'undefined' && $scope.member) {
		    $scope.member.siblings = [];
		    $.each(selection, function(idx, selectedItem) {
			var collectionItem = {};
			collectionItem.id = selectedItem.value;
			$scope.member.siblings.push(collectionItem);
		    });
		}
	    });
	    $scope.paymentsSelection = $scope.paymentsSelection || [];
	    $scope.$watch("paymentsSelection", function(selection) {
		if (typeof selection != 'undefined' && $scope.member) {
		    $scope.member.payments = [];
		    $.each(selection, function(idx, selectedItem) {
			var collectionItem = {};
			collectionItem.id = selectedItem.value;
			$scope.member.payments.push(collectionItem);
		    });
		}
	    });
	    $scope.bookingsSelection = $scope.bookingsSelection || [];
	    $scope.$watch("bookingsSelection", function(selection) {
		if (typeof selection != 'undefined' && $scope.member) {
		    $scope.member.bookings = [];
		    $.each(selection, function(idx, selectedItem) {
			var collectionItem = {};
			collectionItem.id = selectedItem.value;
			$scope.member.bookings.push(collectionItem);
		    });
		}
	    });
	    $scope.aktivList = [ "true", "false" ];
	    $scope.aktivExternList = [ "true", "false" ];
	    $scope.trailList = [ "true", "false" ];
	    $scope.gildeList = [ "true", "false" ];
	    $scope.altERList = [ "true", "false" ];
	    $scope.infoMailList = [ "true", "false" ];
	    $scope.supportList = [ "true", "false" ];
	    $scope.freeList = [ "true", "false" ];

	    $scope.get();
	});