%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%   Grammatikregler
%

s(s(NP,VP)) --> np(NP),vp(VP).

np(np(N)) --> pron(N).
np(np(N)) --> n(N).
np(np(N,PP)) --> n(N),pp(PP).
np(np(DET,N)) --> det(DET),n(N).
np(np(DET,N,PP)) --> det(DET),n(N),pp(PP).

vp(vp(V,NP)) --> v(V),np(NP).
vp(vp(V,NP,PP)) --> v(V),np(NP),pp(PP).

pp(pp(Prep,NP)) --> prep(Prep),np(NP).

%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%
%   Lexikon

pron(pron(han)) --> [han].
pron(pron(hon)) --> [hon].
v(v('målade')) --> ['målade'].
prep(prep(med)) --> [med].
prep(prep(i)) --> [i].
prep(prep(bakom)) --> [bakom].
det(det(en)) --> [en].
det(det(ett)) --> [ett].
n(n(man)) --> [man].
n(n(tavla)) --> [tavla].
n(n(pensel)) --> [pensel]. 
n(n(garaget)) --> [garaget].
n(n(huset)) --> [huset].


