<% ui.decorateWith("appui", "standardEmrPage") %>

<% if (context.authenticated) { %>

${ ui.includeFragment("ugandaemrreports", "careTreatment") }


<% } else { %>

<% } %>