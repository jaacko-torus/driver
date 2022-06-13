import dayjs from "https://cdn.skypack.dev/dayjs";

let socket = new WebSocket("ws://localhost:8081/greeter?username=example-username")

const $messaging_data = document.getElementById("messaging_data")
const $messaging_button = document.getElementById("messaging_button")
const $chat_area = document.getElementById("chat_area")

/**
 * @param {string | null} data
 */
const send_data = (data = null) => {
    if (data !== null) socket.send(data)

    /** @type {string} */
    const body = $messaging_data.value
    $messaging_data.value = ""
    socket.send(body)
}

$messaging_button.onclick = e => {
    send_data()
    $messaging_button.blur()
}

document.onkeydown = e => {
    if (e.key === "Enter" && document.activeElement === $messaging_data) {
        send_data()
    }
    if (e.key === "Escape") {
        $messaging_data.blur()
    }
    return true;
};

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
    $header.classList.add("message_head")
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

    $chat_area.append($message)

    $message.scrollIntoView()
}

/**
 * @param {string} username
 * @param {string} timestamp
 * @param {string} body
 */
function on_message(username, timestamp, body) {
    MaterializeMessage(username, timestamp, `[message] Data received from server: ${body}`)
}

socket.onmessage = event =>
    on_message("[message]", dayjs().format("HH:mm:ss"), event.data)

socket.onopen = e => {
    MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), "[open] Connection established")
    socket.send("My name is John");
};

socket.onclose = event => {
    if (event.wasClean) {
        MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), `[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`)
    } else {
        // server process killed or network down, event.code is *usually* 1006 in this case
        MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), "[close] Connection died")
    }
};

socket.onerror = error => {
    MaterializeMessage("[server]", dayjs().format("HH:mm:ss"), `[error] ${error.message}`)
};