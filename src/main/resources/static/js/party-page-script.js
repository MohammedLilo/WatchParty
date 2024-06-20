 let userEmail = '';
    let userName = '';
    let eventOwnerId;
    document.addEventListener('DOMContentLoaded', async () => {
        try {
            const response = await fetch('/users');
            if (response.ok) {
                const data = await response.json();
                userEmail = data.email;
                userName = data.name;
                userId= data.id;
               eventOwnerId=userId;
                console.log(`User Email: ${userEmail}, User Name: ${userName},User id: ${userId}`);
            } else {
                console.error('Failed to fetch user info');
            }
        } catch (error) {
            console.error('Error fetching user info:', error);
        }
    });
    
    /*function setupEventSource(partyId) {
        const eventSource = new EventSource(`/watch-parties/${partyId}/members-count`);

        eventSource.addEventListener("PartyMembersCount", function(event) {
            const data = event.data;
            document.getElementById("membersCount").textContent = data;
        });
    }
    */
    

    const videoPlayer = document.getElementById('videoPlayer');
    const videoSource = document.getElementById('videoSource');
    const startPartyForm = document.getElementById('startPartyForm');
    const joinPartyForm = document.getElementById('joinPartyForm');
    const videoUrlInput = document.getElementById('videoUrl');
    const partyIdInput = document.getElementById('partyId');
    const partyIdDisplay = document.getElementById('partyIdDisplay');
    const chatMessages = document.getElementById('chatMessages');
    const chatInput = document.getElementById('chatInput');
    const sendMessageButton = document.getElementById('sendMessageButton');
    const startPartyButton=document.getElementById('start-party-button');
    let stompClient;
    let lastEventTime = 0;
    let currentPartyId;
    let isJoining=false;
    
    document.addEventListener("DOMContentLoaded", function() {
        const urlParams = new URLSearchParams(window.location.search);
        let src = urlParams.get('src');
        if (src) {
        	const url = new URL(window.location.href);
        	const origin = url.origin;
        	src= `${origin}/videos/${src}`;
        	videoUrlInput.value=src;
        	startPartyButton.click();
        }
    });
    
    startPartyForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const videoUrl = videoUrlInput.value;
        videoSource.src = videoUrl;
        videoPlayer.load();
//        videoPlayer.play();

        try {
            const response = await fetch('/watch-parties', { method: 'POST' });
            if (response.ok) {
                const partyId = await response.text();
                currentPartyId = partyId;
                displayPartyId(partyId);
                connectWebSocket(partyId, videoUrl);
              //  setupEventSource(partyId);
            } else {
                console.error('Failed to create watch party');
            }
        } catch (error) {
            console.error('Error creating watch party:', error);
        }
    });

    joinPartyForm.addEventListener('submit', async (event) => {
        event.preventDefault();
        const partyId = partyIdInput.value;
        connectWebSocket(partyId);
        try {
            const response = await fetch(`/watch-parties/${partyId}`, {
                method: 'PUT',
                });
            if (response.ok) {
                const data = await response.json();
                    
               console.log(data);
               let videoUrl=data.videoUrl;
               handleFirstEvent(data);

                currentPartyId = partyId;
                displayPartyId(partyId);
                console.log('before connecting to websocket '+videoUrl);
             //shifted to the beginning of the function   connectWebSocket(partyId);
             
             
            //    setupEventSource(partyId);

            } else {
                console.error('Failed to join watch party');
            }
        } catch (error) {
            console.error('Error joining watch party:', error);
        }
    });

    sendMessageButton.addEventListener('click', sendMessage);

    chatInput.addEventListener('keypress', (event) => {
        if (event.key === 'Enter') {
            sendMessage();
            event.preventDefault();  // Prevent the default action (form submission)
        }
    });
    

    function displayPartyId(partyId) {
        partyIdDisplay.textContent = `Party ID: ${partyId}`;
    }

    let stompSubscriptions=[];
    function connectWebSocket(partyId, videoUrl) {
        if (stompSubscriptions.length > 0) {
        	
            stompSubscriptions.forEach(sub => {
            	sub.unsubscribe();
            });
            stompClient.disconnect(() => {
                console.log('Disconnected from WebSocket');
            });
            stompSubscriptions = [];
        } 
        const socket = new SockJS('/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect({}, () => {
            console.log('Connected');

            let watchPartySubscription =  stompClient.subscribe(`/topic/watch-party.${partyId}`, (message) => {
                const syncMessage = JSON.parse(message.body);
                if(syncMessage.event!== 'join' && syncMessage.event!== 'left'){
                eventOwnerId=syncMessage.userId;
                console.log(eventOwnerId);
            }
                handleVideoEvent(syncMessage);
            });
            
        let membersCountSubscription = stompClient.subscribe(`/topic/watch-party-members-count.${partyId}`, (message) => {
                const data = JSON.parse(message.body);
                console.log('received a count '+data);
                document.getElementById("membersCount").textContent = data;
            });
            
          let chatSubscription = stompClient.subscribe(`/topic/chat.${partyId}`, (message) => {
                const chatMessage = JSON.parse(message.body);
                displayChatMessage(chatMessage);
            });
            stompSubscriptions.push(watchPartySubscription, membersCountSubscription, chatSubscription);

            videoPlayer.addEventListener('play', sendVideoEvent);
            videoPlayer.addEventListener('pause', sendVideoEvent);
            videoPlayer.addEventListener('seeked', sendVideoEvent);
            
			
            // Send initial video URL to the party
            if(videoUrl)
                sendVideoUrl(partyId, videoUrl);
        });
    }
