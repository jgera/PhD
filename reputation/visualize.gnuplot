set term svg enhanced size 640,480

set xrange[20:4000]

set xlabel "time"
set key outside top horizontal

set style line 1 lw 2 lc rgb "#555555"
set style line 2 lw 1 lc rgb "#000000"
set style line 3 lw 2 lc rgb "#000000" lt 2
set style line 4 lw 2 lc rgb "#444444"

set output "reputations.svg"

pr = "./providers.csv"
e = "./earnings.csv"

set ylabel "Reputation"

plot pr using 3 w lines ls 1 title "Random",\
pr using 7 w lines ls 2 title "Revenue Maximization",\
pr using 11 w lines ls 3 title "Reputation Maximization"
#pr using 15 w lines ls 4 title "context-aware"

set output "earnings.svg"
set ylabel "Spot revenue"
plot e using 2 w lines ls 1 title "Random",\
e using 3 w lines ls 2 title "Revenue Maximization",\
e using 4 w lines ls 3 title "Reputation Maximization"

