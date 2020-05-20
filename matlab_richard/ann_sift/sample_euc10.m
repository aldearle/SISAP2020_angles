
fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_10_constrained.txt",'r');

euc10_dists_constrained = fscanf(fileID,"%f");
fclose(fileID);

fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_10_unconstrained.txt",'r');

euc10_dists_unconstrained = fscanf(fileID,"%f");
fclose(fileID);

%%

figure
hold on
histogram(euc10_dists_constrained);
histogram(euc10_dists_unconstrained);
xlabel('angle (radians)');
ylabel('count');
legend("euc10 constrained","euc10 unconstrained");
hold off
