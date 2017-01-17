set datafile separator ","
set terminal postscript eps enhanced color font 'Helvetica,20'
set output 'speedup.eps'
set xrange [1:16]
set xlabel 'Threads'
set ylabel 'Speedup'

set grid xtics
set grid ytics
set xtics 1

plot 'result_2' using 1:(15420/$2) title 'HarpMLR 2 mapper' with linespoint linewidth 5.0 pointtype 4 pointsize 2.5, \
     'result_4' using 1:(7961/$2) title 'HarpMLR 4 mapper' with linespoint linewidth 5.0 pointtype 3 pointsize 2.5, \

exit
