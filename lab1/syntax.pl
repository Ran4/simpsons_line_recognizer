% -------------------------------------------------------------- %

% labb4.pl
%
% Johan Boye

% -------------------------------------------------------------- %


:- use_module( library( codesio )).
:- ensure_loaded( drawtree ).
:- ensure_loaded( sentences ).


% -------------------------------------------------------------- %


% Analyses a phrase. Input the phrase as a string ending with a period '.'
% e.g. like this:
%
%  analyse( "en gammal man" ).
%
% The predicate will print out "GRAMMATICAL" or "NOT GRAMMATICAL" depending
% on the result of the analysis
%
analyse( S ) :-
	string_to_word_list( S, L ),
	( s( L, [] ) ->
	    format( "~s GRAMMATICAL\n", [S] )
	;
	    format( "~s NOT GRAMMATICAL\n", [S] )
	).

% Analyses a phrase which is supposed to be correct Swedish. Input the phrase as
% for the previous predicate.
%
% If the phrase is not grammatical according to the grammar, "NOT GRAMMATICAL" 
% will be printed, otherwise nothing will happen.
%
analyse_correct_phrase( S ) :-
	string_to_word_list( S, L ),
	( s( L, [] ) ->
	    true  %  The phrase is grammatical, as it should be.
	;
	    format( "~s NOT GRAMMATICAL\n", [S] )
	).

% Analyses a phrase which is supposed to be incorrect Swedish. Input the phrase as
% for the previous predicate.
%
% If the phrase is grammatical according to the grammar, "GRAMMATICAL" will be printed,
% otherwise nothing will happen.
%
analyse_incorrect_phrase( S ) :-
	string_to_word_list( S, L ),
	( s( L, [] ) ->
	    % The phrase is grammatical, although it should not be.
	    format( "~s GRAMMATICAL\n", [S] )
	;
	    % The phrase is ungrammatical, as it should be.
	    true
	).


tree_analyse( S, Tree ) :-
	string_to_word_list( S, L ),
	s( Tree, L, [] ),
	pp_termtree( Tree ).

analyse_all_phrases :-
	findall( S, (sentence(S), analyse(S)), _ ).

analyse_correct_phrases :-
	findall( S, (sentence(S,correct), analyse_correct_phrase(S)), _ ).

analyse_incorrect_phrases :-
	findall( S, (sentence(S,incorrect), analyse_incorrect_phrase(S)), _ ).


% -------------------------------------------------------------- %


string_to_word_list( String, Words ) :-
	open_codes_stream( String, Stream ),
	read_phrase( Stream, Words ).


% ---------- %


read_phrase( Stream, S ) :-
  get_code( Stream, C ),
  on_exception( 
      illegal_character_exception(_Char),
      read_phrase_1( Stream, C, S ),
      S=[] 
  ).


read_phrase( Stream, S, G ) :-
  get_code( Stream, C ),
  on_exception( 
      illegal_character_exception(Char),
      ( read_phrase_1( Stream, C, S ),
        get_code( Stream, D ),
        read_phrase_1( Stream, D, G )
      ),
      ( format("Illegal character '~c'\n", Char), S=[] )
  ).


% ---------- %


% A period '.' ends the phrase
read_phrase_1( _Stream, 46, [] ) :-
  !.

% Skip whitespace
read_phrase_1( Stream, C, S ) :-
  whitespace( C ),
  !,
  get_code( Stream, C0 ),
  read_phrase_1( Stream, C0, S ).

% Read a new word
read_phrase_1( Stream, C, [W|Ws] ) :-
  allowed_char( C ),
  !,
  read_word( Stream, C, LastChar, Cs ),
  atom_codes( W, Cs ),
  read_phrase_1( Stream, LastChar, Ws ).

% There is some garbage in the input
read_phrase_1( _Stream, C, _ ) :-
  raise_exception( illegal_character_exception(C) ).


% ---------- %


read_word( _Stream, 46, 46, [] ) :-
  !.
read_word( _Stream, C, C, [] ) :-
  whitespace( C ),
  !.
read_word( Stream, C, L, [C|Cs] ) :-
  allowed_char( C ),
  !,
  get_code( Stream, C0 ),
  read_word( Stream, C0, L, Cs ).
read_word( _Stream, C, _, _ ) :-
  raise_exception( illegal_character_exception(C) ).


% ---------- %


whitespace( 9 ).
whitespace( 10 ).
whitespace( 13 ).
whitespace( 32 ).


% ---------- %


allowed_char( C ) :-
  number( C ),
  ( C >= 97, C =< 122 ) ;
  ( C >= 65, C =< 90 ) ;
  ( C >= 48, C =< 57 ) ;
  C = 39 ;
  C = 40 ;
  C = 41 ;
  C = 44 ;
  C = 196 ;
  C = 197 ;
  C = 214 ;
  C = 228 ;
  C = 229 ;
  C = 246 ;
  % Wide characters
  C = 195 ;
  C = 165 ; % å
  C = 164 ; % ä
  C = 182 ; % ö
  C = 133 ; % Å
  C = 132 ; % Ä
  C = 150.  % Ö



