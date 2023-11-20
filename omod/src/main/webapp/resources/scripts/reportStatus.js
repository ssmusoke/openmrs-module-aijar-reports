var reportinguiApp = angular.module('ugandaemrreports');

reportinguiApp.controller('ReportStatusCtrl', [ '$scope', '$timeout', '$resource', '$location', function($scope, $timeout, $resource, $location) {

    var resource = $resource("/" + OPENMRS_CONTEXT_PATH  + "/ws/rest/v1/reportingrest/reportRequest/:uuid", { });

    $scope.loading = true;

    $scope.checkStatus = function(uuid) {
        if (uuid) {
            $scope.uuid = uuid;
        } else {
            uuid = $scope.uuid;
        }
        $scope.reportRequest = resource.get({ uuid: uuid }, function(req) {
            if (req.status != 'COMPLETED' && req.status != 'SAVED' && req.status != 'FAILED' && req.status != 'SCHEDULE_COMPLETED') {
                $timeout($scope.checkStatus, 3000);
            }
        });
        $scope.loading = false;
    }

    $scope.canDownload = function() {
        return $scope.reportRequest.status == 'COMPLETED' || $scope.reportRequest.status == 'SAVED';
    }

    $scope.download = function() {
        location.href = '/' + OPENMRS_CONTEXT_PATH + '/module/reporting/reports/viewReport.form?uuid=' + $scope.reportRequest.uuid;
    }

    $scope.canSave = function() {
        return false;
        // TODO when we support marking reports as saved via REST
        // return $scope.reportRequest.status == 'COMPLETED';
    }

    $scope.save = function() {
        // TODO when we support marking reports as saved via REST
    }

}]);