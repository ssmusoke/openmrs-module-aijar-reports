<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>

<style>
.container-dashboard{
	display:grid;
	height:100vh;

	grid-template-columns:1.2fr 0.3fr 0.3fr 1.2fr;

	grid-template-rows:0.4fr 3.4fr 5.2fr;

	grid-template-areas:
	"filter filter filter filter"
	"content0 content1 content1 content2"
	"graph1 graph1 graph2 graph2";

	grid-gap:4px;
	background:#F1F1F1;
	}

	#filter{
		background:#FFFFFF;
		grid-area:filter;
	}

	#content0{
		background:#FFFFFF;
		grid-area:content0;
	}

	#content1{
		background:#FFFFFF;
		grid-area:content1;
	}

	#content2{
		background:#FFFFFF;
		grid-area:content2;
	}

	#graph1{
		background:#FFFFFF;
		grid-area:graph1;
	}

	#graph2{
		background:#FFFFFF;
		grid-area:graph2;
	}

	@media only screan and (max-width:550px){

	}
/* ********************** */
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
</div>


<!--Section for Care and Treatment dashboard-->
<div id="careandtreatment" class="tabcontent" style="display:block">
<div class="container-dashboard">

<div id=filter><b>Reporting Period:${ui.format(quarter)}</b></div>

<div id=content0><canvas id="TxNewTxCurr" style="width:100%;max-width:600px;height:100%"></canvas></div>

<div id=content1>

    <table border="1" summary="Facility Summary">
        <tr>
            <th>Indicator</th>
            <th>Value</th>
        </tr>

        <tr>
        <td>Total Patients</td>
        <td>${ ui.format(total_patients_system)}</td>
        </tr>

        <tr>
        <td>Total Enrolled</td>
        <td>${ ui.format(total_enrolled_in_a_care)}</td>
        </tr>

        <tr>
        <td>Total Enrolled (Period)</td>
        <td>${ ui.format(total_enrolled_in_a_period)}</td>
         </tr>

         <tr>
         <td>Current In Care (Period)</td>
         <td>${ ui.format(total_enrolled_in_a_period)}</td>
         </tr>

         <tr>
         <td>New on ART (Period)</td>
         <td>${ ui.format(tx_new_new_on_art_period)}</td>
         </tr>

         <tr>
         <td>Total Clients with VL(Today)</td>
         <td>${ ui.format(tt_clients_with_vl_in_period)}</td>
         </tr>

         <tr>
         <td>Suppressed(Today)</td>
         <td>${ ui.format(tt_suppressed_in_period)}</td>
         </tr>

         <tr>
         <td>Non suppressed(Today)</td>
         <td>${ ui.format(tt_non_suppressed_in_period)}</td>
         </tr>
</table>

</div>

<div id=content2><canvas id="FacilityStatus" style="width:100%;height:100%"></canvas></div>

<div id=graph1><canvas id="GeneralFacilityStatus" style="width:100%;height:100%"></canvas></div>

<div id=graph2><canvas id="ViralLoadBar" style="width:100%;height:100%"></canvas></div>

</div>
</div>

<!--Section HTS dashboard-->
<div id="hts" class="tabcontent">
<div class="container-dashboard">
        <div id=filter><b>Reporting Period:${ui.format(quarter)}</b></div>
		<div id=content0>HTS one</div>
		<div id=content1>HTS two</div>
		<div id=content2>HTS Three</div>
		<div id=graph1 >HTS Four</div>
		<div id=graph2>HTS Five</div>
</div>
</div>

<!--Section Appointments dashboard-->
<div id="apointments" class="tabcontent">
<div class="container-dashboard">
        <div id=filter><b>Reporting Period:${ui.format(quarter)}</b></div>
		<div id=content0>HTS one</div>
		<div id=content1>HTS two</div>
		<div id=content2>HTS Three</div>
		<div id=graph1 >HTS Four</div>
		<div id=graph2>HTS Five</div>
</div>
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

