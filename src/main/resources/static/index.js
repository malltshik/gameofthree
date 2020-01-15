function connect() {
    var socket = new WebSocket('ws://localhost:8080/api/websocket');
    ws = Stomp.over(socket);
    ws.connect({}, function (frame) {
        ws.subscribe("/user/queue/errors", function (message) {
            console.error("Error " + message.body);
        });
        ws.subscribe("/user/queue/reply", function (message) {
            console.info("Message " + message.body);
        });
    }, function (error) {
        console.error("STOMP error " + error);
    });
}

function disconnect() {
    if (ws != null) {
        ws.close();
    }
    console.log("Disconnected");
}