function handleFirstEvent(syncNewUserMessage){
    //let lastEvent=syncNewUserMessage.event;
    let videoCurrentTime = syncNewUserMessage.videoCurrentTime;
    let eventDateTime = syncNewUserMessage.eventDateTime;
    let videoUrl = syncNewUserMessage.videoUrl;

  //  const eventTime=new Date(eventDateTime).valueOf();
  //  const myTime=new Date().valueOf();
    const myTime=new Date().valueOf();
    console.log(eventDateTime);
    console.log(myTime);
    const offset=(myTime-eventDateTime)/1000;
    console.log('offset is : '+offset);
    isJoining=true;
    switch (syncNewUserMessage.event) {
        case 'play':
            videoSource.src = syncNewUserMessage.videoUrl;
            videoPlayer.load();
            videoPlayer.currentTime = videoCurrentTime+offset;
            videoPlayer.play();
            break;
        case 'pause':
            videoSource.src = syncNewUserMessage.videoUrl;            
            videoPlayer.load();
            videoPlayer.currentTime = syncNewUserMessage.videoCurrentTime;
            videoPlayer.pause();
            break;
        case 'seeked':
            videoSource.src = syncNewUserMessage.videoUrl;
            videoPlayer.load();
            videoPlayer.currentTime = syncNewUserMessage.videoCurrentTime;
            if(syncNewUserMessage.previousEvent === 'play'){
            videoPlayer.currentTime = videoCurrentTime+offset;
            videoPlayer.play();
            }
            break;
        case 'url':
        console.log('videoUrl is : '+syncNewUserMessage.videoUrl)
            videoSource.src = syncNewUserMessage.videoUrl;
            videoPlayer.load();
            break;
    }
    isJoining=false;

}
    function sendVideoEvent(event) {
        console.log('user id here is :'+userId);
        console.log('event owner id here is '+ eventOwnerId);
        if(eventOwnerId!==userId){
            console.log('it is not my event.. i am returning without sending it to the server');
            eventOwnerId=userId;
            return;
        }
        console.log('isjoining '+isJoining);
        if(isJoining){
            isJoining=false;
            console.log('i just joined so I ignored the events I fired for sync');
            return;
        }
            

        const currentTime = Date.now();
     
     //if (currentTime - lastEventTime < 750) {  // Ensuring a minimum of 0.5 second between requests
     //      return;
     // }
     
       lastEventTime = currentTime;
        const syncMessage = {
            event: event.type,
            videoCurrentTime: videoPlayer.currentTime,
            videoUrl: videoSource.src,
            userId: userId,
            //eventDateTime: new Date().toISOString()
            eventDateTime: new Date().valueOf()
        };
        stompClient.send(`/app/watch-parties/${currentPartyId}`, {}, JSON.stringify(syncMessage));
    }

    function sendVideoUrl(partyId, videoUrl) {
        const syncMessage = {
            event: 'url',
            videoUrl: videoUrl,
            userId: userId,
            userName: userName,
        //eventDateTime: new Date().toISOString()
        eventDateTime: new Date().valueOf()
        };
        stompClient.send(`/app/watch-parties/${partyId}`, {}, JSON.stringify(syncMessage));
    }

    function sendMessage() {
        const messageContent = chatInput.value.trim();
        if (messageContent && stompClient) {
            const chatMessage = {
                content: messageContent,
                senderName: userName || 'User' // Use the user's name if available
            };
            stompClient.send(`/app/watch-party-chats/${currentPartyId}`, {}, JSON.stringify(chatMessage));
            chatInput.value = '';
        }
    }

    function displayChatMessage(message) {
        const messageElement = document.createElement('div');
        messageElement.textContent = `${message.senderName}: ${message.content}`;
        chatMessages.appendChild(messageElement);
        chatMessages.scrollTop = chatMessages.scrollHeight;
    }

    function handleVideoEvent(syncMessage) {
    	if(syncMessage.userId===userId){
            return;
        }
        if(syncMessage.event==='left'){
            //console.log(`user ${syncMessage.userName} left the party`);
            const messageElement = document.createElement('div');
            messageElement.textContent = `user ${syncMessage.userName} left the party at ${syncMessage.eventDateTime}`;
            chatMessages.appendChild(messageElement);
            chatMessages.scrollTop = chatMessages.scrollHeight;
	        return;
        }
        if(syncMessage.event==='join'){
            //console.log(`user ${syncMessage.userName} joined the party`);
            const messageElement = document.createElement('div');
            messageElement.textContent = `user ${syncMessage.userName} joined the party at ${syncMessage.eventDateTime}`;
            chatMessages.appendChild(messageElement);
            chatMessages.scrollTop = chatMessages.scrollHeight;
	        return;
        }
        switch (syncMessage.event) {
            case 'play':
                videoPlayer.currentTime = syncMessage.videoCurrentTime;
                videoPlayer.play();
                break;
            case 'pause':
                videoPlayer.currentTime = syncMessage.videoCurrentTime;
                videoPlayer.pause();
                break;
            case 'seeked':
                videoPlayer.currentTime = syncMessage.videoCurrentTime;
                //videoPlayer.play();
                break;
            case 'url':
                videoSource.src = syncMessage.videoUrl;
                videoPlayer.load();
                break;
        }
    }
    
    function copyPartyId() {
        const partyIdText = partyIdDisplay.textContent.split(': ')[1];
        navigator.clipboard.writeText(partyIdText).then(() => {
        //    alert('Party ID copied to clipboard!');
        }).catch(err => {
            console.error('Error copying Party ID: ', err);
        });
    }