<!--scripts dashboards for Care and Treatment -->
<script>
     var xValues = [];
     var yValues = [];
     var barColors = ["red", "green"];

         xValues.push("${ ui.format("Total Enrolled")}");
         yValues.push(${ ui.format(total_enrolled_in_a_period)});

         xValues.push("${ ui.format("New on ART")}");
         yValues.push(${ ui.format(tx_new_new_on_art_period)});

          new Chart("TxNewTxCurr", {
            type: "pie",
            data: {
              labels: xValues,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues
              }]
            },
             options: {
               legend: {display: true,position:"bottom",},
               title: {
                        display: true,
                        text: "TX New vs Currently Enrolled"
                  },

           }
      });
</script>

<script>
     var xValues = [];
     var yValues = [];
     var barColors = ["blue","orange","brown"];

         xValues.push("${ ui.format("Total Patients")}");
         yValues.push(${ ui.format(total_patients_system)});

         xValues.push("${ ui.format("Total Enrolled")}");
         yValues.push(${ ui.format(total_enrolled_in_a_care)});

         xValues.push("${ ui.format("Not Active in Care")}");
         yValues.push(${ ui.format(not_active_in_care)});

          new Chart("FacilityStatus", {
            type: "doughnut",
            data: {
              labels: xValues,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues
              }]
            },
             options: {
               legend: {display: true,position:"bottom",},
               title: {
                        display: true,
                        text: "Facility Client Status"
                  },
           }
      });
</script>

<script>
     var xValues = [];
     var yValues = [];
     var barColors = ["Red","blue","orange","brown","Yellow"];

         xValues.push("${ ui.format("Total Patients")}");
         yValues.push(${ ui.format(total_patients_system)});

         xValues.push("${ ui.format("Total Enrolled")}");
         yValues.push(${ ui.format(total_enrolled_in_a_care)});

         xValues.push("${ ui.format("Not Active in Care")}");
         yValues.push(${ ui.format(not_active_in_care)});

         xValues.push("${ ui.format("Current In Care")}");
         yValues.push(${ ui.format(total_enrolled_in_a_period)});

         xValues.push("${ ui.format("New on ART")}");
         yValues.push(${ ui.format(tx_new_new_on_art_period)});

          new Chart("GeneralFacilityStatus", {
            type: "bar",
            data: {
              labels: xValues,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "Facility Client Status"
                  },

               scales: {

                 //x axis tittle label
                 xAxes: [{
                 scaleLabel: {
                   display: false,
                   labelString: ''
                 }
               }],

               //y axis tittle label
                 yAxes: [{
                 scaleLabel: {
                   display: true,
                   labelString: '# Number of Clients'
               }
             }],

             },
           }
      });
</script>

<script>
     var xValues = [];
     var yValues = [];
     var barColors = ["Red","blue","Yellow"];

         xValues.push("${ ui.format("Total Patients with VL")}");
         yValues.push(${ ui.format(tt_clients_with_vl_in_period)});

         xValues.push("${ ui.format("Suppressed")}");
         yValues.push(${ ui.format(tt_suppressed_in_period)});

         xValues.push("${ ui.format("Non Suppressed ")}");
         yValues.push(${ ui.format(tt_non_suppressed_in_period)});

          new Chart("ViralLoadBar", {
            type: "bar",
            data: {
              labels: xValues,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "Viral Load Tracker (As of Today)"
                  },

               scales: {

                 //x axis tittle label
                 xAxes: [{
                 scaleLabel: {
                   display: false,
                   labelString: ''
                 }
               }],

               //y axis tittle label
                 yAxes: [{
                 scaleLabel: {
                   display: true,
                   labelString: '# Number of Clients'
               }
             }],

             },
           }
      });
</script>

<!--
<script>
     var xValues = [];
     var yValues = [];
     var barColors = ["red", "green","blue","orange","brown"];

      <% if (monthly_enrollments_in_period) { %>
            <% monthly_enrollments_in_period.each { %>
                xValues.push("${ ui.format(it.get(0))}");
                yValues.push(${ ui.format(it.get(1))});
        <% } %>
        <% }%>

          new Chart("ViralLoadBar", {
            type: "bar",
            data: {
              labels: xValues,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "# Enrolled in a Given Period"
                  },

               scales: {

                 //x axis tittle label
                 xAxes: [{
                 scaleLabel: {
                   display: true,
                   labelString: 'Month'
                 }
               }],

               //y axis tittle label
                 yAxes: [{
                 scaleLabel: {
                   display: true,
                   labelString: '# Enrolled'
               }
             }],

             },
           }
      });
</script>
-->
