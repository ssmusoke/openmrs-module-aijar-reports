<% ui.decorateWith("appui", "standardEmrPage") %>
<% if (context.authenticated) { %>

<!-- Main page for Dashboard -->
<style>
body {font-family: Arial;}
/* Style the tab */
.tab {
  overflow: hidden;
  border: 1px solid #ccc;
  background-color: #f1f1f1;
}

/* Style the buttons inside the tab */
.tab button {
  background-color: inherit;
  float: left;
  border: none;
  outline: none;
  cursor: pointer;
  padding: 14px 16px;
  transition: 0.3s;
  font-size: 17px;
}

/* Change background color of buttons on hover */
.tab button:hover {
  background-color: #ddd;
}

/* Create an active/current tablink class */
.tab button.active {
  background-color: #ccc;
}

/* Style the tab content */
.tabcontent {
  display: none;
  padding: 6px 12px;
  border: 1px solid #ccc;
  border-top: none;
}
</style>

<div class="tab">
  <button class="tablinks" onclick="openDashboardCareandTreatment(event, 'careandtreatment')">Care and Treatment</button>
  <button class="tablinks" onclick="openDashboardHTS(event, 'hts')">HTS</button>
  <button class="tablinks" onclick="openDashboardApointments(event, 'apointments')">Appointments</button>
  <button class="tablinks" onclick="openDashboardWeeklysurge(event, 'weeklysurge')">Weekly Surge</button>


</div>

<!--Section for Care and Treatment dashboard-->
<div id="careandtreatment" class="tabcontent" style="display:block">

    ${ ui.includeFragment("ugandaemrreports", "careTreatment") }

</div>

<!--Section HTS dashboard-->
<div id="hts" class="tabcontent">

    ${ ui.includeFragment("ugandaemrreports", "hts") }

</div>

<!--Section Appointments dashboard-->
<div id="apointments" class="tabcontent">

    ${ ui.includeFragment("ugandaemrreports", "appointments") }

</div>

<!--Weekly Surge dashboard-->
<div id="weeklysurge" class="tabcontent">

    ${ ui.includeFragment("ugandaemrreports", "weeklySurge") }

</div>


<!--Scripts Section-->
<script>
function openDashboardCareandTreatment(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}
</script>

<script>
function openDashboardHTS(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}
</script>

<script>
function openDashboardApointments(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}
</script>

<script>
function openDashboardWeeklysurge(evt, tabName) {
  var i, tabcontent, tablinks;
  tabcontent = document.getElementsByClassName("tabcontent");
  for (i = 0; i < tabcontent.length; i++) {
    tabcontent[i].style.display = "none";
  }
  tablinks = document.getElementsByClassName("tablinks");
  for (i = 0; i < tablinks.length; i++) {
    tablinks[i].className = tablinks[i].className.replace(" active", "");
  }
  document.getElementById(tabName).style.display = "block";
  evt.currentTarget.className += " active";
}
</script>



<% } else { %>

<% } %>