<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>

<style>
.container-dashboard{
	display:grid;
	height:100vh;


	grid-template-columns:repeat(4,1.4fr);
	grid-template-rows:0.4fr 3fr 5fr;


	grid-template-areas:
	"filter filter filter filter"
	"content0 content1 content1 content2"
	"graph1 graph1 graph2 graph2";

	grid-gap:2px;
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

</style>

<!--Section for Care and Treatment dashboard-->
<div class="container-dashboard">

<div id=filter><b>Reporting Period:${ui.format(quarter)}</b></div>

<div id=content0><canvas id="TxNewTxCurr" style="width:100%;max-width:600px;height:100%"></canvas></div>

<div id=content1>

    <table border="1" summary="Facility Summary" style="width:100%;height:100%">
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
         <td>Clients with updated VL (Today)</td>
         <td>${ ui.format(tt_clients_with_vl_in_period)}</td>
         </tr>

         <tr>
         <td>Clients with Suppressed VL (Today)</td>
         <td>${ ui.format(tt_suppressed_in_period)}</td>
         </tr>

         <tr>
         <td>Clients with Non Suppressed VL (Today)</td>
         <td>${ ui.format(tt_non_suppressed_in_period)}</td>
         </tr>
</table>

</div>

<div id=content2><canvas id="FacilityStatus" style="width:100%;height:100%"></canvas></div>

<div id=graph1><canvas id="GeneralFacilityStatus" style="width:100%;height:100%"></canvas></div>

<div id=graph2><canvas id="ViralLoadBar" style="width:100%;height:100%"></canvas></div>

</div>


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
                   labelString: '# of Clients'
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
                   labelString: '# of Clients'
               }
             }],

             },
           }
      });
</script>
