
const id = 6;
const socket = new SockJS('/ws');
stompClient = Stomp.over(socket);
setTimeout(() => console.log('finished waiting'), 2000);
console.log('after waiting');
stompClient.connect({}, () => {
	console.log('connected');

	stompClient.subscribe(`/topic/chat.${id}`, onMessageReceived);

	// register the connected user
	stompClient.send(`/app/watch-party-chats/${id}`,
		{},
		JSON.stringify({ 'content': 'hello', 'senderName': 'liloo' })
	);



});




/*
async function findAndDisplayConnectedUsers() {
	const connectedUsersResponse = await fetch('/users');
	let connectedUsers = await connectedUsersResponse.json();
	connectedUsers = connectedUsers.filter(user => user.nickName !== nickname);
	const connectedUsersList = document.getElementById('connectedUsers');
	connectedUsersList.innerHTML = '';

	connectedUsers.forEach(user => {
		appendUserElement(user, connectedUsersList);
		if (connectedUsers.indexOf(user) < connectedUsers.length - 1) {
			const separator = document.createElement('li');
			separator.classList.add('separator');
			connectedUsersList.appendChild(separator);
		}
	});
}



function displayMessage(senderId, content) {
	const messageContainer = document.createElement('div');
	messageContainer.classList.add('message');
	if (senderId === nickname) {
		messageContainer.classList.add('sender');
	} else {
		messageContainer.classList.add('receiver');
	}
	const message = document.createElement('p');
	message.textContent = content;
	messageContainer.appendChild(message);
	chatArea.appendChild(messageContainer);
}

async function fetchAndDisplayUserChat() {
	const userChatResponse = await fetch(`/messages/${nickname}/${selectedUserId}`);
	const userChat = await userChatResponse.json();
	chatArea.innerHTML = '';
	userChat.forEach(chat => {
		displayMessage(chat.senderId, chat.content);
	});
}


function sendMessage(event) {
	const messageContent = messageInput.value.trim();
	if (messageContent && stompClient) {
		const chatMessage = {
			senderId: nickname,
			recipientId: selectedUserId,
			content: messageInput.value.trim(),
			timestamp: new Date()
		};
		stompClient.send("/watch-party-chats/{id}", {}, JSON.stringify(chatMessage));
		displayMessage(nickname, messageInput.value.trim());
		messageInput.value = '';
	}
	chatArea.scrollTop = chatArea.scrollHeight;
	event.preventDefault();
}
*/

async function onMessageReceived(payload) {
	//await findAndDisplayConnectedUsers();
	console.log('Message received', payload);
//	const message = JSON.parse(payload.body);
	//	displayMessage(message.senderId, message.content);
}






