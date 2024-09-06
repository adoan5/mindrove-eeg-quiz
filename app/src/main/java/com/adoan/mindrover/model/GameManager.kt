package com.adoan.mindrover.model

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adoan.mindrover.MainActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
//import org.jetbrains.kotlinx.dl.dataset.Dataset

enum class GameCommand {
    CHOOSE, REST, GO_UP, GO_DOWN, PAUSE
}

data class Question(val question: String, val answers: Array<String>, var correctIdx: Int) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Question

        return answers.contentEquals(other.answers)
    }

    override fun hashCode(): Int {
        return answers.contentHashCode()
    }

    fun shuffle(): Question {
        val answer = answers[correctIdx]
        answers.shuffle()
        correctIdx = answers.indexOf(answer)
        return this
    }

}

class GameManager(): ViewModel() {

//    var network: NeuralNetwork? = null
    private var _eegDataListener: EEGDataListener? = null

    private val _actualCommand = MutableLiveData<GameCommand>()
    val actualCommand: LiveData<GameCommand> = _actualCommand

    private val _actualQuestion = MutableLiveData<Question>()
    val actualQuestion: LiveData<Question> = _actualQuestion

    @SuppressLint("StaticFieldLeak")
    private var _mainActivity: MainActivity? = null

    var questions: Array<Question> = arrayOf()

    var gameStarted = false

    var correctAnswer = false

    private val _points = MutableLiveData<Int>(-1)
    val points: LiveData<Int> = _points

    private val _running = MutableLiveData<Boolean>()
    val running: LiveData<Boolean> = _running

    private var rightThreshold: Int = -2000
    private var leftThreshold: Int = 5000


   fun setRightThreshold(threshold: Int) {
       rightThreshold = threshold
   }
   fun setLeftThreshold(threshold: Int) {
       leftThreshold = threshold
   }


    fun transformEEG(data: Array<Array<Double>>, averages: Array<Double>? = null, deviations: Array<Double>? = null): FloatArray {

        var floatArray: Array<Float> = arrayOf()

        if (averages != null && deviations != null) {
            for (ch in 0..< data.size) {
                for (time in 0..<data[0].size) {
                    data[ch][time] = (data[ch][time] - averages[ch]) / deviations[ch]
                }
            }
        }

        for (ch in 0..<data.size) {
            for (time in 0..<data[0].size) {
                floatArray = floatArray.plus(data[ch][time].toFloat())
            }
        }

        return floatArray.toFloatArray()
    }

    fun setMainActivity(mainActivity: MainActivity) {
        _mainActivity = mainActivity
    }


    fun cancelGame() {
        gameStarted = false
        _running.postValue(false)
    }

    private fun nextCommand(eeg: Array<Array<Double>>, pos: Array<Array<Int>>) {

        Log.d("NEXT position", "${pos[0].average()} - ${pos[1].average()} - ${pos[2].average()}")
        val averageZ = pos[2].average()
        Log.d("Data average", "${eeg[0].average()} - ${eeg[1].average()} - ${eeg[2].average()}")


//        val response = _mainActivity?.predict(arrayOf(transformEEG(eeg, _mainActivity?.averages, _mainActivity?.deviations)))
        val response = _mainActivity?.predict(arrayOf(transformEEG(eeg, null, null)))

        if (averageZ > leftThreshold) {
            _actualCommand.value = GameCommand.GO_DOWN
        } else if (averageZ < rightThreshold) {
            _actualCommand.value = GameCommand.GO_UP
        } else if (response!![0] > 0.55) {
            _actualCommand.value = GameCommand.CHOOSE
        } else {
            _actualCommand.value = GameCommand.REST
        }

    }

    fun setEEGDataListener(eegListener: EEGDataListener) {
        _eegDataListener = eegListener
    }

