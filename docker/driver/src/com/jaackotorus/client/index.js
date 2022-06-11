import dayjs from "https://cdn.skypack.dev/dayjs";

let socket = new WebSocket("ws://localhost:8081/greeter?username=example-username")

document.getElementById("messaging_button").addEventListener("click", function (e) {
    send_data()
    console.log("...")
})

/**
 * @param {string | null} data
 */
const send_data = (data = null) => {
    if (data !== null) socket.send(data)

    /** @type {string} */
    const body = document.getElementById("messaging_data").value
    socket.send(body)
}

/**
 * Spawns message in html
 * @param {string} username
 * @param {string} timestamp
 * @param {string} body
 */
function MaterializeMessage(username, timestamp, body) {
    let $message = document.createElement("div")
    let $header = document.createElement("div")
    let $username = document.createElement("span")
    let $timestamp = document.createElement("span")
    let $body = document.createElement("div")
    let $p = document.createElement("p")

    // ------------------------------------------------------------------------

    $message.classList.add("message")
    $header.classList.add("message_header")
    $username.classList.add("message_username")
    $timestamp.classList.add("message_timestamp")
    $body.classList.add("message_body")

    // ------------------------------------------------------------------------

    $username.append(document.createTextNode(username))
    $timestamp.append(document.createTextNode(timestamp))
    $p.append(document.createTextNode(body))

    $header.append($username, document.createTextNode(" @ "), $timestamp)
    $body.append($p)
    $message.append($header, $body)

    document.getElementById("chat_area").append($message)
}

/**
 * @param {Event} e
 */
socket.onopen = e => {
    MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), "[open] Connection established")
    socket.send("My name is John");
};

/**
 * @param {MessageEvent} event
 */
socket.onmessage = event => {
    MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), `[message] Data received from server: ${event.data}`)
};

/**
 * @param {CloseEvent} event
 */
socket.onclose = event => {
    if (event.wasClean) {
        MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), `[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`)
    } else {
        // server process killed or network down, event.code is *usually* 1006 in this case
        MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), "[close] Connection died")
    }
};

/**
 * @param {Event & { message?: string }} error
 */
socket.onerror = error => {
    MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), `[error] ${error.message}`)
};