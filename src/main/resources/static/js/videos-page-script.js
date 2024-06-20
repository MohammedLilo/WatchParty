  let currentPage = 0;
        const pageSize = 6;
        const sortBy = "timestamp";
        let loading = false;

        function showNotification(message, type) {
            const notification = document.getElementById('notification');
            notification.textContent = message;
            notification.className = type;
            notification.style.display = 'block';
            setTimeout(() => {
                notification.style.display = 'none';
            }, 3000);
        }

        document.getElementById('uploadForm').addEventListener('submit', function(event) {
            event.preventDefault();
            const formData = new FormData();
            const videoFile = document.getElementById('videoFile').files[0];
            const videoName = document.getElementById('videoName').value;

            if (!videoFile || !videoName.trim()) {
                showNotification('Please fill in all fields and select a video file.', 'error');
                return;
            }

            formData.append('multipartFile', videoFile);
            formData.append('videoName', videoName);

            fetch('/videos', {
                method: 'POST',
                body: formData
            }).then(response => {
                if (response.ok) {
                    return response.text();
                } else {
                    throw new Error('Failed to upload video.');
                }
            }).then(data => {
                showNotification('Video uploaded successfully!', 'success');
                loadVideos(true); // Reload videos after upload
            }).catch(error => {
                console.error('Error uploading video:', error);
                showNotification('Error uploading video.', 'error');
            });
        });

        function loadVideos(reset = false) {
            if (loading) return;
            loading = true;

            if (reset) {
                currentPage = 0;
                document.querySelector('.video-list').innerHTML = '';
            }

            fetch(`/videos?page=${currentPage}&size=${pageSize}&sortBy=${sortBy}`)
                .then(response => response.json())
                .then(data => {
                    const container = document.querySelector('.video-list');
                    data.content.forEach(video => {
                        const videoItem = document.createElement('div');
                        videoItem.className = 'video-item';
                        videoItem.innerHTML = `
                            <video width="120" height="90" controls>
                                <source src="/videos/${video.videoFileName}" type="video/mp4">
                                Your browser does not support the video tag.
                            </video>
                            <div class="video-description">
                                <h3>${video.videoName}</h3>
                                <p>${new Date(video.timestamp).toLocaleDateString()}</p>
                            </div>
                            <div class="controls">
                                <button onclick="deleteVideo('${video.videoFileName}', event)">Delete</button>
                            	<button onclick="startParty('${video.videoFileName}')">watch as a party</button>
                                </div>
                        `;
                        videoItem.addEventListener('click', () => playVideoAtTop(video.videoFileName));
                        container.appendChild(videoItem);
                        extractThumbnail(videoItem.querySelector('video'));
                    });

                    if (data.content.length > 0) {
                        currentPage++;
                    }

                    loading = false;
                }).catch(error => {
                    console.error('Error loading videos:', error);
                    loading = false;
                });
        }

        function extractThumbnail(video) {
            video.addEventListener('loadeddata', () => {
                video.currentTime = 5;
                video.addEventListener('seeked', () => {
                    const canvas = document.createElement('canvas');
                    canvas.width = video.videoWidth;
                    canvas.height = video.videoHeight;
                    const ctx = canvas.getContext('2d');
                    ctx.drawImage(video, 0, 0, canvas.width, canvas.height);
                    const frame = canvas.toDataURL('image/png');
                    video.setAttribute('poster', frame);
                });
            });
        }

        function deleteVideo(videoFileName, event) {
            event.stopPropagation();
            fetch(`/videos/${videoFileName}`, {
                method: 'DELETE'
            }).then(response => {
                if (response.ok) {
                    // Stop the main video if it is the one being deleted
                    const mainVideo = document.getElementById('mainVideo');
                    const mainVideoSource = document.getElementById('mainVideoSource');
                    if (mainVideoSource.src.endsWith(`/videos/${videoFileName}`)) {
                        mainVideo.pause();
                        mainVideo.currentTime = 0;
                        mainVideoSource.src = '';
                        document.getElementById('mainVideoContainer').style.display = 'none';
                    }
                    showNotification('Video deleted successfully!', 'success');
                    loadVideos(true); // Reload videos after deletion
                } else {
                    showNotification('Failed to delete video.', 'error');
                }
            }).catch(error => {
                console.error('Error deleting video:', error);
                showNotification('Failed to delete video.', 'error');
            });
        }

        function playVideoAtTop(videoFileName) {
            const mainVideoContainer = document.getElementById('mainVideoContainer');
            const mainVideo = document.getElementById('mainVideo');
            const mainVideoSource = document.getElementById('mainVideoSource');

            mainVideoSource.src = `/videos/${videoFileName}`;
            mainVideo.load();
            mainVideoContainer.style.display = 'flex';
            mainVideo.play();
        }

        function handleScroll() {
            const { scrollTop, scrollHeight, clientHeight } = document.documentElement;
            if (scrollTop + clientHeight >= scrollHeight - 5) {
                loadVideos();
            }
        }
        function startParty(videoFileName){
            window.location.href = `/watch-party?src=${videoFileName}`;
        }
        window.addEventListener('scroll', handleScroll);

        loadVideos(); // Initial load of videos