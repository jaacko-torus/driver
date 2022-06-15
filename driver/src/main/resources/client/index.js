import dayjs from "https://cdn.skypack.dev/dayjs";

let username = window.prompt("what is your username");

let socket = new WebSocket(`ws://localhost:9001/greeter?username=${username}`)

const $messaging_data = document.getElementById("messaging_data")
const $messaging_button = document.getElementById("messaging_button")
const $chat_area = document.getElementById("chat_area")

/**
 * @param {string | null} data
 */
const send_data = (data = null) => {
    if (data !== null) {
        let val = data.trim()
        if (val === "") return
        socket.send(val)
        return
    }

    /** @type {string} */
    const body = $messaging_data.value
    $messaging_data.value = ""

    let val = body.trim()
    if (val === "") return
    socket.send(val)
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
    MaterializeMessage(username, timestamp, body)
}

socket.onmessage = event => {
    const message = JSON.parse(event.data);
    const events = {
        "message": data => on_message(data.username, data.timestamp, data.message),
        "user_joined": data => on_message("[server]", data.timestamp, `${data.username} has joined`),
        "user_left": data => on_message("[server]", data.timestamp, `${data.username} has left`),
    }

    events[message.type](message.data)
}

socket.onopen = event => {
    MaterializeMessage("[server]", dayjs().format("hh:mma"), "[open] Connection established")
};

socket.onclose = event => {
    if (event.wasClean) {
        MaterializeMessage("[server]", event.data.timestamp, `[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`)
    } else {
        // server process killed or network down, event.code is *usually* 1006 in this case
        MaterializeMessage("[server]", event.data.timestamp, "[close] Connection died")
    }
};

socket.onerror = error => {
    MaterializeMessage("[server]", error?.data?.timestamp ?? dayjs().format("hh:mma"), `[error] ${error.message}`)
};