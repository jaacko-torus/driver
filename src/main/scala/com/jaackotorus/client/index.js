let socket = new WebSocket("ws://localhost:8080/greeter")

/**
 * @param {string} data
 */
function send_data(data) {
    socket.send(data);
}

/**
 * @param {Event} e
 */
socket.onopen = e => {
    console.log("[open] Connection established");
    console.log("Sending to server");
    socket.send("My name is John");
};

/**
 * @param {MessageEvent} event
 */
socket.onmessage = event => {
    console.log(`[message] Data received from server: ${event.data}`);
};

/**
 * @param {CloseEvent} event
 */
socket.onclose = event => {
    if (event.wasClean) {
        console.log(`[close] Connection closed cleanly, code=${event.code} reason=${event.reason}`);
    } else {
        // server process killed or network down, event.code is *usually* 1006 in this case
        console.log('[close] Connection died');
    }
};

/**
 * @param {Event & { message?: string }} error
 */
socket.onerror = error => {
    console.log(`[error] ${error.message}`);
};