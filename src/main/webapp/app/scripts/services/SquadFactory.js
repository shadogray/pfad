angular.module('pfad').factory('SquadResource', function($resource){
    var resource = $resource('../rest/squads/:SquadId',{SquadId:'@id'},{'queryAll':{method:'GET',isArray:true},'query':{method:'GET',isArray:false},'update':{method:'PUT'}});
    return resource;
});