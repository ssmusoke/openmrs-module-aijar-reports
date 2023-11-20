<%
    ui.includeCss("reportingui", "runReport.css")
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("ugandaemrreports", "runReport.js")

    def interactiveClass = context.loadClass("org.openmrs.module.reporting.report.renderer.InteractiveReportRenderer")
    def isInteractive = {
        interactiveClass.isInstance(it.renderer)
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        <% if (breadcrumb) { %>
        ${ breadcrumb },
        <% } else { %>
        { label: "${ ui.escapeJs(ui.message("reportingui.reportsapp.home.title")) }", link: emr.pageLink("reportingui", "reportsapp/home") },
        <% } %>
        { label: "${ ui.message(ui.format(reportDefinition)) }", link: "${ ui.escapeJs(ui.thisUrl()) }" }
    ];

    window.reportDefinition = {
        uuid: '${ reportDefinition.uuid}'
    };
</script>



<%

    def cohortOptions = cohorts
            .collect {
                [value: it.name, label: ui.message(it.name)]
            }
%>
<div class="row">
    <div class="col-md-12">

            <h1>${ ui.message(reportDefinition.name) }</h1>
            <h3>${ ui.message(reportDefinition.description) }</h3>

            <div class="past-reports" ng-app="runReport"  ng-controller="RunCohortReportFragmentController" ng-init="refreshHistory()">
                <fieldset ng-show="queue" class="report-list">
                    <legend>${ ui.message("reportingui.runReport.queue.legend") }</legend>
                    <table>
                        <thead>
                        <tr>
                            <th>${ ui.message("reportingui.reportRequest.status") }</th>
                            <th>${ ui.message("reportingui.reportRequest.parameters") }</th>
                            <th>${ ui.message("reportingui.reportRequest.requested") }</th>
                            <th>${ ui.message("reportingui.reportRequest.actions") }</th>
                        </tr>
                        </thead>
                        <tbody>
                        <tr ng-repeat="request in queue">
                            <td>
                                <img class="right small" ng-show="request.status=='PROCESSING'" src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
                                {{request.status | translate:'reportingui.reportRequest.Status.'}}
                                <span ng-show="request.status=='REQUESTED'">
                                    <br/>
                                    ${ ui.message("reportingui.reportRequest.priority") }: {{request.priority | translate:'reportingui.ReportRequest.Priority.'}} <br/>
                                    ${ ui.message("reportingui.reportRequest.position") }: {{request.positionInQueue}}
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
                                    ${ ui.message("emr.cancel") }
                                </a>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </fieldset>

                <fieldset class="report-list">
                    <legend>${ ui.message("reportingui.runReport.completed.legend") }</legend>

                    <span ng-show="loading">
                        ${ ui.message("uicommons.loading.placeholder") }
                        <img src="${ ui.resourceLink("uicommons", "images/spinner.gif") }"/>
                    </span>

                    <span ng-show="hasNoResults()">
                        ${ ui.message("emr.none") }
                    </span>

                    <table ng-show="hasResults()">
                        <thead>
                        <tr>
                            <th>${ ui.message("reportingui.reportRequest.status") }</th>
                            <th>${ ui.message("reportingui.reportRequest.parameters") }</th>
                            <th>${ ui.message("reportingui.reportRequest.requested") }</th>
                            <th>${ ui.message("reportingui.reportRequest.actions") }</th>
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
                                    <a href="${ ui.pageLink("reportingui", "viewReportRequest") }?request={{ request.uuid }}">
                                        <span ng-show="request.renderingMode.interactive">
                                            <i class="icon-eye-open small"></i>
                                            ${ ui.message("reportingui.reportHistory.open") }
                                        </span>
                                        <span ng-hide="request.renderingMode.interactive">
                                            <i class="icon-download small"></i>
                                            ${ ui.message("uicommons.downloadButtonLabel") }
                                        </span>
                                    </a>
                                    <br/>
                                    <a ng-show="canSave(request)" ng-click="saveRequest(request)">
                                        <i class="icon-save small"></i>
                                        ${ ui.message("reportingui.reportRequest.save.action") }
                                    </a>
                                    <br/>
                                </span>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </fieldset>
            </div>

        <div class="running-reports" >
            <fieldset>
                <legend>${ ui.message("reportingui.runReport.run.legend") }</legend>

                <form method="post" ng-submit="runMyReport()" id="runreport">

                    ${ui.includeFragment("uicommons", "field/dropDown", [
                            formFieldName : "cohort",
                            label         : "Cohort",
                            hideEmptyLabel: false,
                            options       : cohortOptions

                    ])}

                    <% reportDefinition.parameters.each { %>
                    <p>
                        <% if (it.collectionType) { %>
                        Parameters of type = collection are not yet implemented
                        <% } else if (it?.widgetConfiguration?.uiframeworkFragmentProvider) { %>
                        ${ ui.includeFragment(it.widgetConfiguration.uiframeworkFragmentProvider, it.widgetConfiguration.uiframeworkFragment, [
                                formFieldName: "parameterValues[" + it.name + "]",
                                label: it.labelOrName
                        ])}
                        <% } else if (it.type == java.util.Date) { %>
                        ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                formFieldName: "parameterValues[" + it.name + "]",
                                label: it.labelOrName,
                                useTime: false,
                                defaultDate: it.defaultValue
                        ])}
                        <% } else if (it.type == org.openmrs.Location) { %>
                        ${ ui.includeFragment("uicommons", "field/location", [
                                formFieldName: "parameterValues[" + it.name + "]",
                                label: it.labelOrName,
                                initialValue: it.defaultValue ?: sessionContext.sessionLocation
                        ])}
                        <% } else if (it.type == java.lang.String) { %>
                        <% } else { %>
                        Unknown parameter type: ${ it.type }
                        <% } %>
                    </p>
                    <% } %>

                    <%
                        def renderingOptions = renderingModes.findAll {
                            !isInteractive(it)
                        }
                                .collect {
                                    [ value: it.descriptor, label: ui.message(it.label) ]
                                }
                    %>
                    <% if (renderingOptions.size() == 1) { %>
                    <p>
                        <label>
                            ${ ui.message("reportingui.reportRequest.outputFormat") }
                        </label>
                        <input type="hidden" name="renderingMode" value="${ ui.escapeAttribute(renderingOptions[0].value) }"/>
                        ${ renderingOptions[0].label }
                    </p>
                    <% } else { %>
                    ${ ui.includeFragment("uicommons", "field/dropDown", [
                            formFieldName: "renderingMode",
                            label: ui.message("reportingui.reportRequest.outputFormat"),
                            hideEmptyLabel: true,
                            options: renderingOptions
                    ]) }
                    <% } %>

                    <button type="submit" class="confirm right" ng-class="{disabled: submitting}" ng-disabled="submitting">
                        <i class="icon-play"></i>
                        ${ ui.message("reportingui.runButtonLabel") }
                    </button>
                </form>
            </fieldset>
        </div>
    </div>
</div>

