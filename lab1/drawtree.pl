
% drawtree.pl
%
% Pretty-print trees using ascii characters.
% Richard Tucker / July 1996.
%

%  Provides predicates:
%    pp_tree/1
%    pp_trees/1
%    pp_termtree/1
%
%  Data structure for trees:
%    Tree = leaf(Label)
%         | tree(Label, list of Trees)
%         | beside(list of Trees)          < these two are really for
%         | detatched(Tree)                < fancy formatting
%    where Label = any prolog term*
%
% 'tree' and 'leaf' are self-explanatory
% 'beside' is used to display trees side by side
% 'detatched' cuts a tree off from the enclosing tree -- see example below
%
% * footnote: variables will be displayed as _A, _B etc.
%             cyclic terms in labels won't work (nor will infinite trees!)
%
% The predicate pp_tree(T) prints the tree T.
% The predicate pp_termtree(T) prints the prolog term T as a tree.
% There are also predicates for turning a tree into a list of character
% strings, if you don't want to print it out straight away.
%
% EXAMPLES
%
% ?- pp_tree(tree(np, [tree(det,[leaf(the)]), tree(n,[leaf(house)])])).
%       |
%      np
%       |
%    +-----+
%    |     |
%   det    n
%    |     |
%    |     |
%   the  house
%
%
% ?- pp_tree(beside([tree([0,1], [leaf(0), leaf(1)]),
%                    leaf(2),
%                    tree([3], [leaf(3)])])).
%    |     |   |
%   [0,1]  |  [3]
%    |     |   |
%   +--+   |   |
%   |  |   |   |
%   0  1   2   3
%
% ?- pp_tree(tree('numbers',
%             [detatched(leaf(one)), detatched(leaf(two)), leaf(three)])).
%         |
%      numbers
%         |
%    +----+-----+
%               |
%    |    |     |
%   one  two  three
%
% ?- pp_termtree(spong(wibble, foo(bar), [red, green, blue])).
%              |
%            spong
%              |
%      +-----+---------+
%      |     |         |
%      |     |         .
%      |     |         |
%      |     |    +---------+
%      |     |    |         |
%      |     |    |         .
%      |     |    |         |
%      |     |    |     +--------+
%      |     |    |     |        |
%      |     |    |     |        .
%      |    foo   |     |        |
%      |     |    |     |      +----+
%      |     |    |     |      |    |
%   wibble  bar  red  green  blue  []
%
%
% FORMATTING OPTIONS
%
% The dynamic predicates heavy_node/1 and stretchy_node/1 control
% the tree formatting. With no assertions for either of these predicates,
% trees will be ragged at the bottom, like this:
%         |
%         a
%         |
%   +--+---+----+
%   |  |   |    |
%   b  s   e    h
%      |   |    |
%      |  +--+  |
%      d  |  |  j
%         f  i
%         |
%         |
%         g
%
% Any subtree T which matches a heavy_node(T) or stretchy_node(T) clause
% will 'fall' to the lowest possible level. If the node is 'heavy', the
% subtree label will fall too. If it is 'stretchy', the tree will stretch
% between the label and the subtree. Eg, with the following assertions:
%   heavy_node(tree(h,_)).
%   stretchy_node(tree(s,_)).
% the tree above would look like this:
%         |
%         a
%         |
%   +--+---+----+
%   |  |   |    |
%   b  s   e    |   <----- subtree s(d) is stretchy
%      |   |    |
%      |  +--+  |
%      |  |  |  |
%      |  f  i  h     <--- subtree h(j) is heavy
%      |  |     |
%      |  |     |
%      d  g     j
%
% The default setting is for all leaves and all tree-nodes with one
% daughter to be heavy, and for all other tree-nodes to be stretchy.
% This is the setting used in the first four examples.
%
% ANNOYING FEATURES
%
% In displaying labels, no attention is paid to operator declarations.
% Strings and lists are done as special cases, but other things will give
% ugly results, eg (a->b) gets printed as ->(a,b).
%
% The trees look 'orrible when they get wider than the terminal and
% start to wrap around.
%
% Code could be faster if it used difference lists, but at the moment
% it doesn't 'cos the same list may get many other lists appended
% to it, and we'd have to use copy_term to make it work. Yuk.
%

