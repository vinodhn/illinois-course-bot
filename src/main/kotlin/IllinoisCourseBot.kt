import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.command
import com.github.kotlintelegrambot.entities.ChatId
import okhttp3.*
import java.io.IOException

data class XmlTag(val id: String,
                  val href: String,
                  val value: String)

data class Course(val courseId: String,
                  val parents: List<XmlTag>,
                  val label: String,
                  val desc: String,
                  val numHours: String,
                  val sections:List<XmlTag>)

val baseUrl = "https://courses.illinois.edu/cisapp/explorer/schedule/2021/fall/SUBJ/NUM.xml"

private val client = OkHttpClient()

fun main() {

    val bot = bot {
        token = "$token"
        dispatch {
            command("course") {
                val parsedArgs = parseArgs(args)
                val response = getClassInfo(parsedArgs)
                bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
            }

            command("start") {
                val response = help()
                bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
            }

            command("help") {
                val response = help()
                bot.sendMessage(ChatId.fromId(message.chat.id), text = response)
            }
        }
    }
    bot.startPolling()
}

fun parseArgs(args: List<String>): List<String> {
    val parsedArgs: MutableList<String> = mutableListOf<String>().toMutableList()

    if(args.size == 2){
        parsedArgs += args[0]
        parsedArgs += args[1]
    } else if(args.size == 1) {
        val arg = args[0]
        val courseNumber = arg.substring(arg.length - 3)
        val courseSubject = arg.substring(0, arg.length - 3)

        parsedArgs += courseSubject
        parsedArgs += courseNumber
    }

    return parsedArgs
}

fun getClassInfo(args: List<String>): String {
    if(!args.isEmpty()) {
        val finalUrl = baseUrl.replace("SUBJ", args[0].toUpperCase()).replace("NUM", args[1])
        var xml: String? = ""
        println(finalUrl)
//    val response: String = "CS 233: Computer Architecture\n\n" +
//                            "Fundamentals of computer architecture: digital logic design, working up from the logic gate level to understand the function of a simple computer; machine-level programming to understand implementation of high-level languages; performance models of modern computer architectures to enable performance optimization of software; hardware primitives for parallelism and security. Prerequisite: CS 125 or CS 128 and CS 173; credit or concurrent enrollment in CS 225.\n" +
//                            "\n" +
//                            "Credit Hours: 4 hours.\n" +
//                            "\n" +
//                            "Average GPA: 3.2"
        val request = Request.Builder()
            .url(finalUrl)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")

            xml = response.body()?.string()
            print(xml)
        }
    }
    return ""
}

fun help(): String {
    val response = "Get info with the /course command. \n" +
                  "Do /course subject number \n" +
                  "Ex: /course CS233 or /course LAS 101 \n" +
                  "Space between subject and number not needed."

    return response
}

