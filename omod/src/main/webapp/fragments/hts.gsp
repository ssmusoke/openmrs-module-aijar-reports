<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>

<style>
.container-dashboard-hts{
	display:grid;
	height:100vh;

	grid-template-columns:repeat(3,1.4fr);
	grid-template-rows:0.2fr 2.5fr 3fr;

	grid-template-areas:
	"f f f"
	"g1 g2 g3"
	"g4 g4 g4";

	grid-gap:2px;
	background:#F1F1F1;
	}

	#f{
		background:#FFFFFF;
		grid-area:f;
	}

	#g1{
		background:#FFFFFF;
		grid-area:g1;
	}

	#g2{
		background:#FFFFFF;
		grid-area:g2;
	}

	#g3{
    	background:#FFFFFF;
    	grid-area:g3;
    }
    #g4{
        background:#FFFFFF;
        grid-area:g4;
    }
</style>

<div class="container-dashboard-hts">

<div id=f><b>Reporting Period:${ui.format(quarter)}</b></div>

<div id=g1><canvas id="htsentrypoint" style="width:600px;height:100%"></canvas></div>

<div id=g2>
<table border="1" summary="Summary Table" style="width:100%;height:100%">
        <tr>
            <th>Indicator</th>
            <th>Value</th>
        </tr>

         <tr>
         <td>Counselled</td>
         <td>${ ui.format(htc_co)}</td>
         </tr>

        <tr>
        <td>HIV +</td>
        <td>${ ui.format(hts_p)}</td>
        </tr>

         <tr>
         <td>HIV -</td>
         <td>${ ui.format(hts_n)}</td>
         </tr>

         <tr>
         <td>New on ART</td>
         <td>${ ui.format(hts_total_enrolled_in_a_period)}</td>
         </tr>

         <tr>
         <td>INC (HTC)</td>
         <td>${ ui.format(hts_inc)}</td>
         </tr>

         <tr>
         <td>NT (HTC)</td>
         <td>${ ui.format(hts_nt)}</td>
         </tr>

         <tr>
         <td>Not Counselled</td>
         <td>${ ui.format(hts_not_co)}</td>
         </tr>
</table>
</div>

<div id=g3><canvas id="HTS_Reason_for_testing" style="width:600px;height:100%"></canvas></div>

<div id=g4><canvas id="HTS_HIV_STATUS_And_Counseling" style="width:600px;height:100%"></canvas></div>

</div>

<!-- scripts to plot graphs-->
<script>
     var xValues2 = [];
     var yValues2 = [];
     var barColors = ["yellow","green","brown","red","pink","blue","orange","grey","gold","indigo","cyan","silver"];

         xValues2.push("${ ui.format("OPD")}");
         yValues2.push(${ ui.format(htc_e_opd)});

         xValues2.push("${ ui.format("Ward")}");
         yValues2.push(${ ui.format(htc_e_ward)});

         xValues2.push("${ ui.format("ART Clinic")}");
         yValues2.push(${ ui.format(htc_e_artc)});

         xValues2.push("${ ui.format("TB Clinic")}");
         yValues2.push(${ ui.format(htc_e_tbc)});

         xValues2.push("${ ui.format("Nutrition unit")}");
         yValues2.push(${ ui.format(htc_e_nu)});

         xValues2.push("${ ui.format("YCC")}");
         yValues2.push(${ ui.format(htc_e_ycc)});

         xValues2.push("${ ui.format("ANC")}");
         yValues2.push(${ ui.format(htc_e_anc)});

         xValues2.push("${ ui.format("Maternity")}");
         yValues2.push(${ ui.format(htc_e_m)});

         xValues2.push("${ ui.format("PNC")}");
         yValues2.push(${ ui.format(htc_e_pnc)});

         xValues2.push("${ ui.format("Family Planning")}");
         yValues2.push(${ ui.format(htc_e_fp)});

         xValues2.push("${ ui.format("STI Clinic")}");
         yValues2.push(${ ui.format(htc_e_stic)});

         xValues2.push("${ ui.format("Others")}");
         yValues2.push(${ ui.format(htc_e_o)});

          new Chart("htsentrypoint", {
            type: "doughnut",
            data: {
              labels: xValues2,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues2
              }]
            },
             options: {
               legend: {display: true,position:"bottom",},
               title: {
                        display: true,
                        text: "HTS % Contribution For Entry Point"
                  },
           }
      });
</script>

<script>
     var xValues1 = [];
     var yValues1 = [];
     var barColors = ["green","red","yellow","brown","blue","green"];

         xValues1.push("${ ui.format("Counselled")}");
         yValues1.push(${ ui.format(htc_co)});

         xValues1.push("${ ui.format("HIV +")}");
         yValues1.push(${ ui.format(hts_p)});

         xValues1.push("${ ui.format("HIV -")}");
         yValues1.push(${ ui.format(hts_n)});

         xValues1.push("${ ui.format("New on ART")}");
         yValues1.push(${ ui.format(hts_total_enrolled_in_a_period)});

         xValues1.push("${ ui.format("INC (HTC)")}");
         yValues1.push(${ ui.format(hts_inc)});

         xValues1.push("${ ui.format("NT (HTC)")}");
         yValues1.push(${ ui.format(hts_nt)});

          new Chart("HTS_HIV_STATUS_And_Counseling", {
            type: "bar",
            data: {
              labels: xValues1,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues1
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "HTS HIV STATUS AND COUNSELLING"
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
     var xValues3 = [];
     var yValues3 = [];
      var barColors3 = ["yellow","green","brown","red","pink","blue","orange","grey","gold","indigo","cyan","silver"];

           xValues3.push("${ ui.format("APN")}");
           yValues3.push(${ ui.format(hts_apn)});

           xValues3.push("${ ui.format("ICT Other Than APN")}");
           yValues3.push(${ ui.format(hts_iapn)});

           xValues3.push("${ ui.format("PrEP")}");
           yValues3.push(${ ui.format(hts_prep)});

           xValues3.push("${ ui.format("PEP")}");
           yValues3.push(${ ui.format(hts_pep)});

           xValues3.push("${ ui.format("HIV Self -Test Positive")}");
           yValues3.push(${ ui.format(hts_hitp)});

           xValues3.push("${ ui.format("Inconclusive HIV result")}");
           yValues3.push(${ ui.format(hts_ihr)});

           xValues3.push("${ ui.format("Self-initiative")}");
           yValues3.push(${ ui.format(hts_si)});

           xValues3.push("${ ui.format("PMTCT")}");
           yValues3.push(${ ui.format(hts_pmtct)});

           xValues3.push("${ ui.format("Others")}");
           yValues3.push(${ ui.format(hts_o)});

           xValues3.push("${ ui.format("SNS")}");
           yValues3.push(${ ui.format(hts_sns)});

          new Chart("HTS_Reason_for_testing", {
            type: "pie",
            data: {
              labels: xValues3,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors3,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues3
              }]
            },
             options: {
               legend: {display: true,position:"bottom",},
               title: {
                        display: true,
                        text: "HTS % Reason for Testing"
                  },
           }
      });
</script>



