library(Kendall);

path <- "/Users/al/repos/github/angles/results/SweepyIDIMExplorerLevinaBickel/"

euc_10 <- "Euc10-500.csv"
euc_20 <- "Euc20-500.csv"
euc_30 <- "Euc30-500.csv"

euc10_df <- read.table(paste(path,euc_10,sep=""), header = TRUE, sep = ",", dec = ".");
euc20_df <- read.table(paste(path,euc_20,sep=""), header = TRUE, sep = ",", dec = ".");
euc30_df <- read.table(paste(path,euc_30,sep=""), header = TRUE, sep = ",", dec = ".");

# 1 is perfect
# -1 is not correlated

# p-value: null hypothesis that proposes there relationship between these two data-sets
# Seek p-value < 0.05

print('Euc10:')
k10 <- Kendall(euc10_df$IDIM,euc10_df$PIV_IDIM);
summary( k10 );

print('Euc20:')
k20 <- Kendall(euc20_df$IDIM,euc20_df$PIV_IDIM);
summary( k20 );

print('Euc30:')
k30 <- Kendall(euc30_df$IDIM,euc30_df$PIV_IDIM);
summary( k30 );







