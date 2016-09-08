

angular.module('pfad').controller('EditSquadController', function ($scope, $routeParams, $location, flash, SquadResource, MemberResource, MemberResource,
        MemberResource, MemberResource) {
   var self = this;
   $scope.disabled = false;
   $scope.$location = $location;

   $scope.get = function () {
      var successCallback = function (data) {
         self.original = data;
         $scope.squad = new SquadResource(self.original);
         MemberResource.queryAll(function (items) {
            $scope.leaderMaleSelectionList = $.map(items, function (item) {
               var wrappedObject = {
                  id: item.id
               };
               var labelObject = {
                  value: item.id,
                  text: item.id
               };
               if ($scope.squad.leaderMale && item.id == $scope.squad.leaderMale.id) {
                  $scope.leaderMaleSelection = labelObject;
                  $scope.squad.leaderMale = wrappedObject;
                  self.original.leaderMale = $scope.squad.leaderMale;
               }
               return labelObject;
            });
         });
         MemberResource.queryAll(function (items) {
            $scope.leaderFemaleSelectionList = $.map(items, function (item) {
               var wrappedObject = {
                  id: item.id
               };
               var labelObject = {
                  value: item.id,
                  text: item.id
               };
               if ($scope.squad.leaderFemale && item.id == $scope.squad.leaderFemale.id) {
                  $scope.leaderFemaleSelection = labelObject;
                  $scope.squad.leaderFemale = wrappedObject;
                  self.original.leaderFemale = $scope.squad.leaderFemale;
               }
               return labelObject;
            });
         });
         MemberResource.queryAll(function (items) {
            $scope.assistantsSelectionList = $.map(items, function (item) {
               var wrappedObject = {
                  id: item.id
               };
               var labelObject = {
                  value: item.id,
                  text: item.id
               };
               if ($scope.squad.assistants) {
                  $.each($scope.squad.assistants, function (idx, element) {
                     if (item.id == element.id) {
                        $scope.assistantsSelection.push(labelObject);
                        $scope.squad.assistants.push(wrappedObject);
                     }
                  });
                  self.original.assistants = $scope.squad.assistants;
               }
               return labelObject;
            });
         });
         MemberResource.queryAll(function (items) {
            $scope.scoutsSelectionList = $.map(items, function (item) {
               var wrappedObject = {
                  id: item.id
               };
               var labelObject = {
                  value: item.id,
                  text: item.id
               };
               if ($scope.squad.scouts) {
                  $.each($scope.squad.scouts, function (idx, element) {
                     if (item.id == element.id) {
                        $scope.scoutsSelection.push(labelObject);
                        $scope.squad.scouts.push(wrappedObject);
                     }
                  });
                  self.original.scouts = $scope.squad.scouts;
               }
               return labelObject;
            });
         });
      };
      var errorCallback = function () {
         flash.setMessage({
            'type': 'error',
            'text': 'The squad could not be found.'
         });
         $location.path("/Squads");
      };
      SquadResource.get({
         SquadId: $routeParams.SquadId
      },
      successCallback, errorCallback);
   };

   $scope.isClean = function () {
      return angular.equals(self.original, $scope.squad);
   };

   $scope.save = function () {
      var successCallback = function () {
         flash.setMessage({
            'type': 'success',
            'text': 'The squad was updated successfully.'
         },
         true);
         $scope.get();
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
      $scope.squad.$update(successCallback, errorCallback);
   };

   $scope.cancel = function () {
      $location.path("/Squads");
   };

   $scope.remove = function () {
      var successCallback = function () {
         flash.setMessage({
            'type': 'error',
            'text': 'The squad was deleted.'
         });
         $location.path("/Squads");
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
      $scope.squad.$remove(successCallback, errorCallback);
   };

   $scope.typeList = [
      "WIWO",
      "GUSP",
      "CAEX",
      "RARO"
   ];
   $scope.$watch("leaderMaleSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.squad.leaderMale = {};
         $scope.squad.leaderMale.id = selection.value;
      }
   });
   $scope.$watch("leaderFemaleSelection", function (selection) {
      if (typeof selection != 'undefined') {
         $scope.squad.leaderFemale = {};
         $scope.squad.leaderFemale.id = selection.value;
      }
   });
   $scope.assistantsSelection = $scope.assistantsSelection || [];
   $scope.$watch("assistantsSelection", function (selection) {
      if (typeof selection != 'undefined' && $scope.squad) {
         $scope.squad.assistants = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.squad.assistants.push(collectionItem);
         });
      }
   });
   $scope.scoutsSelection = $scope.scoutsSelection || [];
   $scope.$watch("scoutsSelection", function (selection) {
      if (typeof selection != 'undefined' && $scope.squad) {
         $scope.squad.scouts = [];
         $.each(selection, function (idx, selectedItem) {
            var collectionItem = {};
            collectionItem.id = selectedItem.value;
            $scope.squad.scouts.push(collectionItem);
         });
      }
   });

   $scope.get();
});