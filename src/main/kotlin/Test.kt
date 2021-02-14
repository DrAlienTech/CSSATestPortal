import androidx.compose.foundation.*
import androidx.compose.material.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.InternalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.awt.Desktop
import java.net.URI

class Test(_path: String) {
    var path = ""

    var Questions = arrayListOf<Question>()

    var http = HTTPRequests()

    var test: Firebase.Firestore.Document
    var questions: Firebase.Firestore.Collection
    var answerSheet: Firebase.Firestore.Document

    var data = ""
    var questionsData = ""

//    var active = true

    init {
        path = _path
        test = firestore.Document("tests/$_path")
        questions = firestore.Collection("tests/$_path/questions")
        answerSheet = firestore.Document("users/${firebase.uid}/answers/${_path}")
        data = test.data

        questionsData = questions.list()

        val questionGson: HashMap<String, Any> = Gson().fromJson(questionsData, object : TypeToken<HashMap<String, Any>>(){}.type)

        questionGson.forEach {
            val splitQuestions = it.value.toString().removeSurrounding("[{", "}]").replace("}, {name", "|=|name").split("|=|")

            splitQuestions.forEach { questionData ->
                val dataName = Regex("""(?<=name=)projects/cssa-dev/databases/\(default\)/documents/tests/.*/questions/question.""").find(questionData)!!.value
                val dataType = Regex("""(?<=type=\{stringValue=)(mcq|msq|fitb|srq|lrq|mq)""").find(questionData)!!.value

                println("\n$questionData\n")

                when (dataType) {
                    "mcq" -> {
                        val dataOptions = arrayListOf<String>()
                        Regex("""(?<=options=\{arrayValue=\{values=\[).*?(?=])""").findAll(questionData).forEach { options ->
                            options.value.removeSurrounding("{", "}").replace("}, {", ",").replace("stringValue=", "").split(",").forEach { option ->
                                dataOptions.add(option)
                            }
                        }

                        val newQuestion = MCQ(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean(),
                            options = dataOptions
                        )
                        Questions.add(newQuestion)
                    }

                    "msq" -> {
                        val dataOptions = arrayListOf<String>()
                        Regex("""(?<=options=\{arrayValue=\{values=\[).*?(?=])""").findAll(questionData).forEach { options ->
                            options.value.removeSurrounding("{", "}").replace("}, {", ",").replace("stringValue=", "").split(",").forEach { option ->
                                dataOptions.add(option)
                            }
                        }

                        val newQuestion = MSQ(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean(),
                            options = dataOptions
                        )
                        Questions.add(newQuestion)
                    }

                    "fitb" -> {
                        val newQuestion = FITB(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean()
                        )
                        Questions.add(newQuestion)
                    }

                    "srq" -> {
                        val newQuestion = SRQ(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean()
                        )
                        Questions.add(newQuestion)
                    }

                    "lrq" -> {
                        val newQuestion = LRQ(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean()
                        )
                        Questions.add(newQuestion)
                    }

                    "mq" -> {
                        val dataOptionsA = arrayListOf<String>()
                        Regex("""(?<=optionsA=\{arrayValue=\{values=\[).*?(?=])""").findAll(questionData).forEach { options ->
                            options.value.removeSurrounding("{", "}").replace("}, {", ",").replace("stringValue=", "").split(",").forEach { option ->
                                dataOptionsA.add(option)
                            }
                        }

                        val dataOptionsB = arrayListOf<String>()
                        Regex("""(?<=optionsB=\{arrayValue=\{values=\[).*?(?=])""").findAll(questionData).forEach { options ->
                            options.value.removeSurrounding("{", "}").replace("}, {", ",").replace("stringValue=", "").split(",").forEach { option ->
                                dataOptionsB.add(option)
                            }
                        }

                        val newQuestion = MQ(
                            number = dataName.takeLast(1).toInt(),
                            type = dataType,
                            text = Regex("""(?<=text=\{stringValue=).*?(?=}+,)""").find(questionData)!!.value,
                            image = Regex("""(?<=image=\{)stringValue=.*?(?=}+,)""").find(questionData)!!.value.split("=")[1],
                            points = Regex("""(?<=value=\{integerValue=).*?(?=}+,)""").find(questionData)!!.value.toInt(),
                            tiebreaker = Regex("""(?<=tiebreaker=\{booleanValue=)(true|false)""").find(questionData)!!.value.toBoolean(),
                            optionsA = dataOptionsA,
                            optionsB = dataOptionsB
                        )
                        Questions.add(newQuestion)
                    }
                }
            }
        }
    }

