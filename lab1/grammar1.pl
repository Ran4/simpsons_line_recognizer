%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%   Grammatikregler
%

s --> np,vp.

np --> pron.
np --> n.
np --> n,pp.
np --> det,n.
np --> det,n,pp.

vp --> v,np.
vp --> v,np,pp.

pp --> prep,np.

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%   Lexikon

pron --> [han].
pron --> [hon].
v --> ['målade'].
prep --> [med].
prep --> [i].
prep --> [bakom].
det --> [en].
det --> [ett].
n --> [man].
n --> [tavla].
n --> [pensel]. 
n --> [garaget].
n --> [huset].
n --> [garagen].


