
angular.module('pfad').controller('NewMemberController', function ($scope, $location, locationParser, flash, MemberResource, SquadResource, MemberResource,
        FunctionResource, MemberResource, PaymentResource, BookingResource) {
   $scope.disabled = false;
   $scope.$location = $location;
   $scope.member = $scope.member || {};

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

   $scope.truppList = SquadResource.queryAll(function (items) {
      $scope.truppSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("truppSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.trupp = {};
         $scope.member.trupp.id = selection.value;
      }
   });

   $scope.VollzahlerList = MemberResource.queryAll(function (items) {
      $scope.VollzahlerSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("VollzahlerSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.Vollzahler = {};
         $scope.member.Vollzahler.id = selection.value;
      }
   });

   $scope.funktionenList = FunctionResource.queryAll(function (items) {
      $scope.funktionenSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("funktionenSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.funktionen = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.member.funktionen.push(collectionItem);
         });
      }
   });

   $scope.siblingsList = MemberResource.queryAll(function (items) {
      $scope.siblingsSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("siblingsSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.siblings = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.member.siblings.push(collectionItem);
         });
      }
   });

   $scope.paymentsList = PaymentResource.queryAll(function (items) {
      $scope.paymentsSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("paymentsSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.payments = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.member.payments.push(collectionItem);
         });
      }
   });

   $scope.bookingsList = BookingResource.queryAll(function (items) {
      $scope.bookingsSelectionList = $.map(items, function (item) {
         return ({
            value: item.id,
            text: item.id
         });
      });
   });
   $scope.$watch("bookingsSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.member.bookings = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.member.bookings.push(collectionItem);
         });
      }
   });

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


   $scope.save = function () {
      var successCallback = function (data, responseHeaders) {
         var id = locationParser(responseHeaders);
         flash.setMessage({
            'type': 'success',
            'text': 'The member was created successfully.'
         });
         $location.path('/Members');
      };
      var errorCallback = function (response) {
         if (response && response.data && response.data.message) {
            flash.setMessage({
               'type': 'error',
               'text': response.data.message
            },
            true);
         } else {
            flash.setMessage({
               'type': 'error',
               'text': 'Something broke. Retry, or cancel and start afresh.'
            },
            true);
         }
      };
      MemberResource.save($scope.member, successCallback, errorCallback);
   };

   $scope.cancel = function () {
      $location.path("/Members");
   };
});