# Bot for evolution2 game server

_(for Game#1)_

Set the running game server address at GameClient.scala before compiling:
`val host = "..."`

You may build fat jar with:
`sbt assembly`

The bot makes random play/fold choices by default.

With command-line parameter "smart" it performs strategy of choice based on its card rank.

You may run a couple or more bot instances on the same game server.
