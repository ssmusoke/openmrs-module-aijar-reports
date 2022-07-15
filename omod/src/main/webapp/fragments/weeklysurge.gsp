</style>

<style type="text/css">
.tg  {border-collapse:collapse;border-spacing:0;}
.tg td{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
    overflow:hidden;padding:10px 5px;word-break:normal;}
.tg th{border-color:black;border-style:solid;border-width:1px;font-family:Arial, sans-serif;font-size:14px;
    font-weight:normal;overflow:hidden;padding:10px 5px;word-break:normal;}
.tg .tg-wdya{background-color:#D9D9D9;color:#7030A0;text-align:center;vertical-align:middle}
.tg .tg-qnnl{background-color:#F8FFF9;text-align:center;vertical-align:top}
.tg .tg-8y2u{background-color:#F8FFF9;text-align:right;vertical-align:top}
.tg .tg-o4ah{background-color:#D9D9D9;text-align:center;vertical-align:middle}
.tg .tg-ogju{background-color:#E6F1FF;text-align:center;vertical-align:top}
.tg .tg-9j11{background-color:#D9D9D9;text-align:left;vertical-align:middle}
.tg .tg-0lax{text-align:left;vertical-align:top}
.tg .tg-zlip{background-color:#F8FFF9;text-align:left;vertical-align:top}
.tg .tg-wboj{background-color:#E6F1FF;text-align:right;vertical-align:top}
</style>

<script type="text/javascript">

    function displaySurgeDashboard(report){

        jq.each(report.group, function (index, rowValue) {
            var indicatorCode = rowValue.code.coding[0].code;
            if(rowValue.stratifier.length!==0){
                var disaggregated_rows = rowValue.stratifier[0].stratum;
                var which_sex='';
                var which_age='';
                jq.each(disaggregated_rows, function (key, obj) {
                    jq.each(obj.component,function(k,v){
                        if(v.code.coding[0].code=='SEX'){
                            which_sex = v.value.coding[0].code;
                        }else if (v.code.coding[0].code=='AGE_GROUP'){
                            which_age = v.value.coding[0].code;
                        }
                    });

                    if(which_sex!='' && which_age!=''){
                        jq('#'+indicatorCode+'-'+which_age+'-'+which_sex).html(obj.measureScore.value);
                    }else if (which_sex=='') {
                        jq('#'+indicatorCode+'-'+which_age).html(obj.measureScore.value);
                    }

                });
            }
        });

    }

    var startdate='';
    var enddate='';

    jq(document).ready(function () {

        var json=${ui.format(weeklysurgedata)};

        displaySurgeDashboard(JSON.parse(JSON.stringify(json)));

    });
</script>

<div class="row">

    <div class="col-md-12">
        <div id="display-report" style="overflow-y:scroll;">
            <div class='modal-header'> <label style="text-align: center"><h1>Weekly Surge Report Period(
            From:${ui.format(startdate)} To ${ui.format(enddate)})</h1></label></div>
            <div id="surge-report">
                <table class="tg">
                    <thead>
                    <tr>
                        <th class="tg-9j11">Indicator&nbsp;&nbsp;&nbsp;Code</th>
                        <th class="tg-9j11" colspan="2">Indicator&nbsp;&nbsp;&nbsp;Name &amp; Disaggregation's</th>
                        <th class="tg-o4ah">Female</th>
                        <th class="tg-o4ah">Male</th>
                        <th class="tg-o4ah">Total</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr>
                        <td class="tg-0lax" rowspan="3">TX_NEW</td>
                        <td class="tg-zlip" rowspan="3">Number&nbsp;&nbsp;&nbsp;of adults and children newly enrolled on antiretroviral therapy (ART)</td>
                        <td class="tg-8y2u">0-11M</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl" id="TX_NEW-P0M--12M-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">1-14 years</td>
                        <td class="tg-ogju" id="TX_NEW-P1Y--15Y-F"> </td>
                        <td class="tg-ogju" id="TX_NEW-P1Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ years</td>
                        <td class="tg-qnnl" id="TX_NEW-P15Y--9999Y-F"> </td>
                        <td class="tg-qnnl" id="TX_NEW-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="3">VMMC_CIRC</td>
                        <td class="tg-zlip" rowspan="3">Number of Males&nbsp;&nbsp;&nbsp;circumcised</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju" id="VMMC_CIRC-P0Y--P15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">15-29 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl" id="VMMC_CIRC-P15Y--P30Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">30+Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju" id="VMMC_CIRC-P30Y--P9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="9">TB_IPT</td>
                        <td class="tg-zlip" rowspan="3">Number of contacts with no signs and symptoms of TB</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" > </td>
                        <td class="tg-ogju" > </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="3">Number of contacts with no signs and symptoms of TB started&nbsp;&nbsp;&nbsp;on IPT</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="3">Number of ART patients initiated on TB preventive therapy (IPT)</td>
                        <td class="tg-8y2u"> &lt;5 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P1Y--5Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">5-14 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TB_IPT-P5Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TB_IPT-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TB_IPT-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="12">TLD</td>
                        <td class="tg-zlip" rowspan="2">Number of newly&nbsp;&nbsp;&nbsp;initiated clients started on TLD</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_COARSE-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"  id="TLD_STARTED_COARSE-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_COARSE-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number&nbsp;&nbsp;&nbsp;of newly initiated clients started on TLD at ART clinic</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_ART-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_STARTED_ART-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_ART-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of newly initiated clients started on TLD at Mother- Baby&nbsp;&nbsp;&nbsp;Care point</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_STARTED_MOTHER-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju"  id="TLD_STARTED_MOTHER-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_STARTED_MOTHER-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_TRANS_COARSE-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_COARSE-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_COARSE-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>

                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD at ART clinic</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya"  id="TLD_TRANS_ART-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_ART-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_ART-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>

                    <tr>
                        <td class="tg-zlip" rowspan="2">Number of active ART clients transitioned to TLD at Mother- Baby&nbsp;&nbsp;&nbsp;Care point</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TLD_TRANS_MOTHER-P0Y--15Y-T"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TLD_TRANS_MOTHER-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TLD_TRANS_MOTHER-P15Y--9999Y-M" > </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">TX_SV(D)</td>
                        <td class="tg-zlip" rowspan="2">Number of clients&nbsp;&nbsp;&nbsp;newly initiated on ART due for second visit in the reporting period</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-qnnl" id="TX_SV_DENOM-P0Y--15Y-F"> </td>
                        <td class="tg-qnnl" id="TX_SV_DENOM-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TX_SV_DENOM-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TX_SV_DENOM-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">TX_SV(N)</td>
                        <td class="tg-zlip" rowspan="2">Number of clients&nbsp;&nbsp;&nbsp;newly initiated on ART due for second visit </td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-qnnl" id="TX_SV_NUM-P0Y--15Y-F"> </td>
                        <td class="tg-qnnl" id="TX_SV_NUM-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="TX_SV_NUM-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="TX_SV_NUM-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="12">TX_PRO</td>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients transitioned to ABC/3TC/DTG</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_DTG-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients&nbsp;&nbsp;&nbsp;transitioned to ABC/3TC/LPV/r</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_LPV-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-zlip" rowspan="4">Number of active ART clients transitioned to TLD</td>
                        <td class="tg-8y2u"> &lt; 3 Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P0Y--3Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-wboj">3-9 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P3Y--10Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">10-14Years</td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-qnnl"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P10Y--15Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15-19 Years</td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-ogju"> </td>
                        <td class="tg-wdya" id="TX_PRO_TLD-P15Y--19Y"> </td>
                    </tr>
                    <tr>
                        <td class="tg-0lax" rowspan="2">HTS_RECENT</td>
                        <td class="tg-zlip" rowspan="2">Number of newly&nbsp;&nbsp;&nbsp;diagnosed HIV-positive persons who received testing for recent infection with&nbsp;&nbsp;&nbsp;a documented result</td>
                        <td class="tg-8y2u"> &lt;15 Years</td>
                        <td class="tg-ogju" id="HTS_RECENT-P0Y--15Y-F"> </td>
                        <td class="tg-ogju" id="HTS_RECENT-P0Y--15Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    <tr>
                        <td class="tg-8y2u">15+ Years</td>
                        <td class="tg-ogju" id="HTS_RECENT-P15Y--9999Y-F"> </td>
                        <td class="tg-ogju" id="HTS_RECENT-P15Y--9999Y-M"> </td>
                        <td class="tg-wdya"> </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </div>


    </div>

</div>

