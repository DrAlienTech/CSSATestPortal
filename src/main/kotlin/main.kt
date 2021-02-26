import androidx.compose.desktop.AppManager
import androidx.compose.desktop.AppWindow
import androidx.compose.desktop.Window
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.CoreTextField
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.imageFromResource
import androidx.compose.ui.text.*
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.MenuBar
import androidx.compose.ui.zIndex
import com.firefly.codec.http2.model.HttpMethod
import com.firefly.kotlin.ext.http.HttpServer
import kotlinx.coroutines.*
import org.jetbrains.skija.Image
import java.awt.image.BufferedImage
import javax.imageio.ImageIO
import java.awt.Desktop
import java.io.File
import java.net.URI

var auth = Authentication()
var firebase = Firebase()
var firebaseAuth = firebase.Authentication()
var firestore = firebase.Firestore()
var tests = hashMapOf<String, Test>()

var deactivated = true

fun main() = Window(title = "CSSA Test Portal", icon = loadImageResource("CSSA.png"), size = IntSize(1080, 712)) {
    var noUsername by remember {
        mutableStateOf(false)
    }

    var authenticated by remember {
        mutableStateOf(true)
    }

    var currentPage by remember {
        mutableStateOf(0)
    }

    var test by remember {
        mutableStateOf("")
    }

    MaterialTheme {
        if (authenticated) {
            Row {
                Column(Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.0831f)
                    .background(Color(66, 133, 244)),
                    Arrangement.spacedBy(30.dp)
                ) {
                    IconButton(modifier = Modifier.padding(top = 10.dp).align(Alignment.CenterHorizontally).scale(1.0f), onClick = {
                        currentPage = 0
                    }) {
                        Icon(bitmap = imageFromResource("Home Icon.png"))
                    }

                    IconButton(modifier = Modifier.align(Alignment.CenterHorizontally).scale(1.0f), onClick = {
                        currentPage = 1
                    }) {
                        Icon(bitmap = imageFromResource("Events Icon.png"))
                    }

                    IconButton(modifier = Modifier.align(Alignment.CenterHorizontally).scale(1.0f), onClick = {
                        currentPage = 2
                    }) {
                        Icon(bitmap = imageFromResource("Settings Icon.png"))
                    }
                }

                Column(Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(),
                    Arrangement.spacedBy(50.dp)) {

                    when (currentPage) {
                        0 -> { // Home Page

                            Text(text = "Welcome, ${auth.username}!", Modifier.align(Alignment.CenterHorizontally), fontSize = 40.sp)

                            Column(Modifier
                                .fillMaxWidth(0.8f)
                                .fillMaxHeight(0.6f)
                                .align(Alignment.CenterHorizontally)
                                .background(Color(243, 243, 243))) {

                                Text("First Mini-Competition", fontSize = 20.sp)

                                if (tests.size == 0) {
                                    val userDocResponse = firestore.get("users/${firebase.uid}")
                                    val eventSequence = Regex("""(?<="event.": \{\n {6}"stringValue": ")(?!None|(.*)!).*(?=")""").findAll(userDocResponse)
                                    eventSequence.forEach {
                                        tests[it.value] = Test(it.value)
                                    }
                                }

                                tests.forEach { (event, _) ->
                                    TextButton(onClick = {
                                        currentPage = 3
                                        test = event
                                    }) {
                                        Text(event, color = Color.Black)
                                    }
                                }

                            }

                        }

                        1 -> { // Competition Page

                            Text(text = "My Competitions", Modifier.align(Alignment.CenterHorizontally), fontSize = 40.sp)

                            Column(Modifier
                                .fillMaxWidth(0.8f)
                                .fillMaxHeight(0.6f)
                                .align(Alignment.CenterHorizontally)
                                .background(Color(243, 243, 243))
                            ) {

                                Text("First Mini-Competition", fontSize = 20.sp)

                                if (tests.size == 0) {
                                    val userDocResponse = firestore.get("users/${firebase.uid}")
                                    val eventSequence = Regex("""(?<="event.": \{\n {6}"stringValue": ")(?!None|(.*)!).*(?=")""").findAll(userDocResponse)
                                    eventSequence.forEach {
                                        tests[it.value] = Test(it.value)
                                    }
                                }

                                tests.forEach { (event, _) ->
                                    TextButton(onClick = {
                                        currentPage = 3
                                        test = event
                                    }) {
                                        Text(event, color = Color.Black)
                                    }
                                }

                            }

                        }

                        2 -> { // Settings Page

                            Text(text = "Settings", Modifier.align(Alignment.CenterHorizontally), fontSize = 40.sp)

                            Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 5.dp, 0.dp, 0.dp),
                                onClick = {
                                    auth = Authentication()
                                    authenticated = false
                                }
                            ) {
                                Text("Sign Out")
                            }

                        }

                        3 -> { // Test-Taking Page

                            Text(text = test, Modifier.align(Alignment.CenterHorizontally), fontSize = 40.sp)

                            val testUI = tests[test]!!.UI()

                            Column(Modifier
                                .fillMaxWidth(0.831f)
                                .fillMaxHeight(0.9f)
                                .align(Alignment.CenterHorizontally)
                            ) {

                                testUI.load()

                                GlobalScope.launch {
                                    while (!deactivated) {
                                        delay(1500)

                                        if (deactivated) {
                                            delay(60000)

                                            println("Returning to home screen...")

                                            currentPage = 1

                                            this.cancel()
                                        }
                                    }
                                }

                            }

                        }
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize(), Arrangement.spacedBy(5.dp)) {

                Row(Modifier.fillMaxSize().align(Alignment.CenterHorizontally)) {

                    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("CSSA Test Portal", modifier = Modifier.padding(0.dp, 0.dp, 0.dp, 30.dp), fontSize = 40.sp, textAlign = TextAlign.Right)

                        Column(Modifier
                            .align(Alignment.CenterHorizontally)
                            .background(Color(0xF0, 0xF0, 0xF0), RoundedCornerShape(8.dp))
                            .border(3.dp, Color(33, 33, 33), RoundedCornerShape(8.dp))
                        ) {

                            Column(Modifier
                                .align(Alignment.CenterHorizontally)
                                .padding(10.dp, 15.dp, 10.dp, 15.dp), Arrangement.spacedBy(15.dp)
                            ) {

                                if (noUsername) {
                                    var username by remember {
                                        mutableStateOf("")
                                    }

                                    var email by remember {
                                        mutableStateOf("")
                                    }

                                    var fName by remember {
                                        mutableStateOf("")
                                    }

                                    var lName by remember {
                                        mutableStateOf("")
                                    }

                                    var password by remember {
                                        mutableStateOf("")
                                    }

                                    var confirmPassword by remember {
                                        mutableStateOf("")
                                    }

                                    Row(Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 0.dp)) {
                                        Column(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp)) {
                                            Text("Username", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = username,
                                                onValueChange = { username = it },
                                            )
                                        }

                                        Column(Modifier.padding(15.dp, 0.dp, 0.dp, 0.dp)) {
                                            Text("Email", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = email,
                                                onValueChange = { email = it },
                                            )
                                        }
                                    }

                                    Row(Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 0.dp)) {
                                        Column(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp)) {
                                            Text("First Name", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = fName,
                                                onValueChange = { fName = it },
                                            )
                                        }

                                        Column(Modifier.padding(15.dp, 0.dp, 0.dp, 0.dp)) {
                                            Text("Last Name", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = lName,
                                                onValueChange = { lName = it },
                                            )
                                        }
                                    }

                                    Row(Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 0.dp)) {
                                        Column(Modifier.padding(0.dp, 0.dp, 0.dp, 15.dp)) {
                                            Text("Password", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = password,
                                                onValueChange = { password = it },
                                            )
                                        }

                                        Column(Modifier.padding(15.dp, 0.dp, 0.dp, 0.dp)) {
                                            Text("Confirm Password", textAlign = TextAlign.Left)

                                            TextField(
                                                modifier = Modifier.align(Alignment.CenterHorizontally),
                                                value = confirmPassword,
                                                onValueChange = {
                                                    confirmPassword = it
                                                },
                                            )
                                        }
                                    }

                                    if ((password != confirmPassword || confirmPassword == "") && confirmPassword != "") {
                                        Row {
                                            Text(text = "Passwords do not match!", textAlign = TextAlign.Left, color = Color(188, 88, 88))
                                        }
                                    }

                                    Row(Modifier.align(Alignment.CenterHorizontally)) {
                                        Column {
                                            var signUpPopup by remember {
                                                mutableStateOf(0)
                                            }

//                                            var loading by remember {
//                                                mutableStateOf(false)
//                                            }

                                            Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 5.dp, 0.dp, 0.dp),
                                                onClick = {
                                                    GlobalScope.launch {
                                                        val authState = auth.createAccount(fName, lName, username, email, password)

                                                        if (authState[0] as Boolean) {
//                                                          loading = false

                                                            authenticated = true
                                                        } else {
//                                                          loading = false

                                                            signUpPopup = 1

                                                            println("Error creating account")
                                                        }

                                                        this.cancel()
                                                    }
                                                }
                                            ) {
                                                Text("Sign Up")
                                            }

                                            TextButton(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 5.dp, 0.dp, 0.dp),
                                                onClick = {
                                                    noUsername = false
                                                }
                                            ) {
                                                Text("Already have an account? Sign in!")
                                            }

                                            if (signUpPopup != 0) {
                                                Window(
                                                    title = "CSSA Test Portal | Authentication Error",
                                                    icon = loadImageResource("CSSA.png"),
                                                    size = IntSize(450, 195),
                                                    onDismissRequest = { signUpPopup = 0 }
                                                ) {
                                                    Column (Modifier.align(Alignment.CenterHorizontally)) {
                                                        androidx.compose.material.Text(
                                                            text = when (signUpPopup) {
                                                                1 -> "There was an error creating your account, please retry after a while or restart the testing portal!"
                                                                2 -> "That email is already being used by another account, please sign in or use a different email!"
                                                                3 -> "That username is already being used by another account, please sign in or use a different username!"
                                                                else -> "Unknown error signing up, please retry after a while or restart the testing portal!"
                                                            },
                                                            Modifier.align(Alignment.CenterHorizontally).padding(20.dp),
                                                            fontSize = 15.sp, textAlign = TextAlign.Center
                                                        )

                                                        Button(
                                                            onClick = { signUpPopup = 0; AppManager.focusedWindow!!.close() },
                                                            Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp),
                                                        ) {
                                                            androidx.compose.material.Text(text = "Ok", fontSize = 15.sp, textAlign = TextAlign.Center)
                                                        }
                                                    }
                                                }
                                            }

