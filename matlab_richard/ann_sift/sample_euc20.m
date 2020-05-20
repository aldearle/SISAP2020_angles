
fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_20_constrained.txt",'r');

euc20_dists_constrained = fscanf(fileID,"%f");
fclose(fileID);

fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_20_unconstrained.txt",'r');

euc20_dists_unconstrained = fscanf(fileID,"%f");
fclose(fileID);

%%

figure
hold on
histogram(euc20_dists_constrained);
histogram(euc20_dists_unconstrained);
xlabel('angle (radians)');
ylabel('count');
legend("euc20 constrained","euc20 unconstrained");
hold off
