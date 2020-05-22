# Plots the angles graphs

setwd("/Users/al/repos/github/angles/src/main/R/")
source("FunctionBank.R")

path <- "/Users/al/repos/github/angles/results/SweepyIDIMExplorerLevinaBickel/"
filename <- "Euc20-500.csv"
conditionLoadIntoGlobal( paste(path,filename,sep=""), "idim" )

plot <- ggplot( idim, aes( x=Distance ) ) +
  ggtitle( paste( "LIDIM Euc20" ) ) +
  geom_line( aes( y=IDIM ) ) +
  ylab( "IDIM" ) +
  ylim(0,40) +
  xlab( "Distance" )

ggsave( "/tmp/lidim.png",plot )

