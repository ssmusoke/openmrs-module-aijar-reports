<% ui.decorateWith("appui", "standardEmrPage") %>

<div> <b>Patient in Pages 2</b></div>

<% if (context.authenticated) { %>

<% } else { %>

<% } %>

${ ui.includeFragment("ugandaemrreports", "patients") }