<%
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("ugandaemrreports.title")])
%>

${ ui.includeFragment("ugandaemrreports", "summarize") }