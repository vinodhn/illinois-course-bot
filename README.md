# Unofficial Illinois Course Info Bot

A Telegram bot written in Kotlin to allow for easy and convenient retrieval of course information for courses 
at the University of Illinois at Urbana-Champaign

## Setup

First, you wil need to set up a bot within Telegram. You can do this by DMing @BotFather. 

Once you've obtained a bot token, open up IllinoisCourseBot.kt and replace `$token` on line 27 with the token given to you by BotFather.

Then you may just build the project and run it. 

## How It Works
Currently the bot works by polling for updates from Telegram so it is not suited for running on a server. In the future, a webhook will be implemented so that the bot may be run remotely from a Heroku Dyno or AWS instance.