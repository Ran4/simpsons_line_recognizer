echo "\n"





echo "With minimum_words_per_file = 2\n\n\n"


echo "Without stoplist:"
echo -n "Shortest word == 3: "
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "

echo -n "Shortest word == 4: "
sed -i "s/shortest_word = 3/shortest_word = 4/" TrainHP.properties
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "
sed -i "s/shortest_word = 4/shortest_word = 3/" TrainHP.properties

echo "\nWith stoplist:"
sed -i "s/stoplist = False/stoplist = True/" TrainHP.properties

echo -n "Shortest word == 3: "
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "

echo -n "Shortest word == 4: "
sed -i "s/shortest_word = 3/shortest_word = 4/" TrainHP.properties
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "
sed -i "s/shortest_word = 4/shortest_word = 3/" TrainHP.properties

sed -i "s/stoplist = True/stoplist = False/" TrainHP.properties


echo "\n\n\nWith minimum_words_per_file = 4"
sed -i "s/minimum_words_per_file = 2/minimum_words_per_file = 4/" TrainHP.properties


echo "Without stoplist:"
echo -n "Shortest word == 3: "
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "

echo -n "Shortest word == 4: "
sed -i "s/shortest_word = 3/shortest_word = 4/" TrainHP.properties
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "
sed -i "s/shortest_word = 4/shortest_word = 3/" TrainHP.properties

echo "\nWith stoplist:"
sed -i "s/stoplist = False/stoplist = True/" TrainHP.properties

echo -n "Shortest word == 3: "
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "

echo -n "Shortest word == 4: "
sed -i "s/shortest_word = 3/shortest_word = 4/" TrainHP.properties
java -Xmx1000m -cp lib:RI:OrdHP OrdHP ../ri_dataset1/ test1.dat | grep "Correct: "
sed -i "s/shortest_word = 4/shortest_word = 3/" TrainHP.properties

sed -i "s/stoplist = True/stoplist = False/" TrainHP.properties


sed -i "s/minimum_words_per_file = 4/minimum_words_per_file = 2/" TrainHP.properties
