<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>
<script type="text/javascript" src="https://www.gstatic.com/charts/loader.js"></script>

<style>
.container-dashboard-hts{
	display:grid;
	height:100vh;

	grid-template-columns:repeat(3,1.4fr);
	grid-template-rows:0.2fr 6fr;

	grid-template-areas:
	"f f f"
	"g1 g2 g3";

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
</style>

<div class="container-dashboard-hts">

<div id=f><b>Reporting Period:${ui.format(quarter)}</b></div>
<div id=g1><canvas id="deliverymodelmodalities" style="width:100%;height:100%"></canvas></div>
<div id=g2><canvas id="HTS_HIV_STATUS_And_Counseling" style="width:100%;height:100%"></canvas></div>

<div id=g3><canvas id="g3_2" style="width:100%;height:100%"></canvas></div>

<!-- <div id=g3 style="width:100%; max-width:600px; height:500px;"></div> -->

</div>

<!-- scripts to plot graphs-->
<script>
     var xValues2 = [];
     var yValues2 = [];
     var barColors = ["yellow","green","brown"];

         xValues2.push("${ ui.format("Community Testing Point")}");
         yValues2.push(${ ui.format(hts_ctp)});

         xValues2.push("${ ui.format("Facility Based(HCT)")}");
         yValues2.push(${ ui.format(hts_hct)});

         xValues2.push("${ ui.format("Others")}");
         yValues2.push(${ ui.format(hts_others)});

          new Chart("deliverymodelmodalities", {
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
                        text: "HTS Delivery Model Modalities"
                  },
           }
      });
</script>

<script>
     var xValues1 = [];
     var yValues1 = [];
     var barColors = ["red","blue","orange","brown","yellow","green","black"];

         xValues1.push("${ ui.format("Total Enrolled")}");
         yValues1.push(${ ui.format(hts_total_enrolled_in_a_period)});

         xValues1.push("${ ui.format("HIV +")}");
         yValues1.push(${ ui.format(hts_p)});

         xValues1.push("${ ui.format("HIV -")}");
         yValues1.push(${ ui.format(hts_n)});

         xValues1.push("${ ui.format("INC (HTC)")}");
         yValues1.push(${ ui.format(hts_inc)});

         xValues1.push("${ ui.format("NT (HTC)")}");
         yValues1.push(${ ui.format(hts_nt)});

         xValues1.push("${ ui.format("Counselled")}");
         yValues1.push(${ ui.format(htc_co)});

         xValues1.push("${ ui.format("Not Counselled")}");
         yValues1.push(${ ui.format(hts_not_co)});

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
                   labelString: '# Number of Clients'
               }
             }],

             },
           }
      });
</script>

<!-- sampling 3D graph -->
<script>
    google.charts.load('current', {'packages':['corechart']});
    google.charts.setOnLoadCallback(drawChart);


       xValues.push("${ ui.format("Community Testing Point")}");
             yValues.push(${ ui.format(hts_ctp)});

             xValues.push("${ ui.format("Facility Based(HCT)")}");
             yValues.push(${ ui.format(hts_hct)});

             xValues.push("${ ui.format("Others")}");
             yValues.push(${ ui.format(hts_others)});

    function drawChart() {
        var data = google.visualization.arrayToDataTable([
        ['Indicator', 'Score'],
        ['Facility Based(HCT)',${ ui.format(hts_hct)}],
         ['Community Testing Point',${ ui.format(hts_ctp)}],
        ['Others',${ ui.format(hts_others)}]
]);

var options = {
  title:'HTS Delivery Model Modalities',
  is3D:true
};

var chart = new google.visualization.PieChart(document.getElementById('g3'));
  chart.draw(data, options);
}
</script>



