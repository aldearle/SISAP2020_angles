# Plots the angles graphs

library("ggplot2")

setwd("/Users/al/repos/github/angles/src/main/scripts/R/")
source("FunctionBank.R")

path <- "/Users/al/repos/github/angles/results/SweepyIDIMExplorerLevinaBickel/"
filename <- "Euc20-500.csv"
conditionLoadIntoGlobal( paste(path,filename,sep=""), "idim" )

plot <- ggplot( idim, aes( x=Distance ) ) +
  ggtitle( paste( "LIDIM Euc20" ) ) +
  geom_line( aes( y=IDIM ) ) +
  ylab( "IDIM" ) +
  ylim(0,40) +
  xlab( "Distance across unit cube" ) +
  theme(axis.title = element_text(size = 22), # axis.title.x = element_text(color = "blue", size = 20, face = "bold")
        plot.title=element_text(size = 22))

ggsave( "/tmp/EUC20_lidim.png",plot )

