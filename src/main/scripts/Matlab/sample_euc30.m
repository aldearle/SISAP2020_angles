
fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_30_constrained.txt",'r');

euc30_dists_constrained = fscanf(fileID,"%f");
fclose(fileID);

fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_30_unconstrained.txt",'r');

euc30_dists_unconstrained = fscanf(fileID,"%f");
fclose(fileID);

bins = 0:0.01:2.6;

%%

figure
hold on
histogram(euc30_dists_constrained,bins);
histogram(euc30_dists_unconstrained,bins);

xlabel('angle (radians)',"FontSize",24);
ylabel('count',"FontSize",24);
title("Euc30 distributions","FontSize",24);
legend("constrained","unconstrained","FontSize",24);


hold off
