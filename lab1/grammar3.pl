%-*- Mode: Prolog -*-

% Grammatikregler
s --> np.

np --> det(Genus, Pluralitet, Def), jj(Genus, Pluralitet, Def), n(Genus, Pluralitet, Def, n).
np --> det(Genus, Pluralitet, Def), jj(Genus, Pluralitet, Def), n(Genus, Pluralitet, Def, g), n(_, _, i, n).

          
% Lexikon
% n, s, m -> n-ord (utrum), t-ord (neutrum), maskulinum
% s, p -> singularis, pluralis
% i, d -> indefinit form, definit form
% n, g -> nominativ, genitiv (finns enbart på substantiven, n/6)
det(n,s,i) --> [ en ].
det(t,s,i) --> [ ett ].
det(n,s,d) --> [ den ].
det(m,s,d) --> [ den ].
det(t,s,d) --> [ det ].
det(n,p,i) --> [ några ].
det(t,p,i) --> [ några ].
det(m,p,i) --> [ någre ].
det(_,p,d) --> [ de ].
% det(t,p,d) --> [ de ]. % är onödig

jj(n,s,i) --> [ gammal ].
jj(t,s,i) --> [ gammalt ].
% jj(m,_,d) --> [ gamle ].   % nej inte gamle i pluralis
jj(m,s,d) --> [ gamle ].
jj(_,_,d) --> [ gamla ].
% jj(m,p,i) --> [ gamle ]. % ???????????????????? ska vi ha denna?  "några gamle män". nej inte gamle i pluralis alls
jj(_,p,i) --> [ gamla ].

jj(n,s,i) --> [ 'röd' ].
jj(t,s,i) --> [ 'rött' ].
% jj(m,_,d) --> [ 'röde' ].  % inte röde i pluralis
jj(m,s,d) --> [ 'röde' ].
jj(_,_,d) --> [ 'röda' ].
% jj(m,p,i) --> [ 'röde' ].  % inte röde i pluralis
jj(_,p,i) --> [ 'röda' ].

% n(n,s,i) --> [ man ].
% n(n,s,i) --> [ man ].
% % n(n,s,d) --> [ mannen ].  % är onödig  (se jj(_,_,d) --> [ gamla ]. )
% n(m,s,d) --> [ mannen ].
% n(n,p,i) --> [ män ].
% n(m,p,d) --> [ männen ].
% n(n,s,i) --> [ mans ].    % genitiv
% n(m,s,d) --> [ mannens ]. % genitiv
% n(n,p,i) --> [ mäns ].    % genitiv 
% n(m,p,d) --> [ männens ]. % genitiv

n(n,s,i,n) --> [ man ].
n(m,s,i,n) --> [ man ].
n(n,s,d,n) --> [ mannen ].  % är onödig  (se jj(_,_,d) --> [ gamla ]. )
n(m,s,d,n) --> [ mannen ].
n(n,p,i,n) --> [ män ].
n(m,p,i,n) --> [ män ].
n(n,p,d,n) --> [ männen ].        % eg. onödig
n(m,p,d,n) --> [ männen ].
n(n,s,i,g) --> [ mans ].    % genitiv
n(m,s,i,g) --> [ mans ].    % genitiv
n(n,s,d,g) --> [ mannens ]. % genitiv   eg. onödig
n(m,s,d,g) --> [ mannens ]. % genitiv   
n(n,p,i,g) --> [ mäns ].    % genitiv
n(m,p,i,g) --> [ mäns ].    % genitiv 
n(n,p,d,g) --> [ männens ]. % genitiv   eg. onödig
n(m,p,d,g) --> [ männens ]. % genitiv

n(n,s,i,n) --> [ kvinna ].
n(n,s,d,n) --> [ kvinnan ].
n(n,p,i,n) --> [ kvinnor ].
n(n,p,d,n) --> [ kvinnorna ].
n(n,s,i,g) --> [ kvinnas ].    % genitiv
n(n,s,d,g) --> [ kvinnans ].   % genitiv
n(n,p,i,g) --> [ kvinnors ].   % genitiv
n(n,p,d,g) --> [ kvinnornas ]. % genitiv

n(t,_,i,n) --> [ bord ].    % bord har samma form indefinit i både pluralis och singularis. 
n(t,s,d,n) --> [ bordet ].
% n(t,p,i,n) --> [ bord ].  % är onödig
n(t,p,d,n) --> [ borden ].
n(t,_,i,g) --> [ bords ].   % genitiv
n(t,s,d,g) --> [ bordets ]. % genitiv
n(t,p,d,g) --> [ bordens ]. % genitiv

n(t,_,i,n) --> [ skal ]. % skal har samma form indefinit i både pluralis och singularis. 
n(t,s,d,n) --> [ skalet ].
n(t,p,d,n) --> [ skalen ].
n(t,_,i,g) --> [ skals ].         % genitiv
n(t,s,d,g) --> [ skalets ].       % genitiv
n(t,p,d,g) --> [ skalens ].       % genitiv

n(t,s,i,n) --> [ äpple ].
n(t,s,d,n) --> [ äpplet ].
n(t,p,i,n) --> [ äpplen ].
n(t,p,d,n) --> [ äpplena ].
n(t,s,i,g) --> [ äpples ].        % genitiv
n(t,s,d,g) --> [ äpplets ].       % genitiv
n(t,p,i,g) --> [ äpplens ].       % genitiv
n(t,p,d,g) --> [ äpplenas ].      % genitiv

n(n,s,i,n) --> [ kant ].
n(n,s,d,n) --> [ kanten ].
n(n,p,i,n) --> [ kanter ].
n(n,p,d,n) --> [ kanterna ].
n(n,s,i,g) --> [ kants ].         % genitiv
n(n,s,d,g) --> [ kantens ].       % genitiv
n(n,p,i,g) --> [ kanters ].       % genitiv
n(n,p,d,g) --> [ kanternas ].     % genitiv


