var runReportApp = angular.module("runReport", [ ]).
    filter('translate', function() {
        return function(input, prefix) {
            var code = prefix ? prefix + input : input;
            return emr.message(code, input);
        }
    });

runReportApp.controller('RunCohortReportFragmentController', ['$scope', '$http', '$window', '$timeout', function($scope, $http, $window, $timeout) {

    $scope.loading = true;

    $scope.queue = [];

    $scope.completed = [];

    $scope.submitting = true;

    $scope.refreshHistory = function() {
        $http.get("reportStatus/getQueuedRequests.action?reportDefinition=" + $window.reportDefinition.uuid).
            success(function(data, status, headers, config) {
                $scope.queue = data;
                $scope.loading = false;
                if ($scope.queue.length > 0) {
                    $timeout($scope.refreshHistory, 10000);
                }
            }).
            error(function(data, status, headers, config) {
                console.log("Error getting queue: " + status);
                $scope.queue = [];
            });

        $http.get("reportStatus/getCompletedRequests.action?reportDefinition=" + $window.reportDefinition.uuid).
            success(function(data, status, headers, config) {
                $scope.completed = data;
            }).
            error(function(data, status, headers, config) {
                console.log("Error getting completed: " + status);
                $scope.completed = [];
            });
    }

    $scope.hasResults = function() {
        return !$scope.loading && $scope.completed.length > 0;
    }

    $scope.hasNoResults = function() {
        return !$scope.loading && $scope.completed.length == 0;
    }

    var defaultSuccessAction = function(data, status, headers, config) {
        emr.successMessage(data.message);
        $scope.refreshHistory();
    }

    var defaultErrorAction = function(data, status, headers, config) {
        emr.errorMessage(data.message);
        $scope.refreshHistory();
    }

    $scope.cancelRequest = function(request) {
        $http.post("reportStatus/cancelRequest.action?reportRequest=" + request.uuid).
            success(defaultSuccessAction).
            error(defaultErrorAction);
    }

    $scope.canSave = function(request) {
        return request.status == 'COMPLETED';
    }

    $scope.saveRequest = function(request) {
        $http.post("reportStatus/saveRequest.action?reportRequest=" + request.uuid).
            success(defaultSuccessAction).
            error(defaultErrorAction);
    }


    $scope.runMyReport = function() {
        var form = angular.element('#runreport');
        var submission = form.serializeArray();
        var missingParams = [ ];
        console.log(submission);
        for (var i = 0; i < submission.length; ++i) {
            var p = submission[i];
            if (p.name.indexOf("parameterValues[") == 0) {
                if (!p.value) {
                    missingParams.push(p.name);
                }
            }
        }
        if (missingParams.length > 0) {
            emr.errorMessage(emr.message("reportingui.runReport.missingParameter", "Missing parameter values"));
        }
        else {
            $scope.submitting = true;
            console.log(submission);
            // $.post("runReport.page?reportDefinition=" + $window.reportDefinition.uuid, submission, function() {
            //     location.href = location.href;
            // });
        }

    }

}]);