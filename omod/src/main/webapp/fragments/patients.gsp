<table>
  <tr>
   <th>Patient Id</th>
   <th>Name</th>
   <th>Gender</th>
  </tr>
  <% if (patients) { %>
     <% patients.each { %>
      <tr>
        <td>${ ui.format(it.getId()) }</td>
        <td>${ ui.format(it.getNames()) }</td>
        <td>${ ui.format(it.getGender()) }</td>
      </tr>
    <% } %>
  <% } else { %>
  <tr>
    <td colspan="2">${ ui.message("general.none") }</td>
  </tr>
  <% } %>
</table>