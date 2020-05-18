
viewpoint = data(:,floor(rand() * 1000000) + 1);
viewpoint = zeros(128,1);

r_angles = zeros(100000,1);
s_angles = zeros(100000,1);


for queryNo = 1 : 1000
    q = queries(:,queryNo);
    
    
    for i = 1 : 100
        r1 = floor(rand() * 1000000) + 1;
        sample = data(:,r1);
        
        
        d1 = euc(viewpoint,q);
        d2 = euc(viewpoint,sample);
        d3 = euc(sample,q);
        
        cosTh = (d1 * d1 + d3 * d3 - d2 * d2) / (2 * d1 * d3);
        r_angles((queryNo - 1) * 100 + i) = acos(cosTh);
    end
    %%
    
    
    for i = 1 : 100
        r1 = ground_truth(i,queryNo);
        sample = data(:,r1);
        
        
        d1 = euc(viewpoint,q);
        d2 = euc(viewpoint,sample);
        d3 = euc(sample,q);
        
        cosTh = (d1 * d1 + d3 * d3 - d2 * d2) / (2 * d1 * d3);
        s_angles((queryNo - 1) * 100 + i) = acos(cosTh);
    end
end


%%


figure
hold on
histogram(r_angles);
histogram(s_angles);
legend("random","nn");
hold off