//                                            if (loading) {
//                                                Column (Modifier.fillMaxSize().background(Color(0,0,0,4)).zIndex(1f)) {
//                                                    Column(Modifier.fillMaxSize(0.27f).background(Color.White).align(Alignment.CenterHorizontally)) {
//                                                        Text(text = "Loading...")
//
//                                                        CircularProgressIndicator(Modifier.wrapContentWidth(Alignment.CenterHorizontally))
//                                                    }
//                                                }
//                                            }
                                        }
                                    }
                                } else {
                                    var username by remember {
                                        mutableStateOf("")
                                    }

                                    var password by remember {
                                        mutableStateOf("")
                                    }

                                    var signInPopup by remember {
                                        mutableStateOf(0)
                                    }

                                    Text("Username", textAlign = TextAlign.Left)

                                    TextField(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        value = username,
                                        onValueChange = { username = it },
                                    )

                                    Text("Password", textAlign = TextAlign.Left)

                                    TextField(
                                        modifier = Modifier.align(Alignment.CenterHorizontally),
                                        value = password,
                                        onValueChange = { password = it },
                                    )

                                    Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 5.dp, 0.dp, 0.dp),
                                        onClick = {
                                            GlobalScope.launch {
                                                if (username == "") {
                                                    signInPopup = 2
                                                } else if (password == "") {
                                                    signInPopup = 3
                                                } else {
                                                    val trySignIn = auth.manualSignIn(username, password)

                                                    if (trySignIn == 0) {
                                                        authenticated = true
                                                        signInPopup = 0
                                                    } else {
                                                        println("Unknown error occurred")
                                                    }
                                                }

                                                this.cancel()
                                            }
                                        }) {
                                        Text("Sign In")
                                    }

                                    TextButton(modifier = Modifier.align(Alignment.CenterHorizontally),
                                        onClick = {
                                            noUsername = true
                                        }) {
                                        Text("Don't have an account? Sign up!")
                                    }

                                    if (signInPopup != 0) {
                                        Window(
                                            title = "CSSA Test Portal | Authentication Error",
                                            icon = loadImageResource("CSSA.png"),
                                            size = IntSize(450, 195)
                                        ) {
                                            Column (Modifier.align(Alignment.CenterHorizontally)) {
                                                androidx.compose.material.Text(
                                                    text = when (signInPopup) {
                                                        2 -> "The username field is empty! Please make sure to enter a username!"
                                                        3 -> "The password field is empty! Please make sure to enter a password!"
                                                        4 -> "An error occurred with your account, please contact crewcssa@gmail.com or join our Discord server at bit.ly/cssa-discord for assistance!"
                                                        5 -> "Please enter a valid password without any special characters like \" or }!"
                                                        6 -> "It looks like you don't have an account! Please sign up or contact crewcssa@gmail.com or join our Discord server at bit.ly/cssa-discord for assistance!"
                                                        7 -> "Please enter a valid password without any special characters like \" or }!"
                                                        8 -> "Invalid credentials! Please make sure you typed in your username and password correctly!"
                                                        9 -> "Sorry, it looks like your account has been disabled! Please contact crewcssa@gmail.com or join our Discord server at bit.ly/cssa-discord for assistance!"
                                                        else -> "Unknown error signing in, please retry after a while or restart the testing portal!"
                                                    },
                                                    Modifier.align(Alignment.CenterHorizontally).padding(20.dp),
                                                    fontSize = 15.sp, textAlign = TextAlign.Center
                                                )

                                                Button(
                                                    onClick = { signInPopup = 0; AppManager.focusedWindow!!.close() },
                                                    Modifier.align(Alignment.CenterHorizontally).padding(top = 20.dp),
                                                ) {
                                                    androidx.compose.material.Text(text = "Ok", fontSize = 15.sp, textAlign = TextAlign.Center)
                                                }
                                            }
                                        }
                                    }
                                }

                                Divider(color = Color.Gray, thickness = 2.dp, modifier = Modifier.width(250.dp).align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 5.dp))

                                Button(modifier = Modifier.align(Alignment.CenterHorizontally).padding(0.dp, 0.dp, 0.dp, 5.dp),
                                    onClick = {
                                        if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                                            auth.googleHttpServer()

                                            Desktop.getDesktop().browse(URI("https://accounts.google.com/o/oauth2/v2/auth/oauthchooseaccount?response_type=code&scope=openid%20profile&redirect_uri=http%3A%2F%2F127.0.0.1%3A8310%2F&client_id=834594227639-b7pj2rqb1eijd2pbfvice7bp0ndsdp7i.apps.googleusercontent.com&flowName=GeneralOAuthFlow"));
                                        }

                                        runBlocking {
                                            val host = "localhost"
                                            val port = 8081

                                            HttpServer {
                                                router {
                                                    httpMethod = HttpMethod.GET
                                                    path = "/get-code/4/:code"

                                                    asyncHandler {
                                                        val codeId = getPathParameter("code")

                                                        println(codeId)

                                                        auth.googleSignIn("4/$codeId")

                                                        end("Done")
                                                    }
                                                }
                                            }.listen(host, port)
                                        }

                                        authenticated = true
                                    }) {
                                    Text("Google")
                                }

                            }

                        }
                    }

                }

            }
        }
    }
}

