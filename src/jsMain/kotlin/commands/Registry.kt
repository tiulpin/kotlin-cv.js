package commands

val commands: Map<String, Command> = mapOf(
    "help" to Help,

    "cat" to Cat,
    "head" to Head,
    "tail" to Tail,
    "ls" to Ls,
    "tree" to Tree,
    "grep" to Grep,
    "wc" to Wc,

    "cd" to Cd,
    "mkdir" to Mkdir,
    "pwd" to Pwd,
    "touch" to Touch,
    "rm" to Rm,
    "cp" to Cp,
    "mv" to Mv,

    "man" to Man,

    "clear" to Clear,
    "history" to History,
    "echo" to Echo,
    "exit" to Exit,

    "date" to DateCmd,
    "whoami" to Whoami,
    "printenv" to Printenv,

    "open" to Open,

    "neofetch" to Neofetch,
    "uptime" to Uptime,
    "make" to Sandwich,

    "uuid" to Uuid,
    "base64" to Base64Cmd,
    "json" to Json,
    "urlencode" to UrlEncode,
    "urldecode" to UrlDecode,
    "color" to Color,
)


