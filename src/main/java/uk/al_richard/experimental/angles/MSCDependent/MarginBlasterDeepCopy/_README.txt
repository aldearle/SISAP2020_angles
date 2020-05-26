Notes for Richard

1.  I have created a new package package uk.al_richard.branch_git_experiment.MarginBlasterDeepCopy;
    This has no dependencies on the old MSF.

2.  I cannot test this - I don't have the data.

3.  CommonBase converts from float[] to double[] not sure if it needs to.

4.  PopulateMap in LIDIMtoAngleMap uses Volume thresholds Not sure if this is legal.
    We could use the NN info instead.

5.  Data paths are almost certainly wrong.

6.  If any of that works should beable to run TestMarginsBalls