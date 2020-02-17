<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("reportingui", "runReport.css")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("reportingui", "runReport.js")

    def interactiveClass = context.loadClass("org.openmrs.module.reporting.report.renderer.InteractiveReportRenderer")
    def isInteractive = {
        interactiveClass.isInstance(it.renderer)
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        <% if (breadcrumb) { %>
        ${ breadcrumb },
        <% } else { %>
        {
            label: "${ ui.escapeJs(ui.message("reportingui.reportsapp.home.title")) }",
            link: emr.pageLink("reportingui", "reportsapp/home")
        },
        <% } %>
        {label: "${ ui.message(ui.format(reportDefinition)) }", link: "${ ui.escapeJs(ui.thisUrl()) }"}
    ];


    window.reportDefinition = {
        uuid: '${ reportDefinition.uuid}'
    };

    if (jQuery) {
        function sendDataToDHIS2(reportuuid) {
            jQuery.ajax({
                url: emr.fragmentActionLink("ugandaemrreports", "sendReportRequest", "post", {request: reportuuid}),
                dataType: 'text',
                type: 'POST',
            }).success(function (data) {
                var responseData = JSON.parse(data.replace("message=", "\"message\":").replace("responseCode=","\"responseCode\":").trim());
                jq('#edit-preview-report-form').modal('hide');
                if ((responseData.responseCode === 200 || responseData.responseCode === 201)) {
                    responseMessage = "Data Sent Succesfully " + " Imported: " + responseData.message.importCount.imported + " Updated: " + responseData.message.importCount.updated + " Ignored: " + responseData.message.importCount.ignored + " Deleted: " + responseData.message.importCount.deleted;
                    jq().toastmessage("showSuccessToast", responseMessage);
                }else {
                    jq().toastmessage("showErrorToast",responseCode);
                }
            }).complete(function (data) {

            }).error(function (data) {

            });
        }

        function previewReport(reportuuid) {
            jQuery.ajax({
                url: emr.fragmentActionLink("ugandaemrreports", "sendReportRequest", "previewReport", {request: reportuuid}),
                dataType: 'text',
                type: 'POST'
            }).success(function (data) {

                var responsedata =JSON.parse(data.replace("data=", "\"data\":").replace("x-","x"));
                displayReport(responsedata,reportuuid);
                jq('#edit-preview-report-form').modal('show');
            })
        }

        function displayReport(report,reportuuid) {
            var reportDataString="";
            var tableHeader = "<table><thead><tr><th>Indicator</th><th>Data Element</th><th>Value</th></thead><tbody>";
            var tableFooter = "</tbody></table>";
            var rowspan="";
            var rowspanCount=0;
            jq.each(report.data.dataValues, function (index, dataValue) {
                var dataValueToDisplay = "";
                dataValueToDisplay += "<tr>";
                if(rowspan===dataValue.xshortname){
                    rowspanCount+=1;
                }else {
                    var rowspanAttribute="rowspan=\""+rowspanCount+"\"";
                    reportDataString = reportDataString.replace("rowspan=\"1\"",rowspanAttribute);
                    rowspanCount=1;
                    rowspan=dataValue.xshortname;
                    dataValueToDisplay += "<th rowspan=\"1\" width=\"20%\">" + dataValue.xshortname + "</th>";
                }
                dataValueToDisplay += "<td>" + dataValue.xcategoryoptioncombo + "</td>";
                dataValueToDisplay += "<td>" + dataValue.value + "</td>";
                dataValueToDisplay += "</tr>";
                reportDataString += dataValueToDisplay;
            });
            var lastRowspanAttribute="rowspan=\""+rowspanCount+"\"";
            reportDataString = reportDataString.replace("rowspan=\"1\"",lastRowspanAttribute);
            jq("#display-report").append(tableHeader + reportDataString + tableFooter);
            jq("#sendreporttodhis2").attr("onclick", "sendDataToDHIS2(\""+reportuuid+"\")");
        }

        function transpose() {
            jq("table").each(function() {
                var tableToTranspose = jq(this);
                var newrows = [];
                tableToTranspose.find("tr").each(function () {
                    var i = 0;
                    jq(this).find("td").each(function () {
                        i++;
                        if (newrows[i] === undefined) {
                            newrows[i] = jq("<tr></tr>");
                        }
                        newrows[i].append(jq(this));
                    });
                });
                tableToTranspose.find("tr").remove();
                jq.each(newrows, function () {
                    tableToTranspose.append(this);
                });
            })
        }
    }