% dynamic parameters

:- dynamic heavy_node/1, stretchy_node/1.

% default settings

heavy_node(leaf(_)).
heavy_node(tree(_, [_])).
stretchy_node(tree(_, _)).

% main predicates

pp_trees(Ts) :-
 pp_tree(beside(Ts)).

pp_tree(T) :-
 copy_term(T, TT),
 numbervars(TT, 0, _),
 tree_to_unitree(TT, U, _, _),
 tree_height(U, Ht),
 tree_to_block(U, Ht, B),
 pp_block(B).

pp_termtree(T) :-
 copy_term(T, U),
 term_to_mytree(U, R),
 pp_tree(R).

pp_block([]) :-
 nl.

pp_block([H|T]) :-
 format("~s", [H]),
 nl,
 pp_block(T).

term_to_mytree(V, leaf(V)) :-
 var(V),
 !.

term_to_mytree(T, leaf(T)) :-
 atomic(T),
 !.

term_to_mytree(T, tree(Label, Subs)) :-
 T =.. [Label|Sub1],
 terms_to_mytrees(Sub1, Subs).

terms_to_mytrees([], []).

terms_to_mytrees([H|T], [L|R]) :-
 term_to_mytree(H, L),
 terms_to_mytrees(T, R).

% there aretwo main stages of processing
% 1: Tree --> Unitree
% 2: Unitree --> Block
%
% Unitree representation:
%
% tree is
%  t(Stretch, TopUnit, BotUnits, MinBH)
% leaf is
%  Unit
%
% Stretch is true or false
%
% a unit is
%  u(TopLines, RepLine, BotLines, MinH)
%
% a Block is just a list of strings

% STAGE 1
% convert a tree to a unitree

