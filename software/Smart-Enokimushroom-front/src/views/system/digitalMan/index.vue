<!DOCTYPE html>
<html lang="zh">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>WebRTC RTSP 播放器</title>
  <script src="https://unpkg.com/vue@3/dist/vue.global.js"></script>
  <style>
    body { font-family: Arial; text-align: center; margin-top: 50px; }
    video { width: 800px; height: 600px; border: 1px solid #ccc; }
  </style>
</head>
<body>
  <div id="app">
    <h1>RTSP → WebRTC 播放</h1>
    <video ref="video" autoplay playsinline controls></video>
    <p>状态: {{ status }}</p>
  </div>

  <script>
    const { createApp } = Vue;
    createApp({
      data() {
        return {
          status: "等待连接",
          pc: null,
          videoElement: null,
        };
      },
      mounted() {
        this.videoElement = this.$refs.video;
        this.start();
      },
      methods: {
        async start() {
          this.status = "创建连接...";

          const pc = new RTCPeerConnection();
          this.pc = pc;

          // 监听 ICE 候选
          pc.onicecandidate = (event) => {
            if (event.candidate) {
              console.log("ICE Candidate:", event.candidate);
            }
          };

          // 接收远程视频流
          pc.ontrack = (event) => {
            this.videoElement.srcObject = event.streams[0];
            this.status = "正在播放";
          };

          // 创建 Offer
          const offer = await pc.createOffer();
          await pc.setLocalDescription(offer);

          // 发送到 Python 后端
          const response = await fetch("http://localhost:9090/offer", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({
              sdp: pc.localDescription.sdp,
              type: pc.localDescription.type,
            }),
          });

          const answer = await response.json();
          await pc.setRemoteDescription(
            new RTCSessionDescription({
              sdp: answer.sdp,
              type: answer.type,
            })
          );
        },
      },
    }).mount("#app");
  </script>
</body>
</html>