<script src="https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.9.4/Chart.js"></script>

<style>
.container-dashboard-appointments{
  display:grid;
  height:100vh;

  grid-template-columns:repeat(2,1.4fr);
  grid-template-rows:0.2fr 2fr 3fr;

  grid-template-areas:
  "fa fa"
  "ga1 ga2"
  "ga3 ga3";

  grid-gap:2px;
  background:#F1F1F1;
  }

  #fa{
    background:#FFFFFF;
    grid-area:fa;
  }

  #ga1{
    background:#FFFFFF;
    grid-area:ga1;
  }

  #ga2{
    background:#FFFFFF;
    grid-area:ga2;
  }

  #ga3{
    background:#FFFFFF;
    grid-area:ga3;
  }


</style>

<div class="container-dashboard-appointments">

<div id=fa><b>Reporting Period:${ui.format(quarter)}</b></div>

<div id=ga1><canvas id="appointment_status" style="width:100%;height:100%"></canvas></div>

<div id=ga2><canvas id="weeks_appointments" style="width:100%;height:100%"></canvas></div>

<div id=ga3><canvas id="reasons_for_next_appointment" style="width:600px;height:100%"></canvas></div>

</div>

<!--Graph scripts-->
<script>
     var xValues_a = [];
     var yValues_a = [];
     var barColor_a = ["yellow","green","brown"];

         xValues_a.push("${ ui.format("Todays Appointments")}");
         yValues_a.push(${ ui.format(today_appointments)});

         xValues_a.push("${ ui.format("Attended On Scheduled")}");
         yValues_a.push(${ ui.format(attended_ontime)});

         xValues_a.push("${ ui.format("Not Attended As Scheduled")}");
         yValues_a.push(${ ui.format(non_attended_ontime)});

          new Chart("appointment_status", {
            type: "doughnut",
            data: {
              labels: xValues_a,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColor_a,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues_a
              }]
            },
             options: {
               legend: {display: true,position:"bottom",},
               title: {
                        display: true,
                        text: "Appointment Status"
                  },
           }
      });
</script>

<script>
     var xValues_ra = [];
     var yValues_ra = [];
     var barColors_ra = ["red","blue","orange","brown","yellow","green","black","red","blue","orange","brown",
     "yellow","green","black"];

         xValues_ra.push("${ ui.format("ART Initiation")}");
         yValues_ra.push(${ ui.format(ra_ai)});

         xValues_ra.push("${ ui.format("ART Refill")}");
         yValues_ra.push(${ ui.format(ra_ar)});

         xValues_ra.push("${ ui.format("Substitution")}");
         yValues_ra.push(${ ui.format(ra_ars)});

         xValues_ra.push("${ ui.format("Viral Load")}");
         yValues_ra.push(${ ui.format(ra_vl)});

         xValues_ra.push("${ ui.format("CD4")}");
         yValues_ra.push(${ ui.format(ra_cd4)});

         xValues_ra.push("${ ui.format("IAC")}");
         yValues_ra.push(${ ui.format(ra_iac)});

         xValues_ra.push("${ ui.format("TB Drug Refill")}");
         yValues_ra.push(${ ui.format(ra_tbdr)});

         xValues_ra.push("${ ui.format("TB Follow up")}");
         yValues_ra.push(${ ui.format(ra_tbfu)});

         xValues_ra.push("${ ui.format("ANC")}");
         yValues_ra.push(${ ui.format(ra_anc)});

         xValues_ra.push("${ ui.format("EID")}");
         yValues_ra.push(${ ui.format(ra_eid)});

         xValues_ra.push("${ ui.format("PMTCT")}");
         yValues_ra.push(${ ui.format(ra_pmtct)});

         xValues_ra.push("${ ui.format("Diabetes")}");
         yValues_ra.push(${ ui.format(ra_hord)});

         xValues_ra.push("${ ui.format("Nutrition")}");
         yValues_ra.push(${ ui.format(ra_nc)});

         xValues_ra.push("${ ui.format("Others")}");
         yValues_ra.push(${ ui.format(ra_others)});

          new Chart("reasons_for_next_appointment", {
            type: "bar",
            data: {
              labels: xValues_ra,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors_ra,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues_ra
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "Reasons For Next Appointment"
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
     var xValues_na = [];
     var yValues_na = [];
     var barColors_na = ["red","blue","orange","brown","yellow","green","black"];


         var myDay0 = "${ui.format(Day0)}".split("=:");
         xValues_na.push(myDay0[0])
         yValues_na.push(myDay0[1]);

         var myDay1 = "${ui.format(Day1)}".split("=:");
         xValues_na.push(myDay1[0])
         yValues_na.push(myDay1[1]);

         var myDay2 = "${ui.format(Day2)}".split("=:");
         xValues_na.push(myDay2[0])
         yValues_na.push(myDay2[1]);

         var myDay3 = "${ui.format(Day3)}".split("=:");
         xValues_na.push(myDay3[0])
         yValues_na.push(myDay3[1]);

         var myDay4 = "${ui.format(Day4)}".split("=:");
         xValues_na.push(myDay4[0])
         yValues_na.push(myDay4[1]);

         var myDay5 = "${ui.format(Day5)}".split("=:");
         xValues_na.push(myDay5[0])
         yValues_na.push(myDay5[1]);

         var myDay6 = "${ui.format(Day6)}".split("=:");
         xValues_na.push(myDay6[0])
         yValues_na.push(myDay6[1]);


          new Chart("weeks_appointments", {
            type: "bar",
            data: {
              labels: xValues_na,
              datasets: [{
                fill: false,
                lineTension: 0,
                backgroundColor: barColors_na,
                borderColor: "rgba(0,0,255,0.1)",
                data: yValues_na
              }]
            },
             options: {
               legend: {display: false,position:"bottom",},
               title: {
                        display: true,
                        text: "Weeks Next Appointments"
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


