

angular.module('pfad').controller('EditActivityController', function($scope, $routeParams, $location, flash, ActivityResource , BookingResource) {
    var self = this;
    $scope.disabled = false;
    $scope.$location = $location;
    
    $scope.get = function() {
        var successCallback = function(data){
            self.original = data;
            $scope.activity = new ActivityResource(self.original);
            BookingResource.queryAll(function(items) {
                $scope.bookingsSelectionList = $.map(items, function(item) {
                    var wrappedObject = {
                        id : item.id
                    };
                    var labelObject = {
                        value : item.id,
                        text : item.id
                    };
                    if($scope.activity.bookings){
                        $.each($scope.activity.bookings, function(idx, element) {
                            if(item.id == element.id) {
                                $scope.bookingsSelection.push(labelObject);
                                $scope.activity.bookings.push(wrappedObject);
                            }
                        });
                        self.original.bookings = $scope.activity.bookings;
                    }
                    return labelObject;
                });
            });
        };
        var errorCallback = function() {
            flash.setMessage({'type': 'error', 'text': 'The activity could not be found.'});
            $location.path("/Activities");
        };
        ActivityResource.get({ActivityId:$routeParams.ActivityId}, successCallback, errorCallback);
    };

    $scope.isClean = function() {
        return angular.equals(self.original, $scope.activity);
    };

    $scope.save = function() {
        var successCallback = function(){
            flash.setMessage({'type':'success','text':'The activity was updated successfully.'}, true);
            $scope.get();
        };
        var errorCallback = function(response) {
            if(response && response.data && response.data.message) {
                flash.setMessage({'type': 'error', 'text': response.data.message}, true);
            } else {
                flash.setMessage({'type': 'error', 'text': 'Something broke. Retry, or cancel and start afresh.'}, true);
            }
        };
        $scope.activity.$update(successCallback, errorCallback);
    };

    $scope.cancel = function() {
        $location.path("/Activities");
    };

    $scope.remove = function() {
        var successCallback = function() {
            flash.setMessage({'type': 'error', 'text': 'The activity was deleted.'});
            $location.path("/Activities");
        };
        var errorCallback = function(response) {
            if(response && response.data && response.data.message) {
                flash.setMessage({'type': 'error', 'text': response.data.message}, true);
            } else {
                flash.setMessage({'type': 'error', 'text': 'Something broke. Retry, or cancel and start afresh.'}, true);
            }
        }; 
        $scope.activity.$remove(successCallback, errorCallback);
    };
    
    $scope.bookingsSelection = $scope.bookingsSelection || [];
    $scope.$watch("bookingsSelection", function(selection) {
        if (typeof selection != 'undefined' && $scope.activity) {
            $scope.activity.bookings = [];
            $.each(selection, function(idx,selectedItem) {
                var collectionItem = {};
                collectionItem.id = selectedItem.value;
                $scope.activity.bookings.push(collectionItem);
            });
        }
    });
    $scope.typeList = [
        "Membership",  
        "Camp",  
        "Other"  
    ];
    $scope.statusList = [
        "planned",  
        "started",  
        "finished",  
        "cancelled"  
    ];
    
    $scope.get();
});