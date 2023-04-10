let ws;

function newRoom(){
    // calling the ChatServlet to retrieve a new room ID
    let callURL= "http://localhost:8080/WSChatServer-1.0-SNAPSHOT/chat-servlet";
    fetch(callURL, {
        method: 'GET',
        headers: {
            'Accept': 'text/plain',
        },
    })
        .then(response => response.text())
        .then(response => enterRoom(response,"new")); // enter the room with the code
}
function enterRoom(code,status){

    // refresh the list of rooms
    if(status === "new"){
        updateList(code);
    }
    changeTitle(code)
    // create the web socket
    ws = new WebSocket("ws://localhost:8080/WSChatServer-1.0-SNAPSHOT/ws/"+code);

    changeTitle(code)
    // parse messages received from the server and update the UI accordingly
    ws.onmessage = function (event) {
        console.log(event.data);
        // parsing the server's message as json
        let message = JSON.parse(event.data);

        // handle message
        document.getElementById("log").value += "[" + timestamp() + "] " + message.message + "\n";
        }
}
document.getElementById("input").addEventListener("keyup", function (event) {
    if (event.keyCode === 13) {
        let request = {"type":"chat", "msg":event.target.value};
        ws.send(JSON.stringify(request));
        event.target.value = "";
    }
});
function timestamp() {
    var d = new Date(), minutes = d.getMinutes();
    if (minutes < 10) minutes = '0' + minutes;
    return d.getHours() + ':' + minutes;
}

function updateList(code){
    let status = "old";
    let room = "<a href=\"#\" onClick=\"enterRoom("+ code+ ","+ status+ ")\">code</a>";

    document.getElementById("list").innerHTML += <p></p>;
    document.getElementById("list").innerHTML += room;

}

//updates title outlining room in
function changeTitle(code){
    document.getElementById("column2").getElementById("title").innerHTML = "You are chatting in room" + code;
}

