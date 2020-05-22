# Plots Real IDIM and Pivot IDIM
# File schema is: distance,lidim,count,pivot_idim,count

setwd("/Users/al/repos/github/angles/src/main/R/")
source("FunctionBank.R")

path <- "/Users/al/repos/github/angles/results/SweepyIDIMExplorerLevinaBickel/"
filename <- "EUC20-200.csv"

conditionLoadIntoGlobal( paste(path,filename,sep=""), "idim" )

plot <- ggplot( idim, aes( x=distance ) ) +
  ggtitle( paste( "Local IDIM & Pivot IDIM Euc20" ) ) +
  geom_line( aes( y=lidim, colour="lidim" ) ) +
  geom_line(aes(y=pivot_idim, colour="pivot_idim" ) ) +
  ylab( "IDIM" ) +
  ylim(0,30) +
  xlab( "Distance" ) +
  theme(legend.position="bottom") +
  labs( color="") # no label on legend!

ggsave( "/tmp/lidim-pivot_idim.png",plot )


