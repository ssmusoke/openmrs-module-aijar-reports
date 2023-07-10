<% ui.decorateWith("appui", "standardEmrPage") %>

<% if (context.authenticated) { %>

${ ui.includeFragment("ugandaemrreports", "audittool") }


<% } else { %>

<% } %>