

angular.module('pfad').controller('SearchPaymentController', function($scope, $http, $filter, PaymentResource , MemberResource, BookingResource) {

    $scope.search={};
    $scope.currentPage = 0;
    $scope.pageSize= 10;
    $scope.searchResults = [];
    $scope.filteredResults = [];
    $scope.pageRange = [];
    $scope.numberOfPages = function() {
        var result = Math.ceil($scope.filteredResults.length/$scope.pageSize);
        var max = (result == 0) ? 1 : result;
        $scope.pageRange = [];
        for(var ctr=0;ctr<max;ctr++) {
            $scope.pageRange.push(ctr);
        }
        return max;
    };
    $scope.payerList = MemberResource.queryAll();
    $scope.finishedList = [
        "true",
        "false"
    ];
    $scope.acontoList = [
        "true",
        "false"
    ];
    $scope.typeList = [
        "Membership",
        "Camp",
        "Donation",
        "Advert"
    ];

    $scope.performSearch = function() {
        $scope.searchResults = PaymentResource.queryAll(function(){
            $scope.filteredResults = $filter('searchFilter')($scope.searchResults, $scope);
            $scope.currentPage = 0;
        });
    };
    
    $scope.previous = function() {
       if($scope.currentPage > 0) {
           $scope.currentPage--;
       }
    };
    
    $scope.next = function() {
       if($scope.currentPage < ($scope.numberOfPages() - 1) ) {
           $scope.currentPage++;
       }
    };
    
    $scope.setPage = function(n) {
       $scope.currentPage = n;
    };

    $scope.performSearch();
});