private fun loadImageResource(path: String): BufferedImage {
    val resource = Thread.currentThread().contextClassLoader.getResource(path)
    requireNotNull(resource) { "Resource at path '$path' not found" }
    return resource.openStream().use(ImageIO::read)
}

fun imageFromFile(file: File): ImageBitmap {
    return Image.makeFromEncoded(file.readBytes()).asImageBitmap()
}

@Composable
fun Text(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily = fontFamily(androidx.compose.ui.text.platform.font("Quicksand", "Quicksand-Medium.ttf")),
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = AmbientTextStyle.current
) {
    Text(
        AnnotatedString(text),
        modifier,
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        emptyMap(),
        onTextLayout,
        style
    )
}

@Composable
fun QuestionText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily = fontFamily(androidx.compose.ui.text.platform.font("Quicksand", "Quicksand-Medium.ttf")),
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    style: TextStyle = AmbientTextStyle.current
) {
    Text(
        AnnotatedString(text),
        modifier.padding(bottom = 10.dp),
        color,
        fontSize,
        fontStyle,
        fontWeight,
        fontFamily,
        letterSpacing,
        textDecoration,
        textAlign,
        lineHeight,
        overflow,
        softWrap,
        maxLines,
        emptyMap(),
        onTextLayout,
        style
    )
}

@InternalTextApi
@Composable
fun QuestionField(
    value: TextFieldValue,
    modifier: Modifier = Modifier,
    onValueChange: (TextFieldValue) -> Unit,
    textStyle: TextStyle = TextStyle.Default,
    onImeActionPerformed: (ImeAction) -> Unit = {},
    visualTransformation: VisualTransformation = VisualTransformation.None,
    onTextLayout: (TextLayoutResult) -> Unit = {},
    onTextInputStarted: (SoftwareKeyboardController) -> Unit = {},
    cursorColor: Color = Color.Black,
    softWrap: Boolean = true,
    imeOptions: ImeOptions = ImeOptions.Default
) {
    CoreTextField(value,
        modifier.border(1.dp, Color.Black, RoundedCornerShape(2.dp)),
        onValueChange,
        textStyle,
        onImeActionPerformed,
        visualTransformation,
        onTextLayout,
        onTextInputStarted,
        cursorColor,
        softWrap,
        imeOptions)
}