// Copied from CMOS Analog Circuit Design by Phillip E. Allen and Douglas R. Holberg
// Oxford University Press, Inc. , 2012
simulator lang=spice
M1 2 1 0 0 MOSN W=5U L=1U
M2 2 3 4 4 MOSP W=5U L=1U
M3 3 3 4 4 MOSP W=5U L=1U
CL 2 0 5P
R1 3 0 100K
VDD 4 0 DC 5.0
VIN 1 0 PWL(0 0V 1U 0V 1.05U 3V 3U 3V 3.05U 0V 6U 0V)
.MODEL MOSN NMOS VTO = 0.7 KP = 110U GAMMA = 0.4 LAMBDA = 0.04
+ PHI = 0.7
.MODEL MOSP PMOS VTO = 20.7 KP = 50U GAMMA = 0.57 LAMBDA = 0.05
+ PHI = 0.8
.TRAN 0.01U 4U
.END