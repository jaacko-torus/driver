// @ts-check

import "./lib/nodarkreader.min.js"
import dayjs from "https://cdn.skypack.dev/dayjs";

/** @typedef {{ matched: boolean, vars: Record<string,string>}} Theme */
/** @type {Record<string, Theme>} */
const themes = {
    "dark": {
        matched: window.matchMedia && window.matchMedia("(prefers-color-scheme: dark)").matches,
        vars: {
            "--background-color": "#000000",
            "--background-color-focus": "#333333",
            "--foreground-color": "#ffffff",
            "--border-unfocused": "#aaaaaa",
            "--font-weight-light": "200",
            "--font-weight-normal": "400",
            "--font-weight-bold": "600",
        }
    },
    "light": {
        matched: window.matchMedia && window.matchMedia("(prefers-color-scheme: light)").matches,
        vars: {
            "--background-color": "#ffffff",
            "--background-color-focus": "#eeeeee",
            "--foreground-color": "#000000",
            "--border-unfocused": "#555555",
            "--font-weight-light": "300",
            "--font-weight-normal": "400",
            "--font-weight-bold": "500",
        }
    }
}

/** @type {keyof typeof themes} */
let selected_theme = "dark"

let username = window.prompt("what is your username");

let socket = new WebSocket(`ws://localhost:9001/greeter?username=${username}`)

const $messaging_data = document.getElementById("messaging_data")
const $messaging_button = document.getElementById("messaging_button")
const $chat_area = document.getElementById("chat_area")
const $toggle_theme = document.getElementById("toggle_theme")
const $root = document.querySelector(":root");

document.addEventListener("DOMContentLoaded", (e) => {
    // console.log(themes)
    [selected_theme] = Object.entries(themes).find(([theme, props]) => props.matched)
    // console.log(selected_theme)
    // selected_theme = used_theme
})

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

const change_theme = (selected_theme) => {
    const vars = themes[selected_theme].vars
    const set_property = ([name, value]) => $root.style.setProperty(name, value)
    Object.entries(vars).forEach(set_property)
}

const toggle_theme = () => {
    switch (selected_theme) {
        case "dark":
            selected_theme = "light"
            break
        case "light":
            selected_theme = "dark"
            break
        default:
            return
    }
}

$toggle_theme.onclick = e => {
    toggle_theme()
    change_theme(selected_theme)
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