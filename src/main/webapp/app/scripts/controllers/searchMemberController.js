

angular.module('pfad').controller('SearchMemberController', function($scope, $http, $filter, MemberResource , SquadResource, MemberResource, FunctionResource, MemberResource, PaymentResource, BookingResource) {

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
    $scope.geschlechtList = [
        "W",
        "M",
        "X"
    ];
    $scope.rolleList = [
        "Scout",
        "Leader",
        "Assistant",
        "Gilde",
        "Support",
        "undef"
    ];
    $scope.truppList = SquadResource.queryAll();
    $scope.VollzahlerList = MemberResource.queryAll();
    $scope.aktivList = [
        "true",
        "false"
    ];
    $scope.aktivExternList = [
        "true",
        "false"
    ];
    $scope.trailList = [
        "true",
        "false"
    ];
    $scope.gildeList = [
        "true",
        "false"
    ];
    $scope.altERList = [
        "true",
        "false"
    ];
    $scope.infoMailList = [
        "true",
        "false"
    ];
    $scope.supportList = [
        "true",
        "false"
    ];
    $scope.freeList = [
        "true",
        "false"
    ];

    $scope.performSearch = function() {
        $scope.searchResults = MemberResource.queryAll(function(){
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