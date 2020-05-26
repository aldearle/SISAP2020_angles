% data = fvecs_read("/Volumes/Data/ann_sift/siftsmall/siftsmall_base.fvecs");
% queries = fvecs_read("/Volumes/Data/ann_sift/siftsmall/siftsmall_query.fvecs");
% ground_truth = ivecs_read("/Volumes/Data/ann_sift/siftsmall/siftsmall_groundtruth.ivecs");

%%

data = fvecs_read("/Volumes/Data/ann_sift/sift/sift_base.fvecs");
queries = fvecs_read("/Volumes/Data/ann_sift/sift/sift_query.fvecs");
ground_truth = ivecs_read("/Volumes/Data/ann_sift/sift/sift_groundtruth.ivecs");

%% and normalise the vectors so that we really have the correct framework


origin = zeros(128,1);

mags_q = euc(origin, queries);
queries = queries ./ mags_q;

mags_d = euc(origin, data);
data = data ./ mags_d;

%% : check ground truth
% 
% q1 = queries(:,1);
% %assume the gt file has one gt per column
% q1NNs = ground_truth(:,1);
% 
% for n = 1 : 100
%     nnN = q1NNs(n,1);
%     q1NNs(n,2) = euc(q1,data(:,nnN + 1));
% end

%%

clear mags_d mags_q origin
