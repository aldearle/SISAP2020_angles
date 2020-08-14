# Plots the angles graphs

library("ggplot2")

setwd("/Users/al/repos/github/angles/src/main/scripts/R/")
source("FunctionBank.R")

path <- "/Users/al/repos/github/angles/results/SweepyAngleExplorerChavez/"
filename <- "EUC20-centre.csv"
conditionLoadIntoGlobal( paste(path,filename,sep=""), "table" )

plot <- ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Constrained angles for Euc20" ) ) +
  geom_line( aes( y=Mean.Angle, show.legend=T ) ) +
  ylab( "Angles (radians)" ) +
  ylim(0,180) +
  xlab( "Distance across unit cube" ) +
  theme(axis.title = element_text(size = 22), # axis.title.x = element_text(color = "blue", size = 20, face = "bold")
        plot.title=element_text(size = 22))

ggsave( "/tmp/EUC20_angles.png",plot )


plot <-  ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Std Deviations of angles for Euc20" ) ) +
  geom_line( aes( y=std.dev ) ) +
  ylab( "Angles (radians)" ) +
  ylim(0,20) +
  xlab( "Distance across unit cube" ) +
  theme(axis.title = element_text(size = 22),
        plot.title=element_text(size = 22))

ggsave( "/tmp/EUC20_std_dev.png",plot )

plot <- ggplot( table, aes( x=Distance ) ) +
  ggtitle( paste( "Points in hyper sphere for Euc20" ) ) +
  geom_line( aes( y=count ) ) +
  ylab( "Count" ) +
  ylim(0,1000000) +
  xlab( "Distance across unit cube" ) +
  theme(axis.title = element_text(size = 22),
        plot.title=element_text(size = 22))

ggsave( "/tmp/EUC20_count.png",plot )

