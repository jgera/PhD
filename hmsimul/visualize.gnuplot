set term svg enhanced size 480,320

#set yrange[0:1]

set xlabel "#Sample"
set key outside top horizontal

set style line 1 lw 2
set style line 2 lw 2
set style line 3 lw 2
set style line 4 lw 2 lc rgb "#444444"

set yrange[0:1]
set ylabel "% of availability"

set output "availabilities_risk.svg"
plot "avails_riskMin_Reactive.csv" using 2 title "RiskMin Reactive" w lines ls 3,\
"avails_riskMin_Proactive.csv" using 2 title "RiskMin Proactive" w lines ls 4

set output "availabilities_eco.svg"

plot "avails_ecoMax_Reactive.csv" using 2 title "EngEffMax Reactive" w lines ls 1,\
"avails_ecoMax_Proactive.csv" using 2 title "EngEffMax Proactive" w lines ls 2

set ylabel "% of workload"
set autoscale x
set output "allocations_ecoMax_Reactive.svg"

plot "allocations_ecoMax_Reactive.csv" using 2 title "Node 1" w lines ls 1,\
"allocations_ecoMax_Reactive.csv" using 3 title "Node 2" w lines ls 2,\
"allocations_ecoMax_Reactive.csv" using 4 title "Node 3" w lines ls 3,\
"allocations_ecoMax_Reactive.csv" using 5 title "Node 4" w lines ls 4

set output "allocations_riskMin_Reactive.svg"

plot "allocations_riskMin_Reactive.csv" using 2 title "Node 1" w lines ls 1,\
"allocations_riskMin_Reactive.csv" using 3 title "Node 2" w lines ls 2,\
"allocations_riskMin_Reactive.csv" using 4 title "Node 3" w lines ls 3,\
"allocations_riskMin_Reactive.csv" using 5 title "Node 4" w lines ls 4

set output "allocations_ecoMax_Proactive.svg"

plot "allocations_ecoMax_Proactive.csv" using 2 title "Node 1" w lines ls 1,\
"allocations_ecoMax_Proactive.csv" using 3 title "Node 2" w lines ls 2,\
"allocations_ecoMax_Proactive.csv" using 4 title "Node 3" w lines ls 3,\
"allocations_ecoMax_Proactive.csv" using 5 title "Node 4" w lines ls 4

set output "allocations_riskMin_Proactive.svg"

plot "allocations_riskMin_Proactive.csv" using 2 title "Node 1" w lines ls 1,\
"allocations_riskMin_Proactive.csv" using 3 title "Node 2" w lines ls 2,\
"allocations_riskMin_Proactive.csv" using 4 title "Node 3" w lines ls 3,\
"allocations_riskMin_Proactive.csv" using 5 title "Node 4" w lines ls 4

set xrange[30000:50000]
set yrange[0:1]
set ylabel "% Energy Efficiency"

set output "ecos_peak_ecoMax.svg"
plot "allocations_ecoMax_Reactive.csv" using 7 title "EnergyEffMax Reactive" w lines ls 1,\
"allocations_ecoMax_Proactive.csv" using 7 title "EnergyEffMax Proactive" w lines ls 2

set output "ecos_peak_riskMin.svg"
plot "allocations_riskMin_Reactive.csv" using 7 title "RiskMin Reactive" w lines ls 3,\
"allocations_riskMin_Proactive.csv" using 7 title "RiskMin Proactive" w lines ls 4

set yrange[0:7]
set ylabel "Risk Level"

set output "risks_peak_riskMin.svg"
plot "allocations_riskMin_Reactive.csv" using 6 title "RiskMin Reactive" w lines ls 3,\
"allocations_riskMin_Proactive.csv" using 6 title "RiskMin Proactive" w lines ls 4

set output "risks_peak_ecoMax.svg"
plot "allocations_ecoMax_Reactive.csv" using 6 title "EnergyEffMax Reactive" w lines ls 1,\
"allocations_ecoMax_Proactive.csv" using 6 title "EnergyEffMax Proactive" w lines ls 2

set xrange[20000:40000]
set yrange[0:1]
set ylabel "% Energy Efficiency"

set output "ecos_offpeak_ecoMax.svg"
plot "allocations_ecoMax_Reactive.csv" using 7 title "EnergyEffMax Reactive" w lines ls 1,\
"allocations_ecoMax_Proactive.csv" using 7 title "EnergyEffMax Proactive" w lines ls 2

set output "ecos_offpeak_riskMin.svg"
plot "allocations_riskMin_Reactive.csv" using 7 title "RiskMin Reactive" w lines ls 3,\
"allocations_riskMin_Proactive.csv" using 7 title "RiskMin Proactive" w lines ls 4

set yrange[0:7]
set ylabel "Risk Level"

set output "risks_offpeak_riskMin.svg"
plot "allocations_riskMin_Reactive.csv" using 6 title "RiskMin Reactive" w lines ls 3,\
"allocations_riskMin_Proactive.csv" using 6 title "RiskMin Proactive" w lines ls 4

set output "risks_offpeak_ecoMax.svg"
plot "allocations_ecoMax_Reactive.csv" using 6 title "EnergyEffMax Reactive" w lines ls 1,\
"allocations_ecoMax_Proactive.csv" using 6 title "EnergyEffMax Proactive" w lines ls 2
