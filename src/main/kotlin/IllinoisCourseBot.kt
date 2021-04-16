import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import okhttp3.*
import java.io.IOException

// Base API URL
const val baseApiUrl = "https://courses.illinois.edu/cisapp/explorer/schedule/2021/fall/SUBJ/NUM.xml"

// Base Course Explorer URL
const val baseCEUrl = "https://courses.illinois.edu/schedule/2021/fall/SUBJ/NUM"

// Help message
const val helpMsg:String = "Get info with the /course command. \n" +
                           "Do /course <subject> <number> \n" +
                           "Ex: /course CS233 or /course LAS 101 \n" +
                           "Space between subject and number not needed.\n" +
                           "Other commands: /start & /help for this message."

private val client = OkHttpClient()

fun main() {

    val bot = bot {
        // Token from BotFather, this is removed in the GitHub commits
        token = "$token"
        // Handle events from Telegram's polling
        dispatch {
            // Command: /course <args>
            command("course") {
                val parsedArgs = parseArgs(args)
                val response = getClassInfo(parsedArgs)
                bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
            }
            // Command: /c | Shorthand for /course
            command("c") {
                val parsedArgs = parseArgs(args)
                val response = getClassInfo(parsedArgs)
                bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
            }
            // Command: /start
            command("start") {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = helpMsg)
            }
            // Command: /help
            command("help") {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = helpMsg)
            }
            // Command: /? | Shorthand for /help
            command("?") {
                bot.sendMessage(ChatId.fromId(message.chat.id), text = helpMsg)
            }
        }
    }
    // Poll messages from Telegram
    // TODO: Convert to webhook based polling
    bot.startPolling()
}

fun parseArgs(args:List<String>):List<String> {
    // Create mutable list of arguments
    var parsedArgs:MutableList<String> = mutableListOf()

    // TODO: Sanitize inputs to ensure that only letters go in SUBJ and only numbers in NUM

    // If the args list implies the input is formatted as: SUBJ NUM (e.g. CS 233), then just proceed
    if(args.size == 2){
        parsedArgs = args.toMutableList()
    }
    // Else if the args list implies the input is formatted as: SUBJNUM (e.g. CS233), then break it up
    else if(args.size == 1) {
        // Since course numbers are always 3 digits, we can get all other characters as the subject
        val courseSubject = args[0].substring(0, args[0].length - 3)
        // And then get the final 3 characters as the course number
        val courseNumber = args[0].substring(args[0].length - 3)

        // And then add it to our parsed arguments list in the correct order
        parsedArgs.add(0, courseSubject)
        parsedArgs.add(1, courseNumber)
    }

    return parsedArgs
}

fun getClassInfo(args:List<String>):String {
    lateinit var finalResponse:String
    // If the inputs are parsed
    if(args.isNotEmpty()) {
        // Construct the final url based on the UIUC API
        val finalUrl = baseApiUrl.replace("SUBJ", args[0].toUpperCase()).replace("NUM", args[1])
        // Create a lateinit String for the XML response
        lateinit var xml:String

        // Using OkHttp, create an HTTP Request
        val request = Request.Builder()
            .url(finalUrl)
            .build()

        // Try to get a response from the UIUC API
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                finalResponse = "Cannot get information for: $args"
            }
            // If there is a successful response, it will be XMl
            xml = response.body()?.string().toString()
        }

        // Then just extract what we care about
        val courseCode:String = args[0].toUpperCase() + " " + args[1] + ""

        // TODO: Maybe make .substringAfter().substringBefore() a separate method?
        val courseLabel:String = xml.substringAfter("<label>").substringBefore("</label>") + "."

        val courseDesc:String = xml.substringAfter("<description>").substringBefore("</description>")

        val courseCreditHrs:String = xml.substringAfter("<creditHours>").substringBefore("</creditHours")

        // Simple hacky way to count the number of occurrences of a specific String/CharSequence.
        // TODO: Maybe place </section> in its own var?
        val courseSectionCount:Int = xml.windowed("</section>".length){ if (it == "</section>") 1 else 0}.sum()

        val courseLink:String = baseCEUrl.replace("SUBJ", args[0].toUpperCase()).replace("NUM", args[1])

        // Since we might get a response still even if a class is not offered, handle it separately
        finalResponse = if(courseSectionCount == 0) {
            "Cannot get information for $courseCode for Fall 2021\n" +
            "This course is probably not offered this semester."
        }else {
            "$courseCode: $courseLabel\n\n$courseDesc\n\n" +
            "Number of credit hours: $courseCreditHrs\n\n" +
            "Number of sections: $courseSectionCount\n\n" +
            "View in Course Explorer: $courseLink"
        }
    } else {
        finalResponse = "Please try with proper inputs. \n$helpMsg"
    }
    return finalResponse
}
