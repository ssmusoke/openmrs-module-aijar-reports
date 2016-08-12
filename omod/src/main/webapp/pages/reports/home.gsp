<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("reportingui", "reportsapp/home.css")

    def appFrameworkService = context.getService(context.loadClass("org.openmrs.module.appframework.service.AppFrameworkService"))
    def overview = appFrameworkService.getExtensionsForCurrentUser("org.openmrs.module.ugandaemr.reports.overview")
    def monthly = appFrameworkService.getExtensionsForCurrentUser("org.openmrs.module.ugandaemr.reports.monthly")
    def registers = appFrameworkService.getExtensionsForCurrentUser("org.openmrs.module.ugandaemr.reports.registers")
    def quarterly = appFrameworkService.getExtensionsForCurrentUser("org.openmrs.module.ugandaemr.reports.quarterly")

    def contextModel = [:]
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("reportingui.reportsapp.home.title") }", link: "${ ui.pageLink("aijarreports",
        "reports/home")
}" }
    ];
</script>

<div class="reportBox">
    <% if (overview) { %>
        <p>${ ui.message("ugandaemr.reportsapp.overviewReports") }</p>
        <ul>
            <% overview.each { %>
                <li>
                    ${ ui.includeFragment("uicommons", "extension", [ extension: it, contextModel: contextModel ]) }
                </li>
            <% } %>
        </ul>
    <% } %>

    <% if (registers) { %>
        <p>${ ui.message("ugandaemr.reportsapp.registers") }</p>
        <ul>
            <% registers.each { %>
                <li>
                    ${ ui.includeFragment("uicommons", "extension", [ extension: it, contextModel: contextModel ]) }
                </li>
            <% } %>
        </ul>
    <% } %>
</div>

<div class="reportBox">
    <% if (monthly) { %>
    <p>${ui.message("ugandaemr.reportsapp.monthlyReports") }</p>
    <ul>
        <% monthly.each { %>
        <li>
            ${ ui.includeFragment("uicommons", "extension", [ extension: it, contextModel: contextModel ]) }
        </li>
        <% } %>
    </ul>
    <% } %>

    <% if (quarterly) { %>
    <p>${ ui.message("ugandaemr.reportsapp.quarterlyReports") }</p>
    <ul>
        <% quarterly.each { %>
        <li>
            ${ ui.includeFragment("uicommons", "extension", [ extension: it, contextModel: contextModel ]) }
        </li>
        <% } %>
    </ul>
    <% } %>
</div>