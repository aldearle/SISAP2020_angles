# Plots the angles graphs

setwd("/Users/al/repos/github/angles/src/main/R/")
source("FunctionBank.R")

path <- "/Users/al/repos/github/angles/results/SweepyAngleExplorerChavez/"
filename <- "EUC20-centre.csv"
conditionLoadIntoGlobal( paste(path,filename,sep=""), "table" )

plot <- ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Constrained angles for Euc20" ) ) +
  geom_line( aes( y=Mean.Angle, show.legend=T ) ) +
  ylab( "Angles" ) +
  ylim(0,180) +
  xlab( "Distance" )

ggsave( "/tmp/angles.png",plot )


plot <-  ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Std Deviations of angles for Euc20" ) ) +
  geom_line( aes( y=std.dev ) ) +
  ylab( "Angles" ) +
  ylim(0,20) +
  xlab( "Distance" )

ggsave( "/tmp/std_dev.png",plot )

plot <- ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Points in hyper sphere for Euc20" ) ) +
  geom_line( aes( y=count ) ) +
  ylab( "Count" ) +
  ylim(0,1000000) +
  xlab( "Distance" )

ggsave( "/tmp/count.png",plot )

