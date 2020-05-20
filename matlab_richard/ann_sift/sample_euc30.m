
fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_30_constrained.txt",'r');

euc30_dists_constrained = fscanf(fileID,"%f");
fclose(fileID);

fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_30_unconstrained.txt",'r');

euc30_dists_unconstrained = fscanf(fileID,"%f");
fclose(fileID);

%%

figure
hold on
histogram(euc30_dists_constrained);
histogram(euc30_dists_unconstrained);
xlabel('angle (radians)');
ylabel('count');
legend("euc30 constrained","euc30 unconstrained");
hold off