tree_to_unitree(detatched(Tree), t(true,u([],Rstr,[Rstr],1),[Bot],MH), W, I) :-
 tree_to_unitree(Tree, Bot, W, I),
 tree_height(Bot, BH),
 MH is BH+1,
 n_string(W,0' ,Rstr).

tree_to_unitree(leaf(Label), u(T,R,B,MinH), W, I) :-
 label_text(Label, Str),
 length(Str, L0),
 W is L0+2, % padding parameter here
 I is W // 2,
 MinH=2,
 sp_pad(Str, W, 1, Lstr),
 sp_pad("|", W, I, Vstr),
 sp_pad(" ", W, 1, Sstr),
 (  heavy_node(leaf(Label))
 -> T=[Vstr],
    R=Vstr,
    B=[Lstr]
 ;  T=[Vstr, Lstr],
    R=Sstr,
    B=[]
 ).

tree_to_unitree(tree(Label, Trees), t(Stch, u(T,R,B,MinH), Bot, MH), W, I) :-
 package_subtrees(Trees, UnderTree, MBH, Wu, Iu),
 MinH=3,
 MH is MinH+MBH,
 label_text(Label, Str),
 length(Str, L0),
 W is max(L0+2, Wu), % padding parameter here
 (  W > Wu
 -> Lsp is (W-Wu) // 2,
    Rsp is Lsp + (W-Wu) mod 2,
    unit_pad(Lsp, Lpad),
    unit_pad(Rsp, Rpad),
    Bot=[Lpad, UnderTree, Rpad],
    I is Iu+Lsp
 ;  Bot=[UnderTree],
    I = Iu
 ),
 Ilab is min(W-L0-1, max(1,I-(L0//2))), % centre label if possible
 sp_pad(Str, W, Ilab, Lstr),
 sp_pad("|", W, I, Vstr),
 (  heavy_node(tree(Label, Trees))
 -> Stch=true,
    T=[Vstr],
    R=Vstr,
    B=[Lstr,Vstr]
 ;  stretchy_node(tree(Label, Trees))
 -> Stch=true,
    T=[Vstr,Lstr],
    R=Vstr,
    B=[Vstr]
 ;  Stch=false,
    T=[Vstr,Lstr],
    R=Vstr,
    B=[Vstr]
 ).


tree_to_unitree(beside(Trees), t(false, u([],Rstr,[],0), B, MH), W, I) :-
 trees_to_unitrees(Trees, B, Ws, Is),
 maxminh(B, 0, MH),
 indent_acc(Ws, Is, 0, W, _),
 I is W//2,
 n_string(W,0' ,Rstr).

%

package_subtrees(Trees, t(false, u([],Rstr,Tstrs,TLH), B, MBH), MinH, W, I) :-
 trees_to_unitrees(Trees, B, Ws, Is),
 indent_acc(Ws, Is, 0, W, Il),
 build_top_lines(Il, W, Tstrs, TLH),
 first_and_last(Il, Ileft, Iright),
 I is Ileft+(Iright-Ileft)//2,
 sp_pad("|", W, I, Rstr),
 maxminh(B, 0, MBH),
 MinH is TLH+MBH.

%

trees_to_unitrees([], [], [], []).

trees_to_unitrees([H|R], [T|Ts], [W|Ws], [I|Is]) :-
 tree_to_unitree(H, T, W, I),
 trees_to_unitrees(R, Ts, Ws, Is).

%

maxminh([], X, X).

maxminh([H|T], X, Y) :-
 tree_height(H, Ht),
 Z is max(X, Ht),
 maxminh(T, Z, Y).

tree_height(t(_,_,_,H), H).
tree_height(u(_,_,_,H), H).

%

first_and_last([X], X, X).

first_and_last([X,Y|Z], X, K) :-
 first_and_last([Y|Z], _, K).

%

indent_acc([], [], W, W, []).

indent_acc([W0|Ws], [I0|Is], Acc, W, [I|Ir]) :-
 I is I0+Acc,
 Acc1 is Acc+W0,
 indent_acc(Ws, Is, Acc1, W, Ir).

unit_pad(W, u([], R, [], 0)) :-
 n_string(W, 0' , R).

%

label_text(L, Txt) :-
 atomic(L), !,
 name(L, Txt).

label_text('$VAR'(N), [0'_,M|T]) :-
 !,
 M is 0'A + N mod 26,
 (  N > 25
 -> K is 0'0+(N // 26),
    T = [K]
 ;  T = []
 ).

label_text([H|T], R) :-
 is_string([H|T]),
 !,
 concat_lists(["""",[H|T],""""], R).

label_text([H0,H1|T], R) :-
 !,
 label_text(H0, Hstr),
 label_text([H1|T],[0'[|K]),
 concat_lists(["[",Hstr,",",K], R).

label_text([H], R) :-
 !,
 label_text(H, Hstr),
 concat_lists(["[",Hstr,"]"], R).

label_text([H|T], R) :-
 !,
 label_text(H, Hstr),
 label_text(T, Tstr),
 concat_lists(["[",Hstr,"|",Tstr,"]"], R).

label_text(X, R) :-
 X =.. [F|A],
 label_text(F, Fstr),
 label_texts(A, Astr),
 concat_lists([Fstr,"(",Astr], R).

label_texts([H], R) :-
 !,
 label_text(H, Hstr),
 concat_lists([Hstr,")"], R).

label_texts([H|T], R) :-
 label_text(H, Hstr),
 label_texts(T, Tstr),
 concat_lists([Hstr,",",Tstr], R).

%

is_string([]).

is_string([H|T]) :-
 integer(H),
 H>31,
 H<256,
 is_string(T).

%

sp_pad(Txt, W, I, L) :-
 length(Txt, Lt),
 Rp is W-Lt-I,
 n_string(Rp, 0' , LR),
 n_string(I, 0' , LL),
 concat_lists([LL, Txt, LR], L).

n_string(0, _, []).
n_string(X, C, [C|T]) :-
 X > 0,
 Y is X-1,
 n_string(Y, C, T).

%

build_top_lines([_], _, [], 0).

build_top_lines([I0,I1|T], W, [Line], 1) :-
 b_t_l(0, I0, 0' , [I1|T], W, Line).

b_t_l(X, X, _, [], W, [0'+|L]) :-
 Z is W-1,
 n_string(Z, 0' , L).

b_t_l(X, X, _, [I0|Is], W, [0'+|L]) :-
 Z is W-1,
 Y is X+1,
 b_t_l(Y, I0, 0'-, Is, Z, L).

b_t_l(X, I, C, Is, W, [C|L]) :-
 X < I,
 Y is X+1,
 Z is W-1,
 b_t_l(Y, I, C, Is, Z, L).


% STAGE 2
% convert a unitree to a block (of the required height)

tree_to_block(u(T,R,B,MinH), Ht, Block) :-
 stretch_unit(u(T,R,B,MinH), Ht, Block, []).

tree_to_block(t(false,T,B,_), Ht, L) :-
 unit_to_block(T, L, R, H),
 Ht1 is Ht-H,
 trees_to_block(B, Ht1, R).

tree_to_block(t(true,T,B,MinH), Ht, L) :-
 Str is Ht-MinH,
 stretch_by(T, Str, L, R),
 tree_height(T, HH),
 BH is MinH-HH,
 trees_to_block(B, BH, R).

% do same for a list of trees, and glue the results together

trees_to_block(Trees, Ht, Block) :-
 trees_to_blocks(Trees, Ht, Blocks),
 append_blocks(Ht, Blocks, Block).

trees_to_blocks([], _, []).

trees_to_blocks([H|T], Ht, [B|R]) :-
 tree_to_block(H, Ht, B),
 trees_to_blocks(T, Ht, R).

% unit_to_block converts a unit to a block without stretching

unit_to_block(u(T,R,B,H), L, Lr, H) :-
 stretch_top(T, R, B, 0, L, Lr).

% stretch_unit converts a unit to a block of the required height

stretch_by(u(T,R,B,_), Str, L, Lr) :-
 stretch_top(T, R, B, Str, L, Lr).

stretch_unit(u(T,R,B,MinH), Ht, L, Lr) :-
 Str is Ht-MinH,
 Str >= 0,
 stretch_top(T, R, B, Str, L, Lr).

stretch_top([H|T], Rep, Bot, Str, [H|R], Lr) :-
 stretch_top(T, Rep, Bot, Str, R, Lr).

stretch_top([], Rep, Bot, Str, L, Lr) :-
 stretch_rep(Str, Rep, Bot, L, Lr).

stretch_rep(Str, Rep, Bot, [Rep|R], Lr) :-
 Str > 0,
 Str1 is Str-1,
 stretch_rep(Str1, Rep, Bot, R, Lr).

stretch_rep(0, _, Bot, L, Lr) :-
 stretch_bot(Bot, L, Lr).

stretch_bot([H|T], [H|L], Lr) :-
 stretch_bot(T, L, Lr).

stretch_bot([], L, L).

% append_blocks(Ht, Blocks, Block)

append_blocks(0, _, []).

append_blocks(Ht, Ls, [H|R]) :-
 Ht > 0,
 Ht1 is Ht-1,
 block_top_rest(Ls, Hs, Ts),
 concat_lists(Hs, H),
 append_blocks(Ht1, Ts, R).

% take a block and return a list of first lines and the remaining block

block_top_rest([], [], []).

block_top_rest([[H|T]|R], [H|Hs], [T|Ts]) :-
 block_top_rest(R, Hs, Ts).

% take a list of lists and append them all in sequence

concat_lists([], []).

concat_lists([H], H) :- !.

concat_lists([H|T], L) :-
 concat_list(H, L, R),
 concat_lists(T, R).

concat_list([], L, L).

concat_list([H|T], [H|L], R) :-
 concat_list(T, L, R).

%----------------------------------------------------------------------------


