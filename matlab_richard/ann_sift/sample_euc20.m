
fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_20_constrained.txt",'r');

euc20_dists_constrained = fscanf(fileID,"%f");
fclose(fileID);

fileID = fopen("/Users/al/repos/github/angles/results/justAngles/euc_20_unconstrained.txt",'r');

euc20_dists_unconstrained = fscanf(fileID,"%f");
fclose(fileID);

bins = 0:0.01:2.6;

%%

figure
hold on
histogram(euc20_dists_constrained,bins);
histogram(euc20_dists_unconstrained,bins);
xlabel('angle (radians)');
ylabel('count');
legend("euc20 constrained","euc20 unconstrained");
hold off