</script>

${ui.includeFragment("appui", "messages", [codes: [
        "reportingui.reportRequest.Status.REQUESTED", "reportingui.reportRequest.Status.SCHEDULED", "reportingui.reportRequest.Status.PROCESSING",
        "reportingui.reportRequest.Status.FAILED", "reportingui.reportRequest.Status.COMPLETED", "reportingui.reportRequest.Status.SCHEDULE_COMPLETED",
        "reportingui.reportRequest.Status.SAVED",
        "reportingui.reportRequest.Priority.HIGHEST", "reportingui.reportRequest.Priority.HIGH", "reportingui.reportRequest.Priority.NORMAL",
        "reportingui.reportRequest.Priority.LOW", "reportingui.reportRequest.Priority.LOWEST",
        "reportingui.runReport.missingParameter", "reportingui.reportRequest.send.action"]
])}

<div ng-app="runReportApp" ng-controller="RunReportController" ng-init="refreshHistory()">

    <h1>${ui.message(reportDefinition.name)}</h1>

    <h3>${ui.message(reportDefinition.description)}</h3>

    <div class="past-reports">
        <fieldset ng-show="queue" class="report-list">
            <legend>${ui.message("reportingui.runReport.queue.legend")}</legend>
            <table>
                <thead>
                <tr>
                    <th>${ui.message("reportingui.reportRequest.status")}</th>
                    <th>${ui.message("reportingui.reportRequest.parameters")}</th>
                    <th>${ui.message("reportingui.reportRequest.requested")}</th>
                    <th>${ui.message("reportingui.reportRequest.actions")}</th>
                </tr>
                </thead>
                <tbody>
                <tr ng-repeat="request in queue">
                    <td>
                        <img class="right small" ng-show="request.status=='PROCESSING'"
                             src="${ui.resourceLink("uicommons", "images/spinner.gif")}"/>
                        {{request.status | translate:'reportingui.reportRequest.Status.'}}
                        <span ng-show="request.status=='REQUESTED'">
                            <br/>
                            ${ui.message("reportingui.reportRequest.priority")}: {{request.priority | translate:'reportingui.ReportRequest.Priority.'}} <br/>
                            ${ui.message("reportingui.reportRequest.position")}: {{request.positionInQueue}}
                        </span>
                        <span ng-hide="request.status=='REQUESTED'">
                            <br/>
                            {{request.evaluateCompleteDatetime}}
                        </span>
                    </td>
                    <td>
                        <span ng-repeat="param in request.reportDefinition.mappings">
                            {{ param.value }} <br/>
                        </span>
                    </td>
                    <td>
                        {{request.requestedBy}} <br/>
                        {{request.requestDate}}
                    </td>
                    <td>
                        <a ng-show="request.status=='REQUESTED'" ng-click="cancelRequest(request)">
                            <i class="small icon-remove"></i>
                            ${ui.message("emr.cancel")}
                        </a>
                    </td>
                </tr>
                </tbody>
            </table>
        </fieldset>

        <fieldset class="report-list">
            <legend>${ui.message("reportingui.runReport.completed.legend")}</legend>

            <span ng-show="loading">
                ${ui.message("uicommons.loading.placeholder")}
                <img src="${ui.resourceLink("uicommons", "images/spinner.gif")}"/>
            </span>

            <span ng-show="hasNoResults()">
                ${ui.message("emr.none")}
            </span>

            <table ng-show="hasResults()">
                <thead>
                <tr>
                    <th>${ui.message("reportingui.reportRequest.status")}</th>
                    <th>${ui.message("reportingui.reportRequest.parameters")}</th>
                    <th>${ui.message("reportingui.reportRequest.requested")}</th>
                    <th>${ui.message("reportingui.reportRequest.actions")}</th>
                </tr>
                </thead>
                <tbody>
                <tr ng-repeat="request in completed">
                    <td>
                        {{request.status | translate:'reportingui.reportRequest.Status.'}} <br/>
                        {{request.evaluateCompleteDatetime}}
                    </td>
                    <td>
                        <span ng-repeat="param in request.reportDefinition.mappings">
                            {{ param.value }} <br/>
                        </span>
                    </td>
                    <td>
                        {{request.requestedBy}} <br/>
                        {{request.requestDate}}
                    </td>
                    <td>
                        <span class="download" ng-show="request.status == 'COMPLETED' || request.status == 'SAVED'">
                            <a href="${ui.pageLink("reportingui", "viewReportRequest")}?request={{ request.uuid }}">
                                <span ng-show="request.renderingMode.interactive">
                                    <i class="icon-eye-open small"></i>
                                    ${ui.message("reportingui.reportHistory.open")}
                                </span>
                                <span ng-hide="request.renderingMode.interactive">
                                    <i class="icon-download small"></i>
                                    ${ui.message("uicommons.downloadButtonLabel")}
                                </span>
                            </a>
                            <br/>
                            <a ng-show="canSave(request)" ng-click="saveRequest(request)">
                                <i class="icon-save small"></i>
                                ${ui.message("reportingui.reportRequest.save.action")}
                            </a>
                            <br/>
                            <a id="{{ request.uuid }}" onclick="previewReport(this.id)">
                                <span ng-hide="request.renderingMode.interactive">
                                    <i class="icon-upload small"></i>
                                    To DHIS2
                                </span>
                            </a>
                        </span>
                    </td>
                </tr>
                </tbody>
            </table>
        </fieldset>
    </div>

    <div class="running-reports">
        <fieldset>
            <legend>${ui.message("reportingui.runReport.run.legend")}</legend>

            <form method="post" ng-submit="runReport()" id="run-report">
                <% reportDefinition.parameters.each { %>
                <p>
                    <% if (it.collectionType) { %>
                    Parameters of type = collection are not yet implemented
                    <% } else if (it?.widgetConfiguration?.uiframeworkFragmentProvider) { %>
                    ${
                            ui.includeFragment(it.widgetConfiguration.uiframeworkFragmentProvider, it.widgetConfiguration.uiframeworkFragment, [
                                    formFieldName: "parameterValues[" + it.name + "]",
                                    label        : it.labelOrName
                            ])}
                    <% } else if (it.type == java.util.Date) { %>
                    ${ui.includeFragment("uicommons", "field/datetimepicker", [
                            formFieldName: "parameterValues[" + it.name + "]",
                            label        : it.labelOrName,
                            useTime      : false,
                            defaultDate  : it.defaultValue
                    ])}
                    <% } else if (it.type == org.openmrs.Location) { %>
                    ${ui.includeFragment("uicommons", "field/location", [
                            formFieldName: "parameterValues[" + it.name + "]",
                            label        : it.labelOrName,
                            initialValue : it.defaultValue ?: sessionContext.sessionLocation
                    ])}
                    <% } else { %>
                    Unknown parameter type: ${it.type}
                    <% } %>
                </p>
                <% } %>
                <%
                    def renderingOptions = renderingModes.findAll {
                        !isInteractive(it)
                    }
                            .collect {
                                [value: it.descriptor, label: ui.message(it.label)]
                            }
                %>
                <% if (renderingOptions.size() == 1) { %>
                <p>
                    <label>
                        ${ui.message("reportingui.reportRequest.outputFormat")}
                    </label>
                    <input type="hidden" name="renderingMode" value="${ui.escapeAttribute(renderingOptions[0].value)}"/>
                    ${renderingOptions[0].label}
                </p>
                <% } else { %>
                ${ui.includeFragment("uicommons", "field/dropDown", [
                        formFieldName: "renderingMode",
                        label: ui.message("reportingui.reportRequest.outputFormat"),
                        hideEmptyLabel: true,
                        options: renderingOptions
                ])}
                <% } %>

                <button type="submit" class="confirm right" ng-class="{disabled: submitting}" ng-disabled="submitting">
                    <i class="icon-play"></i>
                    ${ui.message("reportingui.runButtonLabel")}
                </button>
            </form>
        </fieldset>
    </div>

    <div id="edit-preview-report-form" title="preview-report" class="modal fade bd-order-modal-lg" style="">
        <div class="modal-dialog modal-lg" role="document">
            <div class="modal-content">
                <div class="modal-header">
                    <h3 style="color: #FFFFFF">${ui.message("Preview Report")}</h3>
                </div>

                <div class="modal-body">
                    <div id="display-report"></div>
                </div>

                <div class="modal-footer">
                    <button class="cancel" data-dismiss="modal" id="">Cancel</button>
                    <span id="sendreporttodhis2" onclick="sendDataToDHIS2(this.id)" class="button confirm right">Send To DHIS2</span>
                </div>
            </div>
        </div>
    </div>

</div>