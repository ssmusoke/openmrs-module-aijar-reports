<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "moment.min.js")
    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-sanitize.min.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "filters/display.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")
    ui.includeJavascript("uicommons", "services/encounterTypeService.js")
    ui.includeJavascript("uicommons", "services/locationService.js")

    ui.includeJavascript("reportingui", "app.js")
    ui.includeJavascript("reportingui", "reportStatus.js")
%>

<%= ui.includeFragment("appui", "messages", [ codes: [
        "reportingui.reportRequest.Status.REQUESTED",
        "reportingui.reportRequest.Status.SCHEDULED",
        "reportingui.reportRequest.Status.PROCESSING",
        "reportingui.reportRequest.Status.FAILED",
        "reportingui.reportRequest.Status.COMPLETED",
        "reportingui.reportRequest.Status.SCHEDULE_COMPLETED",
        "reportingui.reportRequest.Status.SAVED",
        "reportingui.outputFormat.CsvReportRenderer",
].flatten()
]) %>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.message("reportingui.reportsapp.home.title")) }", link: emr.pageLink("reportingui", "reportsapp/home") },
        { label: "${ ui.message("reportingui.reportHistory.title") }", link: emr.pageLink("reportingui", "reportHistory") },
        { label: "${ ui.escapeJs(ui.message("reportingui.reportStatus.title")) }" }
    ];
</script>

<div ng-app="reportingui" ng-controller="ReportStatusCtrl" ng-init="checkStatus('${ ui.escapeJs(param.request[0]) }')">

    <div ng-show="loading">
        ${ ui.message("uicommons.loading.placeholder") }
        <img src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
    </div>

    <div ng-hide="loading">
        <fieldset>
            <legend>${ ui.message("reportingui.reportStatus.requestHeading") }</legend>

            <p>
                ${ ui.message("reportingui.reportStatus.requestedBy") }: {{ reportRequest.requestedBy | omrsDisplay }}
            </p>
            <p>
                ${ ui.message("reportingui.reportStatus.requestDate") }: {{ reportRequest.requestDate | date:'medium'}}
            </p>
            <p>
                ${ ui.message("reportingui.reportRequest.outputFormat") }: {{ reportRequest.renderingMode.label | omrsDisplay:'reportingui.outputFormat.' }}
            </p>
        </fieldset>

        <fieldset>
            <legend>Status</legend>

            <span ng-show="reportRequest.status == 'REQUESTED'">
                <i class="icon-spinner"></i>
            </span>
            <span ng-show="reportRequest.status == 'SCHEDULED'">
                <i class="icon-time"></i>
            </span>
            <span ng-show="reportRequest.status == 'SCHEDULE_COMPLETED'">
                <i class="icon-time"></i>
                <i class="icon-ok"></i>
            </span>
            <span ng-show="reportRequest.status == 'PROCESSING'">
                <img src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
                <i class="icon-reorder"></i>
            </span>
            <span ng-show="reportRequest.status == 'FAILED'">
                <i class="icon-exclamation-sign"></i>
            </span>
            <span ng-show="reportRequest.status == 'COMPLETED'">
                <i class="icon-ok"></i>
            </span>
            <span ng-show="reportRequest.status == 'SAVED'">
                <i class="icon-ok"></i>
                <i class="icon-save"></i>
            </span>

            {{ reportRequest.status | omrsDisplay:'reportingui.reportRequest.Status.' }}

            <p ng-show="canDownload()">
                <button ng-click="download()">
                    <i class="icon-download-alt"></i>
                    ${ ui.message("uicommons.downloadButtonLabel") }
                </button>
            </p>

            <p ng-show="canSave()">
                <button ng-click=""save()">
                    <i class="icon-save"></i>
                    ${ ui.message("uicommons.save") }
                </button>
            </p>
        </fieldset>
    </div>

</div>