    private fun initQuestions() {

        questions = questions.plus(Question("What was the original name of Mickey Mouse?", arrayOf("Mortimer Mouse","The Rat","Marvin Mouse","Marshall Mouse"), 0).shuffle())
        questions = questions.plus(Question("Which superhero, with the alter ego Wade Wilson and the powers of accelerated healing, was played by Ryan Reynolds in a 2016 film of the same name?", arrayOf("Deadpool","Black Panther","Ant-Man","Hawk"), 0).shuffle())
        questions = questions.plus(Question("What is the next line for American Pie? Bye, bye Miss American Pie _________", arrayOf("Drove my Chevy to the levee","I don’t want to see you again","Good Luck to you","I am hitting the road"), 0).shuffle())
        questions = questions.plus(Question("Who was the first disney character created by Walt Disney", arrayOf("Mickey Mouse","Alladin","Donald Duck","Sleeping beauty"), 0).shuffle())
        questions = questions.plus(Question("Who sings Poker Face?", arrayOf("Lady Gaga","Madonna","Taylor Swift","Kelly Clarkston"), 0).shuffle())
        questions = questions.plus(Question("How would Groot answer this question?", arrayOf("I am groot","YO homie whats up!","Mark is the bomb yo!","Groot I am young padawan"), 0).shuffle())
        questions = questions.plus(Question("What did Aladdin steal in the marketplace at the beginning of \"Aladdin\"?", arrayOf("Bread","Apple","Rice","Gold"), 0).shuffle())
        questions = questions.plus(Question("Stark Industries is associated with which fictional superhero?", arrayOf("Iron Man","Iron Fist","Hulk","Captain America"), 0).shuffle())
        questions = questions.plus(Question("In Zootopia, Officer Judy Hopps is what kind of animal?", arrayOf("Rabbit","Kangaroo","Deer","Fox"), 0).shuffle())
        questions = questions.plus(Question("Which superhero gains his transformation following accidental exposure to gamma rays during the detonation of an experimental bomb?", arrayOf("Hulk","Iron Man","The Human Flame","Silversurfer"), 0).shuffle())
        questions = questions.plus(Question("What are the names of Cinderella’s evil stepsisters?", arrayOf("Anastasia and Drizella","Gizelle and Anabelle","Florence and Marge","Pam and Shirley"), 0).shuffle())
        questions = questions.plus(Question("Mary Jane Watson has been portrayed by which actress in three movies directed by Sam Raimi?", arrayOf("Kirsten Dunst","Stephanie Tyler","Zendaya","Shailene Woodley"), 0).shuffle())
        questions = questions.plus(Question("Which College Is Elle Applying For In Legally Blonde?", arrayOf("Harvard","Yale","Duke","Princeton"), 0).shuffle())
        questions = questions.plus(Question("In Harry Potter, who is Fluffy?", arrayOf("Hagrid’s 3 Headed Dog","Harry’s Owl","Hagrid’s Dragon","Hermione’s Cat"), 0).shuffle())
        questions = questions.plus(Question("What’s the name of the sword in The Sword In The Stone?", arrayOf("Excalibur","Glamdring","Callandor","Nibue"), 0).shuffle())
        questions = questions.plus(Question("Which Museum Is Featured In Night at the Museum?", arrayOf("Museum of Natural History","The Louvre","The Smithsonian","National Museum of the American Indian"), 0).shuffle())
        questions = questions.plus(Question("In the early days it was called ‘The DB’, but which fictional New York City tabloid newspaper often appears in the comic books published by Marvel Comics?", arrayOf("The Daily Bugle","The Times","The Daily News","The Chronicle"), 0).shuffle())
        questions = questions.plus(Question("Which superhero is commonly known as Logan and sometimes as Weapon X?", arrayOf("Woleverine","Silver Surfer","Green Lantern","Aquaman"), 0).shuffle())
        questions = questions.plus(Question("Which Magazine Does Miranda Work For In The Devil Wears Prada?", arrayOf("Runway","The Thread","Upper Elite","Fashion Bash"), 0).shuffle())
        questions = questions.plus(Question("Who sings \"Blurred Lines\"?", arrayOf("Robin Thicke","Pharrell Williams","Nick Cannon","Pitbull"), 0).shuffle())
        questions = questions.plus(Question("The Playstation game console was developed by which company?", arrayOf("Sony","Nintendo","Sega","Capcom"), 0).shuffle())
        questions = questions.plus(Question("Which part of his body did Charlie Chaplin insure?", arrayOf("Feet","Moustache","Hands","Face"), 0).shuffle())
        questions = questions.plus(Question("What is the license plate of the DeLorean in the Back to the Future films?", arrayOf("Outatime","GoFuture","88timego","1Time"), 0).shuffle())
        questions = questions.plus(Question("What is the name of the element with the chemical symbol ‘He’?", arrayOf("Helium","Hydrogen","Holmium","Hafnium"), 0).shuffle())
        questions = questions.plus(Question("Which one of the following is the largest ocean in the world?", arrayOf("Pacific Ocean","Atlantic Ocean","Arctic Ocean","Indian Ocean"), 0).shuffle())
        questions = questions.plus(Question("Which star is the brightest star in the night sky?", arrayOf("Sirius A","Arcturus","North Star","None of these"), 0).shuffle())
        questions = questions.plus(Question("Sodium Hydrogen Bicarbonate is a scientific name of which common thing?", arrayOf("Baking Soda","Carbonated Water","Cream or Tartar","Salt"), 0).shuffle())
        questions = questions.plus(Question("Name the bird in the following which has the largest wingspan?", arrayOf("Albatross bird","Emperor Penguin","Dalmatian Pelican","Emu"), 0).shuffle())
        questions = questions.plus(Question("Which animal can be seen on the Porsche logo?", arrayOf("Horse","Cougar","Dog","Cheetah"), 0).shuffle())
        questions = questions.plus(Question("What type of scientist studies living plants?", arrayOf("Botanist","Geologist","Paleontologist","Entomologist"), 0).shuffle())
        questions = questions.plus(Question("Which of the following is NOT scientifically considered a fruit?", arrayOf("Broccoli","Pumpkin","Pear","Tomato"), 0).shuffle())
        questions = questions.plus(Question("How is the Earth protected from the effects of Solar Winds from the Sun?", arrayOf("Magnetic field","Oxygen","The color of the sky","Gravity"), 0).shuffle())
        questions = questions.plus(Question("All species of lemurs are native to which island country?", arrayOf("Madagascar","Sri Lanka","Australia","Indonesia"), 0).shuffle())
        questions = questions.plus(Question("How many litres are there in a barrel of oil?", arrayOf("159","59","29","189"), 0).shuffle())
        questions = questions.plus(Question("Which British archaeologist discovered Tutankhamun’s tomb?", arrayOf("Howard Carter","Thomas Young","Karl Richard Lepsius","Ippolito Rosellini"), 0).shuffle())
        questions = questions.plus(Question("A \"lepidopterist\" is someone who studies which type of creature?", arrayOf("Butterflies","Birds","Fish","Ants"), 0).shuffle())
        questions = questions.plus(Question("A lobster’s teeth are located in which part of its body?", arrayOf("Stomach","Mouth","Claws","Legs"), 0).shuffle())
        questions = questions.plus(Question("What do you call traditional Japanese female entertainers who act as hostesses and whose skills include performing various Japanese arts?", arrayOf("Geisha","Hakama","Kimono","Maiko"), 0).shuffle())
        questions = questions.plus(Question("Which city in India would you find the Taj Mahal in?", arrayOf("Agra","Bangalore","Chennai","Kolkata"), 0).shuffle())
        questions = questions.plus(Question("The Aztecs even used cocoa beans as what?", arrayOf("Currency","Drugs","Stress relievers","Weapon decorations"), 0).shuffle())
        questions = questions.plus(Question("If you are afraid of Halloween, it might be said that you suffer from which of the following?", arrayOf("Samhainophobia","Pumpkinophobia","Sanaphobia","Umbraphobia"), 0).shuffle())
        questions = questions.plus(Question("What was a samurai’s original task?", arrayOf("Protect the nobility","Assassination","They were monks, like the Shaolin in China","Invade other countries"), 0).shuffle())
        questions = questions.plus(Question("\"Mama Mia\" is based on a song by which Swedish musical act?", arrayOf("ABBA","Bee Gees","Queen","Elton John"), 0).shuffle())
        questions = questions.plus(Question("In the Strange case of Dr Jekyll and Mr Hyde, who is Hyde?", arrayOf("Jekyll’s dark side","Jekyll’s insane brother","Jekyll’s son born out of wedlock","Jekyll’s self-made creature"), 0).shuffle())
        questions = questions.plus(Question("The celebration of Samhain is to honor …", arrayOf("The end of summer.","The God of the dead.","The priest who held the first Halloween service.","The souls of the dead"), 0).shuffle())
        questions = questions.plus(Question("What musical features the song \"If I Were a Rich Man\"?", arrayOf("Fiddler on the Roof","The Music Man","My Fair Lady","Annie"), 0).shuffle())
        questions = questions.plus(Question("In Norse mythology, what is Thor the God of?", arrayOf("**Thunder **","Rain","Dancing","Pie"), 0).shuffle())
        questions = questions.plus(Question("Who was the second president of the USA?", arrayOf("John Adams","Thomas Jefferson","Benjamin Franklin","John Quincy Adams"), 0).shuffle())
        questions = questions.plus(Question("Where is the Great Wall Located?", arrayOf("China","Japan","South Korea","North Korea"), 0).shuffle())
        questions = questions.plus(Question("Mr. Pibb was a soft drink created by the Coca-Cola Company to compete with what other soft drink?", arrayOf("Dr. Pepper","Cherry Cola","Mountain Dew","Root beer"), 0).shuffle())
        questions = questions.plus(Question("What is the smallest country in the world?", arrayOf("Vatican City","Tobago","Maldives","Seychelles"), 0).shuffle())
        questions = questions.plus(Question("Which \"Special administrative region of China\" has over 7.5 million residents and is therefor one of the most densely populated places in the world?", arrayOf("Hong Kong","Hubei","Guangdong","Shandong"), 0).shuffle())
        questions = questions.plus(Question("Who was married to John F. Kenedy and was first lady from 1961 until 1963?", arrayOf("Jacqueline Kennedy Onassis","Mamie Geneva Doud Kenedy","Eleanor Kenedy","Michelle LaVaughn Robinson Kenedy"), 0).shuffle())
        questions = questions.plus(Question("What was the average life expectancy of an Englishman in the middle ages?", arrayOf("33 years","13 years","21 years","41 years"), 0).shuffle())
        questions = questions.plus(Question("In what year was the Salyut 1, the first space station ever launched?", arrayOf("1971","1998","1956","2001"), 0).shuffle())
        questions = questions.plus(Question("What year did the Chernobyl disaster occur?", arrayOf("1986","1984","1985","1987"), 0).shuffle())
        questions = questions.plus(Question("Which country was NOT a Portugese colony?", arrayOf("Colombia","Brazil","Angola","Mozambique"), 0).shuffle())
        questions = questions.plus(Question("What is the offical name fo the French civil code, established under the French Consulate in 1804 and still in force today?", arrayOf("Code civil des Français","Code de la route","Constitution des empereurs","Oeil pour Dent"), 0).shuffle())
        questions = questions.plus(Question("Which war was fought in South Africa between 1899 and 1902?", arrayOf("Second Boer War (Allow Boer War)","Boer War","Anglo-Zulu War","War of South Africa"), 0).shuffle())
        questions = questions.plus(Question("Which of these animals don’t live in the wild in Australia?", arrayOf("Opossum","Kookaburra","Koala","Possum"), 0).shuffle())
        questions = questions.plus(Question("Hickory trees produce which types of nuts?", arrayOf("Pecans","Walnuts","Pistachios","Macadamia"), 0).shuffle())
        questions = questions.plus(Question("Which planet is known as the morning star, as well as the evening star?", arrayOf("Venus","Saturn","Jupiter","Mars"), 0).shuffle())
        questions = questions.plus(Question("What colour skin does a polar bear have?", arrayOf("Black","White","Pink","Gray"), 0).shuffle())
        questions = questions.plus(Question("A Blue Whale has a heart roughly the size of a what?", arrayOf("VW Beetle","Basketball","Grapefruit","Peanut"), 0).shuffle())
        questions = questions.plus(Question("This region, famous for its wines, only produces 4% of California’s wines. What is the name of this region?", arrayOf("Napa Valley","Snake River Valley","Sonoma","Los Carneros"), 0).shuffle())
        questions = questions.plus(Question("A mongoose would typically feed on which of the following type of animal", arrayOf("Earthworm","Meerkat","Stork","Hyena"), 0).shuffle())
        questions = questions.plus(Question("Which country flag, nicknamed \"The Maple Leaf’ consists of a red field with a white square and features a red maple leaf at its center?", arrayOf("Canada","Turkey","Vietnam","Colombia"), 0).shuffle())
        questions = questions.plus(Question("Which athlete has won eight gold medals at a single Olympics?", arrayOf("Michael Phelps","Lloyd Spooner","Vera Caslavska","Agnes Keleti"), 0).shuffle())
        questions = questions.plus(Question("What popular beverage once contained cocaine?", arrayOf("Coca-Cola","Powerade","Schweppes","Dr Pepper"), 0).shuffle())
        questions = questions.plus(Question("Which is the largest food and drink company in the world?", arrayOf("Nestlé","Pepsi","Danone","Kellogg Company"), 0).shuffle())
        questions = questions.plus(Question("Henry John Heinz founded a company specializing in the production of which food product?", arrayOf("Ketchup","Mustard","Mayonnaise","Relish"), 0).shuffle())
        questions = questions.plus(Question("The name of which game is derived from the Swahili word which means ‘to build’?", arrayOf("Jenga","Lego","K’Nex","Kepla"), 0).shuffle())
        questions = questions.plus(Question("What is the primary ingredient in guacamole?", arrayOf("Avocado","Pineapple","Banana","Tomato"), 0).shuffle())
        questions = questions.plus(Question("In a game of bingo, which number is traditionally represented by the phrase \"two little ducks\"?", arrayOf("22","11","59","14"), 0).shuffle())
        questions = questions.plus(Question("At the 1996 Summer Olympics, in what sport was the U.S. team nicknamed the \"Magnificent 7\"?", arrayOf("Gymnastics","Track and Field","Diving","Swimming"), 0).shuffle())
        questions = questions.plus(Question("Which animal is, according to the New York times, by far the most expensive animal to keep in a zoo?", arrayOf("Giant panda","Elephant","Toucan","Hippo"), 0).shuffle())
        questions = questions.plus(Question("How many players are on the ice per team in an Ice Hockey game?", arrayOf("6","8","5","7"), 0).shuffle())
        questions = questions.plus(Question("What is the alcoholic beverage ‘sake’ made of?", arrayOf("Rice","Seafood","Wasabi","Soybeans"), 0).shuffle())
        questions = questions.plus(Question("What is the maximum time allowed to find a lost ball while playing Golf?", arrayOf("5","4","6","7"), 0).shuffle())
        questions = questions.plus(Question("Which is an Icelandic traditional dish?", arrayOf("Sheep’s head","Rugbrød","Krebinetter","Lutefisk"), 0).shuffle())
        questions = questions.plus(Question("In 1989, NHL player Pelle Eklund scored the fastest goal in NHL playoff history. How long did it take?", arrayOf("5 Seconds","22 Seconds","31 Seconds","11 Seconds"), 0).shuffle())
        questions = questions.plus(Question("First released in 1982, what actor’s workout videos gained worldwide popularity?", arrayOf("Jane Fonda","Raquel Welch","Jaqueline Smith","Heather Locklear"), 0).shuffle())
        questions = questions.plus(Question("What Italian brand of handbags, footwear, accessories, … was founded in 1921 in Florence?", arrayOf("Gucci","Hugo Boss","Dolce & Gabbana","Delpozo"), 0).shuffle())
        questions = questions.plus(Question("Which country does gouda cheese come from?", arrayOf("Netherlands","Denmark","Belgium","Switzerland"), 0).shuffle())
        questions = questions.plus(Question("Which of these martial arts has its origins in China?", arrayOf("Kung fu","Karate","Jujutsu","Krav Maga"), 0).shuffle())
        questions = questions.plus(Question("Worldwide, what is the third most popular drink?", arrayOf("Beer","Water","Tea","Coffee"), 0).shuffle())
        questions = questions.plus(Question("Which company was the first to use Santa Claus in an ad?", arrayOf("Coca Cola","Walmart","Pepsi","Target"), 0).shuffle())
        questions = questions.plus(Question("What is Mickey Mouses dog called?", arrayOf("Pluto","Bruce","Jude","Prick"), 0).shuffle())
        questions = questions.plus(Question("What is the name of the largest planet in the Solar System?", arrayOf("Jupiter","Earth","Mars","Venus"), 0).shuffle())
        questions = questions.plus(Question("In which city is the Disney movie Ratatouille based?", arrayOf("Paris","Copenhagen","Dublin","London"), 0).shuffle())
        questions = questions.plus(Question("What are Santa’s little helpers called?", arrayOf("Elves","Fairy’s","Gremlins","Little People"), 0).shuffle())
        questions = questions.plus(Question("Which Italian city is famous for its leaning tower?", arrayOf("Pisa","San Michele degli Scalzi","San Nicola","Venice"), 0).shuffle())
        questions = questions.plus(Question("In the following which one food Giant Pandas normally eat?", arrayOf("Bamboo","Corn","Fish","Bananas"), 0).shuffle())
        questions = questions.plus(Question("In the movie Finding Nemo, which country has Nemo been taken to?", arrayOf("Australia","England","Japan","New Zealand"), 0).shuffle())
        questions = questions.plus(Question("What type of animals pull Santa’s sleigh?", arrayOf("Reindeer","Dogs","Horses","Cats"), 0).shuffle())
        questions = questions.plus(Question("What color is the Grinch, who stole Christmas?", arrayOf("Green","Blue","Purple","Brown"), 0).shuffle())
        questions = questions.plus(Question("What is the name of the boy who owns Buzz Lightyear in the movie Toy Story?", arrayOf("Andy","Woody","Jeremy","Jack"), 0).shuffle())
        questions = questions.plus(Question("In Jungle Book what is the name of bear?", arrayOf("Baloo","Dabloo","Chang","Mushu"), 0).shuffle())
        questions = questions.plus(Question("In Disney’s Frozen, what is the name of the kingdom where Elsa and Anna live?", arrayOf("Arendelle","Caprica","Kalyyk","Laoag"), 0).shuffle())
        questions = questions.plus(Question("Which color do you find at the top of a rainbow?", arrayOf("Red","Blue","Yellow","Green"), 0).shuffle())

        questions.shuffle()
        _running.value = true

    }

    fun answerQuestion(idx: Int): Boolean {
        correctAnswer = actualQuestion.value?.correctIdx == idx
        if (correctAnswer) {
            _points.value = (_points.value?.plus(1))
        }
//        _actualCommand.value = GameCommand.CHOOSE
        return correctAnswer
    }

    fun startGame(numOfQuestions: Int) {
        if (!gameStarted) {
            gameStarted = true
            initQuestions()
            _points.value = 0
        } else {
            return
        }

        viewModelScope.launch {

            for (i in 0..< numOfQuestions) {
                _actualCommand.value = GameCommand.REST
                _actualQuestion.value = questions[i%questions.size]
                while (actualCommand.value != GameCommand.CHOOSE) {
                    delay(1000)

                    var eeg = _eegDataListener?.getLatestData(500)
                    var pos = _eegDataListener?.getLatestAcceleration(500)

                    if (eeg != null && pos != null) {
                        nextCommand(eeg, pos)
                    }
                    if (!gameStarted) {
                        break
                    }
                }
                _actualCommand.value = GameCommand.PAUSE
                delay(1000)
                if (!gameStarted) {
                    break
                }
            }
            gameStarted = false
            _running.postValue(false)
        }
    }
}