    interface Question {
        val number: Int
            get() = 0

        val type: String
            get() = "Default"

        val text: String
            get() = ""

        val image: String
            get() = ""

        val points: Int
            get() = 5

        val tiebreaker: Boolean
            get() = false

        var answer: String

        @Composable
        fun UI() {

        }
    }

    // Multiple Choice Question
    inner class MCQ (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        val options: ArrayList<String>,
        override var answer: String = ""
    ) : Question {
        @Composable
        override fun UI() {
            super.UI()

            Row(Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp)) {
                var selected by remember {
                    mutableStateOf("")
                }

                Column {
                    Row {
                        QuestionText("$number. $text ($points point${if (points > 1) "s" else ""})")
                    }

                    Column {
                        options.forEach {
                            Row(Modifier
                                .fillMaxWidth(0.831f)
                                .selectable((it == selected), onClick = { selected = it; answer = it })
                                .padding(horizontal = 16.dp)
                            ) {
                                RadioButton((it == selected), onClick = { selected = it; answer = it })

                                Text(it, style = MaterialTheme.typography.body1.merge(), modifier = Modifier.padding(start = 16.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Multiple Select Question
    inner class MSQ (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        val options: ArrayList<String>,
        override var answer: String = ""
    ) : Question {
        @Composable
        override fun UI() {
            super.UI()

            Row(Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp)) {
                Column {
                    Row {
                        QuestionText("$number. $text ($points point${if (points > 1) "s" else ""})")
                    }

                    Column {
                        val answers = arrayListOf<String>()

                        options.forEach { option ->
                            Row(Modifier.padding(16.dp)) {
                                var checked by remember {
                                    mutableStateOf(false)
                                }

                                Checkbox(checked,
                                    onCheckedChange = {
                                        checked = !checked

                                        if (checked) {
                                            answers.remove(option)
                                        } else {
                                            answers.add(option)
                                        }

                                        answer = answers.toString()
                                    }
                                )

                                Text(option, Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    // Fill-In-The-Blank
    inner class FITB (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        override var answer: String = ""
    ) : Question {
        @InternalTextApi
        @Composable
        override fun UI() {
            super.UI()

            Row(Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp)) {
                var blankAnswer by remember {
                    mutableStateOf(TextFieldValue(""))
                }

                val blankIndex = text.indexOf("|~~~~|")
                val half1 = text.substring(0, blankIndex)
                val half2 = text.substring(blankIndex + 6)

                Column {
                    QuestionText("$number. Fill in the blank below. ($points point${if (points > 1) "s" else ""})")

                    Row {
                        Text(half1)
                        QuestionField(
                            modifier = Modifier
                                .width(200.dp)
                                .height(25.dp),
                            value = blankAnswer,
                            onValueChange = {
                                if (it.text.length < 25) {
                                    blankAnswer = it

                                    answer = blankAnswer.text
                                }
                            },
                        )
                        Text(half2)
                    }
                }
            }
        }
    }

    // Short Response Question
    inner class SRQ (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        override var answer: String = ""
    ) : Question {

    }

    // Long Response Question
    inner class LRQ (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        override var answer: String = ""
    ) : Question {
        @InternalTextApi
        @Composable
        override fun UI() {
            super.UI()

            Row(Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp)) {
                Column {
                    Row {
                        QuestionText("$number. $text ($points point${if (points > 1) "s" else ""})")
                    }

                    Column {
                        var response by remember {
                            mutableStateOf(TextFieldValue(""))
                        }

                        QuestionField(
                            modifier = Modifier.width(519.dp).heightIn(100.dp),
                            value = response,
                            onValueChange = {
                                response = it

                                if (it.text.length > 300) {
                                    response = TextFieldValue(it.text.substring(0, 300))
                                }

                                answer = it.text
                            }
                        )
                    }
                }
            }
        }
    }

    // Matching Question
    inner class MQ (
        override val number: Int,
        override val type: String,
        override val text: String,
        override val image: String,
        override val points: Int,
        override val tiebreaker: Boolean,
        val optionsA: ArrayList<String>,
        val optionsB: ArrayList<String>,
        override var answer: String = ""
    ) : Question {
        @InternalTextApi
        @Composable
        override fun UI() {
            super.UI()

            Row(Modifier.padding(15.dp, 5.dp, 0.dp, 5.dp)) {
                Column {
                    Row {
                        QuestionText("$number. $text ($points point${if (points > 1) "s" else ""})")
                    }

                    Row {
                        val answers by remember {
                            mutableStateOf(arrayListOf<String>())
                        }

                        Column {
                            var count = 1

                            optionsA.forEach {

                                val index = count - 1

                                answers.add("")

                                var optionAnswer by remember {
                                    mutableStateOf(TextFieldValue(""))
                                }

                                Row(Modifier.padding(vertical = 10.dp)) {
                                    Row(Modifier
                                        .width(48.dp)
                                        .height(48.dp)
                                        .padding(end = 10.dp), Arrangement.Center) {
                                        QuestionField(
                                            modifier = Modifier
                                                .width(50.dp)
                                                .height(50.dp)
                                                .padding(7.dp, 8.dp, 9.dp, 8.dp),
                                            textStyle = TextStyle(
                                                fontSize = 22.sp,
                                                textAlign = TextAlign.Center
                                            ),
                                            value = optionAnswer,
                                            onValueChange = {
                                                if (it.text.length < 2) {
                                                    optionAnswer = it

                                                    answers[index] = it.text
                                                    answer = answers.joinToString(",").replace(",,", "")
                                                }
                                            },
                                        )
                                    }

                                    Text(text = it,
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = TextStyle(fontSize = 22.sp))
                                }

                                count++
                            }
                        }

                        Column(Modifier.padding(start = 50.dp)) {
                            var count = 1

                            optionsB.forEach {
                                Row(Modifier.padding(vertical = 10.dp).fillMaxWidth().height(48.dp)) {
                                    Text(text = "$count. $it",
                                        color = (if (answers.toString().contains(count.toString())) Color.Black else Color.DarkGray),
                                        modifier = Modifier.padding(vertical = 8.dp),
                                        style = TextStyle(fontSize = 22.sp))
                                }

                                count++
                            }
                        }
                    }
                }
            }
        }
    }

    inner class UI {
        init {
            deactivated = false
        }

        @Composable
        fun load() {
            var active by remember {
                mutableStateOf(true)
            }

            lateinit var persistenceJob: Job

            if (active) {
                Column {
                    var time by remember {
                        mutableStateOf(0)
                    }

                    Text(text = "UID: ${firebase.uid} | Time Remaining: ${(3600000 - time)/1000} Seconds", Modifier.align(Alignment.CenterHorizontally), fontSize = 30.sp, textAlign = TextAlign.Center)

                    GlobalScope.launch {
                        while (active && !deactivated) {
                            delay(1000)
                            time += 1000

                            if (time >= 3600000) {
                                deactivated = true

                                persistenceJob.cancel()

                                saveAnswers()

                                println("Saved all answers!")

                                active = false
                            }

                            if (!active || deactivated) {
                                this.cancel()
                            }
                        }
                    }
                }

                ScrollableColumn(
                    scrollState = rememberScrollState(),
                    modifier = Modifier.fillMaxHeight().border(2.dp, Color.Black).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(15.dp)
                ) {
                    Questions.forEach {
                        it.UI()

                        Divider(Modifier.fillMaxWidth())

                        println("Added UI for Question #${it.number}")
                    }

                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 12.dp),
                        onClick = {
                            deactivated = true

                            persistenceJob.cancel()

                            saveAnswers()

                            println("Saved all answers!")

                            active = false
                        }
                    ) {
                        Text("Submit")
                    }
                }
            } else {
                Text("Good job! You're done! You can return to the home screen and take another test or close the application. " +
                        "You will be automatically returned to the home screen after a minute.")
            }

            persistenceJob = GlobalScope.launch {
                delay(1500)
                while (active) {
                    delay(30000)

                    println("Saving answers...")

                    saveAnswers()
                }
            }
        }
    }

    fun saveAnswers() {
        GlobalScope.launch {
            var documentData = """
                {
                    "fields": {
            """.trimIndent()

            Questions.forEach {
                documentData += """
                        "question${it.number}": {
                            "stringValue": "${it.answer}"
                        },
                """.trimIndent()
            }

            documentData += """
                    }
                }
            """.trimIndent()

            answerSheet.update(documentData)

            println(documentData)

            this.cancel()
        }
    }

}